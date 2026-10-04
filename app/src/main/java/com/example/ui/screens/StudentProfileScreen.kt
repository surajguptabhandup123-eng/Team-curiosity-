package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TopicEntity
import com.example.service.FaceBiometricManager
import com.example.service.FaceVerificationResult
import com.example.ui.components.AdminEnrollFaceDialog
import com.example.ui.components.AdminTestFaceDialog
import com.example.ui.components.RoboticsCategoryChip
import com.example.ui.components.StatCard
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.theme.*

@Composable
fun StudentProfileScreen(
    student: StudentEntity,
    attendanceList: List<AttendanceEntity>,
    activities: List<ActivityEntity>,
    topics: List<TopicEntity>,
    onBack: () -> Unit,
    onEnrollFace: (String, Bitmap) -> Unit,
    onTestFace: ((String, Bitmap) -> FaceVerificationResult)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Attendance, 1=Activities, 2=Topics, 3=Progress
    var showFaceEnrollDialog by remember { mutableStateOf(false) }
    var showFaceTestDialog by remember { mutableStateOf(false) }
    var enrolledPhoto by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(student.studentId, student.faceEnrolled) {
        enrolledPhoto = FaceBiometricManager.getEnrolledFacePhoto(context, student.studentId)
    }

    val studentAttendance = attendanceList.filter { it.studentId == student.studentId }
    val studentActivities = activities.filter { it.studentId == student.studentId }
    val studentTopics = topics.filter { it.studentId == student.studentId || it.studentId == null }

    val totalSessions = studentAttendance.size
    val presentCount = studentAttendance.count { it.status == "Present" }
    val attendancePct = if (totalSessions > 0) (presentCount.toFloat() / totalSessions * 100).toInt() else 0
    val topicsCompleted = studentTopics.count { it.status == "Completed" }

    if (showFaceEnrollDialog) {
        AdminEnrollFaceDialog(
            student = student,
            onDismiss = { showFaceEnrollDialog = false },
            onSaveEnrollment = { bitmap ->
                enrolledPhoto = bitmap
                onEnrollFace(student.studentId, bitmap)
                showFaceEnrollDialog = false
            }
        )
    }

    if (showFaceTestDialog && onTestFace != null) {
        AdminTestFaceDialog(
            student = student,
            allStudents = emptyList(),
            onDismiss = { showFaceTestDialog = false },
            onTestFace = { bitmap ->
                onTestFace(student.studentId, bitmap)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechSlateDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TeamCuriosityLogoBadge(size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "STUDENT PROFILE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyanPrimary
                )
            }
        }

        // Student Card Hero
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F1A28))
                            .border(2.5.dp, if (student.faceEnrolled) TechAccentGreen else CyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (enrolledPhoto != null) {
                            androidx.compose.foundation.Image(
                                bitmap = enrolledPhoto!!.asImageBitmap(),
                                contentDescription = "${student.name}'s Enrolled Face",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = student.name.take(2).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = student.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TechTextPrimaryDark
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = student.role,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ID: ${student.studentId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TechTextSecondaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Joined: ${student.joiningDate} • ${student.email ?: "team@curiouscity.org"}",
                        fontSize = 11.sp,
                        color = TechTextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Face profile enrollment status & test
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showFaceEnrollDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (student.faceEnrolled) TechAccentGreen else CyanPrimary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(if (student.faceEnrolled) TechAccentGreen else CyanPrimary, Color.Transparent)
                                )
                            )
                        ) {
                            Icon(
                                imageVector = if (student.faceEnrolled) Icons.Default.CheckCircle else Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (student.faceEnrolled) "Face Enrolled ✓" else "Enroll Face",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (student.faceEnrolled && onTestFace != null) {
                            Button(
                                onClick = { showFaceTestDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0369A1),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Face", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Attendance",
                    value = "$attendancePct%",
                    icon = Icons.Default.Percent,
                    accentColor = TechAccentGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Sessions",
                    value = totalSessions.toString(),
                    icon = Icons.Default.Event,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Topics Done",
                    value = topicsCompleted.toString(),
                    icon = Icons.Default.School,
                    accentColor = TechAccentPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TechCardDark,
                contentColor = CyanPrimary,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Attendance", fontSize = 11.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Activities", fontSize = 11.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Topics", fontSize = 11.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Progress", fontSize = 11.sp) })
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Attendance History
                if (studentAttendance.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = TechCardDark)
                        ) {
                            Text("No attendance recorded for this student.", modifier = Modifier.padding(16.dp), color = TechTextSecondaryDark, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(studentAttendance) { att ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = TechCardDark)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(att.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TechTextPrimaryDark)
                                    Text("${att.time} • ${att.verificationMethod}", fontSize = 11.sp, color = TechTextSecondaryDark)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (att.status == "Present") TechAccentGreen.copy(alpha = 0.2f) else TechAccentYellow.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = att.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (att.status == "Present") TechAccentGreen else TechAccentYellow,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Activities History
                if (studentActivities.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = TechCardDark)
                        ) {
                            Text("No activities logged yet.", modifier = Modifier.padding(16.dp), color = TechTextSecondaryDark, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(studentActivities) { act ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = TechCardDark)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(act.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechTextPrimaryDark)
                                    RoboticsCategoryChip(category = act.category, isSelected = true)
                                }
                                Text(act.workDescription, fontSize = 12.sp, color = Color(0xFFF1F5F9), modifier = Modifier.padding(top = 4.dp))
                                if (act.learningDescription.isNotEmpty()) {
                                    Text("Learned: ${act.learningDescription}", fontSize = 11.sp, color = CyanPrimary, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Topics
                items(studentTopics) { top ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TechCardDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(top.topicName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TechTextPrimaryDark)
                                Text(top.category, fontSize = 10.sp, color = CyanPrimary)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (top.status == "Completed") TechAccentGreen.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = top.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (top.status == "Completed") TechAccentGreen else CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // Progress / FTC Technical Skills Matrix
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TechCardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "FTC ROBOTICS READINESS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            SkillProgressRow(title = "Safety & Protocol Compliance", progress = 0.95f)
                            SkillProgressRow(title = "Domain Mastery (${student.role})", progress = 0.82f)
                            SkillProgressRow(title = "Autonomous Routine Execution", progress = 0.70f)
                            SkillProgressRow(title = "Engineering Notebook Logging", progress = (studentActivities.size * 0.2f).coerceIn(0.1f, 1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillProgressRow(title: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 11.sp, color = TechTextPrimaryDark)
            Text("${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CyanPrimary,
            trackColor = Color(0xFF1E293B)
        )
    }
}
