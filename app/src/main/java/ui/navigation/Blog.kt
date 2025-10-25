package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Modelo simple
data class BlogPost(
    val id: String,
    val titulo: String,
    val resumen: String,
    val categoria: String,
    val fecha: String,
    val rating: Double
)

private fun samplePosts() = listOf(
    BlogPost("1","Top 10 indies de la semana",
        "Revisamos los lanzamientos que están rompiendo en Steam y Switch...",
        "Tendencias","Hoy",4.6),
    BlogPost("2","Parche 1.2.3 de ‘Galaxy Raid’",
        "Balance a clases, nuevas misiones y modo foto. Te contamos lo bueno y lo malo.",
        "Actualizaciones","Ayer",4.2),
    BlogPost("3","¿Conviene comprar la nueva portátil X?",
        "Probamos la consola 4 días: batería, calor y catálogo real.",
        "Hardware","Hace 2 días",4.0),
    BlogPost("4","Ofertas del fin de semana",
        "JRPGs y roguelikes con hasta 75% de descuento. Lista curada.",
        "Ofertas","Hace 3 días",4.4),
)

@Composable
fun Blog(innerPadding: PaddingValues) {
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    val categorias = listOf("Todas","Tendencias","Actualizaciones","Hardware","Ofertas")
    val posts = remember { samplePosts() }
    val filtrados = remember(categoriaSeleccionada, posts) {
        if (categoriaSeleccionada == "Todas") posts else posts.filter { it.categoria == categoriaSeleccionada }
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        // Filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { cat ->
                FilterChip(
                    selected = categoriaSeleccionada == cat,
                    onClick = { categoriaSeleccionada = cat },
                    label = { Text(cat) }
                )
            }
        }

        // Lista
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtrados, key = { it.id }) { post ->
                ElevatedCard(
                    onClick = { /* abrir detalle futuro */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(post.categoria, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(post.fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(post.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(6.dp))
                        Text(post.resumen, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("${post.rating}")
                        }
                    }
                }
            }
        }
    }
}
