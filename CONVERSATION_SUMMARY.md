# Conversation Summary: Nippon Android-9Pro Ultra Car Launcher

## Vehicle & Hardware
- Car: Maruti Suzuki Baleno 2023
- Stereo: Nippon Android-9Pro Ultra
- Platform: Allwinner T507-CH (QuadCore)
- Android: 10.0
- RAM: 4GB
- Storage: 64GB
- Sound Processor: AK7604
- Radio Module: SI47XX
- 360IC: TP2856
- Bluetooth: Traditional Bluetooth
- IMEI: 340416114588520
- Boot Version: T5Q.2.1_JBDxF00_M8_L8_TP2856_20231121
- MCU Version: T5Q.5.1-100-A84-A5189D-231201(FT072)
- System Version: T5Q.2.1_20240619.134806_THEME1
- CarSetting Version: master_Dev.20240618_899

## User Request
Create a full-featured launcher app for the car stereo that:
1. Reads CAN bus data and generates trip-wise & overall insights
2. Logs engine start/stop events
3. Controls car AC from UI and other car settings
4. Shows music control, all apps, widgets (map widgets, etc.)

## Architecture Overview

### Layers
1. UI / Launcher - Home screen with widgets, tabs (Home, Apps, Maps, Settings)
2. Service / Background - Persistent CAN bus service, trip logging, analytics
3. CAN Bus Layer - Low-level driver for reading/writing CAN frames
4. Data Layer - Room database for persistence, SharedPreferences for settings
5. Media Control - MediaSessionCompat integration
6. Car Settings Control - AC/climate via CAN commands
7. Widget/App List - App discovery and launching
8. Map Widget - GPS location and navigation

### Key Components

#### 1. Launcher Activity
- Manifest: android.intent.category.HOME, singleTask, excludeFromRecents
- UI: ConstraintLayout with ViewPager2 for tabs
- Widgets: AppWidgetHost for custom widgets

#### 2. CanService (Foreground Service)
- Persistent service (START_STICKY)
- Reads/writes CAN frames via serial port
- Broadcasts CAN events to UI
- Foreground notification with stop action

#### 3. CAN Protocol
- Serial port: /dev/ttyS0 or /dev/socket/can0
- Frame parsing: ID + data bytes
- Need to reverse-engineer specific IDs for:
  - Engine state (start/stop)
  - AC temperature, mode, fan speed
  - Other car settings

#### 4. AC Control UI
- Temperature seekbar, mode toggles, fan speed selector
- Optimistic UI updates on command
- Listens for AC status frames for confirmation

#### 5. Media Control
- MediaSessionCompat integration
- Now-playing widget with play/pause/next/prev

#### 6. App List
- Query installed apps via PackageManager
- Filter out system apps
- Persist shortcuts in Room

#### 7. Map Widget
- Google Maps SDK or Mapbox
- FusedLocationProviderClient for GPS
- Updates every 5s

#### 8. Trip Analytics
- Trip entity: startTime, endTime, distanceKm, fuelConsumedLitres
- Engine events logged to Room
- WorkManager for periodic analytics
- Export CSV functionality

#### 9. Permissions
- ACCESS_FINE_LOCATION, BLUETOOTH, WRITE_EXTERNAL_STORAGE
- RECORD_AUDIO (voice), READ_EXTERNAL_STORAGE
- INTERNET, ACCESS_NETWORK_STATE

#### 10. Testing & Debugging
- adb logcat, dumpsys, serial port checks
- Unit tests for parsing, instrumentation tests for UI

#### 11. Packaging
- Gradle assembleRelease, signed APK
- Install via ADB or OTA

## CAN Bus Research Findings (CRITICAL)

### Allwinner T507 Does NOT Have Built-in CAN
The Allwinner T507 processor itself does NOT support CAN natively. CAN functionality must be added via an external MCP2515 SPI-to-CAN controller (per Forlinx documentation).

### How Nippon Stereo Likely Handles CAN
The Nippon Android-9Pro Ultra uses a separate MCU (T5Q.5.1-100-A84-A5189D) that:
- Communicates with the T507 SoC over serial (likely /dev/ttyS*)
- Handles CAN bus communication on the vehicle side
- Exposes CAN data to Android via a proprietary protocol over the serial link

### What This Means for Our Launcher
1. We CANNOT directly access raw CAN frames from Android
2. We MUST work with whatever data the MCU exposes over the serial interface
3. The MCU likely already translates CAN data into human-readable values (speed, RPM, etc.)
4. The stereo already uses CAN for steering wheel controls - so SOME CAN access exists

### Existing Projects That Help
- OpenXC Android Library (github.com/openxc/openxc-android) - Ford's open-source vehicle interface library
- CanBusApp (github.com/AlekseiBazhenov/CanBusApp) - Android NDK-based CAN socket library
- AndrOBD - Open-source OBD-II diagnostic app
- XDA Forum threads - Multiple discussions about Android head unit CAN access

### Next Step: Probe the Serial Interface
1. Check what serial ports exist on the device (adb shell ls /dev/tty*)
2. Try reading from them to see what data format the MCU uses
3. Look for any existing system apps that already parse this data

## Next Steps
1. Probe serial interface on the device to understand CAN data format
2. Implement CanService skeleton
3. Build UI skeleton (home, app list, AC controls)
4. Hook up media session and map widget
5. Persist trip data and expose analytics

## Files to Create
- CONVERSATION_SUMMARY.md - This file
- CAN_RESEARCH.md - CAN bus research findings
- CanService.kt - CAN bus service
- LauncherActivity.kt - Main launcher
- Trip.kt - Room entity
- AcControlFragment.kt - AC controls
- AppListFragment.kt - App launcher
- MapWidget.kt - Map widget
- MediaControllerHelper.kt - Media integration