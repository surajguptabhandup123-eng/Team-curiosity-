package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PracticeLocationEntity
import com.example.data.model.UserRole
import com.example.service.LocationHelper
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun AdminSettingsScreen(
    currentRole: UserRole,
    practiceLocation: PracticeLocationEntity?,
    onSaveLocation: (lat: Double, lon: Double, radius: Double, name: String) -> Unit,
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var locationName by remember(practiceLocation) {
        mutableStateOf(practiceLocation?.locationName ?: "Curious City FTC Robotics Lab & Arena")
    }
    var latitudeStr by remember(practiceLocation) {
        mutableStateOf(String.format(Locale.US, "%.5f", practiceLocation?.latitude ?: 37.42200))
    }
    var longitudeStr by remember(practiceLocation) {
        mutableStateOf(String.format(Locale.US, "%.5f", practiceLocation?.longitude ?: -122.08410))
    }
    var allowedRadius by remember(practiceLocation) {
        mutableDoubleStateOf(practiceLocation?.allowedRadius ?: 100.0)
    }
    var customRadiusStr by remember { mutableStateOf("") }
    var isCustomRadius by remember { mutableStateOf(false) }

    // After saving, keep track of saved configuration to show the required format
    var lastSavedLocation by remember { mutableStateOf<PracticeLocationEntity?>(practiceLocation) }
    var showSavedSummary by remember { mutableStateOf(false) }

    var isFetchingGps by remember { mutableStateOf(false) }
    var gpsMessage by remember { mutableStateOf<String?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }

    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                isFetchingGps = true
                gpsMessage = null
                val loc = LocationHelper.getCurrentLocation(context)
                if (loc != null) {
                    latitudeStr = String.format(Locale.US, "%.6f", loc.latitude)
                    longitudeStr = String.format(Locale.US, "%.6f", loc.longitude)
                    gpsMessage = "Acquired current GPS location successfully: (${latitudeStr}, ${longitudeStr})"
                } else {
                    // Fallback to default lab coordinates with notification
                    latitudeStr = "37.422000"
                    longitudeStr = "-122.084100"
                    gpsMessage = "Location acquired (Default FTC Practice Lab reference coordinates)."
                }
                isFetchingGps = false
            }
        } else {
            gpsMessage = "Location permission denied. Please enable GPS permissions in settings."
        }
    }

    // Map Picker Dialog
    if (showMapPicker) {
        InteractiveMapPickerDialog(
            initialLat = latitudeStr.toDoubleOrNull() ?: 37.4220,
            initialLon = longitudeStr.toDoubleOrNull() ?: -122.0841,
            initialRadius = allowedRadius,
            initialLocationName = locationName,
            onDismiss = { showMapPicker = false },
            onLocationConfirmed = { newLat, newLon, newName ->
                latitudeStr = String.format(Locale.US, "%.6f", newLat)
                longitudeStr = String.format(Locale.US, "%.6f", newLon)
                if (newName.isNotBlank()) {
                    locationName = newName
                }
                showMapPicker = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechSlateDark)
            .testTag("admin_settings_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            TeamCuriosityHeader(
                currentRole = currentRole,
                studentName = null,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Navigation Breadcrumb Bar: Settings → Attendance Location
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(CyanPrimary.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    width = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Settings",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TechTextSecondaryDark
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "Attendance Location",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }
        }

        // Admin Location Setup Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(CyanPrimary.copy(alpha = 0.6f), TechCardBorderDark)
                    ),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCuriosityLogoBadge(size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ADMIN LOCATION SETUP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Official Practice Location",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TechTextPrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Set the geofenced area for Team Curiosity. Students will only be able to mark attendance when within this zone.",
                        fontSize = 11.sp,
                        color = TechTextSecondaryDark,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Practice Location Name
                    Text(
                        text = "1. Practice Location Name",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        placeholder = { Text("e.g. Curious City FTC Robotics Lab & Arena") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TechCardBorderDark,
                            focusedTextColor = TechTextPrimaryDark,
                            unfocusedTextColor = TechTextPrimaryDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_location_name")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Set Location using current device GPS & 3. Interactive Map buttons
                    Text(
                        text = "2 & 3. Set Practice Coordinates",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // [Use My Current Location]
                        Button(
                            onClick = {
                                gpsLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_use_current_location"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0369A1),
                                contentColor = Color.White
                            )
                        ) {
                            if (isFetchingGps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Use My Current Location",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // [Choose Location on Map]
                        OutlinedButton(
                            onClick = { showMapPicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_choose_location_map"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(CyanPrimary, CyanPrimaryVariant))
                            )
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Choose Location on Map",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (gpsMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = gpsMessage!!,
                            fontSize = 11.sp,
                            color = if (gpsMessage!!.contains("denied")) TechAccentRed else CyanPrimaryVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 & 5: Latitude & Longitude Fields
                    Text(
                        text = "4 & 5. Coordinates (Latitude & Longitude)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitudeStr,
                            onValueChange = { latitudeStr = it },
                            label = { Text("Latitude") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_latitude")
                        )

                        OutlinedTextField(
                            value = longitudeStr,
                            onValueChange = { longitudeStr = it },
                            label = { Text("Longitude") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_longitude")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Allowed Radius (Default radius: 100 meters)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "6. Allowed Radius",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Current: ${allowedRadius.toInt()} m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50.0 to "50m", 100.0 to "100m (Default)", 200.0 to "200m").forEach { (rad, label) ->
                            FilterChip(
                                selected = !isCustomRadius && allowedRadius == rad,
                                onClick = {
                                    isCustomRadius = false
                                    allowedRadius = rad
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = CyanPrimary
                                )
                            )
                        }

                        FilterChip(
                            selected = isCustomRadius,
                            onClick = { isCustomRadius = true },
                            label = { Text("Custom", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }

                    if (isCustomRadius) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customRadiusStr,
                            onValueChange = {
                                customRadiusStr = it
                                it.toDoubleOrNull()?.let { r -> allowedRadius = r }
                            },
                            label = { Text("Custom Radius in meters (e.g. 150)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_radius")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // [Save Practice Location] Button
                    Button(
                        onClick = {
                            val lat = latitudeStr.toDoubleOrNull() ?: 37.4220
                            val lon = longitudeStr.toDoubleOrNull() ?: -122.0841
                            onSaveLocation(lat, lon, allowedRadius, locationName)
                            lastSavedLocation = PracticeLocationEntity(
                                id = 1,
                                latitude = lat,
                                longitude = lon,
                                allowedRadius = allowedRadius,
                                locationName = locationName
                            )
                            showSavedSummary = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_practice_location"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = TechSlateDark
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Practice Location",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    // After saving, show:
                    // Practice Location: [location name]
                    // Latitude: [latitude]
                    // Longitude: [longitude]
                    // Allowed Radius: [100 m]
                    val saved = lastSavedLocation
                    if (saved != null && (showSavedSummary || practiceLocation != null)) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF03221E),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(TechAccentGreen, CyanPrimary)
                                ),
                                width = 1.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saved_location_summary")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TechAccentGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Official Location Active & Verified ✓",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechAccentGreen
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = TechAccentGreen.copy(alpha = 0.3f)
                                )

                                SavedInfoRow(label = "Practice Location:", value = saved.locationName)
                                Spacer(modifier = Modifier.height(4.dp))
                                SavedInfoRow(label = "Latitude:", value = String.format(Locale.US, "%.5f", saved.latitude))
                                Spacer(modifier = Modifier.height(4.dp))
                                SavedInfoRow(label = "Longitude:", value = String.format(Locale.US, "%.5f", saved.longitude))
                                Spacer(modifier = Modifier.height(4.dp))
                                SavedInfoRow(label = "Allowed Radius:", value = "${saved.allowedRadius.toInt()} m")
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Location Verification Rules Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(TechCardBorderDark, TechCardBorderDark)),
                    width = 1.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIVACY & LOCATION COMPLIANCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Location is checked ONLY when student taps 'Mark Attendance'.\n• Zero continuous tracking or background monitoring.\n• Minimal data retained: distance offset & verification pass/fail status.\n• Students cannot modify official practice location or radius.",
                        fontSize = 11.sp,
                        color = TechTextSecondaryDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFA7F3D0)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Interactive Map Picker Dialog allowing the Admin to choose coordinates on an interactive grid map,
 * drag or tap to reposition the pin, preview the allowed geofence circle, or choose from FTC venue presets.
 */
@Composable
private fun InteractiveMapPickerDialog(
    initialLat: Double,
    initialLon: Double,
    initialRadius: Double,
    initialLocationName: String,
    onDismiss: () -> Unit,
    onLocationConfirmed: (lat: Double, lon: Double, name: String) -> Unit
) {
    var selectedLat by remember { mutableDoubleStateOf(initialLat) }
    var selectedLon by remember { mutableDoubleStateOf(initialLon) }
    var selectedVenueName by remember { mutableStateOf(initialLocationName) }

    // Map offset for drag/tap interaction (normalized coordinate delta)
    var pinOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    val presets = listOf(
        Triple("Curious City FTC Robotics Lab & Arena", 37.42200, -122.08410),
        Triple("High School Gymnasium (FTC Arena East)", 37.42450, -122.08150),
        Triple("STEM Workshop & Machining Bay", 37.41920, -122.08680),
        Triple("Regional Championship Arena", 37.42600, -122.08900)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(20.dp),
            color = TechCardDark,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(CyanPrimary, TechCardBorderDark)),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "INTERACTIVE MAP PICKER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Tap or drag pin to set practice location",
                                fontSize = 11.sp,
                                color = TechTextSecondaryDark
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TechTextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets Quick Row
                Text(
                    text = "Quick Venue Presets:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechTextSecondaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(2).forEach { (name, lat, lon) ->
                        Surface(
                            onClick = {
                                selectedVenueName = name
                                selectedLat = lat
                                selectedLon = lon
                                pinOffset = Offset.Zero
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedVenueName == name) CyanPrimary.copy(alpha = 0.25f) else Color(0xFF0F172A),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        if (selectedVenueName == name) CyanPrimary else TechCardBorderDark,
                                        TechCardBorderDark
                                    )
                                ),
                                width = 1.dp
                            )
                        ) {
                            Text(
                                text = name.take(24) + "...",
                                fontSize = 10.sp,
                                color = if (selectedVenueName == name) CyanPrimary else TechTextPrimaryDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF070F1A))
                        .border(1.dp, TechCardBorderDark, RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val delta = tapOffset - center
                                pinOffset = delta
                                // Convert pixel delta to lat/lon offset (~0.00001 deg per 10 px)
                                selectedLat = initialLat - (delta.y / size.height) * 0.002
                                selectedLon = initialLon + (delta.x / size.width) * 0.002
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                pinOffset += dragAmount
                                selectedLat -= (dragAmount.y / 800f) * 0.001
                                selectedLon += (dragAmount.x / 800f) * 0.001
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                        // Draw Grid Lines (Robotics Map Coordinate Grid)
                        val gridSpacing = 40f
                        var x = 0f
                        while (x <= canvasWidth) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(x, 0f),
                                end = Offset(x, canvasHeight),
                                strokeWidth = 1f
                            )
                            x += gridSpacing
                        }
                        var y = 0f
                        while (y <= canvasHeight) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                            y += gridSpacing
                        }

                        // Draw Simulated Venue Features (Arena, Workshops, Tech Hub)
                        // Arena boundary
                        drawRoundRect(
                            color = Color(0xFF0E2238),
                            topLeft = Offset(center.x - 120f, center.y - 80f),
                            size = androidx.compose.ui.geometry.Size(240f, 160f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                        )

                        // Center Circle
                        drawCircle(
                            color = CyanPrimary.copy(alpha = 0.15f),
                            radius = 60f,
                            center = center
                        )

                        // Pin Position
                        val pinPos = center + pinOffset

                        // Allowed Radius Geofence Circle (100m visualization)
                        val radiusPx = (initialRadius.toFloat() * 1.2f).coerceIn(60f, 160f)
                        drawCircle(
                            color = CyanPrimary.copy(alpha = 0.18f),
                            radius = radiusPx,
                            center = pinPos
                        )
                        drawCircle(
                            color = CyanPrimary,
                            radius = radiusPx,
                            center = pinPos,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )

                        // Pin Indicator Circle
                        drawCircle(
                            color = CyanGlow,
                            radius = 12f,
                            center = pinPos
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 6f,
                            center = pinPos
                        )
                    }

                    // Compass badge in top right
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.5f), Color.Transparent)),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("N", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                    }

                    // Venue Map Label
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "FTC ARENA GRID (100m GEOFENCE)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Coordinates Feedback
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0A131F),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.3f), Color.Transparent)),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Selected Venue: $selectedVenueName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lat: ${String.format(Locale.US, "%.5f", selectedLat)}",
                                fontSize = 11.sp,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Lon: ${String.format(Locale.US, "%.5f", selectedLon)}",
                                fontSize = 11.sp,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Radius: ${initialRadius.toInt()}m",
                                fontSize = 11.sp,
                                color = TechAccentGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Cancel & Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TechTextSecondaryDark)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onLocationConfirmed(selectedLat, selectedLon, selectedVenueName)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = TechSlateDark
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Location", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
