package com.baleno.launcher

import android.net.Uri
import androidx.compose.runtime.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class Gear(val label: String) {
    R("R"), N("N"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5")
}

enum class WidgetType(val title: String, val spanColumns: Int = 1) {
    LIVE_MAP("Location & GPS", 1),
    SPEED_RPM("Speed & Rev Counter", 1),
    QUICK_CONTROLS("Vehicle Controls", 1),
    MEDIA_PLAYER("Media Player", 1),
    BALENO_HERO("Maruti Suzuki Baleno", 1),
    TRIP_STATS("Trip Statistics", 1),
    FUEL_ECONOMY("Mileage & Fuel Economy", 1),
    ENGINE_LOG("Engine Start / Stop Log", 1),
    SERVICE_RECORD("Service & Maintenance", 1),
    TRIPWISE_HISTORY("Tripwise Mileage Log", 1)
}

data class DriverProfile(
    val id: String,
    val name: String,
    val avatarUri: Uri? = null,
    val preferredThemeDark: Boolean = false
)

data class TripLogEntry(
    val tripName: String,
    val distanceKm: Float,
    val avgMileageKmpl: Float,
    val maxSpeedKmh: Int
)

data class EngineEvent(
    val timestamp: String,
    val eventType: String // "IGNITION ON" or "IGNITION OFF"
)

data class ServiceEntry(
    val date: String,
    val odoKm: Int,
    val notes: String
)

class BalenoDashboardState {
    // Theme & Drivers
    var isDarkTheme by mutableStateOf(false)
    val drivers = mutableStateListOf(
        DriverProfile("1", "Aarav Sharma"),
        DriverProfile("2", "Priya Nair")
    )
    var currentDriverIndex by mutableIntStateOf(0)
    val activeDriver: DriverProfile get() = drivers[currentDriverIndex]

    // Custom Car Image URI (supports gallery picker)
    var customCarImageUri by mutableStateOf<Uri?>(null)

    // Live Telemetry (Feed via OBD-II Bluetooth ELM327 / CAN-Bus)
    var currentSpeedKmh by mutableIntStateOf(64)
    var currentRpm by mutableIntStateOf(2450)
    var selectedGear by mutableStateOf(Gear.FOUR)
    var fuelPercent by mutableIntStateOf(65)
    var ecoMode by mutableStateOf(true)

    // Trip & Economy Metrics
    var maxSpeedKmh by mutableIntStateOf(118)
    var maxRpm by mutableIntStateOf(5200)
    var totalOdometerKm by mutableFloatStateOf(24850.4f)
    var tripAKm by mutableFloatStateOf(142.6f)
    var tripBKm by mutableFloatStateOf(48.2f)
    var instantMileageKmpl by mutableFloatStateOf(19.4f)
    var avgMileageKmpl by mutableFloatStateOf(17.8f)

    // Engine Start/Stop Counter & Logs
    var engineStartCount by mutableIntStateOf(14)
    var isEngineRunning by mutableStateOf(true)
    val engineLogs = mutableStateListOf(
        EngineEvent("Today, 09:15 AM", "IGNITION ON"),
        EngineEvent("Yesterday, 06:42 PM", "IGNITION OFF"),
        EngineEvent("Yesterday, 05:10 PM", "IGNITION ON")
    )

    // Service Dates
    val serviceHistory = mutableStateListOf(
        ServiceEntry("12 Aug 2026", 20000, "20k Major Service + Synthetic Oil"),
        ServiceEntry("14 Feb 2026", 10000, "10k Inspection & Wheel Alignment")
    )

    // Tripwise Mileage History
    val tripwiseList = mutableStateListOf(
        TripLogEntry("Morning Commute", 28.4f, 18.9f, 82),
        TripLogEntry("NH-48 Highway Run", 114.2f, 21.6f, 118),
        TripLogEntry("City Errands", 12.8f, 14.5f, 54)
    )

    // Multi-Page Widget Configuration (3 columns per page at 1024x600)
    var isEditMode by mutableStateOf(false)
    val pages = mutableStateListOf(
        mutableStateListOf(
            WidgetType.LIVE_MAP,
            WidgetType.SPEED_RPM,
            WidgetType.BALENO_HERO
        ),
        mutableStateListOf(
            WidgetType.TRIP_STATS,
            WidgetType.FUEL_ECONOMY,
            WidgetType.ENGINE_LOG
        ),
        mutableStateListOf(
            WidgetType.SERVICE_RECORD,
            WidgetType.TRIPWISE_HISTORY,
            WidgetType.MEDIA_PLAYER
        )
    )

    fun switchDriver() {
        currentDriverIndex = (currentDriverIndex + 1) % drivers.size
    }

    fun toggleEngineState() {
        isEngineRunning = !isEngineRunning
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
        if (isEngineRunning) {
            engineStartCount++
            engineLogs.add(0, EngineEvent(now, "IGNITION ON"))
        } else {
            engineLogs.add(0, EngineEvent(now, "IGNITION OFF"))
        }
    }

    fun addServiceRecord(date: String, odo: Int, note: String) {
        serviceHistory.add(0, ServiceEntry(date, odo, note))
    }
}
