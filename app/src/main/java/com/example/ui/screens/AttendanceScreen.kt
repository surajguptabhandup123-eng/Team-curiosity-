package com.example.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.PracticeLocationEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.service.FaceVerificationResult
import com.example.ui.components.CameraFaceDialog
import com.example.ui.components.StudentFaceVerificationDialog
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.components.VerificationStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TeamViewModel

@Composable
fun AttendanceScreen(
    currentRole: UserRole,
    currentStudent: StudentEntity?,
    students: List<StudentEntity>,
    todayAttendance: List<AttendanceEntity>,
    practiceLocation: PracticeLocationEntity?,
    isVerifying: Boolean,
    verificationMessage: String?,
    verificationError: String?,
    lastDistance: Float?,
    lastFaceStatus: String?,
    lastLocationStatus: Boolean?,
    onMarkAttendance: (context: Context, faceResult: FaceVerificationResult, testMode: TeamViewModel.LocationTestMode) -> Unit,
    onAdminOverride: (studentId: String, studentName: String, status: String, notes: String) -> Unit,
    onClearVerificationState: () -> Unit,
    onSwitchRole: () -> Unit
) {
    val context = LocalContext.current
    var showFaceDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var selectedOverrideStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var overrideStatus by remember { mutableStateOf("Present") }
    var overrideNotes by remember { mutableStateOf("") }
    var locationTestMode by remember { mutableStateOf(TeamViewModel.LocationTestMode.DEVICE_GPS) }

    // Runtime Permission for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        onMarkAttendance(
            context,
            FaceVerificationResult.Match(similarity = 0.95f),
            locationTestMode
        )
    }

    if (showFaceDialog && currentStudent != null) {
        StudentFaceVerificationDialog(
            student = currentStudent,
            allStudents = students,
            onDismiss = { showFaceDialog = false },
            onVerificationSuccess = { signature ->
                showFaceDialog = false
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA
                    )
                )
            },
            onVerificationFailed = { errorMsg ->
                showFaceDialog = false
                onMarkAttendance(
                    context,
                    FaceVerificationResult.NoMatch(
                        similarity = 0.35f,
                        message = errorMsg
                    ),
                    locationTestMode
                )
            },
            onManualOverride = {
                showFaceDialog = false
                selectedOverrideStudent = currentStudent
                showManualDialog = true
            }
        )
    }

    if (showManualDialog && selectedOverrideStudent != null) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            containerColor = TechCardDark,
            title = {
                Text(
                    text = "Mentor Manual Attendance Override",
                    color = CyanPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Student: ${selectedOverrideStudent?.name} (${selectedOverrideStudent?.studentId})",
                        color = TechTextPrimaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Status:", fontSize = 12.sp, color = TechTextSecondaryDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Present", "Late", "Absent", "Excused").forEach { st ->
                            FilterChip(
                                selected = overrideStatus == st,
                                onClick = { overrideStatus = st },
                                label = { Text(st, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = overrideNotes,
                        onValueChange = { overrideNotes = it },
                        label = { Text("Reason / Override Note") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TechCardBorderDark,
                            focusedTextColor = TechTextPrimaryDark,
                            unfocusedTextColor = TechTextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = selectedOverrideStudent ?: return@Button
                        onAdminOverride(s.studentId, s.name, overrideStatus, overrideNotes)
                        showManualDialog = false
                        overrideNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Text("Save Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("Cancel", color = TechTextSecondaryDark)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechSlateDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            TeamCuriosityHeader(
                currentRole = currentRole,
                studentName = currentStudent?.name,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Student Verification Flow Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TeamCuriosityLogoBadge(size = 38.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ATTENDANCE VERIFICATION",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = CyanPrimary
                                )
                                Text(
                                    text = "Curious City FTC Practice",
                                    fontSize = 11.sp,
                                    color = TechTextSecondaryDark
                                )
                            }
                        }

                        // Practice Lab Radius indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.3f), Color.Transparent)),
                                width = 1.dp
                            )
                        ) {
                            Text(
                                text = "Radius: ${practiceLocation?.allowedRadius?.toInt() ?: 100}m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step 2: Current Logged In Student Identity Info
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0A131F),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(TechCardBorderDark, TechCardBorderDark)),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, CyanPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentStudent?.name ?: "No Student Selected",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TechTextPrimaryDark
                                )
                                Text(
                                    text = "ID: ${currentStudent?.studentId ?: "N/A"} • Role: ${currentStudent?.role ?: "N/A"}",
                                    fontSize = 11.sp,
                                    color = TechTextSecondaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Steps Checklist
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VerificationStepRow(
                            stepNumber = "1",
                            title = "Face Verification",
                            status = when (lastFaceStatus) {
                                "PASS" -> "Face Verified ✓"
                                "Failed" -> "Face Verification Failed ✕"
                                "Manual" -> "Manual Override ✓"
                                else -> "Required"
                            },
                            isSuccess = lastFaceStatus == "PASS" || lastFaceStatus == "Manual"
                        )

                        VerificationStepRow(
                            stepNumber = "2",
                            title = "Location Verification",
                            status = when (lastLocationStatus) {
                                true -> "Location Verified ✓"
                                false -> "Location Verification Failed ✕"
                                else -> "Within ${practiceLocation?.allowedRadius?.toInt() ?: 100}m"
                            },
                            isSuccess = lastLocationStatus == true
                        )

                        VerificationStepRow(
                            stepNumber = "3",
                            title = "Attendance Status",
                            status = if (lastFaceStatus == "PASS" && lastLocationStatus == true && verificationMessage != null) {
                                "Attendance Marked Successfully ✓"
                            } else if (lastFaceStatus == "Failed" || lastLocationStatus == false) {
                                "Attendance = NOT MARKED"
                            } else {
                                "Pending Verification"
                            },
                            isSuccess = lastFaceStatus == "PASS" && lastLocationStatus == true && verificationMessage != null
                        )
                    }

                    // Face Verification Failure Banner
                    if (lastFaceStatus == "Failed") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TechAccentRed.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(TechAccentRed, Color.Transparent)),
                                width = 1.dp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = TechAccentRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "NO MATCH → Face Verification Failed ✕",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechAccentRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Face verification failed.\nThis face does not match the registered face for this student.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFCA5A5)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Attendance = NOT MARKED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                    }

                    // Distance & Radius Card (Always shown when evaluated)
                    if (lastDistance != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        if (lastLocationStatus == true) TechAccentGreen else TechAccentRed,
                                        Color.Transparent
                                    )
                                ),
                                width = 1.dp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Distance from practice location: ${lastDistance.toInt()} meters",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lastLocationStatus == true) TechAccentGreen else TechAccentRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Allowed radius: ${practiceLocation?.allowedRadius?.toInt() ?: 100} meters",
                                        fontSize = 11.sp,
                                        color = TechTextSecondaryDark
                                    )
                                    Text(
                                        text = if (lastLocationStatus == true) "INSIDE RADIUS ✓" else "OUTSIDE RADIUS ✕",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lastLocationStatus == true) TechAccentGreen else TechAccentRed
                                    )
                                }
                            }
                        }
                    }

                    // Success Message Banner (When both verification checks pass)
                    if (lastFaceStatus == "PASS" && lastLocationStatus == true && verificationMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TechAccentGreen.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(TechAccentGreen, Color.Transparent)),
                                width = 1.dp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TechAccentGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Attendance Marked Successfully ✓",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechAccentGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Student: ${currentStudent?.name} (${currentStudent?.studentId})",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA7F3D0)
                                )
                                Text(
                                    text = "Face Verification = PASS  •  Location Verification = PASS  •  Attendance = PRESENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TechAccentGreen
                                )
                            }
                        }
                    }

                    // Failed Message Banner (When location is outside allowed area)
                    if (lastLocationStatus == false) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TechAccentRed.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(TechAccentRed, Color.Transparent)),
                                width = 1.dp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = TechAccentRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Location Verification Failed ✕",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TechAccentRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "You are outside the Team Curiosity practice area.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFCA5A5)
                                )
                                Text(
                                    text = "Attendance cannot be marked from this location.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFCA5A5)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Attendance = NOT MARKED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFCA5A5)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedOverrideStudent = currentStudent
                                        showManualDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF334155),
                                        contentColor = CyanPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mentor Manual Attendance Option (If GPS Issue)", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Mode Selector (Physical GPS vs Inside FTC Lab vs Outside FTC Area)
                    Text(
                        text = "Location Testing Mode:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechTextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = locationTestMode == TeamViewModel.LocationTestMode.DEVICE_GPS,
                            onClick = { locationTestMode = TeamViewModel.LocationTestMode.DEVICE_GPS },
                            label = { Text("Device GPS", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                        FilterChip(
                            selected = locationTestMode == TeamViewModel.LocationTestMode.SIMULATED_INSIDE_PRACTICE_AREA,
                            onClick = { locationTestMode = TeamViewModel.LocationTestMode.SIMULATED_INSIDE_PRACTICE_AREA },
                            label = { Text("At Lab (Pass)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TechAccentGreen.copy(alpha = 0.25f),
                                selectedLabelColor = TechAccentGreen
                            )
                        )
                        FilterChip(
                            selected = locationTestMode == TeamViewModel.LocationTestMode.SIMULATED_OUTSIDE_PRACTICE_AREA,
                            onClick = { locationTestMode = TeamViewModel.LocationTestMode.SIMULATED_OUTSIDE_PRACTICE_AREA },
                            label = { Text("Outside (Fail)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TechAccentRed.copy(alpha = 0.25f),
                                selectedLabelColor = TechAccentRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 1: "Mark Attendance" Action Button
                    Button(
                        onClick = {
                            onClearVerificationState()
                            showFaceDialog = true
                        },
                        enabled = !isVerifying && currentStudent != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = TechSlateDark
                        )
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TechSlateDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying Identity & GPS...")
                        } else {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Attendance", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }

                    // Manual Admin/Mentor Attendance Option (Always available below)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            selectedOverrideStudent = currentStudent ?: students.firstOrNull()
                            showManualDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Manual Admin/Mentor Attendance Option (If GPS Unavailable)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Mentor Manual Override Section (Admin Only)
        if (currentRole == UserRole.ADMIN_MENTOR) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "MENTOR ATTENDANCE OVERRIDE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CyanPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TechCardDark),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(TechCardBorderDark, TechCardBorderDark)),
                            width = 1.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Mentors can manually record or correct attendance status if student faces technical difficulties or GPS lock delay.",
                                fontSize = 11.sp,
                                color = TechTextSecondaryDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 160.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(students) { s ->
                                    val isMarked = todayAttendance.any { it.studentId == s.studentId }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F1A28))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(s.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechTextPrimaryDark)
                                            Text(s.studentId, fontSize = 10.sp, color = TechTextSecondaryDark)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isMarked) {
                                                val record = todayAttendance.first { it.studentId == s.studentId }
                                                Text(record.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (record.status == "Present") TechAccentGreen else TechAccentYellow)
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            FilledTonalButton(
                                                onClick = {
                                                    selectedOverrideStudent = s
                                                    showManualDialog = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(if (isMarked) "Edit" else "Override", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Attendance Records List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "TODAY'S ATTENDANCE LOG (${todayAttendance.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyanPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        if (todayAttendance.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark)
                ) {
                    Text(
                        text = "No attendance recorded yet today. Students can mark attendance above.",
                        fontSize = 12.sp,
                        color = TechTextSecondaryDark,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(todayAttendance) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                when (record.status) {
                                    "Present" -> TechAccentGreen.copy(alpha = 0.4f)
                                    "Late" -> TechAccentYellow.copy(alpha = 0.4f)
                                    else -> TechAccentRed.copy(alpha = 0.4f)
                                },
                                TechCardBorderDark
                            )
                        ),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (record.status) {
                                        "Present" -> TechAccentGreen.copy(alpha = 0.15f)
                                        "Late" -> TechAccentYellow.copy(alpha = 0.15f)
                                        else -> TechAccentRed.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (record.status) {
                                    "Present" -> Icons.Default.CheckCircle
                                    "Late" -> Icons.Default.Schedule
                                    else -> Icons.Default.Close
                                },
                                contentDescription = null,
                                tint = when (record.status) {
                                    "Present" -> TechAccentGreen
                                    "Late" -> TechAccentYellow
                                    else -> TechAccentRed
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.studentName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "${record.time} • ${record.verificationMethod}",
                                    fontSize = 11.sp,
                                    color = TechTextSecondaryDark
                                )
                                if (record.distanceMeters > 0f) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${String.format(java.util.Locale.US, "%.0f", record.distanceMeters)}m",
                                        fontSize = 11.sp,
                                        color = CyanPrimary
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (record.status) {
                                "Present" -> TechAccentGreen.copy(alpha = 0.2f)
                                "Late" -> TechAccentYellow.copy(alpha = 0.2f)
                                else -> TechAccentRed.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = record.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (record.status) {
                                    "Present" -> TechAccentGreen
                                    "Late" -> TechAccentYellow
                                    else -> TechAccentRed
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationStepRow(
    stepNumber: String,
    title: String,
    status: String,
    isSuccess: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 12.sp, color = TechTextPrimaryDark)
        }

        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSuccess) TechAccentGreen else TechTextSecondaryDark
        )
    }
}
