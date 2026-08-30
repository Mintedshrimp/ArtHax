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

- **🌐 Puter.js Live Model Ecosystem**: Minimalist authentication dialog that syncs credentials and live-fetches available AI models directly through Puter API.
- **🔥 Unrestricted Developer Mode**: Prompt filter bypass designed for game developers creating 2D horror games, dark fantasy monsters, combat effects, zombies, and gore sketches.
- **🛡️ Smart Copyright Bypass Adapter**: Automatically veers and cleans prompts containing trademarked or copyrighted characters into descriptive stylized equivalents while retaining visual essence.
- **🎯 Precision Canvas Cropping**: Top-right "Canvas" tool lets you drag and resize bounding calibration coordinates directly over your canvas view.
- **🎈 Draggable Floating Bubble & Edge Hugging**: Floating bubble with optional physics snapping to screen edges for quick access while keeping your drawing canvas clear.
- **⚡ High-Speed Procedural Vector Engine**: Offline fallback stroke synthesis engine generating intricate multi-color anime, cyberpunk, and fantasy vector strokes.

---

## 📥 Quick Download & Installation

1. Download the latest release: [**Direct APK Download**](https://github.com/ms/arthax/releases/latest/download/app-release.apk).
2. Install the APK on your Android device (Android 8.0+ recommended).
3. Grant **Overlay Permission** (to render floating bubble and chat window).
4. Enable **ArtHax Drawing Accessibility Service** in system settings (required to automate screen drawing gestures).
5. Launch your favorite paint application, tap the floating bubble, and start generating!

---

## 🎯 Future Updates & Roadmap Goals

Here are the planned features and goals for upcoming releases:

- [ ] **v1.1 - Multi-Layer Canvas Splitting**: Automatically organize generated vector strokes into separate background, line-art, and color fill layers.
- [ ] **v1.2 - Custom Stylus Pressure Sensitivity**: Integrate variable line-weight modulation based on synthetic stylus pressure and velocity curves.
- [ ] **v1.3 - SVG / JSON Import & Export**: Load custom SVG paths or export compiled ArtHax instruction sets to share with other artists.
- [ ] **v1.4 - AI Inpainting & Reference Image Importer**: Load a photo or sketch reference for AI to trace or complete missing contours.
- [ ] **v1.5 - Local On-Device ONNX Vectorizer**: Optional zero-latency offline neural model for complete offline vector generation without network.
- [ ] **v1.6 - Palette Auto-Sampler**: Automatically extract color palettes from reference images and apply them to generated stroke sequences.

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
