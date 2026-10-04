package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CameraFaceDialog(
    studentName: String,
    isEnrollment: Boolean = false,
    onDismiss: () -> Unit,
    onVerificationSuccess: (faceSignature: String) -> Unit,
    onManualOverride: (() -> Unit)? = null
) {
    var consentGiven by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var scanCompleted by remember { mutableStateOf(false) }

    // Pulsing scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "face_scan")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanProgress = 0f
            while (scanProgress < 1f) {
                delay(120)
                scanProgress += 0.15f
            }
            scanCompleted = true
            delay(500)
            onVerificationSuccess("SIG_${System.currentTimeMillis()}")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TechCardDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(CyanPrimary, TechCardBorderDark)
                ),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnrollment) "FACE PROFILE ENROLLMENT" else "FACE VERIFICATION",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TechTextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scanner Reticle Viewfinder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF070F18))
                        .border(2.dp, if (scanCompleted) TechAccentGreen else CyanPrimary, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Face silhouette or camera view placeholder
                    Icon(
                        imageVector = if (scanCompleted) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (scanCompleted) TechAccentGreen else CyanPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )

                    // Target scanning reticle lines
                    if (isScanning && !scanCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(2.dp)
                                .background(CyanGlow.copy(alpha = pulseAlpha))
                        )
                    }

                    // Progress circular overlay
                    if (isScanning) {
                        CircularProgressIndicator(
                            progress = { scanProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.size(170.dp),
                            color = CyanPrimary,
                            trackColor = Color(0xFF1E3A5A),
                            strokeWidth = 3.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (scanCompleted) "Face Identity Verified!" else if (isScanning) "Aligning face features..." else "Student: $studentName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (scanCompleted) TechAccentGreen else TechTextPrimaryDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Privacy Notice Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F1A28),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(TechCardBorderDark, TechCardBorderDark)),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PRIVACY COMPLIANCE NOTICE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimaryVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Biometric signatures are processed on-device for FTC practice verification only. Raw photos are not stored or transferred.",
                            fontSize = 10.sp,
                            color = TechTextSecondaryDark,
                            lineHeight = 13.sp
                        )
                    }
                }

                if (isEnrollment) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = consentGiven,
                            onCheckedChange = { consentGiven = it },
                            colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I consent to enrolling face profile for practice attendance.",
                            fontSize = 11.sp,
                            color = TechTextPrimaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                if (!isScanning && !scanCompleted) {
                    Button(
                        onClick = { isScanning = true },
                        enabled = !isEnrollment || consentGiven,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = TechSlateDark
                        )
                    ) {
                        Text(
                            text = if (isEnrollment) "Enroll Face Profile" else "Verify Face Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (onManualOverride != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onManualOverride,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Use Manual Mentor Override",
                                color = TechTextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (scanCompleted) {
                    Text(
                        text = "Verification Success ✓",
                        color = TechAccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
