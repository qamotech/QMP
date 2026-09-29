# 🎧🎚️ Qamelot Media Studio

**Audio-production project with a React web workstation and native Android Kotlin sources for sequencing, synthesis, performance pads, effects, and project storage.**

🏷️ Maintained in [qamotech/QMP](https://github.com/qamotech/QMP) · 🌐 Public repository

## ✨ What is here

- 🥁 Web step sequencer and performance-pad components.
- 🎚️ Synthesizer, effects, EQ, oscilloscope, and spatial-panning interfaces.
- 📱 Native Android audio, UI, preset, and database source files.
- 🤖 Android Gemini beat-service source alongside conventional audio tools.

## 🧭 Try the project

Use the web development server to explore sequencing and sound controls. Treat the native Android tree as a separate build path and inspect its Gradle configuration before opening it in Android Studio.

## 🚀 Local setup

```sh
git clone https://github.com/qamotech/QMP.git
cd QMP
```

Use Node.js and npm compatible with the versions in `package.json`. Install dependencies locally, then launch the declared development command:

```sh
npm install
npm run dev
```

Open the address printed by the development server. The manifest is the source of truth for commands:

| Command | Declared operation |
|---|---|
| `npm run test` | `echo "Error: no test specified" && exit 1` |
| `npm run dev` | `vite --host 0.0.0.0 --port 3000` |
| `npm run build` | `vite build` |
| `npm run preview` | `vite preview` |

Install/build scripts can execute code. Inspect project configuration and keep secrets in local configuration excluded from Git. No dependency installation or application build was performed for this documentation update.

## 🗂️ Source map

- 📄 [`build.gradle.kts`](build.gradle.kts)
- 📄 [`gradle.properties`](gradle.properties)
- 📄 [`index.html`](index.html)
- 📄 [`metadata.json`](metadata.json)
- 📄 [`package-lock.json`](package-lock.json)
- 📄 [`package.json`](package.json)
- 📄 [`settings.gradle.kts`](settings.gradle.kts)
- 📄 [`tsconfig.json`](tsconfig.json)
- 📄 [`tsconfig.node.json`](tsconfig.node.json)
- 📄 [`vite.config.ts`](vite.config.ts)
- 📄 [`src`](src) — source directory
- 📄 [`app`](app) — source directory
- 📄 [`assets`](assets) — source directory

## ⚙️ Configuration & data

The npm test command is an intentional failing placeholder, not a test suite. Native Android and web builds have not been validated by this documentation pass. Provider-backed AI requires separate configuration.

Keep credentials, private exports, customer records, and personal information out of commits and screenshots. A local browser demo is not evidence of account security, reliable persistence, or connected external services. Preserve exports before changing storage keys or resetting an application.

## 🧪 Verification checklist

- 🔎 Confirm the entry file and asset paths above exist in your checkout.
- ▶️ Start the documented runtime and inspect browser or terminal errors.
- 🧭 Exercise the project-specific workflow described above using sample data.
- 📱 Check narrow and wide layouts when the project has a browser interface.
- 💾 Verify save/export and recovery behavior before trusting important work to it.
- 📝 Record the exact command, browser, operating system, and outcome of your checks.

This guide was prepared from repository files and manifests. It does not claim a fresh build, deployment, security audit, or full functional test of this project.

## 🤝 Contributions & useful reports

Keep changes focused and explain the user-visible result. Preserve existing assets and configuration unless a change requires updating them. Include reproduction steps, expected and actual behavior, and relevant screenshots with personal information removed. For UI work, include the viewport and browser; for runtime issues, include the command and error text.

## 🛠️ Maintenance priorities

- 📚 Keep this guide aligned with implemented behavior and current entry points.
- 🧪 Add or maintain checks for the core workflow before expanding features.
- ♿ Review labels, keyboard navigation, contrast, and responsive layout.
- 📦 Document external services, asset rights, and deployment prerequisites.

## 📜 Licensing & attribution

This documentation update does not grant a new software or asset license. Consult existing license files, source headers, package metadata, and original asset terms; resolve inconsistencies with the owner before redistribution. Third-party names and resources retain their own terms.

---

## 📚 Preserved earlier documentation

The material below is retained verbatim for history and project-specific context. Template instructions and older claims may differ from the source inventory above.

# Qamelot Media Studio

Qamelot Media Studio is a high-performance web-based audio workstation designed for premastering HipHop, Pop, and RnB audio production.

## Features

- **Step Sequencer**: 16-step grid with support for multiple tracks and per-track sound selection.
- **Synthesis Engine**: Real-time sound generation using oscillators and noise sources.
- **Audio Effects**: Built-in reverb, delay, saturation, and multi-band compression.
- **Mastering Tools**: Spectral analyzer, oscilloscope, and master limiter.
- **Performance Pads**: Low-latency pads for live sound triggering.

## Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/) (v18 or higher recommended)
- npm (comes with Node.js)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/qamelot-media-studio.git
   cd qamelot-media-studio
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

### Running Locally

To start the development server:

```bash
npm run dev
```

The app will be available at `http://localhost:3000`.

### Building for Production

To create a production build in the `dist` folder:

```bash
npm run build
```

## Technologies Used

- [React](https://reactjs.org/)
- [Vite](https://vitejs.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Framer Motion](https://www.framer.com/motion/)
- [Lucide React](https://lucide.dev/)
- [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API)

## License

This project is licensed under the ISC License.
