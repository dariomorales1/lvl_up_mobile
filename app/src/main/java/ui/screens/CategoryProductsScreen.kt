package cl.duoc.level_up_mobile.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.utils.ImageLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoria: String,
    productoRepository: ProductoRepository,
    context: Context,
    onBackClick: () -> Unit,
    onProductClick: (Producto) -> Unit,
    onAddToCart: (Producto) -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int,
    currentUser: User? = null,
    onLoginRequired: () -> Unit = {}
) {
    val productosCategoria = productoRepository.obtenerProductosPorCategoria(categoria)

    LaunchedEffect(currentUser) {
        Log.d("CategoryProducts", "Usuario en CategoryProducts: ${currentUser?.email ?: "NO LOGUEADO"}")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(categoria, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${productosCategoria.size} productos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        currentUser?.email?.let { email ->
                            Text(
                                "${email}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                        if (cartItemCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-8).dp),
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    if (cartItemCount > 9) "9+" else cartItemCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
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
        ) {
            if (productosCategoria.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay productos en esta categoría",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CategoryProductsGrid(
                    productos = productosCategoria,
                    onProductClick = onProductClick,
                    onAddToCart = { producto ->
                        if (currentUser == null) {
                            Log.d("CategoryProducts", "Usuario no logueado, requiriendo login")
                            onLoginRequired()
                        } else {
                            Log.d("CategoryProducts", "Usuario logueado, añadiendo producto: ${producto.nombre}")
                            onAddToCart(producto)
                        }
                    },
                    context = context,
                    currentUser = currentUser
                )
            }
        }
    }
}

@Composable
fun CategoryProductsGrid(
    productos: List<Producto>,
    onProductClick: (Producto) -> Unit,
    onAddToCart: (Producto) -> Unit,
    context: Context,
    currentUser: User?
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productos) { producto ->
            CategoryProductCard(
                producto = producto,
                context = context,
                onAddToCart = onAddToCart,
                onClick = { onProductClick(producto) },
                currentUser = currentUser
            )
        }
    }
}

@Composable
fun CategoryProductCard(
    producto: Producto,
    context: Context,
    onAddToCart: (Producto) -> Unit,
    onClick: () -> Unit,
    currentUser: User?
) {
    val imageBitmap = remember(producto.imagenUrl) {
        ImageLoader.loadImageFromAssets(context, producto.imagenUrl)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap, // SIN .asImageBitmap() - ya debe ser ImageBitmap
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            producto.puntuacion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    producto.precio,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    producto.descripcionCorta,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.9
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        Log.d("CategoryProductCard", "Botón presionado - Usuario: ${currentUser?.email ?: "NO LOGUEADO"}")
                        onAddToCart(producto)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Icon(
                        Icons.Filled.AddShoppingCart,
                        contentDescription = "Añadir al carrito",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        if (currentUser != null) "Añadir" else "Inicia sesión",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}