# AAMovies Admin — Native Android App

Admin panel for Aamovies, built with native Kotlin + WebView bridge architecture.

## Architecture

HTML files serve as the **UI layer only**. All logic runs natively in Kotlin:

| Bridge | Registered Name | Responsibility |
|--------|----------------|----------------|
| AdminAuthBridge | `AndroidAuth` | Firebase Auth (whitelist-restricted) |
| FCMSenderBridge | `AndroidFCM` | Send push notifications to all users |
| AppBridge | `AndroidApp` | Toast, Share, platform info |

## Security

- Only whitelisted admin email can log in (enforced in Kotlin, not HTML)
- `google-services.json` and keystores are never committed
- FCM server key stored in environment variable only

## Setup

1. Clone repo
2. Add your `google-services.json` to `app/`
3. Set your keystore env vars (see Aamovies README)
4. Open in Android Studio and build

## Package

`com.aamovies.admin` | Min SDK 24 | Target SDK 34
