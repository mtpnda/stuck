import { Hono } from "hono";

export const supportRouter = new Hono();

// Memory store for support tickets (no third-party cloud trackers)
const tickets: Array<{
  id: string;
  name: string;
  email: string;
  category: string;
  device?: string;
  message: string;
  createdAt: string;
}> = [];

supportRouter.post("/", async (c) => {
  try {
    const body = await c.req.json();
    const { name, email, category, device, message } = body;

    if (!email || !message) {
      return c.json(
        { error: "Email and message are required fields." },
        { status: 400 }
      );
    }

    const ticketId = `stuck-ticket-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
    const newTicket = {
      id: ticketId,
      name: name || "Anonymous User",
      email: email.trim(),
      category: category || "general",
      device: device || "unspecified",
      message: message.trim(),
      createdAt: new Date().toISOString()
    };

    tickets.push(newTicket);

    return c.json({
      success: true,
      ticketId,
      message: "Support inquiry received successfully. A sovereign reply will be sent within 24 hours.",
      privacyGuarantee: "Your inquiry details are held privately and never shared with advertising or telemetry brokers."
    });
  } catch (_e) {
    return c.json(
      { error: "Invalid JSON payload." },
      { status: 400 }
    );
  }
});
