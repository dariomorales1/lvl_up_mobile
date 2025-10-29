package cl.duoc.level_up_mobile.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import android.graphics.Matrix
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.User
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.media.ExifInterface
import androidx.core.content.ContextCompat
import android.os.Build

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    context: Context,
    currentUser: User?,
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }



    fun rotateBitmapIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
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

    fun getProfileImageFile(context: Context): File {
        val storageDir = File(context.filesDir, "profile_images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "user_profile.jpg")
    }

    fun loadSavedProfileImage(context: Context): Bitmap? {
        return try {
            val profileFile = getProfileImageFile(context)
            if (profileFile.exists()) {
                BitmapFactory.decodeFile(profileFile.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    LaunchedEffect(Unit) {
        loadSavedProfileImage(context)?.let { bitmap ->
            profileBitmap = bitmap
        }
    }

    fun createTempImageFile(context: Context): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.cacheDir
            File.createTempFile(
                "TEMP_${timeStamp}_",
                ".jpg",
                storageDir
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    lateinit var startCameraFunc: () -> Unit

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && profileImageUri != null) {
            val bitmap = loadBitmapFromUri(profileImageUri!!)
            if (bitmap != null) {
                val rotatedBitmap = rotateBitmapIfRequired(bitmap, profileImageUri!!)

                val permanentFile = getProfileImageFile(context)
                if (saveBitmapToFile(rotatedBitmap, permanentFile)) {
                    profileBitmap = rotatedBitmap
                    onShowSnackbar("Foto de perfil guardada")
                } else {
                    onShowSnackbar("Error al guardar la imagen")
                }
            } else {
                onShowSnackbar("Error al cargar la imagen")
            }
        } else {
            //onShowSnackbar("Error al tomar la foto")
        }
        isLoading = false

        profileImageUri?.let { uri ->
            try {
                val file = File(uri.path!!)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCameraFunc()
            //permissionAlreadyGranted(permissionAlreadyGranted = true)
        } else {
            onShowSnackbar("Permiso de cámara denegado.")
            isLoading = false
        }
    }

    startCameraFunc = {
        isLoading = true

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val tempFile = createTempImageFile(context)
                if (tempFile != null) {
                    val photoUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        tempFile
                    )
                    profileImageUri = photoUri
                    cameraLauncher.launch(photoUri)
                } else {
                    onShowSnackbar("Error al crear archivo temporal")
                    isLoading = false
                }
            } catch (e: Exception) {
                onShowSnackbar("Error: ${e.message ?: "No se pudo abrir la cámara"}")
                isLoading = false
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun deleteProfileImage() {
        try {
            val profileFile = getProfileImageFile(context)
            if (profileFile.exists()) {
                profileFile.delete()
            }
            profileBitmap = null
            onShowSnackbar("Foto de perfil eliminada")
        } catch (e: Exception) {
            onShowSnackbar("Error al eliminar la foto")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (profileBitmap != null) {
                        IconButton(
                            onClick = { deleteProfileImage() }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar foto",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp)
            ) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap!!.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (!isLoading) {
                            startCameraFunc()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        title = "Nombre",
                        value = currentUser?.displayName ?: "No especificado"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        title = "Email",
                        value = currentUser?.email ?: "No disponible"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Fingerprint,
                        title = "ID de usuario",
                        value = currentUser?.uid?.take(8) + "..." ?: "No disponible"
                    )
                }
            }

            Text(
                "Estadísticas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    icon = Icons.Default.ShoppingCart,
                    value = "12",
                    label = "Compras"
                )
                StatCard(
                    icon = Icons.Default.Favorite,
                    value = "8",
                    label = "Favoritos"
                )
                StatCard(
                    icon = Icons.Default.Star,
                    value = "4.8",
                    label = "Rating"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onShowSnackbar("Editar perfil estará disponible próximamente")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                Icon(
                    Icons.Default.Logout,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}