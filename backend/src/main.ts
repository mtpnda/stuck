import { Hono } from "hono";
import { cors } from "hono/cors";
import { renderHomePage } from "./pages/home.ts";
import { renderPrivacyPage } from "./pages/privacy.ts";
import { renderTermsPage } from "./pages/terms.ts";
import { renderSupportPage } from "./pages/support.ts";
import { renderWhitepaperPage } from "./pages/whitepaper.ts";
import { configRouter } from "./routes/config.ts";
import { supportRouter } from "./routes/support.ts";

export const app = new Hono();

// Global CORS Middleware
app.use("*", cors());

// Project Root Directory Resolution
const PROJECT_ROOT = new URL("../../", import.meta.url).pathname;

// Static Asset Serving
app.get("/assets/icon.svg", async (c) => {
  try {
    const data = await Deno.readFile(`${PROJECT_ROOT}assets/icon.svg`);
    return new Response(data, {
      headers: { "Content-Type": "image/svg+xml", "Cache-Control": "public, max-age=86400" },
    });
  } catch {
    return c.text("Icon not found", 404);
  }
});

app.get("/assets/app-icon.png", async (c) => {
  try {
    const data = await Deno.readFile(`${PROJECT_ROOT}assets/app-icon.png`);
    return new Response(data, {
      headers: { "Content-Type": "image/png", "Cache-Control": "public, max-age=86400" },
    });
  } catch {
    return c.text("App icon not found", 404);
  }
});

app.get("/assets/feature-graphic.png", async (c) => {
  try {
    const data = await Deno.readFile(`${PROJECT_ROOT}assets/feature-graphic.png`);
    return new Response(data, {
      headers: { "Content-Type": "image/png", "Cache-Control": "public, max-age=86400" },
    });
  } catch {
    return c.text("Feature graphic not found", 404);
  }
});

app.get("/assets/screenshots/:filename", async (c) => {
  const filename = c.req.param("filename");
  // Sanitize filename to prevent directory traversal
  const cleanFilename = filename.replace(/[^a-zA-Z0-9_.-]/g, "");
  const screenshotPath = `${PROJECT_ROOT}android/app/src/main/play/listings/en-US/graphics/phone-screenshots/${cleanFilename}`;
  try {
    const data = await Deno.readFile(screenshotPath);
    return new Response(data, {
      headers: { "Content-Type": "image/png", "Cache-Control": "public, max-age=86400" },
    });
  } catch {
    return c.text("Screenshot not found", 404);
  }
});

// Promotional Web Pages
app.get("/", (c) => c.html(renderHomePage()));
app.get("/en", (c) => c.html(renderHomePage()));
app.get("/en/", (c) => c.html(renderHomePage()));

// Section Navigation Shortcuts
app.get("/features", (c) => c.redirect("/#features"));
app.get("/stages", (c) => c.redirect("/#stages"));
app.get("/simulator", (c) => c.redirect("/#simulator"));
app.get("/how-it-works", (c) => c.redirect("/#stages"));
app.get("/download", (c) => c.redirect("/#download"));

// Whitepaper, Legal & Support Pages
app.get("/whitepaper", (c) => c.html(renderWhitepaperPage()));
app.get("/architecture", (c) => c.redirect("/whitepaper"));
app.get("/privacy", (c) => c.html(renderPrivacyPage()));
app.get("/privacy-policy", (c) => c.redirect("/privacy"));
app.get("/terms", (c) => c.html(renderTermsPage()));
app.get("/terms-of-service", (c) => c.redirect("/terms"));
app.get("/support", (c) => c.html(renderSupportPage()));
app.get("/help", (c) => c.redirect("/support"));
app.get("/contact", (c) => c.redirect("/support"));

// Health Check
app.get("/health", (c) => c.text("Stuck Edge Web Server is healthy"));

// Mount API routes
app.route("/api/config", configRouter);
app.route("/api/support", supportRouter);

// Start standalone if executed directly
if (import.meta.main) {
  const port = Number(Deno.env.get("PORT") || 8000);
  console.log(`🌿 Stuck Promotional Website listening on http://localhost:${port}`);
  Deno.serve({ port }, app.fetch);
}
