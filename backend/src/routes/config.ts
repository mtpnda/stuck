import { Hono } from "hono";

export const configRouter = new Hono();

configRouter.get("/", (c) => {
  return c.json({
    app: "Stuck",
    packageId: "com.mtpnda.stuck",
    version: "1.0.0",
    build: 1,
    tagline: "Unstick your thoughts. Serene cognitive load balancer & 4-stage task flow.",
    platforms: {
      ios: {
        supported: true,
        tech: "Swift 6, SwiftUI, LocalAuthentication (Face ID & Touch ID), UserDefaults persistence",
        minVersion: "iOS 17.0+"
      },
      android: {
        supported: true,
        tech: "Kotlin 2.0, Jetpack Compose, Material 3, BiometricPrompt, SharedPreferences persistence",
        minVersion: "Android 8.0+ (API 26)"
      }
    },
    privacy: {
      zeroCloudStorage: true,
      remoteTelemetry: false,
      thirdPartyAnalytics: false,
      dataSelling: false,
      biometricLock: true
    },
    cognitiveModel: {
      defaultCapacity: 3,
      minCapacity: 1,
      maxCapacity: 9,
      stages: ["coming-up", "in-head", "parked", "closed"]
    },
    links: {
      website: "https://context.matapandastudio.deno.net",
      privacyPolicy: "https://context.matapandastudio.deno.net/privacy",
      terms: "https://context.matapandastudio.deno.net/terms",
      support: "https://context.matapandastudio.deno.net/support",
      supportEmail: "support@matapandastudio.com"
    }
  });
});
