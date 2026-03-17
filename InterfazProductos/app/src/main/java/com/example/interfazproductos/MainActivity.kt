package com.example.interfazproductos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interfazproductos.ui.theme.InterfazProductosTheme

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
    val totalProductPrice: Double get() = price * (if (quantity == 0) 1 else quantity)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterfazProductosTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF7F2EC)) {
                    ProductScreen()
                }
            }
        }
    }
}

@Composable
fun ProductScreen() {
    // --- ESTADOS ---
    val products = remember {
        mutableStateListOf(
            Product("00001", "PIPAS EXTRA 80 GR.", "Uds", 87, "%", 11.34, 1, true, R.drawable.pipas),
            Product("00002", "FINI BOOM FRESA 200 U", "Cajas", 90, "%", 13.91, 0, false, R.drawable.fini_boom),
            Product("00003", "TARRO CHILES RELLENOLAS", "Cajas", 70, "25%", 22.50, 0, false, R.drawable.chiles),
            Product("00004", "RESPIRAL LIMON KG", "Cajas", 36, "%", 16.88, 0, false, R.drawable.limon),
            Product("00005", "MENTOLIN MAURI", "Cajas", 0, "%", 13.91, 0, false, R.drawable.mauri),
            Product("00006", "RESPIRAL EUCALIPTO KG", "Cajas", 40, "%", 14.11, 0, false, R.drawable.respiral),
            Product("00007", "ZUMO DE NARANJA NATURAL 330 ML", "Uds", 55, "10%", 2.15, 0, false, R.drawable.zumo)
        )
    }

    val basketProducts = remember { mutableStateListOf<Product>() }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showBasketScreen by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    val selectedIndex = products.indexOfFirst { it.isSelected }
    val selectedProduct = if (selectedIndex != -1) products[selectedIndex] else null

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopMenuRow(
                onMenuClick = { showFilterMenu = true },
                onBasketClick = { showBasketScreen = true }
            )

            val totalGeneral = products.sumOf { it.price * it.quantity }
            OrderHeaderRow(total = totalGeneral)

            ProductEntrySection(
                selectedProduct = selectedProduct,
                onQuantityChange = { newQty ->
                    if (selectedIndex != -1) products[selectedIndex] = products[selectedIndex].copy(quantity = newQty)
                },
                onPriceChange = { newPrice ->
                    if (selectedIndex != -1) products[selectedIndex] = products[selectedIndex].copy(price = newPrice)
                }
            )

            QuickActionsRow(
                onAddQuantity = { extra ->
                    if (selectedIndex != -1) {
                        val currentQty = products[selectedIndex].quantity
                        products[selectedIndex] = products[selectedIndex].copy(quantity = currentQty + extra)
                    }
                },
                onResetQuantity = {
                    if (selectedIndex != -1) products[selectedIndex] = products[selectedIndex].copy(quantity = 0)
                }
            )

            ProductList(
                products = products,
                modifier = Modifier.weight(1f),
                onSelect = { clickedProduct ->
                    products.forEachIndexed { index, p ->
                        val isThisSelected = p.id == clickedProduct.id
                        products[index] = p.copy(
                            isSelected = isThisSelected,
                            quantity = if (isThisSelected && p.quantity == 0) 1 else p.quantity
                        )
                    }
                }
            )
            BottomStatusBar()
        }

        // --- BOTONES FLOTANTES ---
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 72.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = showOptions, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingOptionButton(
                        icon = Icons.Default.ArrowCircleUp,
                        label = "Upsell (Añadir)",
                        onClick = {
                            selectedProduct?.let {
                                if (it.quantity > 0) {
                                    basketProducts.add(it.copy()) // Añadimos copia a la cesta
                                    showOptions = false
                                }
                            }
                        }
                    )
                    FloatingOptionButton(Icons.Default.SyncAlt, "Cross-Sell", onClick = {})
                    FloatingOptionButton(Icons.Default.Autorenew, "Sustitutivos", onClick = {})
                }
            }

            FloatingActionButton(
                onClick = { showOptions = !showOptions },
                containerColor = Color(0xFFA8B8D0),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = if (showOptions) Icons.Default.Close else Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White)
            }
        }

        // --- PANTALLAS SUPERPUESTAS (Independientes) ---

        // Filtro
        AnimatedVisibility(visible = showFilterMenu, enter = fadeIn(), exit = fadeOut()) {
            FilterSideMenu(onClose = { showFilterMenu = false })
        }

        // Cesta (FUERA del AnimatedVisibility de filtros)
        AnimatedVisibility(
            visible = showBasketScreen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            BasketScreen(
                basketItems = basketProducts,
                onClose = { showBasketScreen = false },
                onRemove = { basketProducts.remove(it) }
            )
        }
    }
}

// --- Pantalla cesta ---

@Composable
fun BasketScreen(
    basketItems: List<Product>,
    onClose: () -> Unit,
    onRemove: (Product) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .background(Color.White)
                .align(Alignment.CenterEnd)
                .clickable(enabled = false) { }
        ) {
            // Cabecera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2196F3))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Precompra", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            // Lista de productos
            LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
                items(basketItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = null,
                            modifier = Modifier.size(45.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${item.quantity} x ${"%.2f".format(item.price)}€", fontSize = 11.sp)
                        }
                        IconButton(onClick = { onRemove(item) }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }

            // --- SECCIÓN INFERIOR: TOTAL Y BOTÓN DE PAGAR ---
            val total = basketItems.sumOf { it.price * it.quantity }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color(0xFFF1F3F4)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL:", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("${"%.2f".format(total)} €", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN DE PAGAR
                    Button(
                        onClick = {
                            println("Procesando pago de: $total €")
                            if (basketItems.isNotEmpty()) {
                                val totalCesta = basketItems.sumOf { it.price * it.quantity }
                                procesarPagoEnFirebase(basketItems, totalCesta)
                            } else {
                                println("LA CESTA ESTÁ VACÍA")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32) // Verde oscuro tipo "éxito"
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "PAGAR AHORA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "IVA incluido en el precio final",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun FilterSideMenu(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onClose() }) {
        Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.75f).background(Color(0xFFEBE6DF), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)).align(Alignment.CenterEnd).clickable(enabled = false) {}) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF8B5A33), RoundedCornerShape(topStart = 16.dp)).padding(horizontal = 20.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Filtro articulos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp).border(1.5.dp, Color.White, CircleShape)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
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

@Composable
fun FilterMenuItem(text: String) {
    Text(text = text, color = Color.DarkGray, fontSize = 18.sp, fontWeight = FontWeight.Normal, modifier = Modifier.fillMaxWidth())
}

@Composable
fun TopMenuRow(onMenuClick: () -> Unit, onBasketClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        TopMenuItem(Icons.Default.ExitToApp, "Salir", onClick = {})
        TopMenuItem(Icons.Default.ShoppingCart, "Cesta", hasBadge = true, onClick = onBasketClick)
        TopMenuItem(Icons.Default.Refresh, "Sugerido", onClick = {})
        TopMenuItem(Icons.Default.MenuBook, "Tarifa", isSelected = true, onClick = {})
        TopMenuItem(Icons.Default.StarBorder, "Favoritos", hasBadge = true, onClick = {})
        TopMenuItem(Icons.Default.GridView, "Catálogo", onClick = {})
        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = null) }
    }
}

@Composable
fun TopMenuItem(icon: ImageVector, label: String, isSelected: Boolean = false, hasBadge: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box {
            Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) Color.DarkGray else Color.Gray, modifier = if (isSelected) Modifier.background(Color.LightGray, CircleShape).padding(8.dp) else Modifier.padding(8.dp))
            if (hasBadge) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd))
            }
        }
        Text(text = label, fontSize = 10.sp, color = Color.DarkGray)
    }
}

@Composable
fun OrderHeaderRow(total: Double) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF535A66), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pedido", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFFFF5252))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.background(Color.DarkGray, CircleShape).size(24.dp), contentAlignment = Alignment.Center) { Text("P", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
        Text(text = "Total: ${"%.2f".format(total)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEntrySection(selectedProduct: Product?, onQuantityChange: (Int) -> Unit, onPriceChange: (Double) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val tarifasFijas = remember(selectedProduct?.id) {
        if (selectedProduct != null) listOf(selectedProduct.price, selectedProduct.price * 0.9, selectedProduct.price * 0.8) else emptyList()
    }
    var textValue by remember(selectedProduct?.id, selectedProduct?.quantity) {
        mutableStateOf(selectedProduct?.quantity?.toString() ?: "0")
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F4F8)).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (selectedProduct != null) {
                Image(painter = painterResource(id = selectedProduct.imageRes), contentDescription = null, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(Color.White), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text("Precio", color = Color.Gray, modifier = Modifier.weight(0.7f))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(2f)) {
                OutlinedTextField(value = "${"%.2f".format(selectedProduct?.price ?: 0.0)}", onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().height(48.dp).fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent), shape = RoundedCornerShape(8.dp), textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                    tarifasFijas.forEach { precio -> DropdownMenuItem(text = { Text("${"%.2f".format(precio)} €") }, onClick = { onPriceChange(precio); expanded = false }) }
                }
            }
            Spacer(modifier = Modifier.width(12.dp)); Text("Uds", color = Color.Gray)
            OutlinedTextField(value = textValue, onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) { textValue = it; onQuantityChange(it.toIntOrNull() ?: 0) } }, modifier = Modifier.height(48.dp).width(65.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent), textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("%Dto", color = Color.Gray); Text(" 10% + ", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("${"%.2f".format(selectedProduct?.totalProductPrice ?: 0.0)} €", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f)); Text("Total ", color = Color.Gray); Text("${"%.2f".format(selectedProduct?.totalProductPrice ?: 0.0)} €", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
        }
    }
}

@Composable
fun QuickActionsRow(onAddQuantity: (Int) -> Unit, onResetQuantity: () -> Unit) {
    val amounts = listOf(1, 5, 10, 25, 50)
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF6B7280)).padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        amounts.forEach { amount -> Box(modifier = Modifier.border(1.dp, Color.LightGray, CircleShape).clickable { onAddQuantity(amount) }.padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) { Text("+$amount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
        Box(modifier = Modifier.background(Color(0xFF8B92A0), CircleShape).padding(6.dp).clickable { onResetQuantity() }) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        Box(modifier = Modifier.background(Color(0xFFFF7043), CircleShape).padding(6.dp).clickable { }) { Icon(Icons.Default.CardGiftcard, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        Box(modifier = Modifier.background(Color.Black, CircleShape).padding(6.dp).clickable { }) { Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
    }
}

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
    Card(colors = CardDefaults.cardColors(containerColor = if (product.isSelected) Color(0xFFE3F2FD) else Color.White), modifier = Modifier.fillMaxWidth().clickable { onClick() }, elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = product.imageRes), contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(product.id, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(45.dp))
                    Text("Stock: ${product.stock}", fontSize = 10.sp, modifier = Modifier.width(60.dp))
                    Spacer(modifier = Modifier.weight(1f)); Text("${"%.2f".format(product.totalProductPrice)} €", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.CheckCircle, null, tint = if (product.isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = product.quantity.toString(), modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (product.isSelected) Color(0xFF2196F3) else Color.Gray)
        }
    }
}

@Composable
fun BottomStatusBar() {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEFEBE4)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("20003 Articulos", color = Color.DarkGray, fontSize = 14.sp)
        Row {
            Icon(Icons.Default.SortByAlpha, null, tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.FilterList, null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun FloatingOptionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(32.dp), color = Color(0xFF9BAABF), shadowElevation = 4.dp, modifier = Modifier.clickable { onClick() }) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}