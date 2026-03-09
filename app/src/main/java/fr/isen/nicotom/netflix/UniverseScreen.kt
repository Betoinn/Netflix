package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Universe(
    val id: Int,
    val title: String,
    val colorStart: Color,
    val colorEnd: Color
)
@Composable
fun UniverseScreen(onUniverseClick: (String) -> Unit) {
    // Liste des univers
    val universes = listOf(
        Universe(1, "Action", Color(0xFFE50914), Color(0xFFB20710)), // Rouge Netflix
        Universe(2, "Comédie", Color(0xFF564d4d), Color(0xFF141414)),
        Universe(3, "Science-Fiction", Color(0xFF221f1f), Color(0xFF000000)),
        Universe(4, "Horreur", Color(0xFF434343), Color(0xFF000000)),
        Universe(5, "Documentaires", Color(0xFF831010), Color(0xFF000000))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414)) // Fond noir Netflix
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Parcourir les univers",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(universes) { universe ->
                UniverseItem(universe) {
                    onUniverseClick(universe.title)
                }
            }
        }
    }
}
@Composable
fun UniverseItem(universe: Universe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(universe.colorStart, universe.colorEnd)
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = universe.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}


