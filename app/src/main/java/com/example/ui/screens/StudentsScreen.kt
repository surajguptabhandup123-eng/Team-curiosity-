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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.service.FaceVerificationResult
import com.example.ui.components.AdminEnrollFaceDialog
import com.example.ui.components.AdminTestFaceDialog
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.theme.*

@Composable
fun StudentsScreen(
    currentRole: UserRole,
    students: List<StudentEntity>,
    onAddStudent: (name: String, studentId: String, role: String, phone: String, email: String) -> Unit,
    onDeleteStudent: (StudentEntity) -> Unit,
    onEnrollFace: (studentId: String, bitmap: Bitmap) -> Unit,
    onTestFace: (studentId: String, bitmap: Bitmap) -> FaceVerificationResult,
    onSelectStudentProfile: (StudentEntity) -> Unit,
    onSwitchRole: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToEnrollFace by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToTestFace by remember { mutableStateOf<StudentEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Form fields
    var newName by remember { mutableStateOf("") }
    var newId by remember { mutableStateOf("TC-${(100 + students.size + 1)}") }
    var newRole by remember { mutableStateOf("Programmer") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val roles = listOf(
        "Driver", "Operator", "Programmer", "Builder",
        "CAD/Designer", "Strategy", "Documentation", "Mentor", "Other"
    )

    val filtered = students.filter { s ->
        searchQuery.isEmpty() ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.studentId.contains(searchQuery, ignoreCase = true) ||
                s.role.contains(searchQuery, ignoreCase = true)
    }

    // Admin Face Enrollment Dialog
    if (studentToEnrollFace != null) {
        AdminEnrollFaceDialog(
            student = studentToEnrollFace!!,
            onDismiss = { studentToEnrollFace = null },
            onSaveEnrollment = { bitmap ->
                onEnrollFace(studentToEnrollFace!!.studentId, bitmap)
                studentToEnrollFace = null
            }
        )
    }

    // Admin Test Mode Dialog
    if (studentToTestFace != null) {
        AdminTestFaceDialog(
            student = studentToTestFace!!,
            allStudents = students,
            onDismiss = { studentToTestFace = null },
            onTestFace = { bitmap ->
                onTestFace(studentToTestFace!!.studentId, bitmap)
            }
        )
    }

    if (studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            containerColor = TechCardDark,
            title = {
                Text("Confirm Removal", color = TechAccentRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove student ${studentToDelete?.name} (${studentToDelete?.studentId}) from Team Curiosity? This cannot be undone.",
                    color = TechTextPrimaryDark,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = studentToDelete ?: return@Button
                        onDeleteStudent(s)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechAccentRed)
                ) {
                    Text("Delete Student", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel", color = TechTextSecondaryDark)
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = TechCardDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamCuriosityLogoBadge(size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REGISTER NEW STUDENT",
                        color = CyanPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name * (e.g. Rahul)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newId,
                            onValueChange = { newId = it },
                            label = { Text("Student ID * (e.g. TC001)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            )
                        )
                    }

                    item {
                        Text("FTC Team Role", fontSize = 11.sp, color = TechTextSecondaryDark)
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(newRole, color = CyanPrimary, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(TechCardDark)
                            ) {
                                roles.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r, color = TechTextPrimaryDark) },
                                        onClick = {
                                            newRole = r
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone Number (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TechCardBorderDark,
                                focusedTextColor = TechTextPrimaryDark,
                                unfocusedTextColor = TechTextPrimaryDark
                            )
                        )
                    }

                    if (formError != null) {
                        item {
                            Text(formError!!, color = TechAccentRed, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank() || newId.isBlank()) {
                            formError = "Name and Student ID are required"
                            return@Button
                        }
                        onAddStudent(newName, newId, newRole, newPhone, newEmail)
                        showAddDialog = false
                        newName = ""
                        newPhone = ""
                        newEmail = ""
                        formError = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Text("Register Student", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
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
                studentName = null,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Action banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TEAM ROSTER & BIOMETRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "Student Face Management",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimaryDark
                        )
                        Text(
                            text = "Enroll & test student-specific facial biometric profiles",
                            fontSize = 11.sp,
                            color = TechTextSecondaryDark
                        )
                    }

                    if (currentRole == UserRole.ADMIN_MENTOR) {
                        Button(
                            onClick = {
                                newId = "TC-${(100 + students.size + 1)}"
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = TechSlateDark
                            )
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Student", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name, ID, or role...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechTextSecondaryDark) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TechCardBorderDark,
                    focusedTextColor = TechTextPrimaryDark,
                    unfocusedTextColor = TechTextPrimaryDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        item {
            Text(
                text = "TEAM MEMBERS (${filtered.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CyanPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (students.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(TechCardBorderDark, TechCardBorderDark)),
                        width = 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students registered yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin can register students and enroll their face profiles.",
                            fontSize = 12.sp,
                            color = TechTextSecondaryDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (currentRole == UserRole.ADMIN_MENTOR) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    newId = "TC-${(100 + students.size + 1)}"
                                    showAddDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Student", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        items(filtered) { student ->
            Card(
                onClick = { onSelectStudentProfile(student) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(TechCardBorderDark, CyanPrimary.copy(alpha = 0.2f))
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F1A28))
                                .border(1.5.dp, CyanPrimaryVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.name.take(2).uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = student.role,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyanPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ID: ${student.studentId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBAE6FD)
                                )
                            }
                        }

                        // Face Enrollment Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (student.faceEnrolled) TechAccentGreen.copy(alpha = 0.2f) else Color(0xFFFBBF24).copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(if (student.faceEnrolled) TechAccentGreen else Color(0xFFFBBF24), Color.Transparent)
                                ),
                                width = 1.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (student.faceEnrolled) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = if (student.faceEnrolled) TechAccentGreen else Color(0xFFFBBF24),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (student.faceEnrolled) "Face Enrolled ✓" else "Not Enrolled",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (student.faceEnrolled) TechAccentGreen else Color(0xFFFBBF24)
                                )
                            }
                        }

                        if (currentRole == UserRole.ADMIN_MENTOR) {
                            IconButton(
                                onClick = { studentToDelete = student },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = TechTextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Admin Actions: [Enroll Face] & [Test Face]
                    if (currentRole == UserRole.ADMIN_MENTOR) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { studentToEnrollFace = student },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (student.faceEnrolled) Color(0xFF0369A1) else CyanPrimary,
                                    contentColor = if (student.faceEnrolled) Color.White else TechSlateDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (student.faceEnrolled) "Re-Enroll Face" else "Enroll Face",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { studentToTestFace = student },
                                enabled = student.faceEnrolled,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CyanPrimary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(CyanPrimary, CyanPrimaryVariant))
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Face", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
