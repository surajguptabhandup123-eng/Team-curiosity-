package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.*

/**
 * Live Camera Feed Composable for face verification & enrollment.
 * Uses CameraX PreviewView with front-facing camera in normal resolution,
 * clean rounded rectangle, preserving exact aspect ratio without distortion or pixelation.
 */
@Composable
fun LiveCameraFeed(
    modifier: Modifier = Modifier,
    useFrontCamera: Boolean = true,
    onPreviewViewCreated: (PreviewView) -> Unit = {},
    showReticle: Boolean = true,
    overlayText: String = "Position your face inside the frame"
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraError by remember { mutableStateOf<String?>(null) }
    val currentLensFacing = if (useFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF070F18))
            .border(2.dp, CyanPrimary, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Camera permission is required for face enrollment.",
                    fontSize = 12.sp,
                    color = Color(0xFFFCA5A5),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = TechSlateDark)
                ) {
                    Text("Grant Camera Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (cameraError != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = TechAccentRed,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Camera Feed Unavailable",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechAccentRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cameraError ?: "No camera hardware detected",
                    fontSize = 11.sp,
                    color = TechTextSecondaryDark,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Live CameraX Feed with normal resolution and correct aspect ratio
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    onPreviewViewCreated(previewView)

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val selector = try {
                                val targetSelector = CameraSelector.Builder()
                                    .requireLensFacing(currentLensFacing)
                                    .build()
                                if (cameraProvider.hasCamera(targetSelector)) {
                                    targetSelector
                                } else {
                                    val fallback = if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    }
                                    if (cameraProvider.hasCamera(fallback)) fallback else null
                                }
                            } catch (_: Exception) {
                                null
                            }

                            if (selector != null) {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview
                                )
                            } else {
                                cameraError = "No camera found on this device"
                            }
                        } catch (e: Exception) {
                            cameraError = e.localizedMessage ?: "Camera error"
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = { previewView ->
                    onPreviewViewCreated(previewView)
                }
            )

            // Scanning guide reticle overlay (corner accents)
            if (showReticle) {
                FaceFrameReticleOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Subtitle: "Position your face inside the frame"
            if (overlayText.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC070F18)
                ) {
                    Text(
                        text = overlayText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Clean corner reticle marks for face alignment inside camera preview.
 */
@Composable
fun FaceFrameReticleOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val cornerLength = 22.dp.toPx()
        val color = CyanPrimary.copy(alpha = 0.85f)
        val pad = 16.dp.toPx()

        val left = pad
        val top = pad
        val right = size.width - pad
        val bottom = size.height - pad

        // Top-left corner
        drawLine(color, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(color, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

        // Top-right corner
        drawLine(color, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
        drawLine(color, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)

        // Bottom-left corner
        drawLine(color, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
        drawLine(color, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)

        // Bottom-right corner
        drawLine(color, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
        drawLine(color, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)
    }
}
