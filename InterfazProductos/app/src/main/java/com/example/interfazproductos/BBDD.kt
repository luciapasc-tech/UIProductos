package com.example.interfazproductos

import com.google.firebase.firestore.FirebaseFirestore

fun procesarPagoEnFirebase(items: List<Product>, total: Double) {
    println("DEBUG: Iniciando proceso...")
    val db = FirebaseFirestore.getInstance()

    // Comprobación rápida: si no hay items, no enviamos
    if (items.isEmpty()) {
        println("DEBUG: La lista de items está vacía")
        return
    }

    // Esto obliga a Firestore a intentar conectar ahora mismo
    db.enableNetwork().addOnCompleteListener {
        println("DEBUG: Estado de la red activado")
    }

    val venta = hashMapOf(
        "fecha" to System.currentTimeMillis(),
        "total" to total,
        "articulos" to items.map {
            mapOf("nombre" to it.name, "cantidad" to it.quantity, "precio" to it.price)
        }
    )

    println("DEBUG: Enviando a colección 'ventas'...")

    db.collection("ventas")
        .add(venta)
        .addOnSuccessListener { doc ->
            println("¡CONECTADO! Venta subida con ID: ${doc.id}")
        }
        .addOnFailureListener { e ->
            println("ERROR DE RED O PERMISOS: ${e.message}")
        }
}