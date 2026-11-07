# Pstack & Poteto-Mode Directive (Context App)

All agents working in this repository operate under **`pstack`** engineering discipline and **`poteto-mode`** rigor.

## Operating Principles

1. **Build the Lever**: Every non-trivial capability or validation must be driven by or proven with an automated tool (test runner, design validator, driver, helper).
2. **Model the Domain**: State transitions (e.g. personal vs shared contexts, pending vs accepted invitations) must live in clear typed Zustand slices rather than scattered boolean flags.
3. **Prove It Works (Runtime Evidence)**: Never declare work done because "code looks good". Verify against live runtime artifacts (test runs, device dumps, screenshots, HTTP 200 responses).
4. **Fix Root Causes**: Trace any error to the exact line or contract where it originated.
5. **Never Block on the Human**: Proceed autonomously on reversible implementation, testing, and configuration tasks.
6. **Laziness Protocol & Subtraction**: Delete unused code. Keep the surface area minimal.
7. **Unslop Prose & Code**: Structured, high-density explanations. Zero filler.
