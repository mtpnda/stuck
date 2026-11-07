# Reminders & Interactive Notifications Specification

## 1. Notification Structure & Categories
Stuck utilizes native system notifications (`UserNotifications` on iOS and `NotificationCompat` on Android) with deterministic notification IDs (`${id}:start`, `${id}:end`) to prevent duplicates.

### 1.1 Category: `ctx-start`
Fires when a scheduled ("Coming up") context reaches its start time.
- **Actions**:
  1. `focus`: "Work on it now" (launches app to foreground, initiates focus timer session).
  2. `snooze`: "Remind me in 1 hour" (snoozes start by 60 minutes).

### 1.2 Category: `ctx-end`
Fires when a context reaches its target completion deadline.
- **Actions**:
  1. `done`: "It's done" (requires device unlock; marks context as `done`).
  2. `extend`: "Give it 1 more day" (reschedules timeline by +1 day).
  3. `open`: "Not yet" (launches app and presents the context details popup).

## 2. Notification Synchronization
- Scheduling triggers automatically on context creation or update.
- Fixed identifier mapping:
  - `${id}:start` for context start time.
  - `${id}:end` for context deadline.
- Marking a thought `done` or deleting it immediately cancels any pending notifications from the system notification center.
