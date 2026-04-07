# Porthole Setup Guide

## Prerequisites

- Android 8.0 (Oreo) or later
- A VPN app that supports per-app exclusion (split tunneling)
- An active VPN configuration with a kill switch enabled

## Step 1: Install Porthole

Install Porthole from one of these sources:

- **GitHub Releases**: Download the latest signed APK from the [Releases page](https://github.com/sfoerster-dev/porthole/releases)
- **F-Droid**: _Coming soon_
- **Google Play Store**: _Coming soon_

If installing from an APK, you may need to enable "Install unknown apps" for your browser or file manager in Android Settings → Security.

## Step 2: Exclude Porthole from Your VPN

This is the critical step. Porthole must be excluded from your VPN tunnel so it can reach the captive portal gateway directly.

### WireGuard for Android

1. Open the **WireGuard** app
2. Tap your active tunnel (e.g., "My VPN")
3. Tap the **pencil icon** (edit) in the top right
4. Scroll down to the **Interface** section
5. Tap **Excluded Applications**
6. Find **Porthole** in the app list and check the box
7. Tap the **back arrow** to return to the tunnel config
8. Tap the **save icon** (floppy disk) in the top right
9. If the tunnel is active, toggle it off and on for the change to take effect

### Mullvad VPN

1. Open the **Mullvad VPN** app
2. Tap the **gear icon** (Settings)
3. Tap **VPN settings**
4. Tap **Split tunneling**
5. Enable split tunneling if not already enabled
6. Find **Porthole** in the app list and toggle it **on** (excluded)
7. Return to the main screen — changes take effect immediately

### ProtonVPN

1. Open the **ProtonVPN** app
2. Tap the **hamburger menu** (three lines) or navigate to **Settings**
3. Tap **Split Tunneling**
4. Enable split tunneling
5. Choose **Exclude apps from VPN**
6. Tap **Add app** and select **Porthole**
7. Save and reconnect the VPN if prompted

### IVPN

1. Open the **IVPN** app
2. Go to **Settings**
3. Tap **Split tunnel**
4. Enable split tunneling
5. Select **Exclude** mode
6. Add **Porthole** to the exclusion list
7. Reconnect if the VPN is active

### Other VPN Apps

Most VPN apps that support split tunneling have a similar interface. Look for:
- "Split tunneling" or "Per-app VPN"
- "Excluded apps" or "Bypass VPN"
- An app selection list where you can choose apps to exclude

If your VPN app doesn't support per-app exclusion, Porthole cannot function with that VPN. You would need to temporarily disconnect the VPN to use a captive portal.

## Step 3: First Launch

1. Open **Porthole**
2. Read through the setup information explaining what Porthole does and does not do
3. Tap **"I Understand"** to proceed to the main screen
4. This setup screen only appears once

## How to Use Porthole

### Authenticating with a Captive Portal

1. Connect to the WiFi network (the captive portal network)
2. Your VPN will likely fail to connect or show limited connectivity — this is expected
3. Open **Porthole**
4. Verify the **Gateway Detected** card shows your gateway IP (e.g., 192.168.1.1)
5. Tap **"Launch Portal Browser"**
6. The captive portal login page should load in the sandboxed browser
7. Authenticate as required (click through terms, enter email, enter room number, etc.)
8. Porthole will show **"CONNECTED"** in green when it detects internet access
9. Tap the **X** button in the status bar to close the session
10. All session data is wiped. Your VPN should now work normally on the authenticated network.

### During a Session

- The **red status bar** at the top shows "OUTSIDE TUNNEL" and a countdown
- A **notification** that cannot be dismissed reminds you the tunnel is bypassed
- The session will **auto-close** when the timer expires
- You can **close manually** at any time using the X button

## Troubleshooting

### Portal page won't load

**Symptom**: The WebView shows a blank page or an error.

**Possible causes**:
1. **Porthole is not excluded from your VPN** — Check your VPN's split tunnel settings. If Porthole is still going through the tunnel, it can't reach the captive portal.
2. **No WiFi gateway detected** — The main screen shows "No Gateway". Ensure you're connected to WiFi and the network has assigned an IP address.
3. **Portal is on a non-standard host** — Some portals run on a specific hostname rather than the gateway IP. Try switching to **permissive mode** in Settings.

### Portal requires JavaScript

**Symptom**: The portal page loads but buttons don't work, forms don't submit, or you see a "JavaScript required" message.

**Fix**: Go to Settings and enable JavaScript. Note the security warning — only enable JS when required and disable it after.

### "Navigation Blocked" message

**Symptom**: You see a "Navigation Blocked" page in the WebView.

**Cause**: The portal tried to redirect to a host outside the allowlist (not on the local network).

**Fix**: 
- If the portal legitimately needs to reach an external domain (e.g., a third-party authentication service), switch to **permissive mode** in Settings. You'll be asked to confirm each new domain.
- If you don't recognize the domain, it may be a tracking/analytics domain. Blocking it is the safe choice.

### Session expired before I finished

**Symptom**: The session auto-closed while you were still authenticating.

**Fix**: Increase the timeout in Settings. Options are 30s, 60s, 120s, or 300s (5 minutes). The maximum possible timeout is 10 minutes.

### VPN still doesn't work after authenticating

**Symptom**: You authenticated with the portal via Porthole, but your VPN still can't connect.

**Possible causes**:
1. The portal may require a cookie or session token that only works in a regular browser. Some portals tie authentication to a specific user agent or cookie.
2. The portal may have a secondary authentication step that Porthole's allowlist blocked.
3. Try again with permissive mode and JavaScript enabled.
4. As a last resort, you may need to temporarily disable your VPN, authenticate in a regular browser, then re-enable the VPN.

### Notification won't go away

**Symptom**: The "Porthole active — VPN tunnel bypassed" notification persists.

**This is by design** during an active session. Close the session in Porthole to dismiss the notification. If the app crashed, the notification may persist — opening and closing Porthole should clear it.
