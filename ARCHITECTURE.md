# Resthalf — Architecture

A modular-monolith Kotlin Multiplatform app organized **package-by-feature** inside a single `:shared` module. Designed to grow into proper Gradle module-per-feature only when justified.

## Stack

- **Kotlin Multiplatform** — Android + iOS (`iosArm64`)
- **Compose Multiplatform** — shared UI for both targets
- **Material 3 Expressive** (alpha) — wrapped in `core/design/` to insulate from API churn
- **Decompose + Essenty** — navigation, component tree, state survival
- **Koin** — dependency injection (one module per feature, composed at root)
- **Ktor** — multiplatform HTTP client
- **multiplatform-settings** — key-value storage (tokens, prefs)

## Package layout (inside `shared/src/commonMain/kotlin/com/resthalflab/resthalfapp/`)

```
app/                         host: root + main shell + Koin module composition
  RootComponent.kt           Decompose root: Login | Main (driven by AuthApi.session)
  MainComponent.kt           authenticated bottom-nav shell (Home/Bookings/Favorites/Profile tabs)
  AppModule.kt               list of all Koin modules

core/                        cross-cutting capabilities — no feature knowledge
  domain/                    AppError, AppResult, Logger (expect/actual)
  network/                   Ktor HttpClient factory, Bearer Auth slot, error mapping
  storage/                   SettingsFactory + SecureTokenStore (settings-backed; Keychain/EncryptedPrefs in Phase 3)
  design/                    M3 Expressive theme + wrapped components (RhButton, RhTextField, RhScaffold, ...)
  navigation/                shared Decompose helpers if needed                    [as needed]

feature/                     one folder per feature, self-contained
  <feature>/
    api/                     public interfaces other features may import
    data/                    repositories, DTOs, Ktor calls
    domain/                  use cases
    ui/                      Decompose component + composables
    di.kt                    Koin module for this feature
```

Platform-specific actuals live under their source set, mirroring the common path:
`shared/src/androidMain/kotlin/com/resthalflab/resthalfapp/core/domain/Logger.android.kt`, etc.

## Rules

1. **Features may not import other features' `data/`, `domain/`, `ui/`, or `di.kt`.** Only `feature/<other>/api/` is fair game.
2. **`core/` may not import anything from `feature/`.** Direction of dependency is `app` → `feature` → `core`.
3. **Each feature ships its own Koin module** in `feature/<x>/di.kt` and is appended to `appModules` in `app/AppModule.kt`.
4. **Each feature exposes one Decompose component** as its public entry; the root composes them into the stack.
5. **Wrap M3 Expressive components** through `core/design/` before using in features. Never call raw `androidx.compose.material3.Button` from a feature.
6. **Networking errors map to `AppError`** at the `core/network/` boundary. Features never see `Throwable` from Ktor.

## Adding a new feature

1. Create `feature/<name>/{api,data,domain,ui}/` and `di.kt`.
2. Define your public surface in `api/` — interfaces, navigation key/config, DTO shapes other features need.
3. Build the Decompose component in `ui/` plus its `UiState` and `Intent`.
4. Create the Koin module in `di.kt` and append it to `appModules`.
5. Add a `Config.<Name>` entry in `RootComponent` and route to it.

## Graduation criteria — when to extract a feature into its own Gradle module

Move from package to module when **any** of these is true:
- A second team starts contributing to the codebase
- Incremental build time on `:shared` exceeds ~30s
- A feature grows past ~5k LOC and rarely changes (stable surface ⇒ cheap to isolate)
- You need different release cadence for some surface (e.g., kiosk variant)
- A feature needs to be reused across apps

Until then, modules are pure ceremony. The package rules above keep extraction cheap when the day comes.

## Testing

- **Unit tests** in `commonTest` for use cases and components (no UI).
- **Turbine** for asserting state flows from Decompose components.
- **Kotest assertions** for readable expectations.
- Fakes/fixtures live in a future `core/testing/` (added when first needed).

## Not yet decided / deferred

- Convention plugins under `build-logic/` — deferred until module count makes them worth it.
- Konsist or module-graph-assert enforcement — add when a second contributor joins.
- Analytics / crash reporting — interface lands in `core/domain/` when first needed.
- Deep links — Decompose `DeepLink` wiring, added with the first deep-linkable feature.
