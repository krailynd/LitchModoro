# Contributing to LitchModoro

Thanks for being here. LitchModoro is small on purpose, and that's exactly what makes it a friendly place to contribute: a clear core, a thin UI layer, and fast tests.

## Dev setup

1. **JDK 21+** (`java -version` should say 21 or newer)
2. **Maven 3.9+**
3. Any IDE (IntelliJ/Eclipse/VS Code). JavaFX 21.0.5 comes in as a normal Maven dependency — no SDK download needed for development.

```bash
mvn test        # 48 JUnit 5 tests, all in-memory, finish in seconds
mvn package     # target/litchmodoro-1.0.0.jar
```

Run the app from your IDE with main class `com.krailynd.pomodoro.MainApp`. Building the native Windows package is only needed for release work — see the README's *Build the Windows app* section.

## Project layout

```
src/main/java/com/krailynd/pomodoro/
├── core/      # pure Java state machine — TimerEngine, BreakPolicy, SessionPlan
├── config/    # persistence & validation — SettingsStore, CustomConfigFactory, PlanPreview
├── ui/        # JavaFX — MainWindow, MainController, RingProgress, PresetPanel
└── MainApp    # composition root
```

## Code style

- **Package boundaries are the contract.** `core/` must stay pure Java: no JavaFX imports, no threads, no clock reads. `config/` may use `java.util.prefs` but must not import JavaFX either. Only `ui/` touches JavaFX.
- **The engine is tick-driven.** Never add timers or `Thread.sleep` to `core/` — time enters the engine exclusively through `tick(seconds)`. This is what keeps the tests deterministic.
- **Follow the existing idiom:** `final` classes in `core/`, records where they fit (`SessionSettings`, `Segment`), sealed interfaces for closed result sets (`CustomConfigFactory.Result`), builder for validated configs (`SessionConfig`).
- **Tests are required for `core/` changes.** Any change to the state machine, plan generation, or policy logic must come with JUnit 5 tests in the matching `src/test` package. UI changes need a manual smoke run instead (see below).

## Proposing features

Open an issue **before** writing code for anything beyond a typo or an obvious bug fix. Describe the problem or the idea, and let's agree on the shape — this saves everyone rework and keeps the scope honest. Break-policy ideas in particular are cheap to add (one class implementing `BreakPolicy`) and very welcome.

## Pull requests

- **Small and focused.** One logical change per PR. A policy tweak and a UI refactor are two PRs.
- **Tests green.** `mvn test` must pass before you open it.
- **UI changes:** include a screenshot of the before/after state and note the window sizes you tested (the layout is designed around 360×560, minimum 300×480). Also verify the reduced-motion path still looks right (`Animations.ENABLED = false`).
- **Windows packaging changes:** test `packaging\build-windows.ps1` on a Windows host; the default portable build needs no WiX.
- Describe *what* and *why* in the PR body; the diff shows *how*.

## Where to look

| You want to… | Start at |
|---|---|
| Add a new break strategy | `src/main/java/com/krailynd/pomodoro/core/AdaptiveBreakPolicy.java` (implement `BreakPolicy`, mirror its test class) |
| Change the visual design | `src/main/resources/com/krailynd/pomodoro/ui/theme.css` — colors, radii, and typography all live there |
| Tweak the ring animation | `ui/RingProgress.java` (`LERP_FACTOR` controls smoothing) |
| Add a setting | `config/SettingsStore.java` + `config/SessionSettings.java` |
| Understand the state machine | `core/TimerEngine.java` + its test, `core/TimerState.java` |

## Ground rules

- MIT license — by contributing you agree your contribution is offered under it.
- Be kind. This is a hobby project run by one person; patience scales better than features.
