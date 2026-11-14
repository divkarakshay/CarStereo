package com.baleno.launcher

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun RenderWidget(
    type: WidgetType,
    state: BalenoDashboardState,
    textColor: Color,
    subTextColor: Color,
    onUploadCarImage: () -> Unit
) {
    when (type) {
        WidgetType.BALENO_HERO -> BalenoVehicleWidget(state, textColor, subTextColor, onUploadCarImage)
        WidgetType.SPEED_RPM -> SpeedAndRpmWidget(state, textColor, subTextColor)
        WidgetType.LIVE_MAP -> LiveMapWidget(textColor, subTextColor)
        WidgetType.TRIP_STATS -> TripStatsWidget(state, textColor, subTextColor)
        WidgetType.FUEL_ECONOMY -> FuelEconomyWidget(state, textColor, subTextColor)
        WidgetType.ENGINE_LOG -> EngineLogWidget(state, textColor, subTextColor)
        WidgetType.SERVICE_RECORD -> ServiceRecordWidget(state, textColor, subTextColor)
        WidgetType.TRIPWISE_HISTORY -> TripwiseHistoryWidget(state, textColor, subTextColor)
        WidgetType.QUICK_CONTROLS, WidgetType.MEDIA_PLAYER -> QuickControlsWidget(state, textColor)
    }
}

// 1. MARUTI SUZUKI BALENO HERO + 5-SPEED MANUAL GEAR SELECTOR (R, N, 1, 2, 3, 4, 5)
@Composable
fun BalenoVehicleWidget(
    state: BalenoDashboardState,
    textColor: Color,
    subTextColor: Color,
    onUploadCarImage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Car Details", color = subTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = onUploadCarImage, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Rounded.AddAPhoto,
                    contentDescription = "Upload Car Photo",
                    tint = subTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Maruti Suzuki Baleno", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Alpha 1.2L DualJet • 5MT", color = subTextColor, fontSize = 12.sp)
        }

        // Car Image Container (Tap to upload custom photo of your Baleno)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF9CA3AF).copy(alpha = 0.12f))
                .clickable { onUploadCarImage() },
            contentAlignment = Alignment.Center
        ) {
            if (state.customCarImageUri != null) {
                AsyncImage(
                    model = state.customCarImageUri,
                    contentDescription = "Maruti Suzuki Baleno",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Tap to add Baleno photo", color = subTextColor, fontSize = 11.sp)
                }
            }
        }

        // Manual Gear Pill Bar: ONLY R, N, 1, 2, 3, 4, 5
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Color(0xFF9CA3AF).copy(alpha = 0.15f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (gear in Gear.values()) {
                val isSelected = state.selectedGear == gear
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected && gear == Gear.R -> Color(0xFFEF4444)
                                isSelected -> AccentDarkPill
                                else -> Color.Transparent
                            }
                        )
                        .clickable { state.selectedGear = gear },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gear.label,
                        color = if (isSelected) Color.White else textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// 2. SPEEDOMETER & REV COUNTER (RPM)
@Composable
fun SpeedAndRpmWidget(state: BalenoDashboardState, textColor: Color, subTextColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cluster Telemetry", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("GEAR ${state.selectedGear.label}", color = AccentLime, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        // Dual Arc Gauges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GaugeArc(
                value = state.currentSpeedKmh.toFloat(),
                maxValue = 180f,
                primaryText = "${state.currentSpeedKmh}",
                unitText = "km/h",
                arcColor = AccentLime,
                textColor = textColor,
                subTextColor = subTextColor
            )
            GaugeArc(
                value = state.currentRpm.toFloat(),
                maxValue = 7000f,
                primaryText = String.format("%.1f", state.currentRpm / 1000f),
                unitText = "x1000 RPM",
                arcColor = if (state.currentRpm > 5500) Color(0xFFEF4444) else Color(0xFF3B82F6),
                textColor = textColor,
                subTextColor = subTextColor
            )
        }

        // Instant Fuel Economy Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF9CA3AF).copy(alpha = 0.12f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("INSTANT ECONOMY", color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${state.instantMileageKmpl} km/L", color = AccentLime, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("AVG MILEAGE", color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${state.avgMileageKmpl} km/L", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GaugeArc(
    value: Float,
    maxValue: Float,
    primaryText: String,
    unitText: String,
    arcColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp
            val sweep = (value / maxValue).coerceIn(0f, 1f) * 240f
            val strokePx = stroke.toPx()
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            drawArc(
                color = arcColor,
                startAngle = 150f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(primaryText, color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(unitText, color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// 3. LIVE NAVIGATION MAP WIDGET (Self-contained Canvas route rendering - no Google Maps API key required)
@Composable
fun LiveMapWidget(textColor: Color, subTextColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Location", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Icon(
                Icons.Rounded.MyLocation,
                contentDescription = null,
                tint = subTextColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF10131A))
        ) {
            // Native route canvas: street grid + animated destination route
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 36f
                val gridPath = Path().apply {
                    var x = 0f
                    while (x < size.width) {
                        moveTo(x, 0f)
                        lineTo(x, size.height)
                        x += gridStep
                    }
                    var y = 0f
                    while (y < size.height) {
                        moveTo(0f, y)
                        lineTo(size.width, y)
                        y += gridStep
                    }
                }
                drawPath(
                    path = gridPath,
                    color = Color(0xFF1B2130),
                    style = Stroke(width = 1f, cap = StrokeCap.Round)
                )

                val route = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.8f)
                    lineTo(size.width * 0.32f, size.height * 0.58f)
                    lineTo(size.width * 0.55f, size.height * 0.66f)
                    lineTo(size.width * 0.78f, size.height * 0.38f)
                    lineTo(size.width * 0.9f, size.height * 0.28f)
                }
                drawPath(
                    path = route,
                    color = AccentLime,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )

                // Origin / destination markers on the route
                drawCircle(
                    brush = SolidColor(Color(0xFF00E676)),
                    radius = 8f,
                    center = Offset(size.width * 0.18f, size.height * 0.8f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
                drawCircle(
                    brush = SolidColor(Color(0xFFFFFFFF)),
                    radius = 8f,
                    center = Offset(size.width * 0.9f, size.height * 0.28f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            // Floating Dark Destination Card (Matches reference image)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentDarkPill.copy(alpha = 0.94f))
                    .padding(12.dp)
            ) {
                Text("NH-48 Gurugram Express", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Live GPS • Heading NW", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("4.2 km", color = AccentLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("8 min", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

// 4. STATISTICS WIDGET (Max Speed, Max RPM, Total Odometer)
@Composable
fun TripStatsWidget(state: BalenoDashboardState, textColor: Color, subTextColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Trip & Peak Statistics", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        StatTile("MAX SPEED", "${state.maxSpeedKmh} km/h", AccentLime, textColor, subTextColor)
        StatTile("MAX ENGINE RPM", "${state.maxRpm} RPM", Color(0xFF3B82F6), textColor, subTextColor)
        StatTile("TOTAL ODOMETER", "${state.totalOdometerKm} km", textColor, textColor, subTextColor)
    }
}

@Composable
fun StatTile(label: String, value: String, accent: Color, textColor: Color, subTextColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF9CA3AF).copy(alpha = 0.12f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = subTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

// 5. MILEAGE, TRIP A, TRIP B & FUEL ECONOMY WIDGET
@Composable
fun FuelEconomyWidget(state: BalenoDashboardState, textColor: Color, subTextColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fuel & Trips", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Fuel ${state.fuelPercent}%", color = AccentLime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Trip A & Trip B Cards with Reset Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TripBox(
                "TRIP A",
                "${state.tripAKm} km",
                textColor,
                subTextColor,
                Modifier.weight(1f)
            ) { state.tripAKm = 0f }
            TripBox(
                "TRIP B",
                "${state.tripBKm} km",
                textColor,
                subTextColor,
                Modifier.weight(1f)
            ) { state.tripBKm = 0f }
        }

        StatTile("INSTANT ECONOMY", "${state.instantMileageKmpl} km/L", AccentLime, textColor, subTextColor)
        StatTile("OVERALL MILEAGE", "${state.avgMileageKmpl} km/L", textColor, textColor, subTextColor)
    }
}

@Composable
fun TripBox(
    title: String,
    distance: String,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier,
    onReset: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF9CA3AF).copy(alpha = 0.12f))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = subTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(
                "RESET",
                color = Color(0xFFEF4444),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onReset() }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(distance, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// 6. ENGINE START / STOP TIMING & IGNITION COUNTER
@Composable
fun EngineLogWidget(state: BalenoDashboardState, textColor: Color, subTextColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Engine Start/Stop", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Total Starts: ${state.engineStartCount}", color = AccentLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { state.toggleEngineState() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isEngineRunning) Color(0xFFEF4444) else AccentLime
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (state.isEngineRunning) "Log Stop" else "Log Start",
                    color = if (state.isEngineRunning) Color.White else Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.engineLogs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF9CA3AF).copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        log.eventType,
                        color = if (log.eventType.contains("ON")) AccentLime else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(log.timestamp, color = subTextColor, fontSize = 11.sp)
                }
            }
        }
    }
}

// 7. MANUAL SERVICE DATE RECORDER
@Composable
fun ServiceRecordWidget(state: BalenoDashboardState, textColor: Color, subTextColor: Color) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Service History", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add Service Date",
                    tint = AccentLime,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.serviceHistory) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF9CA3AF).copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.date, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${item.odoKm} km", color = AccentLime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(item.notes, color = subTextColor, fontSize = 11.sp)
                }
            }
        }
    }

    if (showAddDialog) {
        var dateInput by remember { mutableStateOf("14 Nov 2026") }
        var odoInput by remember { mutableStateOf(state.totalOdometerKm.toInt().toString()) }
        var noteInput by remember { mutableStateOf("Periodic Service & Filter Change") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Record Baleno Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("Service Date") })
                    OutlinedTextField(value = odoInput, onValueChange = { odoInput = it }, label = { Text("Odometer (km)") })
                    OutlinedTextField(value = noteInput, onValueChange = { noteInput = it }, label = { Text("Work Done") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        state.addServiceRecord(dateInput, odoInput.toIntOrNull() ?: 0, noteInput)
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }
}

// 8. TRIPWISE MILEAGE LOG
@Composable
fun TripwiseHistoryWidget(state: BalenoDashboardState, textColor: Color, subTextColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Tripwise Mileage", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.tripwiseList) { trip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF9CA3AF).copy(alpha = 0.12f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(trip.tripName, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${trip.distanceKm} km • Max ${trip.maxSpeedKmh} km/h", color = subTextColor, fontSize = 11.sp)
                    }
                    Text(
                        "${trip.avgMileageKmpl} km/L",
                        color = AccentLime,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// 9. QUICK VEHICLE CONTROLS (Matches bottom-left 3x2 dark/light tile grid)
@Composable
fun QuickControlsWidget(state: BalenoDashboardState, textColor: Color) {
    val controls = listOf(
        "Lock" to Icons.Rounded.Lock,
        "Voice" to Icons.Rounded.Mic,
        "Bluetooth" to Icons.Rounded.Bluetooth,
        "Maps" to Icons.Rounded.Map,
        "OBD-II" to Icons.Rounded.Speed,
        "Music" to Icons.Rounded.Headphones
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quick Controls", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        controls.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (label, icon) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(86.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AccentDarkPill),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            icon,
                            label,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(label, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
