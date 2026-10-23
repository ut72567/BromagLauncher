# Bromag Home Launcher

A native Android launcher starter project designed for phone-only development with cloud APK builds.

## Current features
- Default Android Home launcher activity
- Cyber-dark interface with teal accent
- Clock/date and battery percentage
- Installed app listing and search
- Open apps, long-press app info, wallpaper shortcut
- GitHub Actions workflow that builds and uploads a debug APK

## Build APK using GitHub Actions (phone-only)
1. Download and extract this project ZIP.
2. Create a new GitHub repository (for example `BromagHomeLauncher`).
3. Upload **all files and folders** from this project into the repository root. Keep the `.github/workflows/android.yml` path intact.
4. Open the repository's **Actions** tab. If asked, enable Actions.
5. Run **Build Bromag Home APK** using **Run workflow**, or push a commit to `main`.
6. Open the completed workflow run and download the `Bromag-Home-debug-apk` artifact.
7. Extract the downloaded artifact ZIP to get `app-debug.apk`, then install it on your Android phone.
8. Choose **Bromag Home** in Android Settings → Apps → Default apps → Home app (wording differs by phone).

## Important
- This is an early starter build, not a polished release.
- Debug APK is for testing; it is not a Play Store release.
- Android/OEM behavior may vary. Some devices may hide or restrict apps depending on OS version and package visibility.
- Before publishing, add a proper launcher icon, accessibility checks, tests, signing configuration, and privacy disclosures if needed.
- The project currently uses package ID `com.bromag.launcher`, min Android 8.0 (API 26), compile SDK 36 and target SDK 35.
