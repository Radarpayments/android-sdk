#!/bin/bash
set -e
cd "$(dirname "$0")"
source ./script_lint_tools.sh

DETEKT="$(detekt_jar)"
java -jar "$DETEKT" --config config/detekt/detekt.yml \
  --input sdk_core/src,sdk_payment/src \
  --excludes "**.kts"
