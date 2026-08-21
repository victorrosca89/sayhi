# SayHi! — Android Messaging & Calling App

A modern Android messaging app with **video/audio calls**, built with **Kotlin + Jetpack Compose**, powered by **Firebase** and **WebRTC** via a **HuggingFace Docker Space** signaling server.

---

## ✨ Features

- 🟣 **Beautiful UI** — Dark/Light theme with violet + white + black design
- 💬 **Real-time Messaging** — Firebase Firestore with offline support
- 📞 **Voice Calls** — WebRTC P2P with signaling server
- 📹 **Video Calls** — Full-screen video with PiP self-view
- 🔔 **Push Notifications** — Firebase Cloud Messaging
- 🔊 **Custom Sounds** — Notification + ringtone MP3 support
- ⚙️ **Settings** — Username, theme, notification preferences
- 📎 **File Transfer** — Direct P2P (no cloud storage)
- 🚫 **No Image Transfer** — Intentionally excluded per design

---

## 🏗 Architecture

```
┌─────────────────┐         ┌──────────────────────┐         ┌─────────────────┐
│   Android App   │◄──────►│  HuggingFace Docker   │◄──────►│   Android App   │
│   (Kotlin +     │ Socket  │  Signaling Server     │ Socket  │   (Kotlin +     │
│    Compose)     │  .IO    │  (Node.js + Socket.IO)│  .IO   │    Compose)     │
└─────────────────┘         └──────────────────────┘         └─────────────────┘
         │                                                            │
         │                         ┌──────────────────┐              │
         └────────────────────────►│   Firebase        │◄─────────────┘
                                   │  - Authentication │
                                   │  - Firestore DB   │
                                   │  - FCM Push       │
                                   └──────────────────┘

         After signaling: Direct WebRTC P2P connection for media
```

---

## 📁 Project Structure

```
SayHi/
├── app/
│   ├── src/main/
│   │   ├── java/com/sayhi/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SayHiApp.kt
│   │   │   ├── di/              # Hilt DI modules
│   │   │   ├── data/
│   │   │   │   ├── model/       # User, Message, ChatInfo
│   │   │   │   └── repository/  # Auth, Message, User repos
│   │   │   ├── service/         # FCM, Signaling, Call services
│   │   │   ├── ui/
│   │   │   │   ├── theme/       # Color, Theme, Typography
│   │   │   │   ├── navigation/  # NavHost + routes
│   │   │   │   └── screens/
│   │   │   │       ├── auth/    # Login/SignUp
│   │   │   │       ├── messages/# Message list
│   │   │   │       ├── chat/    # Chat screen
│   │   │   │       ├── calls/   # Voice + Video call
│   │   │   │       └── settings/# Settings
│   │   │   └── util/            # SoundPlayer
│   │   ├── res/
│   │   │   ├── raw/
│   │   │   │   ├── notification_sound.mp3  ← ADD YOUR FILE
│   │   │   │   └── ringtone_sound.mp3       ← ADD YOUR FILE
│   │   │   ├── drawable/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── server/                       # Deploy to HuggingFace
│   ├── src/index.js              # Signaling server
│   ├── Dockerfile                # HuggingFace Docker
│   ├── package.json
│   └── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

---

## 🚀 Setup Guide

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project → **SayHi**
3. Add Android app with package name: `com.sayhi`
4. Download `google-services.json`
5. Place it in: `app/google-services.json`
6. Enable these services:
   - **Authentication** → Email/Password + Anonymous
   - **Firestore** → Create database (start in test mode)
   - **Cloud Messaging** → Enabled by default

### 2. HuggingFace Server Setup

1. Go to [huggingface.co/new-space](https://huggingface.co/new-space)
2. Select **Docker** SDK
3. Choose **16GB RAM, 2 vCPU** (or free 2 vCPU)
4. Upload the entire `server/` directory
5. Your space URL will be: `https://YOUR-USERNAME-sayhi-server.hf.space`
6. Update this URL in `SignalingService.kt` and `SettingsViewModel.kt`

### 3. Sound Files

Place your MP3 files in:
- `app/src/main/res/raw/notification_sound.mp3`
- `app/src/main/res/raw/ringtone_sound.mp3`

You can use any short MP3 files (under 30 seconds recommended).

### 4. Build & Run

```bash
# Clone the repo
git clone https://github.com/YOUR-USERNAME/SayHi.git
cd SayHi

# Open in Android Studio
# Build → Run on Android device/emulator
```

---

## 🎨 Design System

| Element | Light Mode | Dark Mode |
|---------|-----------|----------|
| Primary | `#6C5CE7` | `#8B5CF6` |
| Background | `#F5F6FA` | `#0F051D` |
| Surface | `#FFFFFF` | `#1A0B2E` |
| Text | `#2D3436` | `#FFFFFF` |
| Accent | `#A29BFE` | `#2D1B4E` |
| Online | `#00B894` | `#00B894` |
| Danger | `#DC2626` | `#DC2626` |

---

## 📱 Screens

1. **Login/Sign Up** — Email, password, or guest mode with animated gradient
2. **Messages List** — Pinned + all messages, avatar scroll, search
3. **Chat** — Text messages + file transfer, purple sent bubbles
4. **Voice Call** — Dark purple gradient, glowing avatar, pulse animation
5. **Video Call** — Full-screen remote video, PiP local view, glassmorphism controls
6. **Settings** — Username, dark mode toggle, notification sounds, sign out

---

## ⚠️ Important Notes

- **No cloud file storage** — Files are referenced by name only; actual P2P transfer needs WebRTC DataChannel (addition needed for production)
- **No image transfer** — Intentionally removed per requirements
- **HuggingFace free tier** — Spaces may sleep after inactivity, causing missed calls. Keep-alive services or paid tier recommended for production
- **STUN only** — Uses Google's public STUN server. For NAT traversal in restricted networks, add a TURN server

---

## 📄 License

MIT License — Use freely for personal or commercial projects.
