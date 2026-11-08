#!/usr/bin/env python3
"""
test_release_automation.py
Release automation and structural integrity tests for Stuck (com.mtpnda.stuck).
Verifies:
  1. Zero divergent release shell scripts in repo
  2. Fastlane orchestration & lane integrity (Fastfile & Gemfile)
  3. Google Play Store listing as code (title <= 30, short description <= 80, graphics dimensions)
  4. BioCue DLS single source of truth tokens integrity
  5. Single-pane Makefile command completeness
  6. Multiplatform dual-native targets (iOS & Android)
"""

import os
import unittest
import struct

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FASTFILE_PATH = os.path.join(PROJECT_ROOT, "fastlane", "Fastfile")
MAKEFILE_PATH = os.path.join(PROJECT_ROOT, "Makefile")
PROJECT_YML = os.path.join(PROJECT_ROOT, "ios", "project.yml")
PLAY_DIR = os.path.join(PROJECT_ROOT, "android", "app", "src", "main", "play", "listings", "en-US")

def get_effective_fastfile():
    with open(FASTFILE_PATH, "r") as f:
        content = f.read()
    central_path = os.path.expanduser("~/.gemini/config/plugins/central-agents/skills/reusable-fastlane/Fastfile")
    if os.path.exists(central_path):
        with open(central_path, "r") as cf:
            content += "\n" + cf.read()
    return content

def get_effective_makefile():
    with open(MAKEFILE_PATH, "r") as f:
        content = f.read()
    central_mk = os.path.expanduser("~/.gemini/config/plugins/central-agents/skills/custom-release-manager/mtpnda.mk")
    if os.path.exists(central_mk):
        with open(central_mk, "r") as cm:
            content += "\n" + cm.read()
    return content

def get_png_dimensions(file_path):
    with open(file_path, "rb") as f:
        data = f.read(24)
        if data[:8] == b"\x89PNG\r\n\x1a\n":
            w, h = struct.unpack(">II", data[16:24])
            return w, h
    return None, None

class TestStuckReleaseAutomation(unittest.TestCase):

    def test_no_divergent_scripts(self):
        """Verifies no ad-hoc release scripts bypass Fastlane and Makefile."""
        for name in ["build_release.sh", "deploy.sh", "release.sh"]:
            self.assertFalse(
                os.path.exists(os.path.join(PROJECT_ROOT, name)),
                f"Ad-hoc script {name} must not exist. Use Makefile/Fastlane single pane.",
            )

    def test_fastlane_configuration_integrity(self):
        """Verifies Fastfile configures Stuck bundle ID and release lanes."""
        self.assertTrue(os.path.isfile(FASTFILE_PATH), "fastlane/Fastfile must exist")
        content = get_effective_fastfile()
        self.assertIn("com.mtpnda.stuck", content)
        self.assertIn("def stuck_apps_dir", content)
        self.assertIn("lane :internal", content)
        self.assertIn("lane :internal_release", content)
        self.assertIn("lane :release_playstore", content)

    def test_ios_project_spec(self):
        """Verifies iOS XcodeGen spec targets com.mtpnda.stuck."""
        self.assertTrue(os.path.isfile(PROJECT_YML), "ios/project.yml must exist")
        with open(PROJECT_YML, "r") as f:
            content = f.read()
        self.assertIn("com.mtpnda.stuck", content)
        self.assertIn("CFBundleDisplayName: Stuck", content)
        self.assertIn("Contexts/Assets.xcassets", content, "ios/project.yml must include Assets.xcassets in target sources")
        self.assertIn("ASSETCATALOG_COMPILER_APPICON_NAME: AppIcon", content)

    def test_ios_app_icon_and_asset_catalog(self):
        """Verifies iOS app icon asset catalog, XcodeGen source mapping, and no-alpha compliance."""
        # 1. project.pbxproj must contain Assets.xcassets in Resources
        pbxproj_path = os.path.join(PROJECT_ROOT, "ios", "Stuck.xcodeproj", "project.pbxproj")
        self.assertTrue(os.path.isfile(pbxproj_path), "ios/Stuck.xcodeproj/project.pbxproj must exist")
        with open(pbxproj_path, "r") as f:
            pbx_content = f.read()
        self.assertIn("Assets.xcassets in Resources", pbx_content, "Assets.xcassets must be included in Resources build phase")

        # 2. Master 1024 icon must exist, be exactly 1024x1024, and have ColorType 2 (RGB, no alpha)
        master_icon = os.path.join(PROJECT_ROOT, "ios", "Contexts", "Assets.xcassets", "AppIcon.appiconset", "AppIcon-1024.png")
        self.assertTrue(os.path.isfile(master_icon), "AppIcon-1024.png must exist in AppIcon.appiconset")
        with open(master_icon, "rb") as f:
            header = f.read(26)
        w, h = struct.unpack(">II", header[16:24])
        color_type = header[25]
        self.assertEqual((w, h), (1024, 1024), f"AppIcon-1024.png must be 1024x1024, got {w}x{h}")
        self.assertEqual(color_type, 2, f"AppIcon-1024.png must be 24-bit RGB without alpha (ColorType 2), got {color_type}")

        # 3. Contents.json must be valid and reference AppIcon-1024.png
        contents_json = os.path.join(PROJECT_ROOT, "ios", "Contexts", "Assets.xcassets", "AppIcon.appiconset", "Contents.json")
        self.assertTrue(os.path.isfile(contents_json), "Contents.json must exist in AppIcon.appiconset")
        with open(contents_json, "r") as f:
            import json
            meta = json.load(f)
        self.assertIn("images", meta)
        filenames = [img.get("filename") for img in meta["images"]]
        self.assertIn("AppIcon-1024.png", filenames)


    def test_play_store_metadata(self):
        """Verifies Google Play metadata meets strict character count, descriptions, release notes, and graphic dimensions."""
        title_file = os.path.join(PLAY_DIR, "title.txt")
        short_desc_file = os.path.join(PLAY_DIR, "short-description.txt")
        full_desc_file = os.path.join(PLAY_DIR, "full-description.txt")
        notes_file = os.path.join(os.path.dirname(PLAY_DIR), "..", "release-notes", "en-US", "default.txt")
        icon_file = os.path.join(PLAY_DIR, "graphics", "icon", "icon.png")
        feature_file = os.path.join(PLAY_DIR, "graphics", "feature-graphic", "feature-graphic.png")
        phone_screenshots_dir = os.path.join(PLAY_DIR, "graphics", "phone-screenshots")

        self.assertTrue(os.path.isfile(title_file), "title.txt must exist")
        with open(title_file, "r") as f:
            title = f.read().strip()
        self.assertLessEqual(len(title), 30, f"Title '{title}' exceeds 30 chars limit")
        self.assertIn("Stuck", title)

        self.assertTrue(os.path.isfile(short_desc_file), "short-description.txt must exist")
        with open(short_desc_file, "r") as f:
            short_desc = f.read().strip()
        self.assertLessEqual(len(short_desc), 80, f"Short description exceeds 80 chars limit ({len(short_desc)})")

        self.assertTrue(os.path.isfile(full_desc_file), "full-description.txt must exist")
        with open(full_desc_file, "r") as f:
            full_desc = f.read().strip()
        self.assertLessEqual(len(full_desc), 4000, f"Full description exceeds 4000 chars limit ({len(full_desc)})")

        self.assertTrue(os.path.isfile(notes_file), "release-notes/en-US/default.txt must exist")
        with open(notes_file, "r") as f:
            notes = f.read().strip()
        self.assertLessEqual(len(notes), 500, f"Release notes exceed 500 chars limit ({len(notes)})")

        self.assertTrue(os.path.isfile(icon_file), "icon.png must exist")
        w, h = get_png_dimensions(icon_file)
        self.assertEqual((w, h), (512, 512), f"Icon must be 512x512, got {w}x{h}")

        self.assertTrue(os.path.isfile(feature_file), "feature-graphic.png must exist")
        fw, fh = get_png_dimensions(feature_file)
        self.assertEqual((fw, fh), (1024, 500), f"Feature graphic must be 1024x500, got {fw}x{fh}")

        # Play Store phone screenshots (between 2 and 8 required)
        self.assertTrue(os.path.isdir(phone_screenshots_dir), "graphics/phone-screenshots directory must exist")
        screenshots = [f for f in os.listdir(phone_screenshots_dir) if f.endswith(".png")]
        self.assertGreaterEqual(len(screenshots), 2, "Must provide at least 2 phone screenshots")
        self.assertLessEqual(len(screenshots), 8, "Must provide at most 8 phone screenshots")

        for s_file in screenshots:
            s_path = os.path.join(phone_screenshots_dir, s_file)
            sw, sh = get_png_dimensions(s_path)
            self.assertIsNotNone(sw, f"Could not read PNG dimensions for {s_file}")
            self.assertGreaterEqual(min(sw, sh), 320, f"Screenshot {s_file} minimum dimension must be >= 320px")
            self.assertLessEqual(max(sw, sh), 3840, f"Screenshot {s_file} maximum dimension must be <= 3840px")
            ratio = max(sw, sh) / min(sw, sh)
            self.assertLessEqual(ratio, 2.0, f"Screenshot {s_file} aspect ratio {ratio:.2f} exceeds Google Play 2:1 limit")

    def test_makefile_command_completeness(self):
        """Verifies Makefile defines required single-pane targets."""
        makefile = get_effective_makefile()
        expected_targets = [
            "help:", "doctor:", "setup-secrets:", "release-local:", "release-ios-local:",
            "release-android-local:", "release-playstore:", "build-all:", "build-ios:",
            "build-android:", "test:", "test-ios:", "test-android:", "dls:", "branding:", "clean:"
        ]
        for target in expected_targets:
            self.assertIn(target, makefile, f"Makefile missing target {target}")

    def test_dls_token_spec(self):
        """Verifies dls.tokens.json contains required BioCue tokens."""
        tokens_json = os.path.join(PROJECT_ROOT, "design", "dls.tokens.json")
        self.assertTrue(os.path.isfile(tokens_json), "design/dls.tokens.json must exist")
        with open(tokens_json, "r") as f:
            content = f.read()
        self.assertIn("action.primary", content)
        self.assertIn("surface.bg", content)
        self.assertIn("state.alert.ink", content)
        self.assertIn("state.calm.ink", content)

    def test_reviewer_automation_script(self):
        """Verifies Play Store reviewer sequential test script exists and is configured for Stuck."""
        script_path = os.path.join(PROJECT_ROOT, "tests", "test_playstore_reviewer.py")
        self.assertTrue(os.path.isfile(script_path), "tests/test_playstore_reviewer.py must exist")
        self.assertTrue(os.access(script_path, os.X_OK), "tests/test_playstore_reviewer.py must be executable")
        with open(script_path, "r") as f:
            content = f.read()
        self.assertIn("com.mtpnda.stuck", content)
        self.assertIn("MainActivity", content)
        self.assertIn("run_sequence", content)


if __name__ == "__main__":
    unittest.main()
