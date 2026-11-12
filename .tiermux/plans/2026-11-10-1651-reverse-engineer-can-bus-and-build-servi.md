---
title: "Reverse‑Engineer CAN Bus and Build Service Skeleton"
created: 2026-11-10T16:54:34+05:30
status: executing
model: "kilo/dots-studio/dots-3-note-preview:free"
session: "smwaksi1qj9ai"
steps: 2
files:
  - "CONVERSATION_SUMMARY.md"
  - "CanService.kt"
---

# Reverse‑Engineer CAN Bus and Build Service Skeleton

> **Request** — lets first plan this first.

## Reading

User wants a concrete plan to reverse‑engineer CAN IDs and build a CAN bus service.

## Steps

- [ ] Identify the CAN frame IDs for engine state and AC controls by capturing live traffic from the device and correlate with known events.
  - Files: `CONVERSATION_SUMMARY.md`
  - Evidence: CONVERSATION_SUMMARY.md: lines 8-15 discuss need to reverse‑engineer CAN IDs for engine state and AC control
  - Verify: Use hardware to capture traffic and log frames via adb logcat
- [ ] Create CanService.kt in src/ folder as a foreground service that opens /dev/ttyS0, reads frames, parses them into CanFrame objects, and posts them via LiveData.
  - Files: `CanService.kt`
  - Evidence: CONVERSATION_SUMMARY.md: lines 30-32 describe building CanService as a foreground service reading serial port
  - Verify: Ensure service starts, binds, and logs input frames
