#!/bin/bash
set -e

./gradlew :sdk_logs:build
./gradlew :sdk_core:build
./gradlew :sdk_forms:build
./gradlew :sdk_payment:build

mkdir -p app_kotlin_ui/libs && cp sdk_forms/libs/card.io-5.5.1-OS1.aar app_kotlin_ui/libs/

mkdir -p app_kotlin_core/libs/ && cp sdk_logs/build/outputs/aar/sdk_logs-release.aar app_kotlin_core/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_logs/build/outputs/aar/sdk_logs-release.aar app_kotlin_ui/libs/
mkdir -p app_java_ui/libs/ && cp sdk_logs/build/outputs/aar/sdk_logs-release.aar app_java_ui/libs/

mkdir -p app_kotlin_core/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_kotlin_core/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_kotlin_ui/libs/
mkdir -p app_java_ui/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_java_ui/libs/

mkdir -p app_kotlin_ui/libs/ && cp sdk_forms/build/outputs/aar/sdk_forms-release.aar app_kotlin_ui/libs/
mkdir -p app_java_ui/libs/ && cp sdk_forms/build/outputs/aar/sdk_forms-release.aar app_java_ui/libs/

mkdir -p app_kotlin_ui/libs/ && cp sdk_payment/build/outputs/aar/sdk_payment-release.aar app_kotlin_ui/libs/
