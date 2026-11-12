# Feasibility Analysis: CAN Bus Access on Nippon Android-9Pro Ultra

## Research Summary

### What We Found
- **Android CAN Bus Development**: Android does not support CAN natively. Hardware bridge (USB-CAN adapter or Bluetooth OBD-II) is required.
- **Allwinner T507**: Automotive-grade SoC (AEC-Q100 qualified), designed for in-vehicle infotainment. Datasheet confirms it's in the T5 series for automotive markets.
- **Nippon Android-9Pro Ultra**: After extensive web search, no public documentation or reverse-engineering projects specific to this exact model were found.

### Key Finding: MCU Integration
The device lists an **MCU version** (`T5Q.5.1-100-A84-A5189D-231201(FT072)`) separate from the **System version** (`T5Q.2.1_20240619.134806_THEME1`). This strongly suggests:
- The T507 runs Android (the main OS)
- A separate MCU handles low-level vehicle integration (CAN, steering wheel, etc.)
- The two communicate via a serial interface (likely UART)

## Feasibility Levels

### Level 1: Direct CAN via Serial Port
- **Feasibility**: Medium
- **Requirements**: Need to confirm `/dev/ttyS0` is wired to a CAN transceiver
- **Action**: `adb shell stty -F /dev/ttyS0 raw` then try reading

### Level 2: Proprietary Serial Protocol
- **Feasibility**: High
- **Requirements**: MCU likely exposes CAN data via a custom UART protocol
- **Action**: Capture UART traffic, reverse-engineer the protocol

### Level 3: USB-CAN Adapter
- **Feasibility**: Low (for this device)
- **Requirements**: USB OTG support, adapter hardware
- **Action**: Connect USB-CAN adapter, use Android USB Host API

## Recommended Approach

1. **First**: Check available serial ports via `adb shell ls -la /dev/tty*`
2. **Second**: Try reading from `/dev/ttyS0` with a test app
3. **Third**: If no direct CAN, look for a proprietary serial protocol
4. **Fallback**: Use an external OBD-II/CAN adapter

## Unknowns
- Exact wiring of CAN lines to the T507
- Whether the MCU bridges CAN to UART
- Baud rate and protocol format
- Whether root is required for serial access