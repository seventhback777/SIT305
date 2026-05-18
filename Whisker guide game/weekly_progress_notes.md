# Weekly Progress Notes — WhiskerGuide (SIT305 HD Task 8.2)

---

## Week 8 (early April 2026)

- Drafted app proposal: Android RPG with an embedded on-device AI guide cat.
- Surveyed candidate models: Gemma 2/3, Phi-3, Llama 3.2. Evaluated size,
  license, and Android runtime support.
- Selected Gemma 3 1B int4 (`.task` format) for its size budget (~554 MB),
  permissive license, and first-class MediaPipe Tasks GenAI support.
- Confirmed integration approach: on-device inference via MediaPipe, with a
  deterministic Mock engine as fallback.

---

## Week 9

- Set up Android Studio project with AGP / Gradle KTS, targetSdk 36.
- Defined core data models: `Player`, `Enemy`, `Item`, `Skill`, `MapData`,
  and associated enums (`ItemType`, `SkillType`, `TileType`).
- Built `GameView` (Canvas-based 8×8 maze renderer) and basic tile movement.
- Implemented turn-based combat loop: Basic Attack, Fireball (AoE, 30 mana,
  40 damage), Health Potion consume.
- **Decision**: used `GameStateHolder` (singleton + `LiveData<GameState>`) to
  decouple game logic from the AI chat UI — the cat can read current state
  without coupling to `GameViewModel`.

---

## Week 10

- Designed `LlmEngine` interface with `InitCallback` and `LlmCallback` to
  abstract away inference backend.
- Built `ContextBuilder`: constructs the full prompt as
  `[SYSTEM] + [GAME STATE] + [KNOWLEDGE] + [PLAYER QUESTION]`.
- Built `JsonKnowledgeRepository` over 5 JSON knowledge files (enemies,
  items, skills, quests, mechanics) — a lightweight keyword-based RAG layer.
- Implemented `MockLlmEngine`: a keyword-routing deterministic engine for
  CI/emulator use and guaranteed demo stability.
- Wired `CatFragment`, `CatViewModel`, and `ChatMessageAdapter` into the UI.
- **Challenge**: first attempts to load MediaPipe on the x86_64 emulator
  failed — discovered that LiteRT is ARM-only at runtime; emulator must use Mock.

---

## Week 11

- Integrated MediaPipe Tasks GenAI 0.10.24 into the Gradle build.
- Implemented `MediaPipeLlmEngine` with `LlmInferenceSession`; tuned
  `topK`, `topP`, `temperature`, and `randomSeed` for coherent short answers.
- Wrote `ResponseFilter`: prefix stripping (`A:`, `Answer:`, `-`),
  repeat-sequence detection, off-topic blacklist, 200-character length cap.
- Built `LoadingFragment` with async model init and automatic Mock fallback
  on load failure.
- First end-to-end real-LLM run on OnePlus Ace 2 (API 36). Gemma 3 1B
  producing contextual answers.
- **Challenge**: initial prompts were in Chinese. Gemma 1B exhibited token
  loops and hallucinated monster names in Chinese. Suspected cause: 1B scale
  insufficient for Chinese instruction-following.

---

## Week 12 (13 May – 18 May 2026)

**13–14 May**
- Reproduced and triaged Chinese-language quality issues: token repetition
  loops, hallucinations of non-existent enemies, user-input echo.
- Root cause confirmed: Gemma 1B's Chinese capability is limited at 1B scale.

**15 May**
- Converted all prompts, knowledge base entries, Mock responses, UI labels,
  and game messages to English. Response quality improved dramatically.
- Made game state more explicit in prompts: each skill now emits
  `"Fireball (on cooldown, 2 turn(s) left)"` rather than appearing only in
  the ready-skills list, so the LLM can directly reason about cooldown state.
- **Attempted model upgrade**: replaced Gemma 3 1B (554 MB) with
  Phi-4-mini-instruct Q8 (3.9 GB) to improve instruction-following.
  Result: native memory peaked at ~11 GB during FP16 dequantisation + KV
  cache allocation, triggering Android's low-memory killer under direct-memory
  reclaim on the OnePlus Ace 2 (16 GB RAM). Process killed.
  **Resolution**: rolled back to Gemma 1B by replacing the `.task` file —
  zero code changes required, validating the `LlmEngine` abstraction.
- Added safety pre-filter in `CatViewModel` covering 6 unsafe input categories;
  filter runs before any engine call.
- Added engine toggle button (Mock ↔ AI), graceful degradation UI states, and
  a persistent privacy notice in the first chat message.
- Enabled predictive back navigation
  (`android:enableOnBackInvokedCallback="true"`); verified `largeHeap` setting.

**16–18 May**
- Captured screenshots on API 35 emulator (3 images) and API 36 real device
  (6 images) covering: main game, combat, AI chat, safety refusal, engine toggle.
- Finalised README, weekly notes, and submission packaging.
- Recorded app demo video and 10-minute presentation video.
