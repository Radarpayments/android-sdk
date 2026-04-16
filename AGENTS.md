# AGENTS.md — PAYRDR Payment SDK

> **One source of truth for coding agents** (Claude Code, Codex, Cursor, etc.).
> `CLAUDE.md` is a symlink to this file.

---

## Project overview

**PAYRDR Payment SDK** is a multi-module Android library by Radarpayments / Runet-Business-Systems.
Root namespace: `net.payrdr.mobile.payment.sdk.*`. Current release line: **3.0.x**.
Distributed as AAR assets attached to GitHub releases — **no Maven Central, no maven-publish configuration**.

> This repo (`Runet-Business-Systems/android-sdk`) is a **public mirror** of an upstream private GitLab
> repo. Commits arrive here as rsync drops labelled `Update <semver>`. Direct edits to `main` may be
> overwritten by the next mirror sync — confirm with the team before landing changes here.

---

## Module map

| Module | Type | Purpose |
|---|---|---|
| `sdk_logs` | library | Sentry-based logging utility. No internal deps. |
| `sdk_core` | library | Card validation, RSA cryptogram generation, payment-string processing, models. Depends on `sdk_logs`. |
| `sdk_forms` | library | Prebuilt UI: card entry / card list / Google Pay / NFC scan (bundles `card.io-5.5.1-OS1.aar`). Depends on `sdk_core` + `sdk_logs`. Uses `kotlin-android-extensions` synthetics. |
| `sdk_payment` | library | High-level orchestration. Public entry point: `SDKPayment` object. 3DS1 web-challenge handling via in-process `Activity3DS2WebChallenge`, `PaymentManager`. Depends on `sdk_forms`, `sdk_core`, `sdk_logs`. |
| `app_kotlin_core` | sample app | Minimal Kotlin sample exercising `sdk_core` only. |
| `app_kotlin_ui` | sample app | Full-feature Kotlin sample using `sdk_payment`. Consumes AARs from `app_kotlin_ui/libs/`. |
| `app_java_ui` | sample app | Java sample using `sdk_forms` directly. |
| `buildSrc/` | convention | `Dependencies.kt` — single source of truth for versions and coordinates. `plugins/jacoco-report.gradle.kts`. |
| `config/` | tooling | `detekt/detekt.yml`, `codeStyle.xml`, `git/hooks/pre-commit.sh`. |

---

## Toolchain — frozen, do not bump

| Tool | Version |
|---|---|
| Gradle wrapper | 7.3.3 |
| Android Gradle Plugin | 4.1.3 |
| Kotlin | 1.7.10 (stdlib pinned to `1.4.30` in `Dependencies.kt`) |
| JVM target | Java 8 |
| `compileSdk` | 33 for SDK modules and `app_kotlin_core`/`app_java_ui`; 36 for `app_kotlin_ui` |
| `minSdk` / `targetSdk` | 21 / 35 for SDK modules |

**Do not modernize** Kotlin/AGP/Gradle or migrate away from `kotlin-android-extensions` synthetics
without explicit instruction. These are deliberate pins tied to the upstream release line.

Source layout: `src/<variant>/kotlin/...` everywhere (each module remaps `java → kotlin` in `sourceSets`).
Exception: `sdk_payment/src/test/java/...` uses the raw Java path.

---

## Bootstrap — must run before sample apps build

```bash
./copy_sdk_lib_to_samples.sh
```

What this script does:
- Builds `:sdk_logs :sdk_core :sdk_forms :sdk_payment` and copies every `*-release.aar` (plus the
  bundled `card.io-5.5.1-OS1.aar` from `sdk_forms/libs/`) into `app_kotlin_core/libs/`,
  `app_kotlin_ui/libs/`, `app_java_ui/libs/`.

Sample apps use `flatDir { dirs("libs") }` AAR drop-ins — **without this script they will not resolve
their dependencies**. The `implementation(project(":sdk_X"))` lines that exist in sample build files
are commented out by design; do not uncomment them.

---

## Common commands

```bash
# Build
./gradlew assemble
./gradlew :sdk_core:assembleRelease        # single module

# Unit tests
./gradlew test                             # all modules
./gradlew :sdk_core:testDebugUnitTest      # one module

# Instrumented tests (connected device or emulator required)
./gradlew :sdk_core:connectedAndroidTest
./gradlew :sdk_forms:connectedAndroidTest
./gradlew :sdk_payment:connectedAndroidTest

# Collect Allure results (after instrumented run)
./script_allure_results_pack.sh

# Lint — Docker must be running
./script_check_detekt.sh
./script_check_ktlint.sh
./script_pre_commit.sh                     # = detekt + ktlint (what the git hook runs)
```

---

## Testing notes

- Unit tests: `src/test/kotlin/...` in `sdk_core` and `sdk_forms`; `src/test/java/...` in `sdk_payment`.
- `sdk_logs` has no tests.
- Instrumented test runner: `io.qameta.allure.android.runners.AllureAndroidJUnitRunner`.
  Allure 2.20.1 / `allure-kotlin` 2.4.0.
- Frameworks: JUnit 4.13, Kotest 4.1.1, MockK 1.13.2 (`mockk-android`), `kotlinx-coroutines-test`,
  OkHttp `mockwebserver`, Espresso, Kaspresso 1.5.2.
- `testOptions.unitTests.isIncludeAndroidResources = true` and `isReturnDefaultValues = true` are set
  in every SDK module — Robolectric-style tests can access Android resources without an emulator.
- JaCoCo is **disabled** everywhere (`isTestCoverageEnabled = false`).

---

## Code style & quality

- Style: `kotlin.code.style=official` (`gradle.properties`).
- **detekt** (`config/detekt/detekt.yml`, `maxIssues: 0`) and **ktlint** run only via Docker images
  (`mdenissov/detekt-checker`, `mdenissov/ktlint-checker:1.0.0`). Targets: `sdk_core`, `sdk_forms`,
  `sdk_payment`. Not checked: `sdk_logs`, sample apps.
- Public APIs need KDoc — detekt's `UndocumentedPublicClass` rule is active. Use
  `@file:Suppress("UndocumentedPublicClass")` only where justified.
- Running `./gradlew clean` auto-installs the git pre-commit hook: the root `build.gradle.kts` defines
  a `copyGitHooks` task (wired to `clean`) that copies `config/git/hooks/pre-commit.sh` →
  `.git/hooks/pre-commit`. **Docker must be running for this hook to pass.**
- `sdk_forms` compiles with `-Xjsr305=strict`,
  `-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi`,
  `-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi`.

---

## Versioning & dependencies

All shared versions and coordinates live in `buildSrc/src/main/java/Dependencies.kt`
(`Versions`, `Libs`, `TestLibs`, `BuildVersionsAndroid`, `SDKBuildVersions`).
**Never hard-code versions in module `build.gradle.kts` files.** Sample apps are the only exception.

`SDKBuildVersions` shows `"local-dev-<module>"` on `main`; CI overwrites these via
`./script_update_modules_version.sh` (using `CI_COMMIT_TAG`, or `Dev(<short sha>)` otherwise).

---

## Publishing

- Artifacts: AARs uploaded as assets to GitHub releases (no Maven Central).
- Public mirror: `git@github.com:Runet-Business-Systems/android-sdk.git`.
- Scripts: `script_publish_github.sh`, `script_publish_github_release.sh`.
- CI: `.github/workflows/main.yml`.

---

## Gotchas

1. **3DS coverage is 3DS v1 only.** The native EMV 3DS2 SDK module (`sdk_threeds`) was removed.
   `sdk_payment` now handles only the legacy ACS-redirect flow via `Activity3DS2WebChallenge` (a
   `WebView` that auto-POSTs `MD/PaReq/TermUrl` to the issuer ACS and waits for `sdk://done`). If the
   backend returns `threeDSMethodURL` (signalling that the merchant requires a native 3DS2 SDK),
   `PaymentManagerImpl` throws `SDKNotConfigureException("Merchant is not configured to be used
   without 3DS2SDK")`. Mirrors the iOS SDK 3.0.7 removal.

2. **Sample apps will not build cold.** Always run `./copy_sdk_lib_to_samples.sh` first.

3. **Docker is required** for the detekt/ktlint scripts and therefore for the gradle-installed git
   pre-commit hook.

4. **This repo is a public mirror.** Direct commits to `main` may be overwritten by the next mirror
   sync. Confirm before landing changes.

5. **Old toolchain is intentional.** Do not bump Gradle/AGP/Kotlin or remove `kotlin-android-extensions`
   without explicit instruction.

---

## Commit conventions

All existing history uses `Update <semver>` (e.g. `Update 3.0.13`) — these are mirror-sync commits, not
authored commits. There are no Conventional Commits prefixes. For real authored commits, write a normal
descriptive subject; do not imitate the `Update X.Y.Z` form.

---

## Key pointers

| What | Where |
|---|---|
| Public SDK entry point | `sdk_payment/src/main/kotlin/.../SDKPayment.kt` |
| Card validation entry point | `sdk_core/src/main/kotlin/.../SDKCore.kt` |
| All versions | `buildSrc/src/main/java/Dependencies.kt` |
| detekt config | `config/detekt/detekt.yml` |
| CI workflow | `.github/workflows/main.yml` |
| Bootstrap script | `copy_sdk_lib_to_samples.sh` |
| Pre-commit hook source | `config/git/hooks/pre-commit.sh` |
| Gradle hook installer | `copyGitHooks` task in root `build.gradle.kts` |
