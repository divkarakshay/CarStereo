# CAN Bus / MCU Findings — Head Unit APK Analysis

**Date:** November 14 (analysis session)
**Source:** Android head-unit APKs copied from device into `/Users/akhay/MyWorkspace/CarStereo/`
- `TW.apk` (2,341,136 bytes — main head-unit app)
- `TWCore.apk` (245,199 bytes — core/services)
- `TDService.apk` (31,785 bytes — telematics/diagnostic service)
- `XTService.apk` (82,408 bytes — service)
- `Allwinner Package Override Application.apk` (8,542 bytes — override app)

---

## 1. Existing Baseline Finding (from `CAN_RESEARCH.md`)

- Allwinner T507 SoC has **no native CAN controller**.
- CAN is handled by an external MCU (reported: **T5Q.5.1-100-A84-A5189D**), communicating with the T507 over serial (likely `/dev/ttyS*`).
- Android side exposes CAN-derived data to apps via a proprietary protocol over the serial link.
- Steering wheel controls already required some CAN access, proving partial data paths exist.

---

## 2. New Findings from Static APK Analysis (this session)

### 2.1 `TW.apk` — Main Head-Unit App (most relevant)

- Contains dedicated CAN stack packages:
  - `com.tw.canuart` — base CAN UART package
  - `com.tw.canuart_track` — **`Canuat_TrackService`** ← CAN tracking service
- CAN I/O primitives found in strings:
  - `canRead`, `canWrite`, `cankao`
- Hardware-path writes found:
  - `/sys/devices/tp9930/cam6timeout`
  - `/sys/devices/tp9930/error`, `/sys/devices/tp9930/id`
  - `/sys/devices/tp9930/brightness`, `/sys/devices/tp9930/saturation`, `/sys/devices/tp9930/contrast`
  - `/sys/devices/tp2850/...` (brightness/saturation/contrast/signal/type/channel)
  - `/sys/class/tw/gtp/...` (spdifstate, usbpower, touch, etc.)
- Vehicle telematics intents (`com.unisound.intent.action.*`):
  - `ACC_ON`, `ACC_OFF`, `NET_WK`, `CHECK_BTSTATUS`, `DO_SLEEP`, `DO_WAKEUP`, `DO_SHUTDOWN`, `DO_MUTE`, `GET_MUTE`, `GET_POWER`, `START_TALK`, `USB_TETHER`, `NET_MSG`
- App-internal intents (`com.unisound.unicar.*`):
  - `action.pause`, `action.unmute`, `kill.allround`, `update.action`, `action.mute`, `action.play`, etc.
- Other automotive services visible in the same dex:
  - `com.tw.ac.AirService`, `com.tw.car.CarService`, `com.tw.carapps.CarService`, `com.tw.caratinfo.ATInfoService`, `com.tw.carinfoservice`, `com.tw.carsettings`, `com.tw.bt.*`, `com.tw.cphelper.DspService`, `com.tw.dsphelper.DspService`, `com.tw.dsp.DSPActivity`, `com.tw.dvd.DVDActivity`, `com.tw.eq.*`, `com.tw.fcam.*`, `com.tw.driving`, `com.tw.dsp`, `com.tw.v` etc.

### 2.2 `TWCore.apk` — Core/Transport Layer

- `createSocket`
- WebSocket support (headers: `Sec-WebSocket-Accept`, `Sec-WebSocket-Key`, `Sec-WebSocket-Protocol`, frames, `SocketTimeoutException`)
- MQTT client support (Eclipse Paho: `mqttv3`, `WebSocket Frame with Stop OpCode`, `Base64`)
- SSL (`SSLSocketFactory`, `SSLServerSocketFactory`)

⇒ Suggests the head unit exposes CAN-derived data to clients via **WebSocket/MQTT** (possibly to the car-stereo app or cloud).

### 2.3 `TDService.apk`, `XTService.apk` — Intent-only

- Primarily intent actions (`com.unisound.intent.action.*`, `com.unisound.unicar.*`) — no new CAN primitives found in quick scan.

### 2.4 `Allwinner Package Override Application.apk`

- Minimal (8.5 KB) — override package only.

---

## 3. Synthesis: How the Head Unit Connects to CAN Data

1. **Vehicle CAN bus** → external MCU (T5Q.5.1…) → serial to Allwinner T507 (`/dev/ttyS*`).
2. **T507 → Android**: via CAN UART service layer (`Canuat_TrackService`, `canRead/canWrite`).
3. **T507 → external consumers**: WebSocket / MQTT (TWCore) — likely the path used to feed the car stereo app (SerialDump) or the cloud.

---

## 4. Implications for CarStereo Pipeline

- The TCP stream the SerialDump app currently reads from PC is likely downstream of the head unit's CAN→MCU→Android chain, OR the head unit pushes CAN data to the stereo app over the transport (WebSocket/MQTT/TCP).
- Next: hook `Canuat_TrackService` to capture frames; determine serial baud/format and message framing (proprietary protocol vs. standard UDS/ISO-TP).

---

## 5. Next Steps

1. Decompile/hook `com.tw.canuart_track.Canuat_TrackService`.
2. Trace socket/serial reads feeding `canRead`/`canWrite`.
3. Map `com.unisound.*` intents to data sources.
4. Correlate with existing TCP listener (`0.0.0.0:8080`) in SerialDumpService.

---

## 6. Capture Attempts & Current Blocker (Nov 14, APK session)

### 6.1 What the new AI shared

- `TWTW` (`com.tw.service.xt.TWTW`) extends `TWUtil`; `TWTW.open()` registers CAN IDs:
  `{513, 515, 516, 523, 769, 1281, 2560, 2561, 2562, 2563, -24804}`.
- Writes via `mTW.write(id, ...)`; reads via `mTW.addHandler("CommandService", this.mHandler)`.
- ID map: `0x0201 (513)` → navigation commands; `0x0203 (515)` → volume; `0x0204 (516)` → light/brightness; `0x020B (523)` → possibly AC; `0x0301 (769)` → source; `0x0501 (1281)` → system status; `0x0A00–0x0A03 (2560–2563)` → additional commands; `-24804` → special command.

### 6.2 Frame format hypothesis

- Each frame = **2-byte big-endian CAN ID + payload** (e.g., `0x0201` = 513 → navigation).

### 6.3 Capture run on Android VM (`127.0.0.1:5555` / `emulator-5554`)

- Installed + launched `SerialDump` (`com.cardumper`), started `SerialDumpService`.
- Service received `ACTION_START` (confirmed via logcat):
  - `Serial port not found: /dev/ttyS0`
  - `Trying alternative: /dev/ttyAMA0`
  - `IOException: /dev/ttyAMA0: open failed: EACCES (Permission denied)`
- Added fallbacks to `SerialDumpService`: `/dev/ttyAMA0`, `/dev/ttyUSB0` (rebuilt + reinstalled).
- **Blocker:** emulator exposes `/dev/ttyAMA0` as `root:root` mode `0600`; app cannot open it (`EACCES`).
- `/dev/ttyS0` does not exist on this emulator; `chmod 666 /dev/ttyAMA0` not permitted (`Operation not permitted`).
- Therefore: **no raw CAN dump file was produced yet** on the emulator.

### 6.4 Implications

- Capture must happen on **real head-unit hardware** (where `/dev/ttyS0` exists with app-accessible permissions) or via **frida-based interception** on the emulator.
