package cl.duoc.level_up_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cl.duoc.level_up_mobile.model.Cart
import cl.duoc.level_up_mobile.model.CartItem
import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.repository.carrito.CartRepositoryRemote
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartRepository: CartRepositoryRemote,
    currentUser: User?,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onLoginRequired: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var cart by remember { mutableStateOf<Cart?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val userId = currentUser?.uid

    // Formato CLP
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }
    }

    // Cargar carrito cuando haya usuario
    LaunchedEffect(userId) {
        if (userId != null) {
            isLoading = true
            errorMessage = null
            try {
                cart = cartRepository.getUserCart(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al cargar el carrito"
            } finally {
                isLoading = false
            }
        } else {
            cart = null
        }
    }

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
            val items = cart?.items.orEmpty()
            if (items.isNotEmpty()) {
                val totalCents = cart?.totalAmountCents
                    ?: items.sumOf { it.subtotalCents }
                CartBottomBarRemote(
                    totalItems = items.sumOf { it.quantity },
                    totalPrecioCents = totalCents,
                    currencyFormatter = currencyFormatter,
                    currentUser = currentUser,
                    onClearCart = {
                        if (userId == null) {
                            onLoginRequired()
                        } else {
                            scope.launch {
                                try {
                                    val ok = cartRepository.clearUserCart(userId)
                                    if (ok) {
                                        cart = cart?.copy(items = emptyList(), totalAmountCents = 0L)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    onCheckoutClick = {
                        if (currentUser != null) {
                            onCheckoutClick()
                        } else {
                            onLoginRequired()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            UserStatusCardRemote(
                currentUser = currentUser,
                onLoginRequired = onLoginRequired,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(errorMessage ?: "Error desconocido")
                    }
                }

                userId == null -> {
                    EmptyCartViewRemote(
                        message = "Inicia sesión para ver tu carrito",
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        onLoginRequired = onLoginRequired
                    )
                }

                cart == null || cart?.items.isNullOrEmpty() -> {
                    EmptyCartViewRemote(
                        message = "Tu carrito está vacío",
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        onLoginRequired = null
                    )
                }

                else -> {
                    val items = cart!!.items
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items) { item ->
                            CartItemCardRemote(
                                item = item,
                                currencyFormatter = currencyFormatter,
                                onUpdateQuantity = { newQuantity ->
                                    if (userId == null) {
                                        onLoginRequired()
                                        return@CartItemCardRemote
                                    }
                                    scope.launch {
                                        try {
                                            val updated = cartRepository.updateUserItemQuantity(
                                                userId = userId,
                                                productId = item.productId, // 👈 String NO null
                                                quantity = newQuantity
                                            )
                                            if (updated != null) {
                                                cart = updated
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                onRemoveItem = {
                                    if (userId == null) {
                                        onLoginRequired()
                                        return@CartItemCardRemote
                                    }
                                    scope.launch {
                                        try {
                                            val ok = cartRepository.removeItemFromUserCart(
                                                userId = userId,
                                                productId = item.productId  // 👈 String NO null
                                            )
                                            if (ok) {
                                                val updatedItems = cart!!.items.filterNot {
                                                    it.productId == item.productId
                                                }
                                                val newTotal = updatedItems.sumOf { it.subtotalCents }
                                                cart = cart!!.copy(
                                                    items = updatedItems,
                                                    totalAmountCents = newTotal
                                                )
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserStatusCardRemote(
    currentUser: User?,
    onLoginRequired: (() -> Unit)?,
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
                            "Sesión iniciada",
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
                            "Inicia sesión para comprar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Necesitas una cuenta para finalizar tu compra",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (onLoginRequired != null) {
                        Button(onClick = onLoginRequired) {
                            Text("Iniciar Sesión")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartViewRemote(
    message: String,
    modifier: Modifier = Modifier,
    onLoginRequired: (() -> Unit)?
) {
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
                message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (onLoginRequired != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onLoginRequired) {
                    Text("Iniciar Sesión")
                }
            }
        }
    }
}

@Composable
fun CartItemCardRemote(
    item: CartItem,
    currencyFormatter: NumberFormat,
    onUpdateQuantity: (Int) -> Unit,
    onRemoveItem: () -> Unit
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
            AsyncImage(
                model = item.imagenUrl,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )

                Text(
                    currencyFormatter.format(item.unitPriceCents / 100.0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Subtotal: ${currencyFormatter.format(item.subtotalCents / 100.0)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (item.quantity > 1) {
                                onUpdateQuantity(item.quantity - 1)
                            }
                        },
                        enabled = item.quantity > 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Disminuir",
                            tint = if (item.quantity > 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        item.quantity.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(30.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { onUpdateQuantity(item.quantity + 1) },
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

            IconButton(onClick = onRemoveItem) {
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
fun CartBottomBarRemote(
    totalItems: Int,
    totalPrecioCents: Long,
    currencyFormatter: NumberFormat,
    currentUser: User?,
    onClearCart: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    val totalPesos = totalPrecioCents / 100.0

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
                    currencyFormatter.format(totalPesos),
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
                    Text(
                        if (currentUser != null)
                            "Proceder al Pago"
                        else
                            "Iniciar Sesión para Comprar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
