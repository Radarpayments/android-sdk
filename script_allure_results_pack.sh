#!/bin/bash
mkdir -p allure-results
mv sdk_core/build/allure-results/* allure-results
mv sdk_forms/build/allure-results/* allure-results
mv sdk_payment/build/allure-results/* allure-results
adb pull /storage/emulated/0/allure-results/ .
adb shell rm -r /storage/emulated/0/allure-results/
adb shell rm -r /storage/emulated/0/video/
adb shell rm -r /storage/emulated/0/logcat/
adb shell rm -r /storage/emulated/0/screenshots/
adb shell rm -r /storage/emulated/0/view_hierarchy/