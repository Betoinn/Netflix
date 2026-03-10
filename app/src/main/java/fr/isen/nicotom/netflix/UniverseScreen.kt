package fr.isen.nicotom.netflix

import fr.isen.nicotom.netflix.Categorie
import fr.isen.nicotom.netflix.Franchise
import fr.isen.nicotom.netflix.SousSaga
import fr.isen.nicotom.netflix.Film
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.database.*
import com.google.firebase.database.database
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun UniverseScreen() {

    val categories = remember { mutableStateListOf<Categorie>() }

    LaunchedEffect(Unit) {
        DataBaseHelper().getCategories {
            categories.clear()
            categories.addAll(it)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        LazyColumn(modifier = Modifier.padding(innerPadding)) {

            items(categories) { categorie ->

                CategoryItem(categorie)

            }
        }
    }
}

@Composable
fun CategoryItem(categorie: Categorie) {

    var expanded by remember { mutableStateOf(false) }

    Column {

        Card(
            modifier = Modifier
                .padding(8.dp)
                .clickable { expanded = !expanded }
        ) {

            Text(
                text = categorie.categorie,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (expanded) {
            FranchiseList(categorie.franchises)
        }
    }
}

@Composable
fun FranchiseList(franchises: List<Franchise>) {

    Column(Modifier.padding(start = 16.dp)) {

        franchises.forEach { franchise ->

            var expanded by remember { mutableStateOf(false) }

            Text(
                franchise.nom,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { expanded = !expanded }
            )

            if (expanded) {

                franchise.sousSagas?.let {
                    SagaList(it)
                } ?: run {
                    FilmList(franchise.tousLesFilms())
                }

            }
        }
    }
}

@Composable
fun SagaList(sagas: List<SousSaga>) {

    Column(Modifier.padding(start = 16.dp)) {

        sagas.forEach { saga ->

            var expanded by remember { mutableStateOf(false) }

            Text(
                saga.nom,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { expanded = !expanded }
            )

            if (expanded) {

                FilmList(saga.films)

            }
        }
    }
}

@Composable
fun FilmList(films: List<Film>) {

    Column(Modifier.padding(start = 16.dp)) {

        films.forEach { film ->

            Text(
                film.titre,
                modifier = Modifier.padding(6.dp)
            )

        }
    }
}