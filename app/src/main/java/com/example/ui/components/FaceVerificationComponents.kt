package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StudentEntity
import com.example.service.FaceBiometricManager
import com.example.service.FaceVerificationResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Admin Face Enrollment Dialog:
 * Select specific student -> Upload or capture face photo -> Save enrollment associated ONLY with that unique Student ID.
 * Shows: Face Enrolled ✓
 */
@Composable
fun AdminEnrollFaceDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onSaveEnrollment: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var enrollmentSaved by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var captureMode by remember { mutableStateOf(false) }

    // Load existing enrolled photo if present
    LaunchedEffect(student.studentId) {
        val existing = FaceBiometricManager.getEnrolledFacePhoto(context, student.studentId)
        if (existing != null) {
            selectedBitmap = existing
        }
    }

    // Photo picker launcher (Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = FaceBiometricManager.decodeBitmapFromUri(context, uri)
            if (bmp != null) {
                selectedBitmap = bmp
                captureMode = false
                enrollmentSaved = false
            }
        }
    }

    // System camera preview fallback launcher
    val cameraFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            selectedBitmap = bmp
            captureMode = false
            enrollmentSaved = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = TechCardDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(CyanPrimary, TechCardBorderDark)),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCuriosityLogoBadge(size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ADMIN FACE ENROLLMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Enroll Student Face Photo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TechTextPrimaryDark
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TechTextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Student Identity Card (Strictly bound to this unique Student ID)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F1A28),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.4f), Color.Transparent)),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Student:", fontSize = 12.sp, color = TechTextSecondaryDark)
                            Text(student.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TechTextPrimaryDark)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Student ID:", fontSize = 12.sp, color = TechTextSecondaryDark)
                            Text(student.studentId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status:", fontSize = 12.sp, color = TechTextSecondaryDark)
                            Text(
                                text = if (student.faceEnrolled || enrollmentSaved) "Face Enrolled ✓" else "Not Enrolled",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (student.faceEnrolled || enrollmentSaved) TechAccentGreen else Color(0xFFFBBF24)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Area: Clean rounded rectangle with correct aspect ratio (3:4)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF070F18))
                        .border(
                            2.dp,
                            if (selectedBitmap != null) TechAccentGreen else CyanPrimary,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedBitmap != null && !captureMode) {
                        // Display actual uploaded or captured photo in normal high resolution
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "${student.name}'s Face Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Clear / Retake badge in top corner
                        Surface(
                            shape = CircleShape,
                            color = Color(0xCC070F18),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    selectedBitmap = null
                                    captureMode = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Retake",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        // Live front-camera feed
                        LiveCameraFeed(
                            modifier = Modifier.fillMaxSize(),
                            useFrontCamera = true,
                            onPreviewViewCreated = { previewView ->
                                previewViewRef = previewView
                            },
                            overlayText = "Position ${student.name}'s face inside the frame"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Capture or Upload Buttons
                if (selectedBitmap == null || captureMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val frame = previewViewRef?.bitmap
                                if (frame != null) {
                                    selectedBitmap = frame
                                    captureMode = false
                                } else {
                                    cameraFallbackLauncher.launch(null)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = TechSlateDark
                            )
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capture Face", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Face Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedBitmap = null
                                captureMode = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retake Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Different", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Privacy statement
                Text(
                    text = "The face enrollment is associated ONLY with ${student.studentId} and must never be shared between students.",
                    fontSize = 10.sp,
                    color = TechTextSecondaryDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (enrollmentSaved) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TechAccentGreen.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Face Enrolled ✓ for ${student.name} (${student.studentId})",
                            color = TechAccentGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Save Enrollment Action Button
                Button(
                    onClick = {
                        val bmp = selectedBitmap ?: previewViewRef?.bitmap
                        if (bmp != null) {
                            onSaveEnrollment(bmp)
                            enrollmentSaved = true
                        }
                    },
                    enabled = selectedBitmap != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Face Enrollment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * Admin Test Mode Dialog:
 * Admin -> Students -> Select Student -> Test Face
 * Admin can test whether the uploaded/captured face matches that student's enrolled face.
 * Shows: Match or No Match.
 */
@Composable
fun AdminTestFaceDialog(
    student: StudentEntity,
    allStudents: List<StudentEntity>,
    onDismiss: () -> Unit,
    onTestFace: (Bitmap) -> FaceVerificationResult
) {
    val context = LocalContext.current
    var testBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var testResult by remember { mutableStateOf<FaceVerificationResult?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var enrolledBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(student.studentId) {
        enrolledBitmap = FaceBiometricManager.getEnrolledFacePhoto(context, student.studentId)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = FaceBiometricManager.decodeBitmapFromUri(context, uri)
            if (bmp != null) {
                testBitmap = bmp
                testResult = onTestFace(bmp)
            }
        }
    }

    val cameraFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            testBitmap = bmp
            testResult = onTestFace(bmp)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = TechCardDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(CyanPrimary, TechCardBorderDark)),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Biotech, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ADMIN TEST MODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Test Face Verification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TechTextPrimaryDark
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TechTextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Student Identity Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F1A28),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Student:", fontSize = 12.sp, color = TechTextSecondaryDark)
                            Text(student.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TechTextPrimaryDark)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Student ID:", fontSize = 12.sp, color = TechTextSecondaryDark)
                            Text(student.studentId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Enrolled Face:", fontSize = 12.sp, color = TechTextSecondaryDark)
                            Text(
                                text = if (student.faceEnrolled) "${student.name}'s registered face" else "Not enrolled",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (student.faceEnrolled) Color(0xFFA7F3D0) else TechAccentRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Camera or Tested Face Preview Frame
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF070F18))
                        .border(
                            2.dp,
                            when (testResult) {
                                is FaceVerificationResult.Match -> TechAccentGreen
                                is FaceVerificationResult.NoMatch -> TechAccentRed
                                else -> CyanPrimary
                            },
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (testBitmap != null) {
                        Image(
                            bitmap = testBitmap!!.asImageBitmap(),
                            contentDescription = "Test Face Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LiveCameraFeed(
                            modifier = Modifier.fillMaxSize(),
                            useFrontCamera = true,
                            onPreviewViewCreated = { previewView ->
                                previewViewRef = previewView
                            },
                            overlayText = "Position face to test against ${student.name}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Result Card (MATCH or NO MATCH)
                if (testResult != null) {
                    when (val res = testResult) {
                        is FaceVerificationResult.Match -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TechAccentGreen.copy(alpha = 0.2f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(TechAccentGreen, Color.Transparent)),
                                    width = 1.dp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TechAccentGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Match ✓",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TechAccentGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Similarity: ${(res.similarity * 100).toInt()}% • Face matches registered profile for ${student.name}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }
                        }
                        is FaceVerificationResult.NoMatch -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TechAccentRed.copy(alpha = 0.2f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(TechAccentRed, Color.Transparent)),
                                    width = 1.dp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = TechAccentRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "No Match ✕",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TechAccentRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "This face does not match the registered face for this student.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFCA5A5)
                                    )
                                }
                            }
                        }
                        is FaceVerificationResult.NotEnrolled -> {
                            Text(res.message, color = Color(0xFFFBBF24), fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                        is FaceVerificationResult.Error -> {
                            Text(res.message, color = TechAccentRed, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                        null -> {}
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Capture or Upload photo for testing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val frame = previewViewRef?.bitmap
                            if (frame != null) {
                                testBitmap = frame
                                testResult = onTestFace(frame)
                            } else {
                                cameraFallbackLauncher.launch(null)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Capture & Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // If enrolled photo exists, allow 1-click test with student's own registered photo (Expected MATCH)
                if (enrolledBitmap != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            testBitmap = enrolledBitmap
                            testResult = onTestFace(enrolledBitmap!!)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TechAccentGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test with ${student.name}'s Enrolled Photo (Expected: Match ✓)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Student Attendance Face Verification Dialog:
 * Verifies the presented face strictly against the logged-in Student ID.
 * Shows live front-camera feed in clean rounded rectangle with correct aspect ratio.
 * Returns MATCH -> Face Verified ✓ or NO MATCH -> Face Verification Failed ✕.
 */
@Composable
fun StudentFaceVerificationDialog(
    student: StudentEntity,
    allStudents: List<StudentEntity>,
    onDismiss: () -> Unit,
    onVerificationSuccess: (faceSignature: String) -> Unit,
    onVerificationFailed: (message: String) -> Unit,
    onManualOverride: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentVerificationResult by remember { mutableStateOf<FaceVerificationResult?>(null) }
    var presentedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    fun executeVerification(bitmap: Bitmap) {
        presentedBitmap = bitmap
        isVerifying = true
        val result = FaceBiometricManager.verifyFaceAgainstStudent(
            capturedBitmap = bitmap,
            targetStudentId = student.studentId,
            registeredSignature = student.faceVectorSignature,
            isEnrolled = student.faceEnrolled
        )
        currentVerificationResult = result
        isVerifying = false

        when (result) {
            is FaceVerificationResult.Match -> {
                onVerificationSuccess(student.faceVectorSignature ?: "MATCH")
            }
            is FaceVerificationResult.NoMatch -> {
                onVerificationFailed("Face verification failed.\nThis face does not match the registered face for this student.")
            }
            is FaceVerificationResult.NotEnrolled -> {
                onVerificationFailed(result.message)
            }
            is FaceVerificationResult.Error -> {
                onVerificationFailed(result.message)
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = FaceBiometricManager.decodeBitmapFromUri(context, uri)
            if (bmp != null) {
                executeVerification(bmp)
            }
        }
    }

    val cameraFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            executeVerification(bmp)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TechCardDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(CyanPrimary, TechCardBorderDark)),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamCuriosityLogoBadge(size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BIOMETRIC VERIFICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Student Face Verification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TechTextPrimaryDark
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TechTextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Logged-in Student Account
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F1A28),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Logged-in Student: ${student.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimaryDark
                            )
                            Text(
                                text = "Student ID: ${student.studentId}",
                                fontSize = 11.sp,
                                color = CyanPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (student.faceEnrolled) TechAccentGreen.copy(alpha = 0.2f) else TechAccentRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (student.faceEnrolled) "Profile Registered ✓" else "Not Registered ✕",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (student.faceEnrolled) TechAccentGreen else TechAccentRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Camera Feed Frame: Clean rounded rectangle with correct aspect ratio
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF070F18))
                        .border(
                            2.dp,
                            when (currentVerificationResult) {
                                is FaceVerificationResult.Match -> TechAccentGreen
                                is FaceVerificationResult.NoMatch -> TechAccentRed
                                else -> CyanPrimary
                            },
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (presentedBitmap != null) {
                        // Display captured frame
                        Image(
                            bitmap = presentedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Face",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Live front-facing camera preview with "Position your face inside the frame"
                        LiveCameraFeed(
                            modifier = Modifier.fillMaxSize(),
                            useFrontCamera = true,
                            onPreviewViewCreated = { previewView ->
                                previewViewRef = previewView
                            },
                            overlayText = "Position your face inside the frame"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Feedback
                val res = currentVerificationResult
                if (res != null) {
                    when (res) {
                        is FaceVerificationResult.Match -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TechAccentGreen.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Face verified ✓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TechAccentGreen
                                    )
                                    Text(
                                        text = "Face matches registered profile for ${student.name} (${student.studentId})",
                                        fontSize = 11.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }
                        }
                        is FaceVerificationResult.NoMatch -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TechAccentRed.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Face does not match the registered student ✕",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TechAccentRed
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Face verification failed.\nThis face does not match the registered face for this student.",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFCA5A5),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        is FaceVerificationResult.NotEnrolled -> {
                            Text(
                                text = res.message,
                                color = TechAccentRed,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        is FaceVerificationResult.Error -> {
                            Text(text = res.message, color = TechAccentRed, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Primary Button: [Capture / Verify Face]
                Button(
                    onClick = {
                        val frame = previewViewRef?.bitmap
                        if (frame != null) {
                            executeVerification(frame)
                        } else {
                            cameraFallbackLauncher.launch(null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Capture / Verify Face", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary: Upload Face Photo or Manual Override
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload Face Photo", color = CyanPrimary, fontSize = 11.sp)
                    }

                    if (onManualOverride != null) {
                        TextButton(onClick = onManualOverride) {
                            Text("Mentor Manual Override", color = TechTextSecondaryDark, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
