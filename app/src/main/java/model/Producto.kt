package cl.duoc.level_up_mobile.model

import androidx.annotation.DrawableRes
import cl.duoc.level_up_mobile.R

data class Producto(
    val codigo: String,
    val nombre: String,
    val precio: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val categoria: String,
    val stock: String,
    val especificaciones: List<String>,
    val puntuacion: String,
    val comentarios: List<String>,
    @DrawableRes val imagenRes: Int,
)

val productosDestacados = listOf(
    Producto(
        codigo = "JM001",
        nombre = "Catan",
        precio = "29.990 CLP",
        descripcionCorta = "Juego de mesa de estrategia donde los jugadores colonizan una isla, construyen pueblos y comercian recursos para expandirse.",
        descripcionLarga = "Juego de mesa de estrategia donde los jugadores colonizan una isla, construyen pueblos y comercian recursos para expandirse. Ideal para reuniones con amigos y familia. Incluye componentes duraderos, reglas claras y alta rejugabilidad para partidas diferentes cada vez.",
        categoria = "Juegos de Mesa",
        stock = "149",
        especificaciones = listOf(
            "Incluye instrucciones en español",
            "Tiempo de juego 45–90 min (aprox.)",
            "Para múltiples jugadores"
        ),
        puntuacion = "4",
        comentarios = listOf(
            "Atención al cliente excelente",
            "Configuración sencilla"
        ),
        imagenRes = R.drawable.catan
    ),
    Producto(
        codigo = "AC002",
        nombre = "Auriculares Gamer HyperX Cloud II",
        precio = "79.990 CLP",
        descripcionCorta = "Audífonos con sonido envolvente 7.1, micrófono desmontable y almohadillas de espuma viscoelástica para máxima comodidad.",
        descripcionLarga = "Audífonos con sonido envolvente 7.1, micrófono desmontable y almohadillas de espuma viscoelástica para máxima comodidad. Pensado para largas sesiones, con materiales de buena calidad y compatibilidad amplia. Ofrece una experiencia confiable tanto para principiantes como para entusiastas.",
        categoria = "Accesorios",
        stock = "190",
        especificaciones = listOf(
            "Diadema acolchada",
            "Sonido envolvente (virtual)",
            "Almohadillas cómodas",
            "Micrófono desmontable"
        ),
        puntuacion = "5",
        comentarios = listOf(
            "La calidad superó mis expectativas",
            "Tal como en la descripción",
            "Rinde muy bien en juegos",
            "Buen producto por el precio"
        ),
        imagenRes = R.drawable.hyperx
    ),
    Producto(
        codigo = "CO001",
        nombre = "PlayStation 5",
        precio = "549.990 CLP",
        descripcionCorta = "Consola de nueva generación con gráficos 4K, SSD ultrarrápido y el innovador control DualSense.",
        descripcionLarga = "Consola de nueva generación con gráficos 4K, SSD ultrarrápido y el innovador control DualSense. Ofrece arranques veloces y una experiencia fluida en títulos modernos. Perfecta para disfrutar juegos actuales y retro con gran comodidad.",
        categoria = "Consolas",
        stock = "165",
        especificaciones = listOf(
            "Aplicación complementaria",
            "Haptic feedback/gatillos adaptativos (PS5)",
            "Share/stream integrado",
            "Audio 3D (según título)"
        ),
        puntuacion = "4",
        comentarios = listOf(
            "Rinde muy bien en juegos",
            "Se nota la diferencia en rendimiento"
        ),
        imagenRes = R.drawable.ps5
    ),
    Producto(
        codigo = "CG003",
        nombre = "Notebook Gamer Acer Predator Helios 300",
        precio = "1.199.990 CLP",
        descripcionCorta = "Notebook con pantalla de 144Hz, GPU NVIDIA y sistema de refrigeración avanzado para sesiones intensas.",
        descripcionLarga = "Notebook con pantalla de 144Hz, GPU NVIDIA y sistema de refrigeración avanzado para sesiones intensas. Listo para juegos exigentes y multitarea. Su diseño favorece temperaturas estables y un rendimiento consistente en sesiones prolongadas.",
        categoria = "Computadores Gamers",
        stock = "148",
        especificaciones = listOf(
            "Teclado retroiluminado",
            "Chasis resistente",
            "Autonomía adecuada",
            "Sistema de refrigeración eficiente"
        ),
        puntuacion = "10",
        comentarios = listOf(
            "Atención al cliente excelente",
            "Llegó rápido y en buen estado"
        ),
        imagenRes = R.drawable.acer
    ),
    Producto(
        codigo = "MS001",
        nombre = "Mouse Gamer Logitech G502 HERO",
        precio = "49.990 CLP",
        descripcionCorta = "Mouse gamer de alta precisión con sensor HERO, botones programables y sistema de pesas ajustable.",
        descripcionLarga = "Mouse gamer de alta precisión con sensor HERO, botones programables y sistema de pesas ajustable. Seguimiento preciso y respuesta inmediata para juegos competitivos. Perfila un control cómodo durante horas.",
        categoria = "Mouse",
        stock = "136",
        especificaciones = listOf(
            "Botones programables",
            "Software de personalización",
            "Diseño ergonómico",
            "Sensor óptico de alta precisión",
            "DPI ajustable"
        ),
        puntuacion = "6",
        comentarios = listOf(
            "Se nota la diferencia en rendimiento",
            "Tal como en la descripción",
            "Configuración sencilla"
        ),
        imagenRes = R.drawable.g503
    ),
    Producto(
        codigo = "MP001",
        nombre = "Mousepad Razer Goliathus Extended Chroma",
        precio = "29.990 CLP",
        descripcionCorta = "Alfombrilla extendida con iluminación RGB perimetral y superficie optimizada para control y velocidad.",
        descripcionLarga = "Alfombrilla extendida con iluminación RGB perimetral y superficie optimizada para control y velocidad. Superficie equilibrada que entrega deslizamiento uniforme y control fiable. Resiste el desgaste del uso continuo.",
        categoria = "Mousepad",
        stock = "163",
        especificaciones = listOf(
            "Compatible con sensores óptico y láser",
            "Base de goma antideslizante",
            "Costuras reforzadas"
        ),
        puntuacion = "5",
        comentarios = listOf(
            "Materiales de buena calidad",
            "Empaque bien protegido",
            "Colores muy fieles"
        ),
        imagenRes = R.drawable.goliathus
    ),
    Producto(
        codigo = "PP001",
        nombre = "Polera Gamer Personalizada 'Level-Up'",
        precio = "14.990 CLP",
        descripcionCorta = "Polera de algodón con estampado gamer 'Level-Up', ideal para uso diario.",
        descripcionLarga = "Polera de algodón con estampado gamer 'Level-Up', ideal para uso diario. Tela suave y respirable para el día a día. El estampado mantiene los colores tras múltiples lavados.",
        categoria = "Poleras Personalizadas",
        stock = "162",
        especificaciones = listOf(
            "Lavado a máquina",
            "Corte unisex",
            "Tallas S a XL"
        ),
        puntuacion = "4",
        comentarios = listOf(
            "Empaque bien protegido",
            "La calidad superó mis expectativas"
        ),
        imagenRes = R.drawable.levelup
    ),
    Producto(
        codigo = "PG001",
        nombre = "Polerón Gamer Personalizado 'Level-Up'",
        precio = "24.990 CLP",
        descripcionCorta = "Polerón con capucha y diseño 'Level-Up', interior suave y cálido.",
        descripcionLarga = "Polerón con capucha y diseño 'Level-Up', interior suave y cálido. Interior agradable y abrigador para climas fríos. El estampado conserva su viveza con el uso.",
        categoria = "Polerones Gamers Personalizados",
        stock = "68",
        especificaciones = listOf(
            "Lavado a máquina",
            "Capucha ajustable",
            "Bolsillo tipo canguro"
        ),
        puntuacion = "3",
        comentarios = listOf(
            "Configuración sencilla",
            "Se siente resistente y cómodo",
            "Buen producto por el precio",
            "Llegó rápido y en buen estado"
        ),
        imagenRes = R.drawable.levelup2
    ),
    Producto(
        codigo = "SG001",
        nombre = "Silla Gamer Secretlab Titan",
        precio = "349.990 CLP",
        descripcionCorta = "Silla ergonómica de alta gama con soporte lumbar ajustable y materiales de primera calidad.",
        descripcionLarga = "Silla ergonómica de alta gama con soporte lumbar ajustable y materiales de primera calidad. Diseño ergonómico que reduce la fatiga en sesiones largas. Materiales resistentes y ensamblaje sencillo para uso diario.",
        categoria = "Sillas Gamers",
        stock = "163",
        especificaciones = listOf(
            "Altura y respaldo ajustables",
            "Estructura de acero",
            "Cojines lumbar y cervical",
            "Ruedas silenciosas",
            "Tapicería resistente"
        ),
        puntuacion = "3",
        comentarios = listOf(
            "Rinde muy bien en juegos",
            "Se nota la diferencia en rendimiento",
            "Me encantó el diseño"
        ),
        imagenRes = R.drawable.secretlab
    )
)
