#!/bin/bash
docker run --rm -v "${PWD}"/sdk_forms/src:/app -v "${PWD}"/config/detekt:/config mdenissov/detekt-checker detekt --config /config/detekt.yml  --input /app/ --excludes "**.kts" && \
docker run --rm -v "${PWD}"/sdk_core/src:/app -v "${PWD}"/config/detekt:/config mdenissov/detekt-checker detekt --config /config/detekt.yml  --input /app/ --excludes "**.kts"  && \
docker run --rm -v "${PWD}"/sdk_payment/src:/app -v "${PWD}"/config/detekt:/config mdenissov/detekt-checker detekt --config /config/detekt.yml  --input /app/ --excludes "**.kts"