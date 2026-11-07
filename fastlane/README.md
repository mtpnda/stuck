fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

### internal_release

```sh
[bundle exec] fastlane internal_release
```

Build and release iOS and Android packages to the centralized webhost portal

----


## iOS

### ios internal

```sh
[bundle exec] fastlane ios internal
```

Build iOS IPA and publish to Tailnet webhost portal

### ios testflight_upload

```sh
[bundle exec] fastlane ios testflight_upload
```

Build and upload iOS app to TestFlight (pilot)

### ios release_appstore

```sh
[bundle exec] fastlane ios release_appstore
```

Build and submit iOS app to App Store (deliver)

----


## Android

### android internal

```sh
[bundle exec] fastlane android internal
```

Build Android release APK and publish to Tailnet webhost portal

### android release_playstore

```sh
[bundle exec] fastlane android release_playstore
```

Build Android release bundle and publish to Google Play Store

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
