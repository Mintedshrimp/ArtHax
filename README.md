# 🎨 ArtHax - AI Auto-Drawing Vector Assistant for Android

[![GitHub Stars](https://img.shields.io/github/stars/ms/arthax?style=for-the-badge&logo=github&color=00F0FF&labelColor=0d1117)](https://github.com/ms/arthax/stargazers)
[![Release](https://img.shields.io/github/v/release/ms/arthax?style=for-the-badge&color=00FF88&labelColor=0d1117)](https://github.com/ms/arthax/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-FF00E5?style=for-the-badge&labelColor=0d1117)](LICENSE)

<p align="center">
  <a href="https://github.com/ms/arthax/releases/latest/download/app-release.apk">
    <img src="https://img.shields.io/badge/⚡_DIRECT_DOWNLOAD-APK_RELEASE_(LATEST)-00F0FF?style=for-the-badge&logo=android&logoColor=white" alt="Direct Download APK" height="42" />
  </a>
</p>

---

## 🌟 Overview

**ArtHax** is an advanced AI-powered overlay assistant for Android that synthesizes vector art in real time and automatically draws strokes into any paint application (Ibis Paint X, Autodesk Sketchbook, Infinite Painter, Medibang Paint, Clip Studio Paint, and Notes) via Android Accessibility Services.

Powered by a headless **Puter.js** neural bridge, ArtHax connects to world-class LLMs (**Claude 3.5 Sonnet**, **Claude 3.7 Sonnet**, **Gemini 2.0 Flash**, **DeepSeek V3/R1**, **GPT-4o**) to compile textual prompts into smooth bezier paths and coordinates normalized directly onto your canvas.

---

## ✨ Key Features

- **🌐 Puter.js Live Model Ecosystem**: Minimalist authentication dialog that syncs credentials and live-fetches available AI models directly through Puter API (**Claude 3.7**, **Claude 3.5**, **DeepSeek R1/V3**, **Gemini 2.0 Flash**, **GPT-4o**).
- **🖼️ AI-Less Image Contour & Tap-to-Segment Vectorizer**: Pick any photo, character sketch, or reference image from your gallery to automatically extract vector strokes completely on-device without needing network or API tokens. Tap specific subjects (characters, plants, tattoos) to isolate localized outlines.
- **👻 Ghost Tracing Mode (AR Light Table HUD)**: Turn your screen into a holographic tracing table. Projects semi-transparent neon vector blueprints directly over FOSS Paint or Ibis Paint so you can manually trace with your physical stylus or finger.
- **🖌️ Pen Type Selector & Dynamic Stroke Weights**: Choose between Fineliner/Ink, Calligraphy Brush, Chisel Marker, Graphite Pencil, and Cyber Neon Glow. Supports both **Auto AI Width** and manual precision thickness sliders.
- **⚡ Cyber Turbo vs. Organic Human Speedpaint**: Toggle between maximum machine execution speed (5ms gesture interval) or organic speedpainting with natural Catmull-Rom/Bezier corner easing and micro-pauses.
- **🎯 Canvas Profiles & Instant Alignment**: Quick-crop presets for **FOSS Paint**, **Ibis Paint X**, **Infinite Painter**, **Sketchbook**, and standard aspect ratios (`1:1 Square`, `9:16 Tall`, `4:3 Landscape`).
- **🔥 Unrestricted Developer Mode**: Prompt filter bypass designed for game developers creating 2D horror games, dark fantasy monsters, combat effects, zombies, and gore sketches.
- **🛡️ Smart Copyright Bypass Adapter**: Automatically veers and cleans prompts containing trademarked or copyrighted characters into descriptive stylized equivalents while retaining visual essence.
- **🎈 Draggable Floating HUD & Edge Hugging**: Floating bubble with optional physics snapping to screen edges for quick access while keeping your drawing canvas clear.

---

## 📥 Quick Download & Installation

1. Download the latest release: [**Direct APK Download**](https://github.com/ms/arthax/releases/latest/download/app-release.apk).
2. Install the APK on your Android device (Android 8.0+ recommended).
3. Grant **Overlay Permission** (to render floating bubble and chat window).
4. Enable **ArtHax Drawing Accessibility Service** in system settings (required to automate screen drawing gestures).
5. Launch your favorite paint application (FOSS Paint, Ibis Paint X, Sketchbook, etc.), position your canvas cutout, and start creating!

---

## 🎯 Release Highlights & Roadmap Progress

- [x] **Multi-Layer Vector Organization**: Separate silhouette outlines, shading, and detail vector passes.
- [x] **AI-Less Image-to-Vector & Tap-to-Segment**: Instant local contour vectorizer with Douglas-Peucker curve smoothing.
- [x] **Ghost Tracing Hologram (AR Guide)**: Overlay neon vector outlines for manual hand tracing.
- [x] **Pen Style & Pressure Profiles**: Dynamic taper, calligraphic curves, markers, and neon glows.
- [x] **Speed Personality Modes**: Cyber Turbo (5ms machine speed) & Organic Human speedpainting.
- [x] **Canvas App Presets**: Instant boundary calibration for FOSS Paint, Ibis Paint, and standard canvas ratios.
- [ ] **Custom SVG / JSON Import & Export**: Import custom vector SVG paths or share compiled stroke sequences.
- [ ] **Palette Auto-Sampler**: Automatically extract color palettes from reference photos and apply them to generated stroke sequences.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin & Jetpack Compose (Material 3 Cyberpunk Neon Theme)
- **AI Engine**: Puter.js headless JavaScript bridge with live model fetching
- **Automation**: Android `AccessibilityService` DispatchGesture API
- **Overlay**: Android `WindowManager` with dynamic layout params & touch passthrough
- **CI/CD**: GitHub Actions automated semantic versioning, APK compilation, keystore signing, and GitHub Releases

---

## 📄 License

This project is licensed under the MIT License.
