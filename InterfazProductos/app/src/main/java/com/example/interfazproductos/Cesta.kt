package com.example.interfazproductos
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

//@Composable
//fun BasketScreen(
//    basketItems: List<Product>,
//    onClose: () -> Unit,
//    onRemove: (Product) -> Unit
//) {
//    // Fondo oscuro que cubre la pantalla
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.6f))
//            .clickable { onClose() }
//    ) {
//        // Panel blanco lateral (derecha)
//        Column(
//            modifier = Modifier
//                .fillMaxHeight()
//                .fillMaxWidth(0.85f)
//                .background(Color.White)
//                .align(Alignment.CenterEnd)
//                .clickable(enabled = false) { } // Evita cerrar al tocar el panel
//        ) {
//            // Cabecera Cesta
//            Row(
//                modifier = Modifier.fillMaxWidth().background(Color(0xFF2196F3)).padding(20.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text("Precompra", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
//                IconButton(onClick = onClose) {
//                    Icon(Icons.Default.Close, null, tint = Color.White)
//                }
//            }
//
//            // Lista de lo añadido
//            LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
//                items(basketItems) { item ->
//                    Row(
//                        modifier = Modifier.fillMaxWidth().padding(8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Image(
//                            painter = painterResource(id = item.imageRes),
//                            contentDescription = null,
//                            modifier = Modifier.size(45.dp).clip(RoundedCornerShape(8.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
//                            Text("${item.quantity} x ${"%.2f".format(item.price)}€", fontSize = 11.sp)
//                        }
//                        IconButton(onClick = { onRemove(item) }) {
//                            Icon(Icons.Default.Delete, null, tint = Color.Red)
//                        }
//                    }
//                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
//                }
//            }
//
//            // Total
//            val total = basketItems.sumOf { it.price * it.quantity }
//            Text(
//                "Total Cesta: ${"%.2f".format(total)} €",
//                modifier = Modifier.padding(20.dp).fillMaxWidth(),
//                textAlign = TextAlign.End,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.ExtraBold,
//                color = Color(0xFF1B5E20)
//            )
//        }
//    }
//}