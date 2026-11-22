# Brawl Menu — Android floating UI demo

This is an Android Studio project with a GitHub Actions workflow that builds an installable debug APK online.

## What works
- Requests Android's "Display over other apps" permission.
- Shows a floating panel above other apps.
- The panel can be dragged by its header.
- The switches toggle locally and show an ON/OFF toast.
- Close stops the floating menu service.

## Important limitation
This is a UI demo only. It does not read, modify, inject into, or automate Brawl Stars. The aim/dodge/marker labels are demonstration controls only.

## Build the APK from your phone (no Android Studio needed)
1. Sign in to GitHub or create an account.
2. Create a **new public or private repository** on GitHub.
3. Upload the contents of this ZIP into the repository (make sure `.github/workflows/build-apk.yml` is included). GitHub's website may hide dot-folders in some upload flows; if it does, use the GitHub mobile browser in desktop mode or upload the files through the repository's web interface.
4. Open the repository's **Actions** tab.
5. Select **Build Android APK** and tap **Run workflow**. If you uploaded to the `main` branch, a build also starts automatically.
6. Wait for the workflow to finish successfully.
7. Open the completed workflow run, scroll to **Artifacts**, and download **Brawl-Menu-Demo-APK**.
8. Extract the downloaded ZIP and install `app-debug.apk`. Android may ask you to allow installation from that browser or file manager.

The build artifact is retained for 14 days.

## Build locally
Open this folder in Android Studio, sync Gradle, then run `gradle assembleDebug` or use Build > Build APK(s).

## Compatibility notes
- Requires Android 8.0 (API 26) or newer.
- The app needs the Android "Display over other apps" permission to show its floating panel.
- Android versions and manufacturer battery/overlay restrictions may affect behavior.
