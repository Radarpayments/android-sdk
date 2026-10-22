#!/bin/bash
# Uploads a bundle to the Maven Central Portal and waits for the result.
#   validate: USER_MANAGED upload, waits for VALIDATED, then drops the deployment (nothing is published)
#   publish:  AUTOMATIC upload, waits for PUBLISHED
# Needs MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD (Central Portal user token).
set -euo pipefail

bundle="$1"
mode="$2"
name="$3"
api="https://central.sonatype.com/api/v1/publisher"
auth="Authorization: Bearer $(printf '%s:%s' "$MAVEN_CENTRAL_USERNAME" "$MAVEN_CENTRAL_PASSWORD" | base64 | tr -d '\n')"

case "$mode" in
  validate) type=USER_MANAGED; target=VALIDATED ;;
  publish) type=AUTOMATIC; target=PUBLISHED ;;
  *) echo "::error::Unknown mode $mode"; exit 1 ;;
esac

id=$(curl -sS --fail-with-body -X POST -H "$auth" \
  -F "bundle=@$bundle;type=application/octet-stream" \
  "$api/upload?publishingType=$type&name=$(printf '%s' "$name" | jq -sRr @uri)")
echo "Deployment $id ($type)"

for _ in $(seq 1 120); do
  status=$(curl -sS --fail-with-body -X POST -H "$auth" "$api/status?id=$id")
  state=$(jq -r .deploymentState <<<"$status")
  echo "$(date +%T) $state"
  case "$state" in
    "$target") break ;;
    FAILED)
      echo "::error::Central rejected the deployment"
      jq . <<<"$status"
      exit 1 ;;
  esac
  sleep 15
done

if [[ "$state" != "$target" ]]; then
  echo "::error::Deployment $id is still $state after 30 minutes"
  exit 1
fi

if [[ "$mode" == "validate" ]]; then
  curl -sS --fail-with-body -X DELETE -H "$auth" "$api/deployment/$id"
  echo "Validation passed, deployment $id dropped"
fi
