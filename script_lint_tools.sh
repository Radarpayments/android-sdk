#!/bin/bash
# Downloads the pinned ktlint and detekt CLI jars into build/lint-tools (once) and verifies their checksums.
# Sourced by script_check_ktlint.sh and script_check_detekt.sh; needs only curl and Java 11+.
# To bump a tool, change its version and checksum together.

KTLINT_VERSION="0.45.2"
KTLINT_SHA256="89491ea865d369b39cfaca2dcf60b38adbdcd74985f5e0170c0bb73034000135"
DETEKT_VERSION="1.10.0"
DETEKT_SHA256="46b4fce4dc2aa8bea86ce40e6a13afd39c312cc217f871eaecfe596dfe1bdfcd"

LINT_TOOLS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/build/lint-tools"

sha256_of() {
  if command -v sha256sum >/dev/null; then sha256sum "$1"; else shasum -a 256 "$1"; fi | cut -d' ' -f1
}

# Prints the path of the tool jar, downloading it if needed.
lint_tool() {
  local name="$1" version="$2" url="$3" sha256="$4"
  local jar="$LINT_TOOLS_DIR/$name-$version.jar"
  if [ ! -f "$jar" ] || [ "$(sha256_of "$jar")" != "$sha256" ]; then
    mkdir -p "$LINT_TOOLS_DIR"
    echo "Downloading $name $version..." >&2
    curl -fsSL -o "$jar.tmp" "$url" || return 1
    if [ "$(sha256_of "$jar.tmp")" != "$sha256" ]; then
      echo "Checksum mismatch for $name $version ($url)" >&2
      rm -f "$jar.tmp"
      return 1
    fi
    mv "$jar.tmp" "$jar"
  fi
  echo "$jar"
}

ktlint_jar() {
  lint_tool ktlint "$KTLINT_VERSION" \
    "https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint" "$KTLINT_SHA256"
}

detekt_jar() {
  lint_tool detekt "$DETEKT_VERSION" \
    "https://github.com/detekt/detekt/releases/download/v$DETEKT_VERSION/detekt" "$DETEKT_SHA256"
}
