package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val user     = UserSession.currentUser ?: return
    val database = FirebaseDatabase.getInstance()

    var watched      by remember { mutableStateOf(listOf<String>()) }
    var wantToWatch  by remember { mutableStateOf(listOf<String>()) }
    var owned        by remember { mutableStateOf(listOf<String>()) }
    var wantToGetRid by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        database.getReference("users")
            .child(user)
            .child("films")
            .get()
            .addOnSuccessListener { snapshot ->

                val watchedList      = mutableListOf<String>()
                val wantToWatchList  = mutableListOf<String>()
                val ownedList        = mutableListOf<String>()
                val wantToGetRidList = mutableListOf<String>()

                for (film in snapshot.children) {
                    val title = film.key ?: continue

                    if (film.child("watched").value      == true) watchedList.add(title)
                    if (film.child("wantToWatch").value  == true) wantToWatchList.add(title)
                    if (film.child("owned").value        == true) ownedList.add(title)
                    if (film.child("wantToGetRid").value == true) wantToGetRidList.add(title)
                }

                watched      = watchedList
                wantToWatch  = wantToWatchList
                owned        = ownedList
                wantToGetRid = wantToGetRidList
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {

        item {
            Text(
                text = "Profil : $user",
                color = Color.Red,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionTitle(emoji = "✅", title = "Watched", count = watched.size)
        }
        if (watched.isEmpty()) {
            item { EmptyMessage() }
        } else {
            items(watched) { FilmRow(it) }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            SectionTitle(emoji = "👁", title = "Want to watch", count = wantToWatch.size)
        }
        if (wantToWatch.isEmpty()) {
            item { EmptyMessage() }
        } else {
            items(wantToWatch) { FilmRow(it) }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            SectionTitle(emoji = "📀", title = "Own on DVD / BluRay", count = owned.size)
        }
        if (owned.isEmpty()) {
            item { EmptyMessage() }
        } else {
            items(owned) { FilmRow(it) }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            SectionTitle(emoji = "🔁", title = "Want to get rid of", count = wantToGetRid.size)
        }
        if (wantToGetRid.isEmpty()) {
            item { EmptyMessage() }
        } else {
            items(wantToGetRid) { FilmRow(it) }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }

        item {
            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("🚪 Se déconnecter", fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun SectionTitle(emoji: String, title: String, count: Int) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "$emoji $title",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "($count)",
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}

@Composable
fun FilmRow(title: String) {
    Text(
        text = "• $title",
        color = Color.LightGray,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 3.dp, horizontal = 8.dp)
    )
}

@Composable
fun EmptyMessage() {
    Text(
        text = "Aucun film",
        color = Color.DarkGray,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    )
}