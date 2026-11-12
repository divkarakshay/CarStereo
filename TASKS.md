# Task Tracker

Use this file to track work across multiple AI sessions. Update it after each session.

## Status Legend
- ⬜ Pending
- 🔄 In Progress
- ✅ Completed
- ❌ Blocked / Needs Input

---

## Phase 1: CAN Bus Reverse Engineering

| Task | Status | Notes |
|------|--------|-------|
| Capture CAN traffic from Nippon device | ✅ Completed | Need USB-to-CAN adapter or use built-in serial |
| Identify engine state CAN ID | ⬜ | Correlate with known start/stop events |
| Identify AC control CAN IDs | ⬜ | Temperature, mode, fan speed |
| Document CAN protocol mapping | ⬜ | Create a reference table |

## Phase 2: Core Service

| Task | Status | Notes |
|------|--------|-------|
| Implement CanService skeleton | ✅ Completed | Foreground service, serial port I/O |
| Implement CAN frame parser/builder | ⬜ | ID + data byte handling |
| Set up LiveData/Broadcast for UI updates | ⬜ | |
| Add error handling & reconnection | ⬜ | |

## Phase 3: Launcher UI

| Task | Status | Notes |
|------|--------|-------|
| Create LauncherActivity | ✅ Completed | Home screen, replace stock launcher |
| Build tab layout (Home, Apps, Maps, Settings) | ⬜ | ViewPager2 |
| Implement app list fragment | ⬜ | Query installed apps |
| Create AC control fragment | ⬜ | Temperature, mode, fan speed |

## Phase 4: Widgets & Integration

| Task | Status | Notes |
|------|--------|-------|
| Map widget | ⬜ | Google Maps SDK, location updates |
| Media control widget | ⬜ | MediaSessionCompat integration |
| Widget drag-and-drop | ⬜ | ItemTouchHelper, persist order |

## Phase 5: Data & Analytics

| Task | Status | Notes |
|------|--------|-------|
| Room database setup | ⬜ | Trip, EngineEvent, AppShortcut entities |
| Engine start/stop logging | ⬜ | |
| Trip analytics computation | ⬜ | Distance, duration, fuel estimate |
| Trip history UI | ⬜ | List of past trips |
| CSV export | ⬜ | |

## Phase 6: Polish & Deployment

| Task | Status | Notes |
|------|--------|-------|
| Permissions implementation | ⬜ | Location, Bluetooth, Storage, etc. |
| Dark mode / themes | ⬜ | |
| Accessibility support | ⬜ | |
| Build signed APK | ⬜ | |
| Install & test on device | ⬜ | |

---

## Session Log

### Session 1 (Initial)
- **AI**: TierMux
- **Work done**: Architecture design, component breakdown, CAN bus approach, UI plan
- **Files created**: `CONVERSATION_SUMMARY.md`, `README.md`, `HANDOFF_TEMPLATE.md`, `TASKS.md`
- **Next step**: Reverse-engineer CAN IDs

---
*Update this file after each AI session*