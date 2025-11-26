package cl.duoc.level_up_mobile.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.level_up_mobile.model.User
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------
// Helpers globales para manejo de imagen de perfil por usuario (local)
// ---------------------------------------------------------------------

fun getProfileImageFile(context: Context, uid: String?): File {
    val storageDir = File(context.filesDir, "profile_images")
    if (!storageDir.exists()) storageDir.mkdirs()
    val safeUid = uid ?: "anonymous"
    return File(storageDir, "user_profile_$safeUid.jpg")
}

fun loadSavedProfileImage(context: Context, uid: String?): Bitmap? {
    return try {
        val file = getProfileImageFile(context, uid)
        if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun createTempImageFile(context: Context): File? {
    return try {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        File.createTempFile("TEMP_${timeStamp}_", ".jpg", storageDir)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun rotateBitmapIfRequired(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
    return try {
        val input = context.contentResolver.openInputStream(uri)
        val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ExifInterface(input!!)
        } else {
            ExifInterface(uri.path!!)
        }
        input?.close()

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
        } else {
            bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        bitmap
    }
}

fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
    return try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// ---------------------------------------------------------------------
// COMPOSABLE PRINCIPAL
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    context: Context,
    currentUser: User?,
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    // ViewModel scopiado por usuario (clave = uid)
    val vm: ProfileViewModel = viewModel(
        key = currentUser?.uid ?: "anonymous_profile"
    )
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingCamera by remember { mutableStateOf(false) }

    // Cada vez que cambia el usuario, recargamos datos y foto local
    LaunchedEffect(currentUser?.uid) {
        vm.loadProfile()
        profileBitmap = loadSavedProfileImage(context, currentUser?.uid)
    }

    // Mensajes desde el ViewModel -> Snackbar
    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            onShowSnackbar(it)
            vm.clearMessage()
        }
        state.error?.let {
            onShowSnackbar(it)
            vm.clearMessage()
        }
    }

    // -------------------- Cámara y captura de foto --------------------

    lateinit var startCameraFunc: () -> Unit

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && profileImageUri != null) {
            val bitmap = try {
                context.contentResolver.openInputStream(profileImageUri!!)?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                e.printStackTrace(); null
            }

            if (bitmap != null) {
                val rotated = rotateBitmapIfRequired(bitmap, profileImageUri!!, context)
                val file = getProfileImageFile(context, currentUser?.uid)
                if (saveBitmapToFile(rotated, file)) {
                    profileBitmap = rotated
                    scope.launch { vm.uploadAvatar(file) }
                    onShowSnackbar("Foto de perfil actualizada")
                } else {
                    onShowSnackbar("Error al guardar la imagen")
                }
            } else {
                onShowSnackbar("Error al cargar la imagen")
            }
        }
        isLoadingCamera = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCameraFunc()
        } else {
            isLoadingCamera = false
            onShowSnackbar("Permiso de cámara denegado")
        }
    }

    startCameraFunc = {
        isLoadingCamera = true

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val temp = createTempImageFile(context)
            if (temp != null) {
                val photoUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    temp
                )
                profileImageUri = photoUri
                cameraLauncher.launch(photoUri)
            } else {
                isLoadingCamera = false
                onShowSnackbar("Error al crear archivo temporal")
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun deleteProfileImage() {
        val file = getProfileImageFile(context, currentUser?.uid)
        if (file.exists()) file.delete()
        profileBitmap = null
        scope.launch { vm.deleteAvatar() }
        onShowSnackbar("Foto de perfil eliminada")
    }

    // ----------------------------- UI ---------------------------------

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- FOTO DE PERFIL ----------
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp)
            ) {
                when {
                    profileBitmap != null -> {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }

                    !state.avatarUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = state.avatarUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                        )
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Foto",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { if (!state.loading && !isLoadingCamera) startCameraFunc() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (state.loading || isLoadingCamera) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto")
                    }
                }
            }

            // ---------- CARD: DATOS PERSONALES ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Datos personales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = state.nombre,
                        onValueChange = vm::onNombreChange,
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.email,
                        enabled = false,
                        onValueChange = {},
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.fechaNacimiento,
                        onValueChange = vm::onFechaNacimientoChange,
                        label = { Text("Fecha de nacimiento (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.Cake, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.direccion,
                        enabled = false,
                        onValueChange = {},
                        label = { Text("Dirección (primera registrada)") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.comuna,
                            enabled = false,
                            onValueChange = {},
                            label = { Text("Comuna") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.region,
                            enabled = false,
                            onValueChange = {},
                            label = { Text("Región") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.pais,
                            enabled = false,
                            onValueChange = {},
                            label = { Text("País") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ---------- ACCIONES ----------
                    Button(
                        onClick = { vm.saveProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.loading
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar cambios")
                    }

                    if (profileBitmap != null || !state.avatarUrl.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = { deleteProfileImage() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            enabled = !state.loading
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Quitar foto de perfil")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- CERRAR SESIÓN ----------
            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onShowSnackbar("¡Hasta pronto!")
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
