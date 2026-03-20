package com.example.interfazproductos

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Función para registrar una transacción comercial en la base de datos de Firebase.
 * @param items Lista de objetos [Product] que el usuario tiene en la cesta.
 * @param total Suma total de los precios de los productos en la cesta.
 */
fun procesarPagoEnFirebase(items: List<Product>, total: Double) {
    println("DEBUG: Iniciando proceso...")

    // 1. Inicialización del punto de acceso a la base de datos Firestore
    val db = FirebaseFirestore.getInstance()

    // 2. Validación de seguridad: Si no hay productos, se cancela la operacion
    if (items.isEmpty()) {
        println("DEBUG: La lista de items está vacía")
        return
    }

    // 3. Gestión de conectividad: se fuerza a la app a intentar conectar con los servidores de Google
    // Útil si la app ha estado en segundo plano o el dispositivo recuperó conexión
    db.enableNetwork().addOnCompleteListener {
        println("DEBUG: Estado de la red activado")
    }

    // 4. Estructuración de los datos (Map):
    // Convertimos nuestra lista de objetos 'Product' en un formato que Firestore entiende
    val venta = hashMapOf(
        "fecha" to System.currentTimeMillis(), // Marca de tiempo (Timestamp)
        "total" to total,                      // Monto total del ticket
        "articulos" to items.map {
            // Solo subimos los campos necesarios para el historial de ventas
            mapOf(
                "nombre" to it.name,
                "cantidad" to it.quantity,
                "precio" to it.price
            )
        }
    )

    println("DEBUG: Enviando a colección 'ventas'...")

    // 5. Operación de escritura:
    // .collection("ventas"): Especificamos la "carpeta" donde se guardará
    // .add(venta): Crea un documento nuevo con un ID aleatorio único
    db.collection("ventas")
        .add(venta)
        .addOnSuccessListener { doc ->
            // Se ejecuta si los datos llegaron correctamente al servidor
            println("¡CONECTADO! Venta subida con ID: ${doc.id}")
        }
        .addOnFailureListener { e ->
            // Se ejecuta si hay error de conexión, falta de permisos (Rules) o cuota excedida
            println("ERROR DE RED O PERMISOS: ${e.message}")
        }
}

fun guardarFavoritoEnFirebase(productId: String, isFavorite: Boolean) {
    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("favoritos_usuario").document(productId)

    if (isFavorite) {
        ref.set(mapOf("id" to productId, "timestamp" to System.currentTimeMillis()))
    } else {
        ref.delete()
    }
}