package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.PracticeLocationEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TopicEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeamRepository(private val db: AppDatabase) {
    private val studentDao = db.studentDao()
    private val attendanceDao = db.attendanceDao()
    private val activityDao = db.activityDao()
    private val topicDao = db.topicDao()
    private val locationDao = db.practiceLocationDao()

    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()
    val allActivities: Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    val allTopics: Flow<List<TopicEntity>> = topicDao.getAllTopics()
    val practiceLocation: Flow<PracticeLocationEntity?> = locationDao.getPracticeLocation()

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudent(studentId)

    fun getActivitiesForStudent(studentId: String): Flow<List<ActivityEntity>> =
        activityDao.getActivitiesForStudent(studentId)

    fun getTopicsForStudent(studentId: String): Flow<List<TopicEntity>> =
        topicDao.getTopicsForStudent(studentId)

    suspend fun getStudentById(studentId: String): StudentEntity? =
        studentDao.getStudentById(studentId)

    fun getStudentFlowById(studentId: String): Flow<StudentEntity?> =
        studentDao.getStudentFlowById(studentId)

    suspend fun insertStudent(student: StudentEntity) = studentDao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)

    suspend fun insertAttendance(attendance: AttendanceEntity): Long =
        attendanceDao.insertAttendance(attendance)

    suspend fun updateAttendance(attendance: AttendanceEntity) =
        attendanceDao.updateAttendance(attendance)

    suspend fun deleteAttendance(attendance: AttendanceEntity) =
        attendanceDao.deleteAttendance(attendance)

    suspend fun getAttendanceForStudentOnDate(studentId: String, date: String): AttendanceEntity? =
        attendanceDao.getAttendanceForStudentOnDate(studentId, date)

    suspend fun insertActivity(activity: ActivityEntity): Long =
        activityDao.insertActivity(activity)

    suspend fun deleteActivity(activity: ActivityEntity) =
        activityDao.deleteActivity(activity)

    suspend fun insertTopic(topic: TopicEntity): Long =
        topicDao.insertTopic(topic)

    suspend fun updateTopic(topic: TopicEntity) =
        topicDao.updateTopic(topic)

    suspend fun deleteTopic(topic: TopicEntity) =
        topicDao.deleteTopic(topic)

    suspend fun setPracticeLocation(location: PracticeLocationEntity) =
        locationDao.setPracticeLocation(location)

    suspend fun getPracticeLocationOnce(): PracticeLocationEntity? =
        locationDao.getPracticeLocationOnce()

    suspend fun seedInitialDataIfEmpty() {
        // Purge any previously seeded fictional/demo students so the app starts clean
        val demoIds = listOf("TC001", "TC-101", "TC-102", "TC-103", "TC-104", "TC-105", "TC-106")
        val existingStudents = studentDao.getAllStudents().firstOrNull()
        existingStudents?.filter { it.studentId in demoIds }?.forEach { demoStudent ->
            studentDao.deleteStudent(demoStudent)
        }

        // Clean up any demo attendance and activities tied to demo IDs
        val existingAtt = attendanceDao.getAllAttendance().firstOrNull()
        existingAtt?.filter { it.studentId in demoIds }?.forEach { att ->
            attendanceDao.deleteAttendance(att)
        }
        val existingAct = activityDao.getAllActivities().firstOrNull()
        existingAct?.filter { it.studentId in demoIds }?.forEach { act ->
            activityDao.deleteActivity(act)
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Seed official practice location (Curious City FTC Robotics Arena) if not configured
        val existingLoc = locationDao.getPracticeLocationOnce()
        if (existingLoc == null) {
            val defaultLoc = PracticeLocationEntity(
                id = 1,
                latitude = 37.4220,
                longitude = -122.0841,
                allowedRadius = 100.0,
                locationName = "Curious City FTC Robotics Lab & Arena",
                updatedBy = "Lead Mentor",
                updatedAt = today
            )
            locationDao.setPracticeLocation(defaultLoc)
        }

        // Seed general team FTC curriculum topics if empty
        val existingTopics = topicDao.getAllTopics().firstOrNull()
        if (existingTopics.isNullOrEmpty()) {
            val initialTopics = listOf(
                TopicEntity(
                    topicName = "Autonomous AprilTag Alignment",
                    category = "Programming",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Completed",
                    notes = "Integrated webcam stream and FTC SDK VisionPortal for AprilTag detection."
                ),
                TopicEntity(
                    topicName = "Mecanum Field-Centric Drive",
                    category = "Driving",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Completed",
                    notes = "IMU gyro angle compensation tuned for swift 360-degree strafing."
                ),
                TopicEntity(
                    topicName = "Linear Slide Cable Rigging",
                    category = "Robot Building",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Learning",
                    notes = "3-stage continuous rigging using Dyneema cord and spool tensioner."
                ),
                TopicEntity(
                    topicName = "OnShape 3D CAD Intake V4",
                    category = "CAD",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Learning",
                    notes = "Designing compliant wheel intake assembly with 3D printed TPU rollers."
                ),
                TopicEntity(
                    topicName = "Scouting Matrix & Auto Points",
                    category = "Game Strategy",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Completed",
                    notes = "Calculated maximum cycle efficiency for alliance high basket scoring."
                ),
                TopicEntity(
                    topicName = "Engineering Portfolio Section 3",
                    category = "Documentation",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Learning",
                    notes = "Engineering notebook breakdown of sensor fusion iterations and chassis stress tests."
                ),
                TopicEntity(
                    topicName = "Battery Safety & Anderson Connectors",
                    category = "Safety",
                    studentId = null,
                    studentName = "Team Curiosity",
                    date = today,
                    status = "Completed",
                    notes = "Standard operating procedures for 12V Slim Battery charging and Anderson Powerpole crimping."
                )
            )
            topicDao.insertTopics(initialTopics)
        }
    }
}
