# Agent Brief 3: Now-Card Ticker, Reminders & Voice

## Starting Point & Architecture Rules

- Multiplatform targets: Android, iOS, and Web (WasmJS / JS).
- Clean Architecture layers:
  - `domain`: entities, repositories, use cases
  - `data`: local database drivers (SQLDelight on Android & iOS, In-Memory on Web), mappers
  - `presentation`: Compose Multiplatform UI, MVVM with ViewModels and reactive StateFlow
  - `core`: design system, utilities, dependency injection (Koin)
- Do not upgrade Kotlin, Compose, Gradle, Koin, or SQLDelight versions.
- Preserve platformModule and appModule separation.
- Common tests in `commonTest` run on all platforms.

---

## Specs

### Prompt 1 — Now-card ticker only

#### Requirements:
1. Create `core/util/MinuteTicker.kt`.
2. Implement `minuteTicker(): Flow<Int>`.
3. Emit the current minutes of the day immediately.
4. Emit again at the beginning of every minute.
5. Update `HomeViewModel` to combine `minuteTicker()` with the observed task list.
6. Ensure the Now task and upcoming tasks update automatically as time changes.
7. Add `commonTest` coverage for Now task selection at three different times using fake time values, not the real clock.

#### Constraints:
- Do not touch reminder scheduling or voice functionality.
- Do not modify unrelated screens.
- Preserve existing dependency versions and all targets.

#### Done Check:
- The Now card automatically moves to the next task when its start time arrives.

---

### Subsequent Prompts (Summary)
- Prompt 2: Reminder scheduling architecture and domain contracts.
- Prompt 3: Platform-specific reminder implementations.
- Prompt 4: Voice integration contracts and speaker interface.
- Prompt 5: Platform-specific voice / TTS engines.
- Prompt 6: Final wiring, settings integration, and comprehensive verification.
