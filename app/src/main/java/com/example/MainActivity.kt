package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.TeamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TeamCuriosityApp()
            }
        }
    }
}

@Composable
fun TeamCuriosityApp(viewModel: TeamViewModel = viewModel()) {
    var showSplash by remember { mutableStateOf(true) }
    var showLogin by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf("home") }
    var detailStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var isViewingDailyReport by remember { mutableStateOf(false) }
    var isViewingAdminSettings by remember { mutableStateOf(false) }

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentStudent by viewModel.currentStudent.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val todayAttendance by viewModel.todayAttendance.collectAsStateWithLifecycle()
    val activities by viewModel.allActivities.collectAsStateWithLifecycle()
    val topics by viewModel.allTopics.collectAsStateWithLifecycle()
    val practiceLocation by viewModel.practiceLocation.collectAsStateWithLifecycle()

    val isVerifying by viewModel.isVerifying.collectAsStateWithLifecycle()
    val verificationMessage by viewModel.verificationMessage.collectAsStateWithLifecycle()
    val verificationError by viewModel.verificationError.collectAsStateWithLifecycle()
    val lastDistance by viewModel.lastDistance.collectAsStateWithLifecycle()
    val lastFaceStatus by viewModel.lastFaceStatus.collectAsStateWithLifecycle()
    val lastLocationStatus by viewModel.lastLocationStatus.collectAsStateWithLifecycle()

    // Handle System Back Button
    BackHandler(enabled = isViewingDailyReport || isViewingAdminSettings || detailStudent != null || showLogin || currentTab != "home") {
        when {
            detailStudent != null -> detailStudent = null
            isViewingDailyReport -> isViewingDailyReport = false
            isViewingAdminSettings -> isViewingAdminSettings = false
            showLogin -> showLogin = false
            currentTab != "home" -> currentTab = "home"
        }
    }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    if (showLogin) {
        LoginScreen(
            students = students,
            onSelectRole = { role, student ->
                viewModel.setRole(role, student)
                showLogin = false
                currentTab = "home"
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TechSlateDark,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            if (!isViewingDailyReport && !isViewingAdminSettings && detailStudent == null) {
                NavigationBar(
                    containerColor = TechCardDark,
                    contentColor = CyanPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    if (currentRole == UserRole.STUDENT) {
                        // Student Bottom Navigation: HOME, ATTENDANCE, ACTIVITY, TOPICS, PROFILE
                        NavigationBarItem(
                            selected = currentTab == "home",
                            onClick = { currentTab = "home" },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("HOME", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == "attendance",
                            onClick = { currentTab = "attendance" },
                            icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Attendance") },
                            label = { Text("ATTENDANCE", fontSize = 9.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_attendance")
                        )
                        NavigationBarItem(
                            selected = currentTab == "activity",
                            onClick = { currentTab = "activity" },
                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Activity") },
                            label = { Text("ACTIVITY", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_activity")
                        )
                        NavigationBarItem(
                            selected = currentTab == "topics",
                            onClick = { currentTab = "topics" },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Topics") },
                            label = { Text("TOPICS", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_topics")
                        )
                        NavigationBarItem(
                            selected = currentTab == "profile",
                            onClick = { currentTab = "profile" },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("PROFILE", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    } else {
                        // Admin Bottom Navigation: HOME, ATTENDANCE, STUDENTS, ACTIVITY, REPORTS
                        NavigationBarItem(
                            selected = currentTab == "home",
                            onClick = { currentTab = "home" },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("HOME", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == "attendance",
                            onClick = { currentTab = "attendance" },
                            icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Attendance") },
                            label = { Text("ATTENDANCE", fontSize = 9.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_attendance")
                        )
                        NavigationBarItem(
                            selected = currentTab == "students",
                            onClick = { currentTab = "students" },
                            icon = { Icon(Icons.Default.Group, contentDescription = "Students") },
                            label = { Text("STUDENTS", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_students")
                        )
                        NavigationBarItem(
                            selected = currentTab == "activity",
                            onClick = { currentTab = "activity" },
                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Activity") },
                            label = { Text("ACTIVITY", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_activity")
                        )
                        NavigationBarItem(
                            selected = currentTab == "reports",
                            onClick = { currentTab = "reports" },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                            label = { Text("REPORTS", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = TechTextSecondaryDark,
                                unselectedTextColor = TechTextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_reports")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                detailStudent != null -> {
                    StudentProfileScreen(
                        student = detailStudent!!,
                        attendanceList = allAttendance,
                        activities = activities,
                        topics = topics,
                        onBack = { detailStudent = null },
                        onEnrollFace = { sid, bmp -> viewModel.enrollStudentFace(sid, bitmap = bmp) },
                        onTestFace = { sid, bmp -> viewModel.verifyStudentFace(sid, bmp) }
                    )
                }

                isViewingDailyReport -> {
                    DailyTeamReportScreen(
                        currentRole = currentRole,
                        currentDate = viewModel.currentDateString,
                        students = students,
                        todayAttendance = todayAttendance,
                        activities = activities,
                        topics = topics,
                        onExportPdf = { ctx -> viewModel.exportPdfReport(ctx, "DAILY TEAM REPORT") },
                        onExportCsv = { ctx -> viewModel.exportDailyAttendanceCsv(ctx) },
                        onSwitchRole = { showLogin = true }
                    )
                }

                isViewingAdminSettings -> {
                    AdminSettingsScreen(
                        currentRole = currentRole,
                        practiceLocation = practiceLocation,
                        onSaveLocation = { lat, lon, rad, name ->
                            viewModel.updatePracticeLocation(lat, lon, rad, name)
                        },
                        onBack = { isViewingAdminSettings = false },
                        onSwitchRole = { showLogin = true }
                    )
                }

                currentTab == "home" -> {
                    DashboardScreen(
                        currentRole = currentRole,
                        currentStudent = currentStudent,
                        students = students,
                        todayAttendance = todayAttendance,
                        activities = activities,
                        topics = topics,
                        currentDate = viewModel.currentDateString,
                        onNavigate = { destination ->
                            when (destination) {
                                "attendance" -> currentTab = "attendance"
                                "students" -> currentTab = "students"
                                "daily_work" -> currentTab = "activity"
                                "topics" -> currentTab = if (currentRole == UserRole.STUDENT) "topics" else "activity"
                                "daily_report" -> isViewingDailyReport = true
                                "reports_export" -> currentTab = "reports"
                                "admin_settings" -> isViewingAdminSettings = true
                            }
                        },
                        onSwitchRole = { showLogin = true }
                    )
                }

                currentTab == "attendance" -> {
                    AttendanceScreen(
                        currentRole = currentRole,
                        currentStudent = currentStudent,
                        students = students,
                        todayAttendance = todayAttendance,
                        practiceLocation = practiceLocation,
                        isVerifying = isVerifying,
                        verificationMessage = verificationMessage,
                        verificationError = verificationError,
                        lastDistance = lastDistance,
                        lastFaceStatus = lastFaceStatus,
                        lastLocationStatus = lastLocationStatus,
                        onMarkAttendance = { ctx, faceStatus, testMode ->
                            viewModel.markAttendance(ctx, faceStatus, testMode)
                        },
                        onAdminOverride = { sid, sname, st, notes ->
                            viewModel.adminManualAttendanceOverride(sid, sname, st, notes)
                        },
                        onClearVerificationState = { viewModel.clearVerificationState() },
                        onSwitchRole = { showLogin = true }
                    )
                }

                currentTab == "students" -> {
                    StudentsScreen(
                        currentRole = currentRole,
                        students = students,
                        onAddStudent = { name, sid, role, phone, email ->
                            viewModel.addStudent(name, sid, role, phone, email)
                        },
                        onDeleteStudent = { student -> viewModel.deleteStudent(student) },
                        onEnrollFace = { sid, bmp -> viewModel.enrollStudentFace(sid, bitmap = bmp) },
                        onTestFace = { sid, bmp -> viewModel.verifyStudentFace(sid, bmp) },
                        onSelectStudentProfile = { student -> detailStudent = student },
                        onSwitchRole = { showLogin = true }
                    )
                }

                currentTab == "activity" -> {
                    DailyWorkScreen(
                        currentRole = currentRole,
                        currentStudent = currentStudent,
                        students = students,
                        activities = activities,
                        currentDate = viewModel.currentDateString,
                        onAddActivity = { sid, sname, st, et, cat, work, learn, comp, pend, chal, sol, notes ->
                            viewModel.addDailyActivity(sid, sname, st, et, cat, work, learn, comp, pend, chal, sol, notes)
                        },
                        onDeleteActivity = { act -> viewModel.deleteActivity(act) },
                        onSwitchRole = { showLogin = true }
                    )
                }

                currentTab == "topics" -> {
                    TopicsScreen(
                        currentRole = currentRole,
                        currentStudent = currentStudent,
                        students = students,
                        topics = topics,
                        onAddTopic = { name, cat, sid, sname, st, notes ->
                            viewModel.addTopic(name, cat, sid, sname, st, notes)
                        },
                        onUpdateTopicStatus = { top, newSt -> viewModel.updateTopicStatus(top, newSt) },
                        onDeleteTopic = { top -> viewModel.deleteTopic(top) },
                        onSwitchRole = { showLogin = true }
                    )
                }

                currentTab == "profile" -> {
                    val s = currentStudent ?: students.firstOrNull()
                    if (s != null) {
                        StudentProfileScreen(
                            student = s,
                            attendanceList = allAttendance,
                            activities = activities,
                            topics = topics,
                            onBack = { currentTab = "home" },
                            onEnrollFace = { sid, bmp -> viewModel.enrollStudentFace(sid, bitmap = bmp) },
                            onTestFace = { sid, bmp -> viewModel.verifyStudentFace(sid, bmp) }
                        )
                    }
                }

                currentTab == "reports" -> {
                    ReportsExportScreen(
                        currentRole = currentRole,
                        students = students,
                        allAttendance = allAttendance,
                        currentDate = viewModel.currentDateString,
                        onExportPdf = { ctx, title -> viewModel.exportPdfReport(ctx, title) },
                        onExportDailyCsv = { ctx -> viewModel.exportDailyAttendanceCsv(ctx) },
                        onExportMonthlyCsv = { ctx, month -> viewModel.exportMonthlyAttendanceCsv(ctx, month) },
                        onExportActivitiesCsv = { ctx -> viewModel.exportActivitiesCsv(ctx) },
                        onSwitchRole = { showLogin = true }
                    )
                }
            }
        }
    }
}
