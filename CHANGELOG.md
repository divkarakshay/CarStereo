# Change Log

**2026‑11‑10**
- Created the **SerialDump** Android project structure.
- Added `SerialDumpService.kt`: foreground service that opens `/dev/ttyS0`, continuously reads the raw data stream, writes it to a log file, and broadcasts progress (`bytes` and `data` string) to the UI.
- Added `SerialDumpActivity.kt`: launches the service, displays status, bytes written, and last data line in the UI. Uses a `BroadcastReceiver` to update the UI in real time.
- Added `SerialDumpReceiver.kt`: receives the same broadcast but logs the data for debugging.
- Updated `AndroidManifest.xml` with required permissions and components.
- Updated `app/build.gradle` to include `kotlin-kapt` plugin and replaced `annotationProcessor` with `kapt` for Room.
- Updated `SerialDump/settings.gradle` to set the project name and include the app module.
- Added a **Change Log** section to `README.md` to record the above changes.
- Updated `TASKS.md` to mark the following tasks as completed:
  - Create Android project structure for SerialDump app
  - Create SerialDumpService.kt – foreground service reading serial port
  - Create SerialDumpActivity.kt – UI displaying data stream
  - Create AndroidManifest.xml with proper permissions
  - Create Gradle build files

These changes now provide a working prototype that can connect to the stereo’s serial port, dump the MCU data stream to a file, and display it in the launcher UI.
