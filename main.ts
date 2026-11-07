import { app } from "./backend/src/main.ts";

if (import.meta.main) {
  const port = Number(Deno.env.get("PORT") || 8000);
  console.log(`🌿 Stuck Cognitive Load Web Server running on http://localhost:${port}`);
  Deno.serve({ port }, app.fetch);
}

export { app };
