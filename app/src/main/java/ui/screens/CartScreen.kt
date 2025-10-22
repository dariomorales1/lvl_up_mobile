package cl.duoc.level_up_mobile.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.CarritoItem
import cl.duoc.level_up_mobile.repository.carrito.CarritoRepository
import cl.duoc.level_up_mobile.utils.ImageLoader
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    carritoRepository: CarritoRepository,
    context: Context,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit
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
                CartBottomBar(
                    totalItems = carritoItems.sumOf { it.cantidad },
                    totalPrecio = carritoItems.sumOf { it.obtenerSubtotal() },
                    onCheckoutClick = onCheckoutClick,
                    onClearCart = {
                        coroutineScope.launch {
                            carritoRepository.limpiarCarrito()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (carritoItems.isEmpty()) {
            EmptyCartView(modifier = Modifier.padding(innerPadding))
        } else {
            CartItemsList(
                items = carritoItems,
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
                context = context,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            Text(
                "Agrega algunos productos para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CartItemsList(
    items: List<CarritoItem>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            CartItemCard(
                item = item,
                onUpdateQuantity = onUpdateQuantity,
                onRemoveItem = onRemoveItem,
                context = context
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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            val imageBitmap = remember(item.productoImagenUrl) {
                ImageLoader.loadImageFromAssets(context, item.productoImagenUrl)
            }

            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = item.productoNombre,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp)
                )
            }

            // Información del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productoNombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Text(
                    item.productoPrecio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Contador de cantidad - VERSIÓN CON ÍCONOS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Botón - con ícono
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

                    // Cantidad
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

                    // Botón + con ícono
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
    onClearCart: () -> Unit
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
                    onClick = onCheckoutClick,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Proceder al Pago", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}