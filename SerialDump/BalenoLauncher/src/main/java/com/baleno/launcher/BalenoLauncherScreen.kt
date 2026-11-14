package com.baleno.launcher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Palette matching the reference Bento-grid UI
val LightCanvas = Color(0xFFF3F4F6)
val LightCard = Color(0xFFFFFFFF)
val DarkCanvas = Color(0xFF121316)
val DarkCard = Color(0xFF1E2025)
val AccentLime = Color(0xFF00E676)
val AccentDarkPill = Color(0xFF141518)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalenoLauncherScreen(
    state: BalenoDashboardState = remember { BalenoDashboardState() }
) {
    val canvasColor = if (state.isDarkTheme) DarkCanvas else LightCanvas
    val cardColor = if (state.isDarkTheme) DarkCard else LightCard
    val textColor = if (state.isDarkTheme) Color.White else Color(0xFF141518)
    val subTextColor = if (state.isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val pagerState = rememberPagerState(pageCount = { state.pages.size })

    val carImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) state.customCarImageUri = uri
    }
    val driverImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val idx = state.currentDriverIndex
            state.drivers[idx] = state.drivers[idx].copy(avatarUri = uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        TopHeaderBar(
            state = state,
            cardColor = cardColor,
            textColor = textColor,
            subTextColor = subTextColor,
            onPickDriverImage = { driverImagePicker.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (state.isEditMode) {
            WidgetEditBanner(
                state = state,
                currentPage = pagerState.currentPage,
                cardColor = cardColor,
                textColor = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 14.dp
        ) { pageIndex ->
            val pageWidgets = state.pages[pageIndex]
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pageWidgets.forEach { widgetType ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(cardColor)
                            .padding(14.dp)
                    ) {
                        RenderWidget(
                            type = widgetType,
                            state = state,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            onUploadCarImage = { carImagePicker.launch("image/*") }
                        )

                        if (state.isEditMode) {
                            IconButton(
                                onClick = { pageWidgets.remove(widgetType) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                repeat((3 - pageWidgets.size).coerceAtLeast(0)) {
                    EmptyWidgetSlot(
                        isEditMode = state.isEditMode,
                        cardColor = cardColor.copy(alpha = 0.5f),
                        onAddClick = { state.isEditMode = true }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(state.pages.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (active) 22.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (active) AccentLime else subTextColor.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
fun TopHeaderBar(
    state: BalenoDashboardState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onPickDriverImage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AccentDarkPill),
            contentAlignment = Alignment.Center
        ) {
            Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .width(240.dp)
                .height(40.dp)
                .clip(CircleShape)
                .background(cardColor)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = subTextColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search destination...", color = subTextColor, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        TextButton(
            onClick = { state.isEditMode = !state.isEditMode },
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (state.isEditMode) AccentLime else Color.Transparent
            ),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (state.isEditMode) Icons.Rounded.Check else Icons.Rounded.Add,
                contentDescription = null,
                tint = if (state.isEditMode) Color.Black else textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (state.isEditMode) "Done Editing" else "Add a panel",
                color = if (state.isEditMode) Color.Black else textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .height(38.dp)
                .clip(CircleShape)
                .background(cardColor)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { state.isDarkTheme = true },
                modifier = Modifier
                    .size(30.dp)
                    .background(if (state.isDarkTheme) AccentDarkPill else Color.Transparent, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DarkMode,
                    contentDescription = null,
                    tint = if (state.isDarkTheme) Color.White else subTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = { state.isDarkTheme = false },
                modifier = Modifier
                    .size(30.dp)
                    .background(if (!state.isDarkTheme) LightCanvas else Color.Transparent, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LightMode,
                    contentDescription = null,
                    tint = if (!state.isDarkTheme) Color.Black else subTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(CircleShape)
                .background(cardColor)
                .padding(start = 4.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentLime)
                    .clickable { onPickDriverImage() },
                contentAlignment = Alignment.Center
            ) {
                if (state.activeDriver.avatarUri != null) {
                    AsyncImage(
                        model = state.activeDriver.avatarUri,
                        contentDescription = "Driver Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = state.activeDriver.name.take(1),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier.clickable { state.switchDriver() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.activeDriver.name,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = subTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun WidgetEditBanner(
    state: BalenoDashboardState,
    currentPage: Int,
    cardColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Add to Page ${currentPage + 1}:",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WidgetType.values().forEach { type ->
                AssistChip(
                    onClick = {
                        if (state.pages[currentPage].size < 3) {
                            state.pages[currentPage].add(type)
                        }
                    },
                    label = { Text(text = "+ ${type.title}", fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { state.pages.add(mutableStateListOf<WidgetType>()) },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentDarkPill)
        ) {
            Text(text = "+ New Page", fontSize = 11.sp, color = Color.White)
        }
    }
}

@Composable
fun RowScope.EmptyWidgetSlot(
    isEditMode: Boolean,
    cardColor: Color,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(cardColor)
            .clickable { onAddClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isEditMode) "Select a widget above" else "+ Empty Slot",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}
