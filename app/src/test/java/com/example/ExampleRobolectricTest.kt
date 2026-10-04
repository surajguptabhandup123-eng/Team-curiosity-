package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TEAM CURIOSITY", appName)
    }

    @Test
    fun `calculate distance accurately between practice location coordinates`() {
        // Test distance from exact same coordinate is near 0
        val distZero = LocationHelper.calculateDistance(37.4220, -122.0841, 37.4220, -122.0841)
        assertTrue(distZero < 1f)

        // Test distance for small displacement
        val distClose = LocationHelper.calculateDistance(37.4220, -122.0841, 37.4221, -122.0841)
        assertTrue(distClose in 10f..20f)
    }

    @Test
    fun `student-specific face verification matches enrolled student and rejects another student`() {
        val studentRahulId = "TC001"
        // Test photo for Rahul (distinct pixel pattern)
        val rahulFaceBmp = android.graphics.Bitmap.createBitmap(128, 128, android.graphics.Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.BLUE)
            setPixel(64, 64, android.graphics.Color.WHITE)
        }
        val rahulVector = com.example.service.FaceBiometricManager.extractBiometricVector(rahulFaceBmp)
        val rahulSignature = com.example.service.FaceBiometricManager.serializeVector(studentRahulId, rahulVector)

        // 1. Rahul's own face presented -> MATCH ✓
        val matchResult = com.example.service.FaceBiometricManager.verifyFaceAgainstStudent(
            capturedBitmap = rahulFaceBmp,
            targetStudentId = studentRahulId,
            registeredSignature = rahulSignature,
            isEnrolled = true
        )
        assertTrue("Rahul's face must match Rahul's profile", matchResult is com.example.service.FaceVerificationResult.Match)

        // 2. Another student (Maya TC-102) presented while Rahul is logged in -> NO MATCH ✕
        val mayaFaceBmp = android.graphics.Bitmap.createBitmap(128, 128, android.graphics.Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.RED)
            setPixel(32, 32, android.graphics.Color.YELLOW)
        }
        val noMatchResult = com.example.service.FaceBiometricManager.verifyFaceAgainstStudent(
            capturedBitmap = mayaFaceBmp,
            targetStudentId = studentRahulId,
            registeredSignature = rahulSignature,
            isEnrolled = true
        )
        assertTrue("Another student's face must NOT match Rahul's profile", noMatchResult is com.example.service.FaceVerificationResult.NoMatch)

        // 3. Random face presented -> NO MATCH ✕
        val randomBmp = android.graphics.Bitmap.createBitmap(128, 128, android.graphics.Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.GREEN)
        }
        val randomResult = com.example.service.FaceBiometricManager.verifyFaceAgainstStudent(
            capturedBitmap = randomBmp,
            targetStudentId = studentRahulId,
            registeredSignature = rahulSignature,
            isEnrolled = true
        )
        assertTrue("Random face must NOT match Rahul's profile", randomResult is com.example.service.FaceVerificationResult.NoMatch)

        // 4. Biometric vector cannot be hijacked by a different Student ID
        val deserializedForDifferentStudent = com.example.service.FaceBiometricManager.deserializeVector(
            serialized = rahulSignature,
            expectedStudentId = "TC-102"
        )
        assertEquals("Vector bound to TC001 must return null if evaluated for TC-102", null, deserializedForDifferentStudent)
    }
}
