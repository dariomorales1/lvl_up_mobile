package cl.duoc.level_up_mobile.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.utils.ImageLoader
import androidx.compose.foundation.background


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    productoRepository: ProductoRepository,
    onMenuClick: () -> Unit,
    onProductClick: (Producto) -> Unit,
    onCartClick: () -> Unit,
    context: Context,
    onAddToCart: (Producto) -> Unit
) {
    val productosDestacados = productoRepository.obtenerProductosDestacados()
    val cartItemCount = 0

    // Estado para la búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filtrar productos basado en la búsqueda
    val productosFiltrados = remember(searchQuery, productosDestacados) {
        if (searchQuery.isEmpty()) {
            productosDestacados
        } else {
            productoRepository.buscarProductos(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                // TopBar de búsqueda activa
                SearchTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchActiveChange = { isSearchActive = it },
                    onBackClick = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else {
                // TopBar normal
                NormalTopBar(
                    onMenuClick = onMenuClick,
                    onCartClick = onCartClick,
                    onSearchClick = { isSearchActive = true },
                    cartItemCount = cartItemCount
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Título condicional
            if (searchQuery.isNotEmpty()) {
                Text(
                    "🔍 Resultados para \"$searchQuery\"",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    "⭐ Productos Destacados",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Mostrar mensaje si no hay resultados
            if (searchQuery.isNotEmpty() && productosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = "Sin resultados",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se encontraron productos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Intenta con otros términos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                ProductosGrid(
                    productos = productosFiltrados,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    context = context
                )
            }
        }
    }
}

@Composable
fun ProductosGrid(
    productos: List<Producto>,
    onProductClick: (Producto) -> Unit,
    onAddToCart: (Producto) -> Unit,
    context: Context
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productos) { producto ->
            ProductoCard(
                producto = producto,
                context = context,
                onAddToCart = onAddToCart,
                onClick = { onProductClick(producto) }
            )
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    context: Context,
    onAddToCart: (Producto) -> Unit,
    onClick: () -> Unit
) {
    val imageBitmap = remember(producto.imagenUrl) {
        ImageLoader.loadImageFromAssets(context, producto.imagenUrl)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
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
                        bitmap = imageBitmap,
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

                // Badge de rating en esquina superior derecha
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

            // Información del producto
            Column(
                modifier = Modifier
                    .padding(12.dp)
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

                // Botón rápido de añadir al carrito - ACTUALIZADO
                Button(
                    onClick = { onAddToCart(producto) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
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
                        "Añadir",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTopBar(
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    cartItemCount: Int
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎮", modifier = Modifier.padding(end = 8.dp))
                Text("Level-Up Gamer")
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            // Icono de búsqueda
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar productos")
            }

            // Icono de carrito con badge
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
                        Text(cartItemCount.toString())
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            SearchTextField(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            // Botón para limpiar búsqueda
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        placeholder = { Text("Buscar productos...") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Buscar")
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { /* Acción al presionar buscar */ }
        )
    )
}