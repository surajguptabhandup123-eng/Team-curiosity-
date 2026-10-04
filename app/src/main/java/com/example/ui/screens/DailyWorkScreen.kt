package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.ui.components.RoboticsCategoryChip
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.theme.*

@Composable
fun DailyWorkScreen(
    currentRole: UserRole,
    currentStudent: StudentEntity?,
    students: List<StudentEntity>,
    activities: List<ActivityEntity>,
    currentDate: String,
    onAddActivity: (
        studentId: String,
        studentName: String,
        startTime: String,
        endTime: String,
        category: String,
        workDescription: String,
        learningDescription: String,
        completedTask: String,
        pendingTask: String,
        challenges: String,
        solution: String,
        notes: String
    ) -> Unit,
    onDeleteActivity: (ActivityEntity) -> Unit,
    onSwitchRole: () -> Unit
) {
    var showFormDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        "Driving", "Programming", "Building", "CAD/Design",
        "Electronics", "Sensors", "Strategy", "Documentation",
        "Teamwork", "Other"
    )

    // Form fields
    var formStudent by remember { mutableStateOf(currentStudent ?: students.firstOrNull()) }
    var startTime by remember { mutableStateOf("16:00") }
    var endTime by remember { mutableStateOf("18:30") }
    var selectedCategory by remember { mutableStateOf("Programming") }
    var workDescription by remember { mutableStateOf("") }
    var learningDescription by remember { mutableStateOf("") }
    var completedTask by remember { mutableStateOf("") }
    var pendingTask by remember { mutableStateOf("") }
    var challenges by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val filteredActivities = activities.filter { act ->
        val matchesCategory = selectedCategoryFilter == null || act.category == selectedCategoryFilter
        val matchesSearch = searchQuery.isEmpty() ||
                act.studentName.contains(searchQuery, ignoreCase = true) ||
                act.workDescription.contains(searchQuery, ignoreCase = true) ||
                act.category.contains(searchQuery, ignoreCase = true)
        val matchesRole = if (currentRole == UserRole.STUDENT && currentStudent != null) {
            act.studentId == currentStudent.studentId
        } else true

        matchesCategory && matchesSearch && matchesRole
    }

    if (showFormDialog) {
        AlertDialog(
            onDismissRequest = { showFormDialog = false },
            containerColor = TechCardDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamCuriosityLogoBadge(size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOG TODAY'S WORK",
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
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Student: ${formStudent?.name ?: "Select student"}",
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimaryDark,
                            fontSize = 13.sp
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Start Time") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = TechCardBorderDark,
                                    focusedTextColor = TechTextPrimaryDark,
                                    unfocusedTextColor = TechTextPrimaryDark
                                )
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = TechCardBorderDark,
                                    focusedTextColor = TechTextPrimaryDark,
                                    unfocusedTextColor = TechTextPrimaryDark
                                )
                            )
                        }
                    }

                    item {
                        Text("Category", fontSize = 11.sp, color = TechTextSecondaryDark)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = CyanPrimary
                                    )
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = workDescription,
                            onValueChange = { workDescription = it },
                            label = { Text("What did you work on today? *") },
                            minLines = 2,
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
                            value = learningDescription,
                            onValueChange = { learningDescription = it },
                            label = { Text("What did you learn today? *") },
                            minLines = 2,
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
                            value = completedTask,
                            onValueChange = { completedTask = it },
                            label = { Text("Task Completed") },
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
                            value = pendingTask,
                            onValueChange = { pendingTask = it },
                            label = { Text("Task Pending (For Next Session)") },
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
                            value = challenges,
                            onValueChange = { challenges = it },
                            label = { Text("Problems / Challenges Encountered") },
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
                            value = solution,
                            onValueChange = { solution = it },
                            label = { Text("Solution / What I Tried") },
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
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Additional Notes") },
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
                        val s = formStudent
                        if (s == null) {
                            formError = "Student selection required"
                            return@Button
                        }
                        if (workDescription.isBlank()) {
                            formError = "Please describe what you worked on"
                            return@Button
                        }
                        onAddActivity(
                            s.studentId,
                            s.name,
                            startTime,
                            endTime,
                            selectedCategory,
                            workDescription,
                            learningDescription,
                            completedTask,
                            pendingTask,
                            challenges,
                            solution,
                            notes
                        )
                        showFormDialog = false
                        workDescription = ""
                        learningDescription = ""
                        completedTask = ""
                        pendingTask = ""
                        challenges = ""
                        solution = ""
                        notes = ""
                        formError = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Text("Submit Activity", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFormDialog = false }) {
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

        // Action banner: "Log Today's Work"
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
                            text = "TODAY'S WORK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "Daily Practice Activity Form",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimaryDark
                        )
                        Text(
                            text = "Record building, programming, and strategy tasks",
                            fontSize = 11.sp,
                            color = TechTextSecondaryDark
                        )
                    }

                    Button(
                        onClick = {
                            formStudent = currentStudent ?: students.firstOrNull()
                            showFormDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = TechSlateDark
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Work", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search & Category Filters
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks, categories, students...", fontSize = 12.sp) },
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

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Categories", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                }
            }
        }

        // Activity List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOGGED ACTIVITIES (${filteredActivities.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyanPrimary
                )
                Text(
                    text = if (currentRole == UserRole.STUDENT) "My Activities" else "All Team Members",
                    fontSize = 11.sp,
                    color = TechTextSecondaryDark
                )
            }
        }

        if (filteredActivities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechCardDark)
                ) {
                    Text(
                        text = "No activities match your filter. Tap 'Add Work' above to log a new session.",
                        fontSize = 12.sp,
                        color = TechTextSecondaryDark,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredActivities) { act ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = act.studentName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TechTextPrimaryDark
                                )
                                Text(
                                    text = "${act.date} • ${act.startTime} - ${act.endTime}",
                                    fontSize = 10.sp,
                                    color = TechTextSecondaryDark
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RoboticsCategoryChip(category = act.category, isSelected = true)
                                if (currentRole == UserRole.ADMIN_MENTOR || act.studentId == currentStudent?.studentId) {
                                    IconButton(
                                        onClick = { onDeleteActivity(act) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = TechTextSecondaryDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Work Question Response
                        Text(
                            text = "What was worked on:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanPrimary
                        )
                        Text(
                            text = act.workDescription,
                            fontSize = 13.sp,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                        )

                        // Learning Question Response
                        if (act.learningDescription.isNotEmpty()) {
                            Text(
                                text = "What was learned:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TechAccentYellow
                            )
                            Text(
                                text = act.learningDescription,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                            )
                        }

                        // Completed / Pending Tasks
                        if (act.completedTask.isNotEmpty() || act.pendingTask.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (act.completedTask.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F2018)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Completed:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TechAccentGreen)
                                            Text(act.completedTask, fontSize = 11.sp, color = Color(0xFFD1FAE5))
                                        }
                                    }
                                }
                                if (act.pendingTask.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF241C10)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Next Session:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TechAccentYellow)
                                            Text(act.pendingTask, fontSize = 11.sp, color = Color(0xFFFEF3C7))
                                        }
                                    }
                                }
                            }
                        }

                        // Challenges & Solution
                        if (act.challenges.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E1520)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Challenge:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                                    Text(act.challenges, fontSize = 11.sp, color = Color(0xFFFFE4E6))
                                    if (act.solution.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Solution tried:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                        Text(act.solution, fontSize = 11.sp, color = Color(0xFFCCFBF1))
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
