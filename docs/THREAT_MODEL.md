# Porthole Threat Model

## Overview

This document describes the threat model for Porthole, a sandboxed captive portal browser for Android. It identifies the assets being protected, the trust boundaries, threat actors, mitigations, non-goals, and residual risks.

## Assets Being Protected

1. **VPN tunnel integrity** — The user's VPN tunnel should remain active for all apps except Porthole during a captive portal session. No other app's traffic should be exposed.

2. **Session isolation** — Data from a captive portal session (cookies, cached pages, form data, JavaScript state) must not persist after the session ends or leak into other contexts.

3. **Navigation scope** — The WebView should only access hosts necessary for captive portal authentication, not arbitrary internet destinations.

4. **User awareness** — The user must always know when they are operating outside the VPN tunnel and for how long.

## Trust Boundaries

```
┌─────────────────────────────────────────────┐
│              VPN Tunnel (trusted)            │
│  ┌───────────┐  ┌───────────┐  ┌──────────┐ │
│  │  Browser   │  │  Email    │  │  Apps    │ │
│  └───────────┘  └───────────┘  └──────────┘ │
└─────────────────────────────────────────────┘
        ↕ All traffic encrypted through tunnel

┌─────────────────────────────────────────────┐
│          Outside Tunnel (untrusted)          │
│  ┌─────────────────────────────────────────┐ │
│  │          Porthole WebView               │ │
│  │  (sandboxed, time-limited, isolated)    │ │
│  └─────────────────────────────────────────┘ │
│        ↕ Plaintext to local WiFi network     │
│  ┌─────────────────────────────────────────┐ │
│  │        Captive Portal Gateway           │ │
│  └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

**Inside the tunnel**: All regular app traffic, protected by the VPN.

**Outside the tunnel**: Only Porthole's WebView traffic, which traverses the local WiFi network in the clear. This boundary is enforced by Android's per-app VPN exclusion mechanism (split tunneling).

**Trust hierarchy**:
- The VPN provider is fully trusted
- The Android OS and WebView engine are trusted (within their known limitations)
- The local WiFi network is untrusted
- The captive portal operator is minimally trusted — enough to submit portal credentials, but not to execute arbitrary code or access arbitrary URLs

## Threat Actors

### 1. Passive Network Observer
**Capability**: Can observe all unencrypted traffic on the WiFi network.
**Goal**: Intercept credentials, track browsing, fingerprint devices.
**Mitigation**: Sessions are time-limited (max 10 minutes), navigation is restricted to local addresses in strict mode, no persistent data is stored, and the WebView is destroyed after each session.

### 2. Hostile Portal Operator
**Capability**: Controls the captive portal page content, can serve arbitrary HTML/JS, can redirect to any URL.
**Goal**: Execute malicious JavaScript, redirect to phishing sites, exfiltrate device data.
**Mitigation**: JavaScript is disabled by default. Navigation is restricted to the allowlist (RFC 1918 in strict mode). No file or content access is permitted. The WebView has no access to device sensors, geolocation, or local storage. The session is time-limited.

### 3. Evil Twin AP Operator
**Capability**: Operates a rogue access point mimicking a legitimate network. Has all capabilities of the hostile portal operator plus full control of DNS and network routing.
**Goal**: Credential theft, man-in-the-middle attacks, device compromise.
**Mitigation**: Same as hostile portal operator mitigations. Additionally, Porthole's session isolation ensures that even if credentials are submitted to a rogue portal, no persistent session state remains on the device. The user is clearly warned that they are operating outside the tunnel.

### 4. Malicious App on Device
**Capability**: Another app on the device attempting to extract data from Porthole.
**Goal**: Read captive portal session data, cookies, or form submissions.
**Mitigation**: Porthole stores no persistent data. The WebView's cache, cookies, and storage are cleared on every session end. The WebView instance itself is destroyed (not just cleared) between sessions. No file access or content provider access is enabled.

## Mitigations Provided by Porthole

| Mitigation | Implementation | Security Rationale |
|---|---|---|
| WebView destroyed per session | `WebView.destroy()` called on session end; new instance created on session start | Prevents any state leakage between sessions. Even if the WebView engine has internal state that `clearCache` misses, destroying the instance eliminates it. |
| Cookies cleared on session end | `CookieManager.removeAllCookies()` + `flush()` | Ensures no authentication tokens or tracking cookies persist beyond the session. |
| Web storage cleared on session end | `WebStorage.deleteAllData()` | Removes localStorage, sessionStorage, and IndexedDB data that a portal might have written. |
| Cache cleared on session end | `WebView.clearCache(true)` | Prevents cached portal pages from being readable after the session. |
| JavaScript disabled by default | `WebSettings.javaScriptEnabled = false` | Dramatically reduces the attack surface. A hostile portal operator cannot execute scripts to probe the WebView, access APIs, or exfiltrate data. |
| File access disabled | `setAllowFileAccess(false)`, `setAllowContentAccess(false)` | Prevents the portal page from reading local files via `file://` URIs or accessing content providers. |
| Geolocation disabled | `setGeolocationEnabled(false)` | No captive portal needs device location. Disabling prevents a hostile portal from requesting it. |
| Navigation allowlist (strict mode) | Only RFC 1918 addresses + gateway allowed | Prevents the portal from loading resources from or redirecting to arbitrary internet hosts. |
| Time-limited sessions | Hard maximum of 600 seconds, enforced in `SessionTimer` | Limits the window of exposure. Even if the user forgets to close the session, it auto-expires. |
| Non-dismissible notification | `setOngoing(true)` during active session | Ensures the user cannot accidentally forget they are operating outside the tunnel. |
| No form data persistence | `setSavePassword(false)`, `setSaveFormData(false)` | Credentials entered in the portal are never saved to disk. |
| Gateway-gated session start | Session cannot start without a resolvable WiFi gateway | Prevents accidental tunnel bypass when not on a WiFi network with a captive portal. |

## Explicit Non-Goals

Porthole **does not** attempt to:

1. **Protect credentials submitted to the portal** — If you enter an email address or password into a captive portal, that data is sent to the portal operator over the local network. Porthole cannot prevent this; it is inherent to captive portal authentication.

2. **Detect evil twin access points** — Porthole cannot distinguish between a legitimate AP and a rogue one. This is a network-layer problem outside the scope of an application.

3. **Protect against WebView engine exploits** — If the Android WebView has a zero-day vulnerability, a hostile portal could potentially exploit it. Porthole relies on Google's WebView updates for engine-level security.

4. **Prevent traffic analysis** — While Porthole is active, traffic between the device and the portal is visible to the network. An observer can see that authentication is taking place.

5. **Provide anonymity** — The device's MAC address, IP address, and WebView user agent are visible to the portal and the network.

6. **Replace a VPN** — Porthole is not a VPN and provides no encryption. It is a controlled exception to the VPN for the specific purpose of captive portal authentication.

## Residual Risks

1. **WebView engine vulnerabilities**: A zero-day in the Chromium-based WebView could allow code execution from a malicious portal page. Mitigation: keep the device's WebView updated via Play Store.

2. **Split tunnel misconfiguration**: If the user forgets to add Porthole to the VPN exclusion list, the app won't be able to reach the portal. This is a usability issue, not a security one — the failure mode is safe (nothing works, nothing is exposed).

3. **JavaScript-dependent portals**: Some portals require JavaScript. When the user enables JS, the attack surface increases significantly. The UI displays a prominent warning when JS is enabled.

4. **Permissive mode redirect chains**: In permissive mode, the user can allow external domains. A sophisticated attacker could use a chain of redirects to reach an unexpected destination. Each domain requires explicit user approval, but social engineering is possible.

5. **Session timeout race condition**: If the device sleeps during a session, the coroutine-based timer may not fire precisely. The session could run slightly longer than the configured timeout. The hard maximum is enforced as a defense-in-depth measure.

## Design Decisions and Security Rationale

### Why sessions are user-initiated
Porthole never starts a session automatically. This ensures the user makes a conscious decision to bypass the tunnel, understands the risk, and is present to close the session when done.

### Why the WebView is destroyed, not reused
Clearing cookies and cache is necessary but not sufficient. The WebView engine may maintain internal state (compiled JS, connection pools, TLS session tickets) that survives a clear operation. Destroying the entire instance and creating a new one ensures a clean slate.

### Why JavaScript is off by default
JavaScript is the primary attack vector for a hostile portal operator. With JS disabled, the portal can only render static HTML and submit forms. The operator cannot probe the WebView's API surface, execute timing attacks, or interact with the page programmatically. Most simple captive portals (click-through, email entry) work without JS.

### Why strict mode is the default
RFC 1918 addresses are definitionally local. A captive portal's gateway and authentication server are almost always on the local network. By restricting navigation to these addresses, Porthole prevents the portal from loading tracking pixels, analytics scripts, or external authentication flows that could fingerprint or track the user.

### Why the notification is non-dismissible
The notification serves as a safety net. If the user switches away from Porthole (e.g., to check an email), the notification is a persistent reminder that their tunnel is bypassed. Making it dismissible would defeat this purpose.
