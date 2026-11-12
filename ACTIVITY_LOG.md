# Activity Log

This file records the recent changes made to the project files.

## Updated Files

- **SerialDumpService.kt** – Added foreground notification, serial port handling, and broadcast of data to UI.
- **SerialDumpActivity.kt** – Fixed `RECEIVER()` typo, added UI logic, and implemented start/stop/share actions.
- **SerialDumpReceiver.kt** – Corrected `RECEIVER()` to `BroadcastReceiver()` and added logging.
- **SerialDump/app/build.gradle** – Updated to use `kapt` for Room annotations and cleaned up dependencies.
- **SerialDump/settings.gradle** – No changes; kept for Gradle configuration.
- **README.md** – Added a reference to the new `ACTIVITY_LOG.md`.
- **CONVERSATION_SUMMARY.md** – Updated to reflect the new file structure and the fact that the serial dump app has been implemented.

---

This log will be appended to whenever new changes are made.
