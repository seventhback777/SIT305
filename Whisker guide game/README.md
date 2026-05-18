# WhiskerGuide

An offline Android RPG demo with an embedded on-device AI guide cat,
built for SIT305 HD Task 8.2.

The player explores a small dungeon while a generative AI assistant
("WhiskerGuide") answers contextual questions about enemies, skills, items,
and the current game state — all running locally on the device, no network.

---

## Quick Facts

| | |
|---|---|
| Language | Java |
| Min SDK | API 24 (Android 7.0) |
| Target / Compile SDK | API 36 (Android 16) |
| Build system | AGP via Gradle KTS |
| LLM | Gemma 3 1B Instruct (int4, `.task` format) |
| LLM runtime | MediaPipe Tasks GenAI 0.10.24 |
| Inference mode | **On-device, fully offline** |
| Tested on | Pixel 6 emulator (API 35), OnePlus Ace 2 (API 36, real device) |

---

## What the LLM Does

The in-game cat ("WhiskerGuide") is a **contextual Q&A assistant** that
helps the player while they explore and fight. It can answer:

- Enemy stats and weaknesses (`How do I beat the goblin?`)
- Skill mechanics (`What does Fireball do?`, `Is fireball ready?`)
- Item effects (`How many potions do I have?`)
- Navigation (`Where's the exit?`)
- Real-time combat advice (`What should I do?`)

Every prompt sent to the model is built from three sources:

1. A short **system prompt** with strict grounding rules
2. The current **game state** (HP, mana, location, combat status,
   inventory, ready skills with cooldown info, last event)
3. **Retrieved knowledge entries** matched by keywords from the user's
   question

This is a small retrieval-augmented generation (RAG) pipeline,
implemented from scratch in `ContextBuilder.java`.

---

## Architecture

```
┌──────────────┐   pushState   ┌──────────────────┐
│ GameViewModel│ ───────────▶  │ GameStateHolder  │
└──────────────┘               │ LiveData<State>  │
                               └────────┬─────────┘
                                        │
                                        ▼
┌─────────────┐   ask(q)     ┌─────────────────────┐
│ CatFragment │ ───────────▶ │   CatViewModel      │
└─────────────┘              │ (Safety pre-filter) │
                             └────────┬────────────┘
                                      │
                  ContextBuilder.build(q)
                  = [SYSTEM] + [GAME STATE] + [KNOWLEDGE] + [QUESTION]
                                      │
                                      ▼
                             ┌─────────────────┐
                             │   LlmEngine     │  ← interface
                             └────────┬────────┘
                                      │
                ┌─────────────────────┴─────────────────────┐
                ▼                                           ▼
     ┌────────────────────┐                     ┌────────────────────┐
     │ MediaPipeLlmEngine │                     │   MockLlmEngine    │
     │  (Gemma 3 1B)      │                     │  (keyword router)  │
     └─────────┬──────────┘                     └─────────┬──────────┘
               │                                           │
               └─────────────────┬─────────────────────────┘
                                 ▼
                       ┌────────────────────┐
                       │  ResponseFilter    │
                       │ (prefix strip,     │
                       │  repeat detect,    │
                       │  length cap)       │
                       └─────────┬──────────┘
                                 ▼
                            UI update
```

The `LlmEngine` interface enables a **hybrid runtime**:

- On real ARM devices, MediaPipe loads Gemma 3 1B and serves answers.
- On x86_64 emulators (where the MediaPipe LiteRT runtime is unavailable),
  the app gracefully degrades to the deterministic Mock engine.
- The user can also manually switch engines via a toggle in the chat
  screen for live demos.

---

## Gemma 3 Integration Notes

**Model**: Gemma 3 1B Instruct, int4 quantised (`.task` format for MediaPipe LiteRT)

**Why Gemma 3 1B?**
- Gemma 3 (released 2025) is newer than the task's suggested Gemma 2 / Llama 3.1 baseline.
- The int4 `.task` variant (~554 MB) fits within the memory budget of mid-range ARM devices.
- MediaPipe Tasks GenAI provides a stable Android-native inference API with session isolation.

**Integration mode**: On-device inference (hybrid with deterministic Mock fallback)

**What data leaves the device**: Nothing. All inference is local. The app makes zero network calls.

**Offline capability**: Fully offline once the model file is pushed to the device.

---

## Privacy

All inference happens on-device. No conversation data, no game state,
and no telemetry leaves the phone. The app makes **zero network calls**.

The first message in the chat surfaces this to the user:

> 🔒 Privacy: I run entirely on your device. Nothing you type ever
> leaves your phone.

---

## Safety

A pre-LLM safety filter (`CatViewModel.UNSAFE_PATTERNS`) blocks unsafe
inputs **before** they ever reach the model. Six categories are covered:

1. Self-harm / harm to people
2. Weapons / explosives
3. Illegal activity (drugs, hacking, ransomware)
4. Personal information exfiltration (passwords, credit cards)
5. Prompt injection / jailbreak attempts
6. Inappropriate content

Blocked inputs receive a fixed refusal message and never trigger the
LLM. Because the filter sits above the engine layer, it works identically
whether the active engine is MediaPipe or Mock.

The filter is deliberately conservative: phrases like `kill the goblin`
are allowed, because the unsafe patterns require specific full phrases
(`kill myself`, `kill a person`, etc.) rather than single keywords.

---

## How to Build & Run

### Prerequisites

- Android Studio Panda 1 (2025.3.1 Patch 1) or later
- Android SDK 36 platform installed
- A physical ARM device running Android 7.0+ for real LLM inference
- (Optional) An API 35 or API 36 emulator for UI testing

### Build

```bash
git clone <this-repo>
cd Whiskerguide
./gradlew assembleDebug
```

### Deploy the LLM model (real device only)

The Gemma 3 1B `.task` file is **not** bundled in the APK (it is ~550 MB).
Download it from Kaggle and push it to the device:

1. Download `gemma3-1b-it-int4` from
   [Kaggle Models](https://www.kaggle.com/models/google/gemma-3/tfLite/gemma-3-1b-it-int4)
   — pick the `.task` variant.
2. Create the target directory on device:
   ```bash
   adb shell mkdir -p /data/local/tmp/llm
   ```
3. Push the file (must be named `gemma.task`):
   ```bash
   adb push gemma.task /data/local/tmp/llm/gemma.task
   ```
4. Install and launch the app:
   ```bash
   ./gradlew installDebug
   ```

If the model file is missing or the device is an x86_64 emulator, the
app automatically falls back to the Mock engine — the rest of the app
remains fully functional.

### Run on emulator (Mock-only mode)

Just `./gradlew installDebug` against any API 35+ emulator. The chat
button will show `Engine: Mock (AI unavailable)`, which is the expected
degraded mode.

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml          (largeHeap + predictive back enabled)
├── assets/knowledge/            (5 JSON knowledge files)
└── java/com/example/whiskerguide/
    ├── MainActivity.java
    ├── WhiskerGuideApp.java     (Service Locator, engine selection)
    ├── game/                    (RPG: model, view, viewmodel)
    ├── cat/                     (AI assistant: model, view, viewmodel, engine)
    ├── knowledge/               (RAG store)
    └── common/                  (GameState + Holder)
```

---

## Compatibility

| Item | Value |
|---|---|
| Min SDK | 24 |
| Target SDK | 36 (Android 16) |
| Compile SDK | 36 |
| Tested API levels | 35 (Pixel 6 emulator), 36 (OnePlus Ace 2, real device) |
| Predictive back navigation | Enabled via `android:enableOnBackInvokedCallback="true"` |
| Large heap | Enabled (required for LLM weights in native memory) |

---

## License

Coursework project — not for redistribution.
