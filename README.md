# Porthole

**A sandboxed captive portal browser for Android.**

Porthole gives security-conscious Android users a safe, controlled way to authenticate with captive portals — hotel WiFi, airport networks, coffee shops — without compromising their VPN tunnel. It opens an isolated, time-limited browser session that operates outside your VPN, authenticates with the portal, and shuts down cleanly when you're done.

## The Problem

If you run WireGuard or another VPN with a kill switch on Android, you've hit this wall: you connect to a WiFi network that requires captive portal authentication, but your VPN blocks the portal page from loading. You can't authenticate without disabling your VPN, which exposes _all_ your traffic — not just the portal interaction.

## What Porthole Does

Porthole is added to your VPN's per-app exclusion list (split tunneling). When you need to authenticate with a captive portal:

1. Open Porthole and tap **Launch Portal Browser**
2. A sandboxed WebView loads the gateway's portal page
3. Authenticate as needed (the session is time-limited)
4. Porthole detects when you're through and you close the session
5. All cookies, cache, and session data are destroyed

While Porthole is active, only Porthole's traffic bypasses the tunnel. All other apps remain protected. A persistent notification warns you that the tunnel is bypassed, and the session auto-expires after a configurable timeout (hard maximum: 10 minutes).

## Threat Model

Porthole assumes that:

- **You trust your VPN** to protect your traffic under normal conditions
- **The WiFi network is hostile** — passive observers, evil twins, and malicious portal operators are all in scope
- **The captive portal is minimally trusted** — you're willing to submit whatever credentials the portal asks for (typically an email or room number), but you don't want that interaction to leak into your regular browsing environment

Porthole **does not** protect against:

- A hostile portal that has already received your credentials (that's the portal's problem, not Porthole's)
- Traffic analysis on the WiFi network during the active session
- Exploits targeting the Android WebView engine itself

For the full threat model, see [docs/THREAT_MODEL.md](docs/THREAT_MODEL.md).

## Security Properties

- **Session isolation**: The WebView is destroyed and recreated on every session — never reused
- **Data destruction**: All cookies, web storage, cache, and form data are wiped on session end
- **Navigation sandboxing**: In strict mode, only RFC 1918 (local network) addresses are allowed
- **JavaScript off by default**: Reduces attack surface from hostile portal operators
- **Time-limited sessions**: Hard maximum of 10 minutes, user-configurable down to 30 seconds
- **Persistent warning**: A non-dismissible notification reminds you the tunnel is bypassed
- **No data persistence**: Porthole stores no user data, browsing history, or credentials
- **No network calls**: The app itself makes no network requests — only the WebView does
- **No analytics or telemetry**: Zero tracking, zero phone-home behavior

## Installation

### F-Droid
_Coming soon._

### Google Play Store
_Coming soon._

### Direct APK
Download the latest signed APK from [GitHub Releases](https://github.com/sfoerster-dev/porthole/releases).

## Setup

Porthole must be excluded from your VPN's tunnel to function. This is a one-time setup step.

### WireGuard for Android

1. Open the WireGuard app
2. Tap your active tunnel configuration
3. Tap the edit (pencil) icon
4. Scroll to **Excluded Applications**
5. Check **Porthole** in the app list
6. Save the configuration

For detailed setup instructions including other VPN apps, see [docs/SETUP.md](docs/SETUP.md).

## Building from Source

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34

### Build

```bash
git clone https://github.com/sfoerster-dev/porthole.git
cd porthole
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Test

```bash
./gradlew testDebugUnitTest
```

### Lint

```bash
./gradlew ktlintCheck detekt lintDebug
```

## Architecture

Porthole is a single-activity Jetpack Compose app with a clean separation between session management, network utilities, WebView sandboxing, and UI.

For the full architecture guide, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Contributing

See [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md).

## License

Copyright 2024 Steven Foerster

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

## Links

- [stevenfoerster.com](https://stevenfoerster.com)
- [Architecture](docs/ARCHITECTURE.md)
- [Threat Model](docs/THREAT_MODEL.md)
- [Setup Guide](docs/SETUP.md)
- [Contributing](docs/CONTRIBUTING.md)
