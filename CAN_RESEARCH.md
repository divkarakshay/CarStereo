# CAN Bus Research Findings

## Key Discovery: Allwinner T507 Does NOT Have Built-in CAN

The Allwinner T507 processor itself does NOT support CAN natively. According to Forlinx documentation, CAN functionality must be added via an external MCP2515 SPI-to-CAN controller.

## How Nippon Stereo Likely Handles CAN

The Nippon Android-9Pro Ultra uses a separate MCU (T5Q.5.1-100-A84-A5189D) that:
- Communicates with the T507 SoC over serial (likely /dev/ttyS*)
- Handles CAN bus communication on the vehicle side
- Exposes CAN data to Android via a proprietary protocol over the serial link

## What This Means for Our Launcher

1. We CANNOT directly access raw CAN frames from Android
2. We MUST work with whatever data the MCU exposes over the serial interface
3. The MCU likely already translates CAN data into human-readable values (speed, RPM, etc.)
4. The stereo already uses CAN for steering wheel controls - so SOME CAN access exists

## Existing Projects That Help

- **OpenXC Android Library** (github.com/openxc/openxc-android) - Ford's open-source vehicle interface library
- **CanBusApp** (github.com/AlekseiBazhenov/CanBusApp) - Android NDK-based CAN socket library
- **AndrOBD** - Open-source OBD-II diagnostic app
- **XDA Forum threads** - Multiple discussions about Android head unit CAN access

## Next Step: Probe the Serial Interface

We need to:
1. Check what serial ports exist on the device (adb shell ls /dev/tty*)
2. Try reading from them to see what data format the MCU uses
3. Look for any existing system apps that already parse this data