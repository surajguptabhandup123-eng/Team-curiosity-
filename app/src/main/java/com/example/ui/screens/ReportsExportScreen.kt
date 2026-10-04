package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.theme.*

@Composable
fun ReportsExportScreen(
    currentRole: UserRole,
    students: List<StudentEntity>,
    allAttendance: List<AttendanceEntity>,
    currentDate: String,
    onExportPdf: (Context, String) -> Unit,
    onExportDailyCsv: (Context) -> Unit,
    onExportMonthlyCsv: (Context, String) -> Unit,
    onExportActivitiesCsv: (Context) -> Unit,
    onSwitchRole: () -> Unit
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(currentDate.take(7)) } // YYYY-MM
    var searchQuery by remember { mutableStateOf("") }

    val monthlyAttendance = allAttendance.filter { it.date.startsWith(selectedMonth) }

    val studentStats = students.map { s ->
        val att = monthlyAttendance.filter { it.studentId == s.studentId }
        val total = att.size
        val present = att.count { it.status == "Present" }
        val absent = att.count { it.status == "Absent" }
        val late = att.count { it.status == "Late" }
        val pct = if (total > 0) (present.toFloat() / total * 100).toInt() else 0
        StudentMonthlyStat(
            studentId = s.studentId,
            name = s.name,
            role = s.role,
            totalDays = total,
            presentDays = present,
            absentDays = absent,
            lateDays = late,
            percentage = pct
        )
    }.filter {
        searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.studentId.contains(searchQuery, ignoreCase = true)
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
                studentName = null,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Export Actions Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(CyanPrimary.copy(alpha = 0.5f), TechCardBorderDark)
                    ),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCuriosityLogoBadge(size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "REPORT EXPORTS (PDF & CSV)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Export team documentation for mentors and sponsors",
                                fontSize = 11.sp,
                                color = TechTextSecondaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export Buttons Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onExportPdf(context, "OFFICIAL PRACTICE REPORT") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onExportDailyCsv(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Daily CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onExportMonthlyCsv(context, selectedMonth) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Monthly CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onExportActivitiesCsv(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TechAccentYellow)
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activity CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Monthly Attendance Section (Requirement 12)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONTHLY ATTENDANCE DASHBOARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CyanPrimary
                    )
                    Text(
                        text = "Month: $selectedMonth",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TechTextSecondaryDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter students by name or ID...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechTextSecondaryDark) },
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
        }

        // Student Monthly Attendance Table Cards
        items(studentStats) { stat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            if (stat.percentage >= 80) TechAccentGreen.copy(alpha = 0.3f) else TechAccentYellow.copy(alpha = 0.3f),
                            TechCardBorderDark
                        )
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
                        Column {
                            Text(
                                text = stat.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            Text(
                                text = "${stat.studentId} • ${stat.role}",
                                fontSize = 11.sp,
                                color = TechTextSecondaryDark
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (stat.percentage >= 80) TechAccentGreen.copy(alpha = 0.2f) else TechAccentYellow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${stat.percentage}% Attendance",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stat.percentage >= 80) TechAccentGreen else TechAccentYellow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill("Total", "${stat.totalDays} sessions", TechTextPrimaryDark)
                        StatPill("Present", "${stat.presentDays}", TechAccentGreen)
                        StatPill("Late", "${stat.lateDays}", TechAccentYellow)
                        StatPill("Absent", "${stat.absentDays}", TechAccentRed)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (stat.percentage >= 80) TechAccentGreen else TechAccentYellow,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, fontSize = 10.sp, color = TechTextSecondaryDark)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

private data class StudentMonthlyStat(
    val studentId: String,
    val name: String,
    val role: String,
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val percentage: Int
)
