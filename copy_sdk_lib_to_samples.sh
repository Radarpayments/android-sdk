#!/bin/bash
set -e

# Extra arguments are passed to Gradle, e.g. -Pandroid.aapt2FromMavenOverride=...
./gradlew :sdk_logs:assembleRelease :sdk_core:assembleRelease :sdk_payment:assembleRelease "$@"

mkdir -p app_kotlin_ui/libs && cp repo/io/github/radarpayments/card-io/5.5.1-OS1/card-io-5.5.1-OS1.aar app_kotlin_ui/libs/card.io-5.5.1-OS1.aar

mkdir -p app_kotlin_core/libs/ && cp sdk_logs/build/outputs/aar/sdk_logs-release.aar app_kotlin_core/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_logs/build/outputs/aar/sdk_logs-release.aar app_kotlin_ui/libs/

mkdir -p app_kotlin_core/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_kotlin_core/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_kotlin_ui/libs/

mkdir -p app_kotlin_ui/libs/ && cp sdk_payment/build/outputs/aar/sdk_payment-release.aar app_kotlin_ui/libs/
