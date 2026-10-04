package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.components.RoboticsCategoryChip
import com.example.ui.components.StatCard
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    currentRole: UserRole,
    currentStudent: StudentEntity?,
    students: List<StudentEntity>,
    todayAttendance: List<AttendanceEntity>,
    activities: List<ActivityEntity>,
    topics: List<TopicEntity>,
    currentDate: String,
    onNavigate: (String) -> Unit,
    onSwitchRole: () -> Unit
) {
    val totalStudents = students.size
    val presentCount = todayAttendance.count { it.status == "Present" }
    val lateCount = todayAttendance.count { it.status == "Late" }
    val absentCount = (totalStudents - presentCount - lateCount).coerceAtLeast(0)
    val topicsCoveredCount = topics.count { it.status == "Completed" }
    val activitiesCompletedCount = activities.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechSlateDark),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            TeamCuriosityHeader(
                currentRole = currentRole,
                studentName = currentStudent?.name,
                onOpenSettingsClick = if (currentRole == UserRole.ADMIN_MENTOR) { { onNavigate("admin_settings") } } else null,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Hero Card with Official Logo & Date
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(CyanPrimary.copy(alpha = 0.5f), Color(0xFF1E3A5A))
                    ),
                    width = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(2.dp, CyanGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.team_curiosity_logo),
                            contentDescription = "Team Curiosity Circular Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FTC ROBOTICS LAB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "Daily Practice Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TechTextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TechTextSecondaryDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Today: $currentDate",
                                fontSize = 12.sp,
                                color = TechTextSecondaryDark
                            )
                        }
                    }
                }
            }
        }

        // Metrics Grid (2 columns x 3 rows)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "TEAM METRICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyanPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Students",
                        value = totalStudents.toString(),
                        icon = Icons.Default.Group,
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Present Today",
                        value = presentCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = TechAccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Absent Today",
                        value = absentCount.toString(),
                        icon = Icons.Default.PersonOff,
                        accentColor = TechAccentRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Late Today",
                        value = lateCount.toString(),
                        icon = Icons.Default.Schedule,
                        accentColor = TechAccentYellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Topics Covered",
                        value = topicsCoveredCount.toString(),
                        icon = Icons.Default.MenuBook,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Activities Done",
                        value = activitiesCompletedCount.toString(),
                        icon = Icons.Default.Build,
                        accentColor = TechAccentPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Navigation Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyanPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionPill(
                            title = "Mark Attendance",
                            icon = Icons.Default.Fingerprint,
                            accent = CyanPrimary,
                            onClick = { onNavigate("attendance") }
                        )
                    }
                    if (currentRole == UserRole.ADMIN_MENTOR) {
                        item {
                            QuickActionPill(
                                title = "Students",
                                icon = Icons.Default.People,
                                accent = Color(0xFF38BDF8),
                                onClick = { onNavigate("students") }
                            )
                        }
                    }
                    item {
                        QuickActionPill(
                            title = "Today's Work",
                            icon = Icons.Default.Assignment,
                            accent = TechAccentYellow,
                            onClick = { onNavigate("daily_work") }
                        )
                    }
                    item {
                        QuickActionPill(
                            title = "Topics",
                            icon = Icons.Default.Lightbulb,
                            accent = TechAccentGreen,
                            onClick = { onNavigate("topics") }
                        )
                    }
                    item {
                        QuickActionPill(
                            title = "Daily Team Report",
                            icon = Icons.Default.Summarize,
                            accent = TechAccentPurple,
                            onClick = { onNavigate("daily_report") }
                        )
                    }
                    if (currentRole == UserRole.ADMIN_MENTOR) {
                        item {
                            QuickActionPill(
                                title = "Settings → Attendance Location",
                                icon = Icons.Default.LocationOn,
                                accent = CyanPrimary,
                                onClick = { onNavigate("admin_settings") }
                            )
                        }
                        item {
                            QuickActionPill(
                                title = "Reports & Export",
                                icon = Icons.Default.Share,
                                accent = Color(0xFFF43F5E),
                                onClick = { onNavigate("reports_export") }
                            )
                        }
                    }
                }
            }
        }

        // Admin-Only Direct Geofence Card
        if (currentRole == UserRole.ADMIN_MENTOR) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(CyanPrimary.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Settings → Attendance Location",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                                Text(
                                    text = "Configure official GPS coordinates & geofence",
                                    fontSize = 10.sp,
                                    color = TechTextSecondaryDark
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigate("admin_settings") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = TechSlateDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recent Activity Feed
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CyanPrimary
                    )
                    TextButton(onClick = { onNavigate("daily_work") }) {
                        Text("View All", fontSize = 12.sp, color = CyanPrimary)
                    }
                }
            }
        }

        if (activities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark)
                ) {
                    Text(
                        text = "No activities logged yet today. Click 'Today's Work' to log practice progress.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 13.sp,
                        color = TechTextSecondaryDark
                    )
                }
            }
        } else {
            items(activities.take(5)) { act ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(TechCardBorderDark, TechCardBorderDark)
                        ),
                        width = 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = act.studentName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            RoboticsCategoryChip(category = act.category, isSelected = true)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = act.workDescription,
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )

                        if (act.learningDescription.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Learned: ${act.learningDescription}",
                                fontSize = 12.sp,
                                color = CyanPrimaryVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${act.date} • ${act.startTime} - ${act.endTime}",
                                fontSize = 11.sp,
                                color = TechTextSecondaryDark
                            )
                            if (act.completedTask.isNotEmpty()) {
                                Text(
                                    text = "✓ ${act.completedTask.take(24)}...",
                                    fontSize = 11.sp,
                                    color = TechAccentGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    title: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = TechCardDark,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(accent.copy(alpha = 0.6f), TechCardBorderDark)
            ),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TechTextPrimaryDark
            )
        }
    }
}
