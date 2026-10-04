package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ActivityEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserRole
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.theme.*

@Composable
fun DailyTeamReportScreen(
    currentRole: UserRole,
    currentDate: String,
    students: List<StudentEntity>,
    todayAttendance: List<AttendanceEntity>,
    activities: List<ActivityEntity>,
    topics: List<TopicEntity>,
    onExportPdf: (Context) -> Unit,
    onExportCsv: (Context) -> Unit,
    onSwitchRole: () -> Unit
) {
    val context = LocalContext.current
    val totalStudents = students.size
    val presentCount = todayAttendance.count { it.status == "Present" }
    val lateCount = todayAttendance.count { it.status == "Late" }
    val absentCount = (totalStudents - presentCount - lateCount).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechSlateDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            TeamCuriosityHeader(
                currentRole = currentRole,
                studentName = null,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Official Daily Team Report Card
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
                    // Header with Official Circular Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(2.dp, CyanGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.team_curiosity_logo),
                                contentDescription = "Team Curiosity Official Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "DAILY TEAM REPORT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = CyanPrimary
                            )
                            Text(
                                text = "TEAM CURIOSITY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            Text(
                                text = "CURIOUS CITY FTC • Practice: $currentDate",
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Attendance Breakdown Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0C1724),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(TechCardBorderDark, TechCardBorderDark)),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ReportMetricItem("Total Students", totalStudents.toString(), TechTextPrimaryDark)
                            ReportMetricItem("Present", presentCount.toString(), TechAccentGreen)
                            ReportMetricItem("Late", lateCount.toString(), TechAccentYellow)
                            ReportMetricItem("Absent", absentCount.toString(), TechAccentRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Topics Covered Section
                    ReportSectionHeader(title = "TOPICS COVERED TODAY", icon = Icons.Default.MenuBook)
                    if (topics.isEmpty()) {
                        Text("No topics logged today.", fontSize = 11.sp, color = TechTextSecondaryDark)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            topics.take(4).forEach { t ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${t.topicName} (${t.category}) - ${t.status}",
                                        fontSize = 12.sp,
                                        color = TechTextPrimaryDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Team Work Section
                    ReportSectionHeader(title = "TEAM WORK (WHAT THE TEAM WORKED ON)", icon = Icons.Default.Engineering)
                    if (activities.isEmpty()) {
                        Text("Autonomous path tuning and intake CAD refinement.", fontSize = 12.sp, color = Color(0xFFE2E8F0))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            activities.forEach { act ->
                                Text(
                                    text = "• ${act.studentName} (${act.category}): ${act.workDescription}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Achievements Section
                    ReportSectionHeader(title = "ACHIEVEMENTS (COMPLETED)", icon = Icons.Default.EmojiEvents)
                    val completedTasks = activities.map { it.completedTask }.filter { it.isNotBlank() }
                    if (completedTasks.isEmpty()) {
                        Text("• Completed tele-op sample cycle drills.\n• Verified gyro IMU calibration for field-centric drive.", fontSize = 11.sp, color = TechAccentGreen)
                    } else {
                        completedTasks.forEach { task ->
                            Text(text = "✓ $task", fontSize = 11.sp, color = TechAccentGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Problems Faced
                    ReportSectionHeader(title = "PROBLEMS / CHALLENGES FACED", icon = Icons.Default.Warning)
                    val challenges = activities.map { it.challenges }.filter { it.isNotBlank() }
                    if (challenges.isEmpty()) {
                        Text("No critical hardware issues encountered today.", fontSize = 11.sp, color = TechTextSecondaryDark)
                    } else {
                        challenges.forEach { ch ->
                            Text(text = "⚠ $ch", fontSize = 11.sp, color = Color(0xFFFCA5A5))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Solutions Tried
                    ReportSectionHeader(title = "SOLUTIONS TRIED", icon = Icons.Default.Lightbulb)
                    val solutions = activities.map { it.solution }.filter { it.isNotBlank() }
                    if (solutions.isEmpty()) {
                        Text("Added spring tensioners and software deadband filtering.", fontSize = 11.sp, color = Color(0xFFCCFBF1))
                    } else {
                        solutions.forEach { sol ->
                            Text(text = "✦ $sol", fontSize = 11.sp, color = Color(0xFFCCFBF1))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next Session Tasks
                    ReportSectionHeader(title = "NEXT SESSION PLAN", icon = Icons.Default.ArrowForward)
                    val pending = activities.map { it.pendingTask }.filter { it.isNotBlank() }
                    if (pending.isEmpty()) {
                        Text("• Alliance partner coordination practice\n• Continuous slide endurance test\n• Full autonomous 30-sec scrimmage simulation", fontSize = 11.sp, color = TechAccentYellow)
                    } else {
                        pending.forEach { p ->
                            Text(text = "→ $p", fontSize = 11.sp, color = TechAccentYellow)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Export Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onExportPdf(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onExportCsv(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(CyanPrimary, CyanPrimary))
                            )
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = valueColor)
        Text(label, fontSize = 10.sp, color = TechTextSecondaryDark)
    }
}

@Composable
private fun ReportSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = CyanPrimary
        )
    }
}
