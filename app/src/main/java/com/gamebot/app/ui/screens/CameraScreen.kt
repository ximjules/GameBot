package com.gamebot.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gamebot.app.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var setupName by remember { mutableStateOf("") }
    var setupSpecs by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadSuccess by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Natrag",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Dodaj setup",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasCameraPermission) {
            // Nema dozvole
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Kamera",
                        tint = TextHint,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Potrebna je dozvola za kameru",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Dozvoli kameru", color = TextPrimary)
                    }
                }
            }
        } else if (capturedImageUri == null) {
            // Kamera preview
            CameraPreview(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                }
            )
        } else {
            // Pregled snimljene slike
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = capturedImageUri,
                    contentDescription = "Snimljeni setup",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Retake gumb
                IconButton(
                    onClick = { capturedImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(BackgroundDark.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Ponovi",
                        tint = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Forma za unos
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "Naziv setupa", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = setupName,
                onValueChange = { setupName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Npr. Moj gaming kutak", color = TextHint, fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Specifikacije", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = setupSpecs,
                onValueChange = { setupSpecs = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Napiši svoje specifikacije...", color = TextHint, fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Upload gumb
            if (uploadSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SuccessGreen.copy(alpha = 0.2f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✅ Setup uspješno objavljen!",
                        fontSize = 14.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (capturedImageUri != null && setupName.isNotEmpty()) {
                            isUploading = true
                            uploadSetup(
                                context = context,
                                imageUri = capturedImageUri!!,
                                setupName = setupName,
                                setupSpecs = setupSpecs,
                                onSuccess = {
                                    isUploading = false
                                    uploadSuccess = true
                                },
                                onFailure = {
                                    isUploading = false
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (capturedImageUri != null && setupName.isNotEmpty())
                            PrimaryPurple else SurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Objavi setup",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CameraPreview(onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gumb za snimanje
        IconButton(
            onClick = {
                val photoFile = File(
                    context.cacheDir,
                    SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                        .format(System.currentTimeMillis()) + ".jpg"
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(Uri.fromFile(photoFile))
                        }
                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimaryPurple)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Snimi",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

fun uploadSetup(
    context: Context,
    imageUri: Uri,
    setupName: String,
    setupSpecs: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    if (uid == null) {
        onFailure()
        return
    }
    val prefs = context.getSharedPreferences("gamebot_prefs", Context.MODE_PRIVATE)
    val username = prefs.getString("username", auth.currentUser?.email?.substringBefore("@") ?: "Player")
        ?: "Player"
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
        .child("setups/$uid/${System.currentTimeMillis()}.jpg")

    storageRef.putFile(imageUri)
        .addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val db = FirebaseFirestore.getInstance()
                val setup = hashMapOf(
                    "userId" to uid,
                    "name" to setupName,
                    "specs" to setupSpecs,
                    "imageUrl" to downloadUrl.toString(),
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("setups").add(setup)
                    .addOnSuccessListener {
                        val postText = buildString {
                            append(setupName)
                            if (setupSpecs.isNotBlank()) {
                                append("\n\n")
                                append(setupSpecs)
                            }
                        }
                        val post = hashMapOf(
                            "userId" to uid,
                            "username" to username,
                            "content" to postText,
                            "imageUrl" to downloadUrl.toString(),
                            "createdAt" to System.currentTimeMillis(),
                            "likedBy" to emptyList<String>(),
                            "comments" to 0,
                            "latestComment" to ""
                        )
                        db.collection("communityPosts").add(post)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure() }
                    }
                    .addOnFailureListener { onFailure() }
            }
        }
        .addOnFailureListener { onFailure() }
}
