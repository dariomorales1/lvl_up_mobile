package cl.duoc.level_up_mobile.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository

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
    cartItemCount: Int
) {
    val productosCategoria = productoRepository.obtenerProductosPorCategoria(categoria)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(categoria, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${productosCategoria.size} productos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Icono de carrito FUNCIONAL
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onCartClick) { // ← AHORA FUNCIONA
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

                // Grid de productos
                ProductosGrid(
                    productos = productosCategoria,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    context = context
                )
            }
        }
    }
}