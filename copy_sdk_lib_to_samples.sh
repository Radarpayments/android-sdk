#!/bin/bash
set -e

if ! [ -d sdk_threeds ]; then
  echo "sdk-threeds source code directory not found. Start downloading the artifact..."
  tag=$(git describe --abbrev=0)
  curl -L https://github.com/Runet-Business-Systems/android-sdk/releases/download/${tag}/sdk_threeds-release.aar >sdk_threeds-release.aar
  echo "Artifact downloaded successfully. Start building..."
  mkdir -p app_kotlin_ui/libs/ && cp sdk_threeds-release.aar app_kotlin_ui/libs/
  mkdir -p sdk_payment/libs/ && cp sdk_threeds-release.aar sdk_payment/libs
  rm sdk_threeds-release.aar
else
  echo "sdk-threeds detected, starting building from source.."
  ./gradlew :sdk_threeds:build
  mkdir -p app_kotlin_ui/libs/ && cp sdk_threeds/build/outputs/aar/sdk_threeds-release.aar app_kotlin_ui/libs/
fi

./gradlew :sdk_core:build
./gradlew :sdk_forms:build
./gradlew :sdk_payment:build

mkdir -p app_kotlin_core/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_kotlin_core/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_kotlin_ui/libs/
mkdir -p app_java_ui/libs/ && cp sdk_core/build/outputs/aar/sdk_core-release.aar app_java_ui/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_forms/build/outputs/aar/sdk_forms-release.aar app_kotlin_ui/libs/
mkdir -p app_java_ui/libs/ && cp sdk_forms/build/outputs/aar/sdk_forms-release.aar app_java_ui/libs/
mkdir -p app_kotlin_ui/libs/ && cp sdk_payment/build/outputs/aar/sdk_payment-release.aar app_kotlin_ui/libs/
