📘 LevelUP Gamer – Aplicación Móvil

Plataforma móvil enfocada en la experiencia de compra gamer, construida en Kotlin + Jetpack Compose, con integración a microservicios propios, Firebase Authentication y un sistema de carrito robusto.
Proyecto académico desarrollado para demostrar el uso de arquitectura moderna en Android, consumo de APIs, manejo de estado y UI declarativa.

🧑‍💻 Integrantes
Rol	Nombre
Desarrollador Android	Felipe Ulloa
Desarrollador Android	Darío Morales

🏗️ Tecnologías Principales


Android Studio Flamingo o superior
Kotlin 1.9+
Gradle 8+
Emulador o dispositivo Android 8.0+
Archivo google-services.json configurado
Interceptor personalizado para JWT


🎮 Descripción General

LevelUP Gamer es una aplicación móvil que permite navegar un catálogo de productos gamers, gestionar un carrito de compras, almacenar direcciones de envío, administrar sesión de usuario y explorar contenidos adicionales como blog y contacto.

Incluye:

UI completamente construida en Jetpack Compose
Consumo de microservicio backend real
Autenticación híbrida: Firebase + Tokens JWT
Carrito persistente por usuario
Sistema de navegación personalizado con Drawer
Manejo de estado reactivo con StateFlow

✨ Funcionalidades
🔐 Autenticación

✔ Inicio de sesión con Firebase
✔ Registro con sincronización a backend
✔ Refresh automático de tokens
✔ Logout y limpieza de sesión

🛍 Catálogo Gamer

✔ Categorías dinámicas
✔ Búsqueda inteligente
✔ Productos destacados
✔ Detalles completos (precio, rating, especificaciones, reseñas)

🛒 Carrito de Compras

✔ Agregar productos
✔ Aumentar/disminuir cantidad
✔ Eliminar productos
✔ Limpiar carrito completo
✔ Persistencia por usuario

👤 Perfil y Direcciones

✔ Mostrar perfil del usuario
✔ CRUD de direcciones
✔ Subida/eliminación de avatar

📝 Blog

✔ Lista de artículos mockeados
✔ Filtros por categoría

📩 Contacto

✔ Formulario funcional
✔ Validación + notificaciones mediante Snackbars

🔗 Microservicios y Endpoints Consumidos
🔐 Auth Service
Método	Endpoint	Descripción
POST	/auth/login	Login usando Firebase ID Token
POST	/auth/refresh	Generar nuevo Access Token
POST	/auth/logout	Cerrar sesión
🧑‍💼 User Service
Método	Endpoint
POST	/users/public/register
GET	/users/me
GET	/users/me/direcciones
POST	/users/me/direcciones
PUT	/users/me/direcciones/{id}
DELETE	/users/me/direcciones/{id}
POST	/users/{id}/avatar
DELETE	/users/{id}/avatar
📦 Product Service
Método	Endpoint
GET	/products/
GET	/products/{productCode}
GET	/products/{productCode}/resenas
🛒 Cart Service
Método	Endpoint
GET	/carts/user/{userId}
POST	/carts/user/{userId}/items
PUT	/carts/user/{userId}/items/{productId}
DELETE	/carts/user/{userId}/items/{productId}
DELETE	/carts/user/{userId}/clear
🏛️ Arquitectura del Proyecto
📦 app/
┣ 📂 data/
┃ ┣ 📂 remote/ → Retrofit, DTO, APIs
┃ ┗ 📂 core/ → Interceptores, Cliente Retrofit
┣ 📂 repository/ → Capa de acceso a datos
┣ 📂 session/ → Manejo de tokens
┣ 📂 model/ → Modelos de dominio
┣ 📂 ui/ → Jetpack Compose Screens + ViewModels
┗ MainActivity.kt → Punto de entrada


Patrones implementados:

MVVM
Repository Pattern
Clean Navigation (sin usar Navigation Compose)
StateFlow para reactividad
Mappers DTO → Dominio

▶️ Cómo Ejecutar el Proyecto
1️⃣ Clonar el repositorio
git clone https://github.com/dariomorales1/lvl_up_mobile.git

2️⃣ Abrir con Android Studio
File → Open → seleccionar carpeta del proyecto

3️⃣ Sincronizar Gradle
Android Studio lo solicitará automáticamente.

4️⃣ Configurar Firebase
Colocar google-services.json en:
app/google-services.json

5️⃣ Ejecutar
Seleccionar un emulador o dispositivo Android → Run ▶

📱 Características de la UI

✔ Modo oscuro por defecto
✔ Tipografías personalizadas
✔ Uso extensivo de Material 3
✔ Comportamientos animados (buttons, drawer, snackbars)

📌 Objetivo del Proyecto

Este proyecto busca demostrar dominio en:

Desarrollo móvil moderno (Compose)
Arquitectura limpia y escalable
Consumo seguro de APIs
Manejo de sesión y tokens
UI/UX profesional

📄 Licencia

Proyecto académico – uso exclusivo para fines educativos.