#!/bin/bash
# Prints a per-module summary of JUnit XML results and the names of failed tests.
for module in "$@"; do
  python3 - "$module" <<'PY'
import glob, sys, xml.etree.ElementTree as ET
module = sys.argv[1]
tests = failures = skipped = 0
failed = []
for path in glob.glob(f"{module}/build/test-results/testDebugUnitTest/*.xml"):
    root = ET.parse(path).getroot()
    tests += int(root.get("tests"))
    failures += int(root.get("failures")) + int(root.get("errors"))
    skipped += int(root.get("skipped"))
    failed += [f"{c.get('classname')}.{c.get('name')}" for c in root.iter("testcase")
               if c.find("failure") is not None or c.find("error") is not None]
print(f"{module}: {tests} tests, {failures} failed, {skipped} skipped")
for name in failed:
    print(f"::error::{module}: {name} failed")
PY
done
