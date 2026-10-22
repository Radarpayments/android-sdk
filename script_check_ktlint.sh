#!/bin/bash
set -e
cd "$(dirname "$0")"
source ./script_lint_tools.sh

KTLINT="$(ktlint_jar)"
java -jar "$KTLINT" --editorconfig=config/ktlint/.editorconfig \
  "sdk_core/src/**/*.kt" \
  "sdk_payment/src/**/*.kt"
