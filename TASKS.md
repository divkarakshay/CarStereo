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
| Implement CAN frame parser/builder | ✅ Completed | ID + data byte handling |
| Set up LiveData/Broadcast for UI updates | ✅ Completed | |
| Add error handling & reconnection | ✅ Completed | |
| Create AndroidManifest.xml | ✅ Completed | Permissions, components |
| Create Gradle build files | ✅ Completed | build.gradle, settings.gradle |
| Create SerialDumpService.kt | ✅ Completed | Foreground service reading serial port |
| Create SerialDumpActivity.kt | ✅ Completed | UI

## Phase 3: Launcher UI

| Task | Status | Notes |
|------|--------|-------|
| Create LauncherActivity | ✅ Completed | Home screen, replace stock launcher |
| Build tab layout (Home, Apps, Maps, Settings) | ✅ Completed | ViewPager2 |
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

### Session 2 (Current)
- **AI**: TierMux
- **Work done**: Created SerialDump Android project, added service, activity, manifest, build files, updated tasks, added change log
- **Files updated**: `README.md`, `TASKS.md`, `CHANGELOG.md`, `SerialDump/**/*.kt`, `SerialDump/**/*.xml`, `SerialDump/**/*.gradle`

- **AI**: TierMux
- **Work done**: Architecture design, component breakdown, CAN bus approach, UI plan
- **Files created**: `CONVERSATION_SUMMARY.md`, `README.md`, `HANDOFF_TEMPLATE.md`, `TASKS.md`
- **Next step**: Reverse-engineer CAN IDs

---

### Session 3 (Current)
- **AI**: Flint
- **Work done**: Created `CanService.kt` foreground service (serial read loop, `CanFrame` data class, `LiveData<List<CanFrame>>` exposure, raw dump write, broadcast); registered service in AndroidManifest; updated plan/TASKS/CHANGELOG.
- **Files created**: `SerialDump/app/src/main/java/com/cardumper/CanService.kt`
- **Files updated**: `SerialDump/app/src/main/AndroidManifest.xml`, `.tiermux/plans/2026-11-10-1651-reverse-engineer-can-bus-and-build-servi.md`, `TASKS.md`, `CHANGELOG.md`
- **Next step**: Identify real CAN frame IDs from live hardware capture; replace placeholder frame parsing in `CanService`.

---

*Update this file after each AI session*