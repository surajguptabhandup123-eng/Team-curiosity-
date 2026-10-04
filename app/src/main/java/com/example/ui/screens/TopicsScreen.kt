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
import com.example.data.model.StudentEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserRole
import com.example.ui.components.RoboticsCategoryChip
import com.example.ui.components.TeamCuriosityHeader
import com.example.ui.components.TeamCuriosityLogoBadge
import com.example.ui.theme.*

@Composable
fun TopicsScreen(
    currentRole: UserRole,
    currentStudent: StudentEntity?,
    students: List<StudentEntity>,
    topics: List<TopicEntity>,
    onAddTopic: (topicName: String, category: String, studentId: String?, studentName: String?, status: String, notes: String) -> Unit,
    onUpdateTopicStatus: (TopicEntity, String) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit,
    onSwitchRole: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        "Driving", "Programming", "Autonomous", "Sensors",
        "Motor Control", "CAD", "Robot Building", "Game Strategy",
        "Driver Practice", "Safety", "Teamwork"
    )

    var newTopicName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Programming") }
    var newTopicStudent by remember { mutableStateOf<StudentEntity?>(currentStudent) }
    var newStatus by remember { mutableStateOf("Learning") }
    var newNotes by remember { mutableStateOf("") }

    val filteredTopics = topics.filter { t ->
        val matchesStatus = selectedStatusFilter == null || t.status == selectedStatusFilter
        val matchesSearch = searchQuery.isEmpty() ||
                t.topicName.contains(searchQuery, ignoreCase = true) ||
                t.category.contains(searchQuery, ignoreCase = true) ||
                (t.studentName ?: "").contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
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
                        text = "ADD LEARNING TOPIC",
                        color = CyanPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newTopicName,
                        onValueChange = { newTopicName = it },
                        label = { Text("Topic Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = TechCardBorderDark,
                            focusedTextColor = TechTextPrimaryDark,
                            unfocusedTextColor = TechTextPrimaryDark
                        )
                    )

                    Text("Category", fontSize = 11.sp, color = TechTextSecondaryDark)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = newCategory == cat,
                                onClick = { newCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary
                                )
                            )
                        }
                    }

                    Text("Initial Status", fontSize = 11.sp, color = TechTextSecondaryDark)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Not Started", "Learning", "Completed").forEach { st ->
                            FilterChip(
                                selected = newStatus == st,
                                onClick = { newStatus = st },
                                label = { Text(st, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Learning Objectives / Notes") },
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTopicName.isNotBlank()) {
                            onAddTopic(
                                newTopicName,
                                newCategory,
                                newTopicStudent?.studentId,
                                newTopicStudent?.name ?: "Team Curiosity",
                                newStatus,
                                newNotes
                            )
                            showAddDialog = false
                            newTopicName = ""
                            newNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Text("Save Topic", fontWeight = FontWeight.Bold)
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
                studentName = currentStudent?.name,
                onSwitchRoleClick = onSwitchRole
            )
        }

        // Topics Header & Add Button
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
                            text = "TOPIC TRACKING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "FTC Technical Curriculum",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimaryDark
                        )
                        Text(
                            text = "Track mastery of robot mechanisms & code",
                            fontSize = 11.sp,
                            color = TechTextSecondaryDark
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = TechSlateDark
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Topic", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search & Status Filters
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search topics, categories, students...", fontSize = 12.sp) },
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(null to "All Statuses", "Completed" to "Completed", "Learning" to "In Progress", "Not Started" to "Not Started").forEach { (filterVal, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == filterVal,
                            onClick = { selectedStatusFilter = filterVal },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                }
            }
        }

        // Topic items list
        item {
            Text(
                text = "TOPICS IN REPOSITORY (${filteredTopics.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CyanPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredTopics) { topic ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            when (topic.status) {
                                "Completed" -> TechAccentGreen.copy(alpha = 0.4f)
                                "Learning" -> CyanPrimary.copy(alpha = 0.4f)
                                else -> TechCardBorderDark
                            },
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.topicName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            Text(
                                text = "Lead: ${topic.studentName ?: "All Team"} • ${topic.date}",
                                fontSize = 11.sp,
                                color = TechTextSecondaryDark
                            )
                        }
                        RoboticsCategoryChip(category = topic.category, isSelected = true)
                    }

                    if (topic.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = topic.notes,
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Toggle Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Not Started", "Learning", "Completed").forEach { statusOption ->
                                val isCur = topic.status == statusOption
                                Surface(
                                    onClick = { onUpdateTopicStatus(topic, statusOption) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCur) {
                                        when (statusOption) {
                                            "Completed" -> TechAccentGreen.copy(alpha = 0.25f)
                                            "Learning" -> CyanPrimary.copy(alpha = 0.25f)
                                            else -> TechCardBorderDark
                                        }
                                    } else Color(0xFF0F172A),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(
                                            if (isCur) listOf(
                                                when (statusOption) {
                                                    "Completed" -> TechAccentGreen
                                                    "Learning" -> CyanPrimary
                                                    else -> TechCardBorderDark
                                                },
                                                Color.Transparent
                                            ) else listOf(TechCardBorderDark, TechCardBorderDark)
                                        ),
                                        width = 1.dp
                                    )
                                ) {
                                    Text(
                                        text = statusOption,
                                        fontSize = 10.sp,
                                        fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCur) {
                                            when (statusOption) {
                                                "Completed" -> TechAccentGreen
                                                "Learning" -> CyanPrimary
                                                else -> TechTextPrimaryDark
                                            }
                                        } else TechTextSecondaryDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (currentRole == UserRole.ADMIN_MENTOR) {
                            IconButton(
                                onClick = { onDeleteTopic(topic) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TechTextSecondaryDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
