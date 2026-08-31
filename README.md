# 📐 ArtHax - Neo-Precision AI Auto-Drawing Vector Instrument for Android

[![GitHub Stars](https://img.shields.io/github/stars/ms/arthax?style=for-the-badge&logo=github&color=FFB800&labelColor=070709)](https://github.com/ms/arthax/stargazers)
[![Release](https://img.shields.io/github/v/release/ms/arthax?style=for-the-badge&color=00E599&labelColor=070709)](https://github.com/ms/arthax/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-388BFF?style=for-the-badge&labelColor=070709)](LICENSE)

<p align="center">
  <a href="https://github.com/ms/arthax/releases/latest/download/app-release.apk">
    <img src="https://img.shields.io/badge/⚡_DIRECT_DOWNLOAD-APK_RELEASE_(LATEST)-FFB800?style=for-the-badge&logo=android&logoColor=black" alt="Direct Download APK" height="42" />
  </a>
</p>

---

## 🔬 Overview & "Neo-Precision" Movement

**ArtHax** is an advanced AI-powered overlay instrument for Android that synthesizes vector art in real time and automatically executes physical drawing gestures into any paint application (Ibis Paint X, Autodesk Sketchbook, Infinite Painter, Medibang Paint, Clip Studio Paint, and Notes) via Android Accessibility Services.

### 📐 The Neo-Precision Design Movement
ArtHax departs from generic, over-simplified corporate design systems as well as retro-cyberpunk tropes. Built on the **Neo-Precision** interface philosophy:
- **Obsidian / Carbon Substrates**: Multi-layered depth built on deep obsidian (`#070709`) and matte carbon (`#0D0E12`) surfaces.
- **1px Hairline Metric Boundaries**: Crisp, surgical hairline borders (`0x1FFFFFFF`) with laser-aligned coordinate callouts.
- **Tungsten & Emerald Spectral Accents**: High-contrast, utilitarian color-coding using Tungsten Amber (`#FFB800`), Signal Emerald (`#00E599`), Cobalt Beam (`#388BFF`), and Laser Crimson (`#FF4D4D`).
- **Telemetry & Monospace Density**: Dense, readable telemetry readouts and monospace technical labeling for immediate control.

---

## ✨ Key Features & Capabilities

- **🌐 Puter.js Neural Bridge & Live Model Ecosystem**: Headless authentication console that syncs credentials and live-fetches available AI models directly through Puter API (**Claude 3.7**, **Claude 3.5**, **DeepSeek R1/V3**, **Gemini 2.0 Flash**, **GPT-4o**).
- **🖼️ AI-Less Image Contour & Tap-to-Segment Vectorizer**: Pick any photo, character sketch, or reference image from your gallery to automatically extract vector strokes completely on-device without needing network or API tokens. Tap specific subjects (characters, plants, tattoos) to isolate localized outlines with Douglas-Peucker curve reduction.
- **👻 Ghost Tracing Mode (AR Light Table HUD)**: Turn your screen into a precision optical tracing table. Projects semi-transparent vector blueprints directly over FOSS Paint or Ibis Paint so you can manually trace with your physical stylus or finger.
- **🖌️ Pen Type Selector & Dynamic Stroke Weights**: Choose between Fineliner/Ink, Calligraphy Brush, Chisel Marker, Graphite Pencil, and Neon Glow. Supports both **Auto AI Width** and manual precision thickness sliders.
- **⚡ Cyber Turbo vs. Organic Human Speedpaint**: Toggle between maximum machine execution speed (5ms gesture interval) or organic speedpainting with natural Catmull-Rom/Bezier corner easing and micro-pauses.
- **🎯 Canvas Profiles & Instant Calibration**: Quick-crop presets for **FOSS Paint**, **Ibis Paint X**, **Infinite Painter**, **Sketchbook**, and standard aspect ratios (`1:1 Square`, `9:16 Tall`, `4:3 Landscape`).
- **🔥 Unrestricted Developer Mode**: Prompt filter bypass designed for game developers creating 2D horror games, dark fantasy monsters, combat effects, zombies, and gore sketches.
- **🛡️ Smart Copyright Bypass Adapter**: Automatically veers and cleans prompts containing trademarked or copyrighted characters into descriptive stylized equivalents while retaining visual essence.
- **🎈 Precision Floating Instrument & Synthesis Console**: Compact HUD with edge snapping, draggable viewfinder cutout, and live coordinate readout.

---

## 📥 Quick Download & Installation

1. Download the latest release: [**Direct APK Download**](https://github.com/ms/arthax/releases/latest/download/app-release.apk).
2. Install the APK on your Android device (Android 8.0+ recommended).
3. Grant **Overlay Permission** (to render floating instrument and synthesis console).
4. Enable **ArtHax Drawing Accessibility Service** in system settings (required to automate screen drawing gestures).
5. Launch your favorite paint application (FOSS Paint, Ibis Paint X, Sketchbook, etc.), position your canvas cutout, and start synthesizing vector art!

---

## 🎯 Feature Matrix & Roadmap

- [x] **Multi-Layer Vector Organization**: Separate silhouette outlines, shading, and detail vector passes.
- [x] **AI-Less Image-to-Vector & Tap-to-Segment**: Instant local contour vectorizer with Douglas-Peucker curve smoothing.
- [x] **Ghost Tracing Light Table (AR Guide)**: Overlay vector blueprints for manual stylus tracing.
- [x] **Pen Style & Pressure Profiles**: Dynamic taper, calligraphic curves, markers, and neon glows.
- [x] **Speed Personality Modes**: Cyber Turbo (5ms machine speed) & Organic Human speedpainting.
- [x] **Canvas App Presets**: Instant boundary calibration for FOSS Paint, Ibis Paint, and standard canvas ratios.
- [x] **Neo-Precision UI Framework**: Modern obsidian-carbon high-density instrument design system.
- [ ] **Custom SVG / JSON Import & Export**: Import custom vector SVG paths or share compiled stroke sequences.
- [ ] **Palette Auto-Sampler**: Automatically extract color palettes from reference photos and apply them to generated stroke sequences.

---

## 🛠️ Architecture & Toolchain

- **Language & UI**: Kotlin, Jetpack Compose, Material 3, Neo-Precision design tokens
- **Neural Engine**: Puter.js headless JavaScript bridge with live model catalog
- **Gesture Automation**: Android `AccessibilityService` DispatchGesture API with multi-stroke batching
- **Overlay Window**: Android `WindowManager` with dynamic layout params & touch passthrough
- **CI/CD Pipeline**: GitHub Actions with path-based filtering (triggers only when app code or workflows change), automated semantic versioning, release tagging, and automated APK signing via base64 keystores

---

## 📄 License

This project is licensed under the MIT License.

