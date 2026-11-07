# ==============================================================================
# ContextApp - Mata Panda Studio Single-Pane Controller
# ==============================================================================
APP_NAME     := ContextApp
PACKAGE_NAME := com.mtpnda.stuck
PLATFORMS    := android ios
WEBHOST_DIR  ?= $(HOME)/Workspace/macbook-air-webhost

-include $(HOME)/.gemini/config/plugins/central-agents/skills/custom-release-manager/mtpnda.mk

# ------------------------------------------------------------------------------
# Project-Specific Overrides or Custom Targets Below:
# ------------------------------------------------------------------------------
test: test-ios test-android test-backend

test-ios:
	cd ios && xcodebuild test -project Stuck.xcodeproj -scheme Stuck -destination 'platform=iOS Simulator,name=iPhone 17' -derivedDataPath build_sim -quiet

test-android:
	cd android && ./gradlew test --quiet

test-backend:
	@echo "🧪 Running Deno Website & Backend Tests..."
	cd backend && deno task test

run-backend:
	@echo "🚀 Starting Stuck Deno Web Server (http://localhost:8000)..."
	deno task start

deploy-deno:
	@echo "🚀 Deploying Stuck Promotional Website to Deno Deploy..."
	@if [ -n "$$DENO_DEPLOY_TOKEN" ]; then \
		cd backend && deno run -Ar jsr:@deno/deployctl deploy --project=context src/main.ts; \
	else \
		echo "Notice: DENO_DEPLOY_TOKEN is not set in environment."; \
		echo "Stuck is linked to Deno Deploy via GitHub repository integration."; \
		echo "Commits pushed to 'origin main' automatically trigger Deno Deploy builds."; \
	fi

test-reviewer-ios:
	python3 tests/test_ios_reviewer.py

dls:
	python3 tools/gen_dls.py

branding:
	python3 tools/gen_branding.py
	swift tools/gen_store_screenshots.swift

screenshots:
	swift tools/gen_store_screenshots.swift

clean:
	rm -rf build/ ios/build/ android/app/build/ android/.gradle/
