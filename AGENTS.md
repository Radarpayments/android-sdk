# AGENTS.md — PAYRDR Payment SDK

> **One source of truth for coding agents** (Claude Code, Codex, Cursor, etc.).
> `CLAUDE.md` is a symlink to this file.

---

## Project overview

**PAYRDR Payment SDK** is a multi-module Android library by BPC (Radarpayments).
Root namespace: `net.payrdr.mobile.payment.sdk.*`. Current release line: **3.0.x**.
Published to Maven Central as `io.github.radarpayments:sdk-payment`, `sdk-core`, `sdk-logs`, `card-io`; the
release AARs are also attached to GitHub releases.

> Development happens here, in `Radarpayments/android-sdk` (since October 2026). The older history consists of
> `Update <semver>` drops mirrored from a private GitLab repo; that mirror flow is no longer used.

---

## Module map

| Module | Type | Purpose |
|---|---|---|
| `sdk_logs` | library | Sentry-based logging utility. No internal deps. |
| `sdk_core` | library | Card validation, RSA cryptogram generation, payment-string processing, models. Depends on `sdk_logs`. |
| `sdk_payment` | library | Public entry point: `SDKPayment` object (`init` in `Application`, `checkout`, `handleCheckoutResult`). Payment orchestration (`PaymentActivity`, `PaymentManager`), 3DS1 web challenge via in-process `Activity3DS2WebChallenge`, and the prebuilt UI formerly in `sdk_forms`: card entry / card list / Google Pay / NFC scan (depends on `card-io`, see `card_io`). Uses `kotlin-android-extensions` synthetics. Depends on `sdk_core`, `sdk_logs`. |
| `card_io` | publishing only | Publishes the prebuilt card.io AAR (`repo/io/github/radarpayments/card-io/5.5.1-OS1/`, OutSystems fork 5.5.1-OS1 with 16 KB page support, sources at github.com/OutSystems/card.io-Android-source@0fe39f0) as `io.github.radarpayments:card-io`. `repo/` is a local Maven repository, so `sdk_payment` resolves it by coordinates. |
| `app_kotlin_core` | sample app | Minimal Kotlin sample exercising `sdk_core` only (card field validation). Consumes AARs from `app_kotlin_core/libs/`. |
| `app_kotlin_ui` | sample app | Kotlin sample using `sdk_payment`: one payment form (`PaymentFormFragment`) that takes an `mdOrder` and calls `SDKPayment.checkout`. Consumes AARs from `app_kotlin_ui/libs/`. |
| `buildSrc/` | convention | `src/main/java/Dependencies.kt` — single source of truth for versions and coordinates. `src/main/java/plugins/jacoco-report.gradle.kts`. |
| `config/` | tooling | `detekt/detekt.yml`, `codeStyle.xml`, `git/hooks/pre-commit.sh`. |

---

## Toolchain — frozen, do not bump

| Tool | Version |
|---|---|
| Gradle wrapper | 7.3.3 |
| Android Gradle Plugin | 4.1.3 |
| Kotlin | 1.7.10 (stdlib pinned to `1.4.30` in `Dependencies.kt`) |
| JVM target | Java 8 |
| `compileSdk` | 33 for SDK modules and `app_kotlin_core`; 36 for `app_kotlin_ui` |
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
- Builds the release AARs of `:sdk_logs :sdk_core :sdk_payment` (extra arguments go to Gradle).
- Copies `sdk_logs-release.aar` and `sdk_core-release.aar` into `app_kotlin_core/libs/`.
- Copies `sdk_logs`, `sdk_core`, `sdk_payment` release AARs plus the bundled
  card.io AAR from `repo/` (as `card.io-5.5.1-OS1.aar`) into `app_kotlin_ui/libs/`.

After changing SDK code, rerun the script (or rebuild the module and copy its AAR) — the samples do
not pick up source changes on their own.

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
./gradlew :sdk_payment:connectedAndroidTest

# Collect Allure results (after instrumented run)
./script_allure_results_pack.sh

# Lint — needs only Java 11+ (pinned jars are downloaded into build/lint-tools on first run)
./script_check_detekt.sh
./script_check_ktlint.sh
./script_pre_commit.sh                     # = detekt + ktlint (what the git hook runs)
```

---

## Testing notes

- Unit tests: `src/test/kotlin/...` in `sdk_core`; `src/test/java/...` in `sdk_payment`.
- `sdk_logs` has no tests.
- Instrumented test runner: `io.qameta.allure.android.runners.AllureAndroidJUnitRunner`.
  Allure 2.20.1 / `allure-kotlin` 2.4.0.
- Frameworks: JUnit 4.13, Kotest 4.1.1, MockK 1.13.2 (`mockk-android`), `kotlinx-coroutines-test`,
  OkHttp `mockwebserver`, Espresso, Kaspresso 1.5.2.
- `testOptions.unitTests.isIncludeAndroidResources = true` and `isReturnDefaultValues = true` are set
  in every SDK module — Robolectric-style tests can access Android resources without an emulator.
- JaCoCo is **disabled** everywhere (`isTestCoverageEnabled = false`).
- UI tests in `sdk_payment` use `test/junit/ConfigurationRule`: each test runs once (English, light
  theme). Annotate with `@ConfigurationLocales` / `@ConfigurationThemes` (empty = all locales / both
  themes) only when the test actually checks localization or theming — each combination is a full rerun.
- **No credentials in the repo.** `sdk_payment` instrumented tests read gateway credentials from
  instrumentation arguments (`TestCredentials`): pass
  `-Pandroid.testInstrumentationRunnerArguments.{merchantLogin,merchantPassword,apiKey}=...` or put
  these keys into `~/.gradle/gradle.properties`. CI takes them from the `MSDK_TEST_MERCHANT_LOGIN`,
  `MSDK_TEST_MERCHANT_PASSWORD`, `MSDK_TEST_API_KEY` secrets. The `app_kotlin_ui` release build is
  signed only when `sampleStorePassword` / `SAMPLE_STORE_PASSWORD` is provided, otherwise it is unsigned.

---

## Code style & quality

- Style: `kotlin.code.style=official` (`gradle.properties`).
- **detekt** 1.10.0 (`config/detekt/detekt.yml`, `maxIssues: 0`) and **ktlint** 0.45.2
  (`config/ktlint/.editorconfig`) run as CLI jars via `java -jar`. `script_lint_tools.sh` pins their
  versions and SHA-256 checksums and caches the jars in `build/lint-tools/`. Targets: `sdk_core`,
  `sdk_payment`. Not checked: `sdk_logs`, sample apps. CI runs the same `./script_pre_commit.sh` in the
  `lint` job, and releases depend on it.
- Public APIs need KDoc — detekt's `UndocumentedPublicClass` rule is active. Use
  `@file:Suppress("UndocumentedPublicClass")` only where justified.
- Running `./gradlew clean` auto-installs the git pre-commit hook: the root `build.gradle.kts` defines
  a `copyGitHooks` task (wired to `clean`) that copies `config/git/hooks/pre-commit.sh` →
  `.git/hooks/pre-commit`. The hook runs detekt + ktlint and blocks the commit on any issue.
- `sdk_payment` compiles with `-Xjsr305=strict`,
  `-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi`,
  `-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi`.

---

## Versioning & dependencies

All shared versions and coordinates live in `buildSrc/src/main/java/Dependencies.kt`
(`Versions`, `Libs`, `TestLibs`, `BuildVersionsAndroid`, `SDKBuildVersions`).
**Never hard-code versions in module `build.gradle.kts` files.** Sample apps are the only exception.

`SDKBuildVersions` shows `"local-dev-<module>"` on `main`; `release.yml` replaces them with the released version.
The Maven publication version comes from `-PsdkVersion=<version>` (`local-dev` without it).

---

## Publishing

- Maven Central, namespace `io.github.radarpayments`: `sdk-logs`, `sdk-core`, `sdk-payment` (version = SDK version)
  and `card-io` (`5.5.1-OS1`, published once). `gradle/publishing.gradle.kts` holds the POM, the sources and
  placeholder javadoc jars and the signing; it is applied by each published module.
- Local check: `./gradlew -PsdkVersion=X publishAllPublicationsToStagingRepository` writes the publication to
  `build/staging-deploy/` (signed when `SIGNING_KEY` / `SIGNING_PASSWORD` are set in the environment).
- `.github/workflows/release.yml` (manual): `dry-run` builds and signs the bundle, `validate` uploads it to the Central
  Portal and drops it after validation, `publish` releases to Maven Central (irreversible) and creates the tag and the
  GitHub release with the AARs. Stable versions only from `main`, `-rc.N` from any branch.
  Secrets: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` (Central Portal user token), `SIGNING_KEY`,
  `SIGNING_PASSWORD` (GPG key `6624D0D08D71522E191A9252018DA6ABF8CB0681`).
- `.github/workflows/ci.yml`: lint, unit tests, AARs, sample apps and an unsigned staging publication on every PR
  and push to `main`.
- `script_publish_github*.sh` belong to the old GitLab mirror flow and are not used.

---

## Gotchas

1. **3DS coverage is 3DS v1 only.** The native EMV 3DS2 SDK module (`sdk_threeds`) was removed.
   `sdk_payment` now handles only the legacy ACS-redirect flow via `Activity3DS2WebChallenge` (a
   `WebView` that auto-POSTs `MD/PaReq/TermUrl` to the issuer ACS and waits for `sdk://done`). If the
   backend returns `threeDSMethodURL` (signalling that the merchant requires a native 3DS2 SDK),
   `PaymentManagerImpl` throws `SDKNotConfigureException("Merchant is not configured to be used
   without 3DS2SDK")`. Mirrors the iOS SDK 3.0.7 removal.

2. **Sample apps will not build cold.** Always run `./copy_sdk_lib_to_samples.sh` first.

3. **Linters are pinned to old versions** (ktlint 0.45.2, detekt 1.10.0) that match the configs. Use
   the scripts, not a locally installed ktlint/detekt (e.g. Homebrew ktlint 1.x): newer versions have
   different rules and will report unrelated issues. To bump, change the version and checksum together
   in `script_lint_tools.sh` and fix the configs.

4. **Maven Central versions are permanent.** A published version (including `-rc.N`) can never be changed or
   deleted. Check the bundle with the `dry-run` and `validate` modes of `release.yml` first.

5. **Old toolchain is intentional.** Do not bump Gradle/AGP/Kotlin or remove `kotlin-android-extensions`
   without explicit instruction.

6. **`sdk_forms` no longer exists.** Its code was merged into `sdk_payment` (packages `sdk.form.*` →
   `sdk.*`). `SDKForms`, `SDKFormsConfigBuilder`, `PaymentConfigBuilder`, `GooglePayConfigBuilder`,
   `ResultCryptogramCallback`, `DeleteCardHandler` are `internal` — integrators use only `SDKPayment`.
   The Java sample `app_java_ui` was removed together with it.

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
| CI / release workflows | `.github/workflows/ci.yml`, `.github/workflows/release.yml` |
| Maven publication | `gradle/publishing.gradle.kts` |
| Bootstrap script | `copy_sdk_lib_to_samples.sh` |
| Pre-commit hook source | `config/git/hooks/pre-commit.sh` |
| Lint tool versions + checksums | `script_lint_tools.sh` |
| Gradle hook installer | `copyGitHooks` task in root `build.gradle.kts` |
