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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage

// --- MODELO DE DATOS ---
data class Product(
    val id: String,
    val name: String,
    val unitType: String,
    val stock: Int,
    val discountPercent: String?,
    val price: Double,
    val quantity: Int = 0,
    val isSelected: Boolean = false,
    val imageRes: Int
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
            Product("00001", "PIPAS EXTRA 80 GR.", "Uds", 87, "%", 11.34, 1, true, R.drawable.pipas),
            Product("00002", "FINI BOOM FRESA 200 U", "Cajas", 90, "%", 13.91, 1, false, R.drawable.fini_boom),
            Product("00003", "TARRO CHILES RELLENOLAS", "Cajas", 70, "25%", 22.50, 1, false, R.drawable.chiles),
            Product("00004", "RESPIRAL LIMON KG", "Cajas", 36, "%", 16.88,1,false, R.drawable.limon),
            Product("00005", "MENTOLIN MAURI", "Cajas", 0, "%", 13.91, 1, false, R.drawable.mauri),
            Product("00006", "RESPIRAL EUCALIPTO KG", "Cajas", 40, "%", 14.11, 1,false, R.drawable.respiral),
            Product("00007", "ZUMO DE NARANJA NATURAL 330 ML", "Uds", 55, "10%", 2.15, 1,false, R.drawable.zumo)
        )
    }

    // Identificar producto seleccionado
    val selectedIndex = products.indexOfFirst { it.isSelected }
    val selectedProduct = if (selectedIndex != -1) products[selectedIndex] else null
    //var showFilterMenu by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopMenuRow(onMenuClick = { showFilterMenu = true })
            // Sincronizado con el total del seleccionado
            OrderHeaderRow(total = selectedProduct?.totalProductPrice ?: 0.0)

            ProductEntrySection(
                selectedProduct = selectedProduct,
                onQuantityChange = { newQty ->
                    if (selectedIndex != -1) {
                        products[selectedIndex] = products[selectedIndex].copy(quantity = newQty)
                    }
                },
                onPriceChange = { newPrice ->
                    if (selectedIndex != -1) {
                        products[selectedIndex] = products[selectedIndex].copy(price = newPrice)
                    }
                }
            )
            // Pasamos la acción de sumar al componente
            QuickActionsRow(
                onAddQuantity = { extra ->
                    if (selectedIndex != -1) {
                        val currentQty = products[selectedIndex].quantity
                        products[selectedIndex] = products[selectedIndex].copy(quantity = currentQty + extra)
                    }
                },
                onResetQuantity = { // logica de x
                    if (selectedIndex != -1) {
                        products[selectedIndex] = products[selectedIndex].copy(quantity = 0)
                    }
                }
            )

            // Lista de productos
            ProductList(
                products = products,
                modifier = Modifier.weight(1f),
                onSelect = { clickedProduct ->
                    // Recorremos la lista para actualizar todos los productos a la vez
                    products.forEachIndexed { index, p ->
                        val isThisSelected = p.id == clickedProduct.id
                        products[index] = p.copy(
                            isSelected = isThisSelected,
                            // Si es el seleccionado ponemos 1, si no, 0
                            quantity = if (isThisSelected) 1 else 0
                        )
                    }
                }
            )
            BottomStatusBar()
        }

        // --- CAPA DE OPCIONES FLOTANTES (Upsell, Cross-Sell...) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp), // Alineado sobre el FAB
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Solo se muestran si showOptions es true
            AnimatedVisibility(
                visible = showOptions,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingOptionButton(Icons.Default.ArrowCircleUp, "Upsell")
                    FloatingOptionButton(Icons.Default.SyncAlt, "Cross-Sell")
                    FloatingOptionButton(Icons.Default.Autorenew, "Sustitutivos")
                }
            }

            // --- BOTÓN DE LOS 3 PUNTOS (Activa/Desactiva las opciones) ---
            FloatingActionButton(
                onClick = { showOptions = !showOptions }, // Cambia el estado al pulsar
                containerColor = Color(0xFFA8B8D0),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                // Cambia el icono si está abierto o cerrado
                Icon(
                    imageVector = if (showOptions) Icons.Default.Close else Icons.Default.MoreHoriz,
                    contentDescription = "Opciones adicionales",
                    tint = Color.White
                )
            }
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

                    // Botón de cerrar
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
        // .clickable { /* Acción del filtro */ }
    )
}

// 1. Menú Superior
@Composable
fun TopMenuRow(onMenuClick: () -> Unit) {
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

        IconButton(onClick = onMenuClick) { // <--- Ahora usa el onMenuClick
            Icon(Icons.Default.Menu, contentDescription = "Abrir Menú Filtros")
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

// 2. Cabecera del pedido (Fondo oscuro)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEntrySection(
    selectedProduct: Product?,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit
) {
    // Control del desplegable
    var expanded by remember { mutableStateOf(false) }

    // Tarifas fijas: se recalculan solo si cambia el ID del producto seleccionado
    val tarifasFijas = remember(selectedProduct?.id) {
        if (selectedProduct != null) {
            listOf(
                selectedProduct.price,       // Tarifa 1 (Base)
                selectedProduct.price * 0.9, // Tarifa 2 (-10%)
                selectedProduct.price * 0.8  // Tarifa 3 (-20%)
            )
        } else emptyList()
    }

    // Sincronizar el texto del cuadro con la cantidad del producto
    var textValue by remember(selectedProduct?.id, selectedProduct?.quantity) {
        mutableStateOf(selectedProduct?.quantity?.toString() ?: "0")
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F4F8)).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            // --- 1. IMAGEN DEL PRODUCTO SELECCIONADO ---
            if (selectedProduct != null) {
                Image(
                    painter = painterResource(id = selectedProduct.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text("Precio", color = Color.Gray, modifier = Modifier.weight(0.7f))

            // --- 2. DESPLEGABLE DE PRECIO ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(2f)
            ) {
                OutlinedTextField(
                    value = "${"%.2f".format(selectedProduct?.price ?: 0.0)}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().height(48.dp).fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    tarifasFijas.forEach { precio ->
                        DropdownMenuItem(
                            text = { Text("${"%.2f".format(precio)} €") },
                            onClick = {
                                onPriceChange(precio)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text("Uds", color = Color.Gray)

            // --- 3. CUADRO DE UNIDADES ---
            OutlinedTextField(
                value = textValue,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        textValue = it
                        onQuantityChange(it.toIntOrNull() ?: 0)
                    }
                },
                modifier = Modifier.height(48.dp).width(65.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila de Totales y Descuento
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("%Dto", color = Color.Gray)
            Text(" 10% + ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("${"%.2f".format(selectedProduct?.totalProductPrice ?: 0.0)} €", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("Total ", color = Color.Gray)
            Text("${"%.2f".format(selectedProduct?.totalProductPrice ?: 0.0)} €", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
        }
    }
}

// 4. Botones de Acción Rápida
@Composable
fun QuickActionsRow(onAddQuantity: (Int) -> Unit, onResetQuantity: () -> Unit) {
    val amounts = listOf(1, 5, 10, 25, 50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF6B7280))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botones numéricos
        amounts.forEach { amount ->
            Box(
                modifier = Modifier
                    .border(1.dp, Color.LightGray, CircleShape)
                    .clickable { onAddQuantity(amount) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+$amount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Boton gris
        Box(
            modifier = Modifier
                .background(Color(0xFF8B92A0), CircleShape)
                .padding(6.dp)
                .clickable { onResetQuantity() } // <--- Aquí llama a la función de reset
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }

        // Botón regalo
        Box(modifier = Modifier.background(Color(0xFFFF7043), CircleShape).padding(6.dp).clickable { /* Acción */ }) {
            Icon(Icons.Default.CardGiftcard, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }

        // Botón carrito
        Box(modifier = Modifier.background(Color.Black, CircleShape).padding(6.dp).clickable { /* Acción */ }) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(16.dp))
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
        colors = CardDefaults.cardColors(
            containerColor = if (product.isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IMAGEN DESDE RES ---
            Image(
                painter = painterResource(id = product.imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .size(50.dp) // Tamaño exacto de tu hueco en el diseño
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop // Para que la foto rellene el cuadro sin deformarse
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(product.id, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(45.dp))
                    Text("Stock: ${product.stock}", fontSize = 10.sp, modifier = Modifier.width(60.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${"%.2f".format(product.totalProductPrice)} €", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ... resto del código (Icono Check y Cantidad)
            Icon(
                Icons.Default.CheckCircle, null,
                tint = if (product.isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = product.quantity.toString(), // Siempre mostrará 1 si está seleccionado o 0 si no
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (product.isSelected) Color(0xFF2196F3) else Color.Gray
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

@Composable
fun FloatingOptionButton(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF9BAABF), // Color gris azulado
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { /* Acción */ },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}