package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


@Composable
fun FilmsScreen(onFilmClick: (Film) -> Unit) {
    val db     = FirebaseDatabase.getInstance().reference
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var films          by remember { mutableStateOf<List<Film>>(emptyList()) }
    var universes      by remember { mutableStateOf<List<Universe>>(emptyList()) }
    var userStatuses   by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedUniverse by remember { mutableStateOf("all") }
    var isLoading      by remember { mutableStateOf(true) }

    val black   = Color(0xFF141414)
    val red     = Color(0xFFE50914)
    val white   = Color.White
    val grey    = Color(0xFF8A8A9A)
    val surface = Color(0xFF2A2A2A)


    LaunchedEffect(Unit) {


        userId?.let { uid ->
            db.child("users").child(uid).child("films")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val statuses = mutableMapOf<String, String>()
                        for (child in snapshot.children) {
                            statuses[child.key!!] = child.value as String
                        }
                        userStatuses = statuses
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        db.child("films").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val loadedFilms = mutableListOf<Film>()
                for (child in snapshot.children) {
                    val id       = child.key ?: continue
                    val title    = child.child("title").getValue(String::class.java) ?: ""
                    val year     = child.child("year").getValue(Int::class.java) ?: 0
                    val universe = child.child("universe").getValue(String::class.java) ?: ""
                    val saga     = child.child("saga").getValue(String::class.java) ?: ""
                    val sagaOrder = child.child("sagaOrder").getValue(Int::class.java) ?: 0
                    loadedFilms.add(Film(id, title, year, universe, saga, sagaOrder))
                }
                films = loadedFilms.sortedWith(
                    compareBy({ it.universe }, { it.saga }, { it.sagaOrder }, { it.year })
                )

                db.child("universes").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val loadedUniverses = mutableListOf(
                            Universe("all", "Tous", "🎬", 0) // option "Tous" en premier
                        )
                        for (child in snapshot.children) {
                            val id    = child.key ?: continue
                            val name  = child.child("name").getValue(String::class.java) ?: ""
                            val emoji = child.child("emoji").getValue(String::class.java) ?: ""
                            val order = child.child("order").getValue(Int::class.java) ?: 99
                            loadedUniverses.add(Universe(id, name, emoji, order))
                        }
                        universes = loadedUniverses.sortedBy { it.order }
                        isLoading = false
                    }
                    override fun onCancelled(error: DatabaseError) { isLoading = false }
                })
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    val filteredFilms = remember(films, selectedUniverse, userStatuses) {
        val list = if (selectedUniverse == "all") films
        else films.filter { it.universe == selectedUniverse }
        // Ajouter le statut utilisateur à chaque film
        list.map { it.copy(userStatus = userStatuses[it.id]) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
    ) {
        Text(
            text = "🎬 Films Disney",
            color = red,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (universes.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(universes) { universe ->
                    val isSelected = universe.id == selectedUniverse
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) red else surface,
                        modifier = Modifier.clickable { selectedUniverse = universe.id }
                    ) {
                        Text(
                            text = "${universe.emoji} ${universe.name}",
                            color = if (isSelected) white else grey,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = red)
                }
            }
            filteredFilms.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun film dans cet univers", color = grey)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFilms) { film ->
                        FilmCard(film = film, onClick = { onFilmClick(film) })
                    }
                }
            }
        }
    }
}

@Composable
fun FilmCard(film: Film, onClick: () -> Unit) {
    val surface = Color(0xFF2A2A2A)
    val white   = Color.White
    val grey    = Color(0xFF8A8A9A)
    val gold    = Color(0xFFF5C518)
    val red     = Color(0xFFE50914)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Infos du film
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = film.title,
                    color = white,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = film.year.toString(),
                    color = grey,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (film.saga.isNotEmpty()) {
                    Text(
                        text = "Saga : ${film.saga}",
                        color = gold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Badge statut utilisateur
            film.userStatus?.let { status ->
                val (emoji, label) = when (status) {
                    "watched"        -> "✅" to "Vu"
                    "want_to_watch"  -> "👁" to "Veux voir"
                    "own"            -> "📀" to "Possédé"
                    "want_to_sell"   -> "🔁" to "À céder"
                    else             -> return@let
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = red.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$emoji $label",
                        color = white,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}