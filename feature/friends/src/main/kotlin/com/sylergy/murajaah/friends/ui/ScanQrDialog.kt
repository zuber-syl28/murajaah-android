package com.sylergy.murajaah.friends.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.sylergy.murajaah.network.model.InvitePreviewDto
import java.util.concurrent.Executors

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)
private val CrimsonError = Color(0xFF8B261D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrDialog(
    isLoading: Boolean,
    preview: InvitePreviewDto?,
    errorMessage: String?,
    onScan: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputUrl by remember { mutableStateOf("") }
    var hasScanned by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ParchmentBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "مسح رمز أو انضمام",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )
            Text(
                text = "Scan QR Code or Enter Companion Link",
                fontSize = 13.sp,
                color = MutedInk,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            if (preview != null) {
                // Show confirmation card to join this companion
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightParchment)
                        .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "انضمام إلى مراجعة",
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${preview.studentName} invites you to mutual Quran revision.",
                            fontSize = 14.sp,
                            color = DeepBrown,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Both of you will be able to recite to each other and assess retention.",
                            fontSize = 12.sp,
                            color = MutedInk,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onAccept(preview.token) },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("قبول والاتصال / Connect Now", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Camera Viewfinder or Permission Request
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, ForestGreen, RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val previewUseCase = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val reader = MultiFormatReader().apply {
                                            setHints(
                                                mapOf(
                                                    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                                                    DecodeHintType.CHARACTER_SET to "UTF-8"
                                                )
                                            )
                                        }

                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null && !hasScanned) {
                                                val buffer = mediaImage.planes[0].buffer
                                                val bytes = ByteArray(buffer.remaining())
                                                buffer.get(bytes)
                                                val source = PlanarYUVLuminanceSource(
                                                    bytes, imageProxy.width, imageProxy.height,
                                                    0, 0, imageProxy.width, imageProxy.height, false
                                                )
                                                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                                                try {
                                                    val result = reader.decodeWithState(binaryBitmap)
                                                    if (result != null && result.text.isNotBlank()) {
                                                        hasScanned = true
                                                        Handler(Looper.getMainLooper()).post {
                                                            onScan(result.text)
                                                        }
                                                    }
                                                } catch (_: Exception) {
                                                    // No QR code detected in this frame
                                                } finally {
                                                    reader.reset()
                                                }
                                            }
                                            imageProxy.close()
                                        }

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            previewUseCase,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, BorderParchment, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📷", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera permission needed to scan QR",
                                fontSize = 12.sp,
                                color = DeepBrown,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Enable Camera · تفعيل الكاميرا", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "— OR PASTE LINK / TOKEN —",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("https://murajaah.../join/... or token", fontSize = 13.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightParchment,
                        unfocusedContainerColor = LightParchment,
                        focusedIndicatorColor = ForestGreen,
                        unfocusedIndicatorColor = BorderParchment
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = CrimsonError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onScan(inputUrl.trim()) },
                        enabled = inputUrl.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("انضمام / Join", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء / Cancel", color = MutedInk, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
