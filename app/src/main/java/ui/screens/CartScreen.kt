package cl.duoc.level_up_mobile.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.CarritoItem
import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.repository.carrito.CarritoRepository
import cl.duoc.level_up_mobile.utils.ImageLoader
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove

// UserStatusCard primero
@Composable
fun UserStatusCard(
    currentUser: User?,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (currentUser != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentUser != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Usuario logueado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "✅ Sesión iniciada",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            currentUser.email ?: "Usuario",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Login,
                        contentDescription = "Iniciar sesión",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🔒 Inicia sesión para comprar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Necesitas una cuenta para finalizar tu compra",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onLoginRequired) {
                        Text("Iniciar Sesión")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    carritoRepository: CarritoRepository,
    context: Context,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    currentUser: User?,
    onLoginRequired: () -> Unit
) {
    val carritoItems by carritoRepository.obtenerCarrito().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", modifier = Modifier.padding(end = 8.dp))
                        Text("Mi Carrito")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            if (carritoItems.isNotEmpty()) {
                // ✅ USAR obtenerSubtotal() que ya maneja la conversión
                val total = carritoItems.sumOf { it.obtenerSubtotal() }
                CartBottomBar(
                    totalItems = carritoItems.sumOf { it.cantidad },
                    totalPrecio = total,
                    onCheckoutClick = onCheckoutClick,
                    onClearCart = {
                        coroutineScope.launch {
                            carritoRepository.limpiarCarrito()
                        }
                    },
                    currentUser = currentUser,
                    onLoginRequired = onLoginRequired
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            UserStatusCard(
                currentUser = currentUser,
                onLoginRequired = onLoginRequired,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (carritoItems.isEmpty()) {
                EmptyCartView(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(carritoItems) { item ->
                        CartItemCard(
                            item = item,
                            onUpdateQuantity = { codigo, nuevaCantidad ->
                                coroutineScope.launch {
                                    carritoRepository.actualizarCantidad(codigo, nuevaCantidad)
                                }
                            },
                            onRemoveItem = { codigo ->
                                coroutineScope.launch {
                                    carritoRepository.eliminarProducto(codigo)
                                }
                            },
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = "Carrito vacío",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tu carrito está vacío",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Agrega algunos productos para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: CarritoItem,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    context: Context
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            val imageBitmap = remember(item.productoImagenUrl) {
                try {
                    ImageLoader.loadImageFromAssets(context, item.productoImagenUrl)
                } catch (e: Exception) {
                    null
                }
            }

            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = item.productoNombre,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = "Sin imagen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Información del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productoNombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )

                // ✅ MOSTRAR PRECIO ORIGINAL (String)
                Text(
                    item.productoPrecio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // ✅ MOSTRAR SUBTOTAL CALCULADO
                Text(
                    "Subtotal: $${String.format("%,.0f", item.obtenerSubtotal())} CLP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Contador de cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (item.cantidad > 1) {
                                onUpdateQuantity(item.productoCodigo, item.cantidad - 1)
                            }
                        },
                        modifier = Modifier.size(36.dp),
                        enabled = item.cantidad > 1
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Disminuir",
                            tint = if (item.cantidad > 1) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        item.cantidad.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(30.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            onUpdateQuantity(item.productoCodigo, item.cantidad + 1)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Aumentar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Botón eliminar
            IconButton(
                onClick = { onRemoveItem(item.productoCodigo) }
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CartBottomBar(
    totalItems: Int,
    totalPrecio: Double,
    onCheckoutClick: () -> Unit,
    onClearCart: () -> Unit,
    currentUser: User?,
    onLoginRequired: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Total items: $totalItems")
                Text(
                    "$${String.format("%,.0f", totalPrecio)} CLP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearCart,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar Carrito")
                }

                Button(
                    onClick = {
                        if (currentUser != null) {
                            onCheckoutClick()
                        } else {
                            onLoginRequired()
                        }
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        if (currentUser != null) "Proceder al Pago" else "Iniciar Sesión para Comprar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}