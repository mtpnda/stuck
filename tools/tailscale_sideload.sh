#!/usr/bin/env bash
# ==============================================================================
# Context App · Tailscale Wireless Sideloading Bridge & Deployment Tool
#
# Supports:
# 1. iOS: Apple CoreDevice (remotepairingd / devicectl) tunnel over Tailnet
# 2. Android: Wireless ADB tunnel over Tailnet
# 3. Web Hub: Displays the /context Tailscale deployment portal link
# ==============================================================================
set -euo pipefail

TARGET_IP="${1:-100.65.90.3}"
PLATFORM="${2:-ios}"
COREDEVICE_PORT=49152
DEVICE_NAME="X’s iPhone"
DEVICE_IDENTIFIER="DCA09247-FFD8-54B9-A693-7821EE3C5CF9"

echo "─────────────────────────────────────────────────────────────"
echo "  Context App Wireless Sideloading Bridge (Tailscale)"
echo "  Target IP : ${TARGET_IP} | Platform: ${PLATFORM}"
echo "  Web Hub   : http://100.73.84.111:8888/apps/context"
echo "─────────────────────────────────────────────────────────────"

# 1. Verify Tailscale
TAILSCALE_BIN="/Applications/Tailscale.app/Contents/MacOS/Tailscale"
if [[ ! -x "${TAILSCALE_BIN}" ]]; then
  TAILSCALE_BIN="$(which tailscale || true)"
fi

if [[ -z "${TAILSCALE_BIN}" ]]; then
  echo "❌ Tailscale binary not found. Please ensure Tailscale.app is installed and active."
  exit 1
fi

echo "🔍 Pinging device over Tailnet (${TARGET_IP})..."
if ! "${TAILSCALE_BIN}" ping -c 1 -timeout 3s "${TARGET_IP}" &>/dev/null; then
  echo "⚠️  Device (${TARGET_IP}) is currently unreachable on Tailnet."
  echo "   👉 Please open the Tailscale app on your device and ensure it is connected."
fi

if [[ "${PLATFORM}" == "android" ]]; then
  echo "🤖 Connecting wireless Android ADB over Tailscale..."
  adb connect "${TARGET_IP}:5555"
  echo "✅ Connected! To install build:"
  echo "   adb -s ${TARGET_IP}:5555 install path/to/app.apk"
  exit 0
fi

# iOS CoreDevice Bridge
echo "🔌 Starting socat CoreDevice tunnel on port ${COREDEVICE_PORT} -> ${TARGET_IP}:${COREDEVICE_PORT}..."
pkill -f "socat.*${COREDEVICE_PORT}" || true
/opt/homebrew/bin/socat TCP-LISTEN:${COREDEVICE_PORT},fork,reuseaddr TCP:${TARGET_IP}:${COREDEVICE_PORT} &
SOCAT_PID=$!

echo "📡 Registering Bonjour proxy (_remotepairing._tcp) locally..."
pkill -f "dns-sd -P ${DEVICE_NAME}" || true
dns-sd -P "${DEVICE_NAME}" _remotepairing._tcp local ${COREDEVICE_PORT} Xs-iPhone.coredevice.local 127.0.0.1 &
DNS_SD_PID=$!

cleanup() {
  echo ""
  echo "🛑 Tearing down bridge..."
  kill "${SOCAT_PID}" 2>/dev/null || true
  kill "${DNS_SD_PID}" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo ""
echo "✅ CoreDevice Bridge ACTIVE!"
echo "   - socat PID:  ${SOCAT_PID}"
echo "   - dns-sd PID: ${DNS_SD_PID}"
echo ""
echo "📱 To install app wirelessly via devicectl:"
echo "   xcrun devicectl device install app --device ${DEVICE_IDENTIFIER} <path_to_app.app>"
echo ""
echo "Press Ctrl+C to terminate the bridge."
wait
