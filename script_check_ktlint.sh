#!/bin/bash
docker run --rm -v "${PWD}"/sdk_forms/src:/app -v "${PWD}"/config/detekt:/config mdenissov/ktlint-checker:1.0.0 ktlint --editorconfig=/config/ktlint/.editorconfig "/app/**/*.kt" && \
docker run --rm -v "${PWD}"/sdk_core/src:/app -v "${PWD}"/config/detekt:/config mdenissov/ktlint-checker:1.0.0 ktlint --editorconfig=/config/ktlint/.editorconfig "/app/**/*.kt" && \
docker run --rm -v "${PWD}"/sdk_payment/src:/app -v "${PWD}"/config/detekt:/config mdenissov/ktlint-checker:1.0.0 ktlint --editorconfig=/config/ktlint/.editorconfig "/app/**/*.kt"