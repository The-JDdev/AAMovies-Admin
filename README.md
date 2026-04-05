# AAMovies Admin — Native Android App

Admin panel for the AAMovies platform. Built with **100% native Android** (XML + Kotlin).

## Features
- Add / Edit / Delete movies
- Set title, year, category, genre, language, quality, description, poster URL
- Toggle trending and pinned flags
- Add screenshots (URLs) and download links
- Type: Movie or Series

## Setup

### 1. Firebase
- Use the same Firebase project as the user app
- Add an Android app with package name `com.aamovies.admin`
- Download `google-services.json` and place it at `app/google-services.json`

### 2. Admin Whitelist
The app checks that only whitelisted email addresses can access the admin panel.
Edit the whitelist in `AdminAuthActivity.kt`.

### 3. Build
```bash
./gradlew assembleRelease
```

## License
MIT
