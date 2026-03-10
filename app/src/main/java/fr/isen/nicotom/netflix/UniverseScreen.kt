package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UniverseScreen(onFilmClick: (Film) -> Unit) { // Ajout du paramètre ici
    val categories = remember { mutableStateListOf<Categorie>() }

    LaunchedEffect(Unit) {
        DataBaseHelper().getCategories {
            categories.clear()
            categories.addAll(it)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(categories) { categorie ->
                CategoryItem(categorie, onFilmClick) // On passe le clic
            }
        }
    }
}

@Composable
fun CategoryItem(categorie: Categorie, onFilmClick: (Film) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = categorie.categorie, color = Color.White, modifier = Modifier.weight(1f))
                Text(text = if (expanded) "▼" else "▶", color = Color.Red)
            }
        }
        if (expanded) {
            FranchiseList(categorie.franchises, onFilmClick) // On passe le clic
        }
    }
}

@Composable
fun FranchiseList(franchises: List<Franchise>, onFilmClick: (Film) -> Unit) {
    Column(Modifier.padding(start = 32.dp, end = 16.dp)) {
        franchises.forEach { franchise ->
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = franchise.nom,
                color = Color.LightGray,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 10.dp)
            )
            if (expanded) {
                franchise.sousSagas?.let {
                    SagaList(it, onFilmClick) // On passe le clic
                } ?: run {
                    FilmList(franchise.tousLesFilms(), onFilmClick) // Correction ici
                }
            }
        }
    }
}

@Composable
fun SagaList(sagas: List<SousSaga>, onFilmClick: (Film) -> Unit) {
    Column(Modifier.padding(start = 16.dp)) {
        sagas.forEach { saga ->
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = "• ${saga.nom}",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 6.dp)
            )
            if (expanded) {
                FilmList(saga.films, onFilmClick) // Correction ici
            }
        }
    }
}

@Composable
fun FilmList(films: List<Film>, onFilmClick: (Film) -> Unit) {
    // Correction du padding : on utilise Modifier.padding(...) sans "paddingValues ="
    Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        films.forEach { film ->
            Text(
                text = film.titre,
                color = Color(0xFFE50914),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFilmClick(film) } // Action de clic finale
                    .padding(vertical = 8.dp)
            )
        }
    }
}