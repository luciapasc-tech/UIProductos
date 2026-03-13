package com.example.interfazproductos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interfazproductos.ui.theme.InterfazProductosTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable

// --- MODELO DE DATOS ---
data class Product(
    val id: String,
    val name: String,
    val unitType: String,
    val stock: Int,
    val discountPercent: String?,
    val price: Double,
    val quantity: Int = 0,
    val isSelected: Boolean = false
) {
    // Calculamos el precio total: precio base por cantidad (mínimo 1 para mostrar el precio base)
    val totalProductPrice: Double get() = price * (if (quantity == 0) 1 else quantity)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterfazProductosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF7F2EC) // Color de fondo beige claro
                ) {
                    ProductScreen()
                }

            }
        }
    }
}

// --- PANTALLA PRINCIPAL ---
@Composable
fun ProductScreen() {
    var showFilterMenu by remember { mutableStateOf(false) }

    // --- ESTADO DE LA LISTA ---
    val products = remember {
        mutableStateListOf(
            Product("00001", "PIPAS EXTRA 80 GR. (1,30€) 20 U 34", "Uds", 87, "%", 11.34, 1, true),
            Product("00002", "FINI BOOM FRESA 200 U", "Cajas", 90, "%", 13.91),
            Product("00003", "TARRO CHILES RELLENOLAS VIDAL 75 U", "Cajas", 70, "25%", 22.50),
            Product("00004", "RESPIRAL LIMON KG", "Cajas", 36, "%", 16.88),
            Product("00005", "MENTOLIN MAURI", "Cajas", 0, "%", 13.91),
            Product("00006", "RESPIRAL EUCALIPTO KG", "Cajas", 40, "%", 14.11)
        )
    }

    // Identificar producto seleccionado
    val selectedIndex = products.indexOfFirst { it.isSelected }
    val selectedProduct = if (selectedIndex != -1) products[selectedIndex] else null

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopMenuRow()
            // Sincronizado con el total del seleccionado
            OrderHeaderRow(total = selectedProduct?.totalProductPrice ?: 0.0)

            ProductEntrySection(
                selectedProduct = selectedProduct,
                onQuantityChange = { newQty ->
                    if (selectedIndex != -1) {
                        products[selectedIndex] = products[selectedIndex].copy(quantity = newQty)
                    }
                }
            )

            QuickActionsRow()

            // Lista de productos
            ProductList(
                products = products,
                modifier = Modifier.weight(1f),
                onSelect = { clickedProduct ->
                    products.forEachIndexed { index, p ->
                        products[index] = p.copy(isSelected = p.id == clickedProduct.id)
                    }
                }
            )
            BottomStatusBar()
        }

        FloatingActionButton(
            onClick = { showFilterMenu = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 72.dp),
            containerColor = Color(0xFFA8B8D0),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.MoreHoriz, "Opciones", tint = Color.White)
        }

        AnimatedVisibility(visible = showFilterMenu, enter = fadeIn(), exit = fadeOut()) {
            FilterSideMenu(onClose = { showFilterMenu = false })
        }
    }
}

@Composable
fun FilterSideMenu(onClose: () -> Unit) {
    // Fondo oscuro semitransparente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            // Si el usuario toca la parte oscura, se cierra el menú
            .clickable { onClose() }
    ) {
        // Panel lateral animado
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(initialOffsetX = { it }), // Entra desde la derecha
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f) // Ocupa el 75% del ancho de la pantalla
                    .background(Color(0xFFEBE6DF), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    // Este clickable vacío evita que al tocar el menú se active el clickable del fondo oscuro
                    .clickable(enabled = false) {}
            ) {
                // Cabecera marrón
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF8B5A33), RoundedCornerShape(topStart = 16.dp))
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtro articulos",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Botón de cerrar (X con círculo)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.5.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                // Opciones del menú
                Column(modifier = Modifier.padding(32.dp)) {
                    FilterMenuItem("Grupo")
                    Spacer(modifier = Modifier.height(32.dp))
                    FilterMenuItem("Familia")
                    Spacer(modifier = Modifier.height(32.dp))
                    FilterMenuItem("Subfamilia")
                }
            }
        }
    }
}

@Composable
fun FilterMenuItem(text: String) {
    Text(
        text = text,
        color = Color.DarkGray,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
        // Aquí podrías añadir un clickable en el futuro para que haga algo
        // .clickable { /* Acción del filtro */ }
    )
}

// 1. Menú Superior
@Composable
fun TopMenuRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopMenuItem(Icons.Default.ExitToApp, "Salir")
        TopMenuItem(Icons.Default.ShoppingCart, "Cesta", hasBadge = true)
        TopMenuItem(Icons.Default.Refresh, "Sugerido")
        TopMenuItem(Icons.Default.MenuBook, "Tarifa", isSelected = true)
        TopMenuItem(Icons.Default.StarBorder, "Favoritos", hasBadge = true)
        TopMenuItem(Icons.Default.GridView, "Catálogo")
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.Menu, contentDescription = "Menú")
        }
    }
}

@Composable
fun TopMenuItem(icon: ImageVector, label: String, isSelected: Boolean = false, hasBadge: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.DarkGray else Color.Gray,
                modifier = if (isSelected) Modifier
                    .background(Color.LightGray, CircleShape)
                    .padding(8.dp) else Modifier.padding(8.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
        Text(text = label, fontSize = 10.sp, color = Color.DarkGray)
    }
}

// 2. Cabecera del Pedido (Fondo oscuro)
@Composable
fun OrderHeaderRow(total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF535A66), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pedido", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFFFF5252))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.background(Color.DarkGray, CircleShape).size(24.dp), contentAlignment = Alignment.Center) {
                Text("P", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = "Total: ${"%.2f".format(total)}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 3. Sección de Entradas (Precio, Dto, Buscar)
@Composable
fun ProductEntrySection(selectedProduct: Product?, onQuantityChange: (Int) -> Unit) {
    // ESTADO ARREGLADO: Usamos un String temporal para permitir borrar el cuadro
    var textValue by remember(selectedProduct?.id) {
        mutableStateOf(selectedProduct?.quantity?.toString() ?: "")
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F4F8)).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Precio", color = Color.Gray, modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = selectedProduct?.price?.toString() ?: "0.00",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.height(48.dp).weight(2f),
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Uds", color = Color.Gray)

            // Cuadro de Unidades con corrección de borrado
            OutlinedTextField(
                value = textValue,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        textValue = it
                        onQuantityChange(it.toIntOrNull() ?: 0)
                    }
                },
                modifier = Modifier.height(48.dp).width(70.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("%Dto", color = Color.Gray)
            Text(" 10% + ", fontWeight = FontWeight.Bold)
            Text("${"%.2f".format(selectedProduct?.totalProductPrice ?: 0.0)} €", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Total ", color = Color.Gray)
            Text("${"%.2f".format(selectedProduct?.totalProductPrice ?: 0.0)} €", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// 4. Botones de Acción Rápida
@Composable
fun QuickActionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF6B7280)) // Gris oscuro
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val actions = listOf("+1", "+5", "+10", "+25", "+50")
        actions.forEach { text ->
            Box(
                modifier = Modifier
                    .border(1.dp, Color.LightGray, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.background(Color(0xFF8B92A0), CircleShape).padding(6.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(16.dp))
        }

        Box(modifier = Modifier.background(Color(0xFFFF7043), CircleShape).padding(6.dp)) {
            Icon(Icons.Default.CardGiftcard, contentDescription = "Regalo", tint = Color.White, modifier = Modifier.size(16.dp))
        }

        Box(modifier = Modifier.background(Color.Black, CircleShape).padding(6.dp)) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

// 5. Lista de Productos
@Composable
fun ProductList(products: List<Product>, modifier: Modifier, onSelect: (Product) -> Unit) {
    LazyColumn(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        items(products) { product ->
            ProductItemRow(product, onClick = { onSelect(product) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProductItemRow(product: Product, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (product.isSelected) Color(0xFFE3F2FD) else Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(product.id, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                    Text("Stock: ${product.stock}", fontSize = 10.sp, modifier = Modifier.width(60.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    // PRECIO FINAL MODIFICADO SEGÚN UNIDADES
                    Text("${"%.2f".format(product.totalProductPrice)} €", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.CheckCircle, null,
                tint = if (product.isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Muestra la cantidad actual
            Text(
                text = if (product.quantity > 0) product.quantity.toString() else "0",
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 6. Barra de Estado Inferior
@Composable
fun BottomStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEBE4))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("20003 Articulos", color = Color.DarkGray, fontSize = 14.sp)
        Row {
            Icon(Icons.Default.SortByAlpha, contentDescription = "Ordenar", tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = Color.DarkGray)
        }
    }
}