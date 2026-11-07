#!/usr/bin/env bash
# ==============================================================================
# Contexts · iOS IPA Packager & Sideload Publisher
# Packages iOS .app bundle into a distributable .ipa and publishes to Tailnet.
# ==============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RN_DIR="${ROOT}/rn"
DEST_DIR="/Users/valdiearsanur/Workspace/gym/apps/context/ipa"

mkdir -p "${DEST_DIR}"

APP_BUNDLE="${1:-}"

if [[ -z "${APP_BUNDLE}" ]]; then
  POSSIBLE_PATHS=(
    "${RN_DIR}/ios/build/Build/Products/Debug-iphoneos/Contexts.app"
    "${RN_DIR}/ios/build/Build/Products/Release-iphoneos/Contexts.app"
    "${HOME}/Library/Developer/Xcode/DerivedData/Contexts-*/Build/Products/*-iphoneos/Contexts.app"
  )
  for p in "${POSSIBLE_PATHS[@]}"; do
    FOUND=$(ls -dt $p 2>/dev/null | head -n 1 || true)
    if [[ -n "${FOUND}" && -d "${FOUND}" ]]; then
      APP_BUNDLE="${FOUND}"
      break
    fi
  done
fi

if [[ -z "${APP_BUNDLE}" || ! -d "${APP_BUNDLE}" ]]; then
  echo "⚠️ Note: To package an IPA, pass the path to Contexts.app:"
  echo "   ./tools/build_ipa.sh <path_to_Contexts.app>"
  exit 1
fi

echo "🍏 Packaging IPA from: ${APP_BUNDLE}"
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "${TEMP_DIR}"' EXIT

mkdir -p "${TEMP_DIR}/Payload"
cp -R "${APP_BUNDLE}" "${TEMP_DIR}/Payload/"

TARGET_IPA="${DEST_DIR}/Contexts-latest.ipa"
(cd "${TEMP_DIR}" && zip -qr "${TARGET_IPA}" Payload)
cp -f "${TARGET_IPA}" "${DEST_DIR}/Contexts-1.0.0.ipa"

echo "✅ IPA published successfully!"
echo "   Destination: ${TARGET_IPA}"
echo "   Portal URL : https://macbook-air-valdie.baboon-gray.ts.net/apps/context/ipa/Contexts-latest.ipa"
