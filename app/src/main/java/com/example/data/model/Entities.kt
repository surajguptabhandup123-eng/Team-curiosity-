package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val studentId: String,
    val name: String,
    val role: String, // Driver, Operator, Programmer, Builder, CAD/Designer, Strategy, Documentation, Mentor, Other
    val profilePhoto: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val joiningDate: String,
    val faceEnrolled: Boolean = false,
    val faceVectorSignature: String? = null
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val attendanceId: Long = 0,
    val studentId: String,
    val studentName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm:ss
    val status: String, // Present, Absent, Late, Excused
    val faceVerification: String, // Verified, Failed, Manual, Skipped
    val locationVerification: String, // Verified, Failed, Manual Override
    val verificationMethod: String, // Face + Location, Manual Mentor Override
    val distanceMeters: Float = 0f,
    val notes: String = ""
)

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val activityId: Long = 0,
    val studentId: String,
    val studentName: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val category: String, // Driving, Programming, Building, CAD/Design, Electronics, Sensors, Strategy, Documentation, Teamwork, Other
    val workDescription: String,
    val learningDescription: String,
    val completedTask: String,
    val pendingTask: String,
    val challenges: String,
    val solution: String,
    val notes: String,
    val photo: String? = null
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val topicId: Long = 0,
    val studentId: String? = null,
    val studentName: String? = null,
    val topicName: String,
    val category: String,
    val date: String,
    val status: String, // Not Started, Learning, Completed
    val notes: String = ""
)

@Entity(tableName = "practice_location")
data class PracticeLocationEntity(
    @PrimaryKey
    val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val allowedRadius: Double = 100.0, // default 100 meters
    val locationName: String = "Curious City FTC Robotics Lab",
    val updatedBy: String = "Mentor",
    val updatedAt: String = ""
)

enum class UserRole {
    ADMIN_MENTOR,
    STUDENT
}
