package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.ActivityEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TopicEntity
import java.io.File
import java.io.FileOutputStream

object ReportExporter {

    fun exportCsv(context: Context, filename: String, csvContent: String): File {
        val file = File(context.cacheDir, filename)
        file.writeText(csvContent)
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun generateDailyAttendanceCsv(
        date: String,
        attendanceList: List<AttendanceEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("TEAM CURIOSITY - CURIOUS CITY FTC\n")
        sb.append("DAILY ATTENDANCE REPORT - Date: $date\n\n")
        sb.append("Student ID,Student Name,Status,Time,Face Verification,Location Verification,Verification Method,Distance (m),Notes\n")
        for (item in attendanceList) {
            sb.append("\"${item.studentId}\",")
            sb.append("\"${item.studentName}\",")
            sb.append("\"${item.status}\",")
            sb.append("\"${item.time}\",")
            sb.append("\"${item.faceVerification}\",")
            sb.append("\"${item.locationVerification}\",")
            sb.append("\"${item.verificationMethod}\",")
            sb.append("\"${String.format(java.util.Locale.US, "%.1f", item.distanceMeters)}\",")
            sb.append("\"${item.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun generateMonthlyAttendanceCsv(
        month: String,
        students: List<StudentEntity>,
        attendanceList: List<AttendanceEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("TEAM CURIOSITY - CURIOUS CITY FTC\n")
        sb.append("MONTHLY ATTENDANCE SUMMARY - Month: $month\n\n")
        sb.append("Student ID,Name,Role,Total Sessions,Present,Absent,Late,Excused,Attendance %\n")

        for (student in students) {
            val studentAtt = attendanceList.filter { it.studentId == student.studentId }
            val total = studentAtt.size
            val present = studentAtt.count { it.status == "Present" }
            val absent = studentAtt.count { it.status == "Absent" }
            val late = studentAtt.count { it.status == "Late" }
            val excused = studentAtt.count { it.status == "Excused" }
            val pct = if (total > 0) (present.toFloat() / total * 100).toInt() else 0

            sb.append("\"${student.studentId}\",\"${student.name}\",\"${student.role}\",$total,$present,$absent,$late,$excused,$pct%\n")
        }
        return sb.toString()
    }

    fun generateActivitiesCsv(
        date: String,
        activities: List<ActivityEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("TEAM CURIOSITY - CURIOUS CITY FTC\n")
        sb.append("DAILY WORK & ACTIVITY REPORT - Date: $date\n\n")
        sb.append("Student ID,Name,Category,Start Time,End Time,Work Description,What I Learned,Completed Tasks,Pending,Challenges,Solutions\n")
        for (act in activities) {
            sb.append("\"${act.studentId}\",\"${act.studentName}\",\"${act.category}\",\"${act.startTime}\",\"${act.endTime}\",")
            sb.append("\"${act.workDescription.replace("\"", "\"\"")}\",")
            sb.append("\"${act.learningDescription.replace("\"", "\"\"")}\",")
            sb.append("\"${act.completedTask.replace("\"", "\"\"")}\",")
            sb.append("\"${act.pendingTask.replace("\"", "\"\"")}\",")
            sb.append("\"${act.challenges.replace("\"", "\"\"")}\",")
            sb.append("\"${act.solution.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun generatePdfReport(
        context: Context,
        reportTitle: String,
        date: String,
        attendanceList: List<AttendanceEntity>,
        activities: List<ActivityEntity>,
        topics: List<TopicEntity>
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Background
        val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Header Cyan Accent Bar
        val accentPaint = Paint().apply { color = android.graphics.Color.parseColor("#007799") }
        canvas.drawRect(0f, 0f, 595f, 90f, accentPaint)

        // Draw Official Team Curiosity Logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.team_curiosity_logo)
            if (logoBitmap != null) {
                val destRect = Rect(20, 10, 90, 80)
                canvas.drawBitmap(logoBitmap, null, destRect, null)
            }
        } catch (_: Exception) {}

        // Header Text
        val titlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("TEAM CURIOSITY", 105f, 38f, titlePaint)

        val subPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E0F7FA")
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("CURIOUS CITY FTC  •  OFFICIAL TEAM REPORT", 105f, 56f, subPaint)
        canvas.drawText("Generated: $date", 105f, 74f, subPaint)

        // Report Title
        val docTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(reportTitle, 30f, 120f, docTitlePaint)

        // Metrics Banner
        val cardBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#F1F5F9") }
        canvas.drawRoundRect(30f, 135f, 565f, 180f, 8f, 8f, cardBgPaint)

        val statTextPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 11f
            isAntiAlias = true
        }
        val statValPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#007799")
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val presentCount = attendanceList.count { it.status == "Present" }
        val absentCount = attendanceList.count { it.status == "Absent" }
        val lateCount = attendanceList.count { it.status == "Late" }

        canvas.drawText("Total Recorded: ${attendanceList.size}", 45f, 155f, statValPaint)
        canvas.drawText("Present: $presentCount", 180f, 155f, statValPaint)
        canvas.drawText("Late: $lateCount", 280f, 155f, statValPaint)
        canvas.drawText("Absent: $absentCount", 360f, 155f, statValPaint)
        canvas.drawText("Topics Active: ${topics.size}", 460f, 155f, statValPaint)

        canvas.drawText("Verified via Face + GPS Geofence", 45f, 172f, statTextPaint)

        // Attendance Table Header
        var currentY = 210f
        val secTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ATTENDANCE RECORDS", 30f, currentY, secTitlePaint)
        currentY += 15f

        val tableHeaderPaint = Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") }
        canvas.drawRect(30f, currentY, 565f, currentY + 22f, tableHeaderPaint)

        val colTextPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ID", 40f, currentY + 15f, colTextPaint)
        canvas.drawText("Student Name", 100f, currentY + 15f, colTextPaint)
        canvas.drawText("Status", 240f, currentY + 15f, colTextPaint)
        canvas.drawText("Time", 310f, currentY + 15f, colTextPaint)
        canvas.drawText("Face Verification", 370f, currentY + 15f, colTextPaint)
        canvas.drawText("GPS Geofence", 475f, currentY + 15f, colTextPaint)
        currentY += 22f

        val rowTextPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 9.5f
            isAntiAlias = true
        }
        val linePaint = Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") }

        for ((index, item) in attendanceList.take(12).withIndex()) {
            if (index % 2 == 1) {
                canvas.drawRect(30f, currentY, 565f, currentY + 20f, cardBgPaint)
            }
            canvas.drawText(item.studentId, 40f, currentY + 14f, rowTextPaint)
            canvas.drawText(item.studentName.take(20), 100f, currentY + 14f, rowTextPaint)
            canvas.drawText(item.status, 240f, currentY + 14f, rowTextPaint)
            canvas.drawText(item.time, 310f, currentY + 14f, rowTextPaint)
            canvas.drawText(item.faceVerification, 370f, currentY + 14f, rowTextPaint)
            canvas.drawText("${item.locationVerification} (${String.format(java.util.Locale.US, "%.0f", item.distanceMeters)}m)", 475f, currentY + 14f, rowTextPaint)
            canvas.drawLine(30f, currentY + 20f, 565f, currentY + 20f, linePaint)
            currentY += 20f
        }

        // Activities & Work Summary
        currentY += 15f
        canvas.drawText("DAILY WORK & LEARNING HIGHLIGHTS", 30f, currentY, secTitlePaint)
        currentY += 15f

        for (act in activities.take(3)) {
            val actCardPaint = Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") }
            canvas.drawRoundRect(30f, currentY, 565f, currentY + 50f, 6f, 6f, actCardPaint)

            val actTitlePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#0284C7")
                textSize = 10f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("${act.studentName} (${act.category}) - ${act.startTime} to ${act.endTime}", 40f, currentY + 15f, actTitlePaint)
            canvas.drawText("Work: ${act.workDescription.take(80)}", 40f, currentY + 30f, rowTextPaint)
            canvas.drawText("Learned: ${act.learningDescription.take(80)}", 40f, currentY + 44f, rowTextPaint)
            currentY += 56f
        }

        // Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawLine(30f, 800f, 565f, 800f, linePaint)
        canvas.drawText("Team Curiosity • Curious City FTC Robotics • Official System Export", 30f, 815f, footerPaint)
        canvas.drawText("Lead Mentor Signature: __________________________", 340f, 815f, footerPaint)

        pdfDoc.finishPage(page)

        val outputFile = File(context.cacheDir, "Team_Curiosity_Report_$date.pdf")
        val outStream = FileOutputStream(outputFile)
        pdfDoc.writeTo(outStream)
        outStream.close()
        pdfDoc.close()

        return outputFile
    }
}
