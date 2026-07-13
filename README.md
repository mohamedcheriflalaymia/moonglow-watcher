# 🌟 Math Heroes

A colourful maths training app for Selsa and Emilia (ages 6–10).
Works on any Android phone from 2020–2024 (Android 8.0 and above).

## What's inside

1. **Greeting screen** — pick Selsa 🦄 or Emilia 🐬 (plus progress buttons)
2. **Operations screen** — toggle + − × ÷ on/off (pick one or more)
3. **Limits screen** —
   - Addition: maximum answer (0–1000)
   - Subtraction: maximum top number (0–1000)
   - Times: toggle tables 1–12
   - Division: toggle tables 1–12
4. **Timer screen** — 30 s / 1 min / 2 min / 5 min challenges
5. **Challenge** — countdown at the top, big question, number pad + Submit,
   red ✕ in the top-right corner to cancel at any time
6. **Results** — correct answers, score %, average speed per answer
7. **Progress** — full history saved per child, on the phone

No internet permission, no ads, no tracking. All data stays on the phone.

---

## How to get the APK — Option A: GitHub builds it for you (no installs)

1. Create a free account at github.com (if you don't have one)
2. Create a **new repository** (e.g. `math-heroes`), keep it private if you like
3. Upload the entire contents of this folder to the repository
   (drag-and-drop works: "Add file → Upload files" — make sure the folder
   structure is kept, including the hidden `.github` folder*)
4. Go to the **Actions** tab → the "Build APK" workflow runs automatically
   (~3–4 minutes)
5. Click the finished run → download the **MathHeroes-APK** artifact
6. Unzip it → you get `app-debug.apk` → send it to the phone
   (email, Google Drive, USB…)
7. On the phone, tap the APK → allow "install from unknown sources" → done!

*Tip: the easiest way to keep the `.github` folder is to use
GitHub Desktop, or `git init` + `git push` from a terminal.

## How to get the APK — Option B: Android Studio (one-time install)

1. Install Android Studio (free): https://developer.android.com/studio
2. Open this folder as a project (File → Open)
3. Let it sync (first time takes a few minutes)
4. Menu: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**
5. Click "locate" in the notification → `app-debug.apk` is your file

---

## Customising

Everything lives in one file:
`app/src/main/java/com/mathheroes/kids/MainActivity.kt`

- Children's names: search for `"Selsa"` and `"Emilia"`
- Colours: the palette at the top of the file
- Timer options: the `options` list in `showTimerPick()`
