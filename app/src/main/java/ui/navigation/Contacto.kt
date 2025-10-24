package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun Contacto(
    innerPadding: PaddingValues,
    onCancel: () -> Unit = {},
    onSend: (nombre: String, correo: String, mensaje: String) -> Unit = { _, _, _ -> },
    onValidationError: (String) -> Unit = {}
) {
    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var correo by remember { mutableStateOf(TextFieldValue("")) }
    var mensaje by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("¿Tienes dudas o sugerencias?", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            label = { Text("Mensaje") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // CANCELAR
            LettuceButton(
                text = "CANCELAR",
                onClick = {
                    nombre = TextFieldValue("")
                    correo = TextFieldValue("")
                    mensaje = TextFieldValue("")
                    onCancel()
                },
                leading = { Icon(Icons.Default.Clear, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )

            // ENVIAR
            LettuceButton(
                text = "ENVIAR",
                onClick = {
                    val ok = nombre.text.isNotBlank() &&
                            correo.text.contains("@") &&
                            mensaje.text.length >= 10
                    if (ok) {
                        onSend(nombre.text, correo.text, mensaje.text)
                        // Limpia después de enviar
                        nombre = TextFieldValue("")
                        correo = TextFieldValue("")
                        mensaje = TextFieldValue("")
                    } else {
                        onValidationError("Completa nombre, correo válido y mensaje (≥ 10 caracteres).")
                    }
                },
                leading = { Icon(Icons.Default.Send, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Botón "verde lechuga" con animación (escala + elevación al presionar).
 * Verde lechuga aproximado: #76FF03 (también puedes probar #7CFC00).
 */
@Composable
fun LettuceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "scale")
    val elevation by animateDpAsState(if (pressed) 2.dp else 6.dp, label = "elevation")

    Button(
        onClick = onClick,
        interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF76FF03),   // verde lechuga
            contentColor = Color(0xFF0B1F0A)      // texto verde muy oscuro para contraste
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 2.dp
        ),
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}





