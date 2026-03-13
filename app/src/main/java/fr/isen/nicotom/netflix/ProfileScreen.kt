package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val user = UserSession.currentUser ?: return

    val database = FirebaseDatabase.getInstance()

    var watched by remember { mutableStateOf(listOf<String>()) }
    var wantToWatch by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {

        database.getReference("users")
            .child(user)
            .child("films")
            .get()
            .addOnSuccessListener {

                val watchedList = mutableListOf<String>()
                val wantList = mutableListOf<String>()

                for (film in it.children) {

                    val title = film.key ?: continue

                    if (film.child("watched").value == true) {
                        watchedList.add(title)
                    }

                    if (film.child("wantToWatch").value == true) {
                        wantList.add(title)
                    }

                }

                watched = watchedList
                wantToWatch = wantList
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {

        Text(
            text = "Profil : $user",
            color = Color.Red,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Watched", color = Color.White)

        LazyColumn {
            items(watched) {
                Text(it, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Want to watch", color = Color.White)

        LazyColumn {
            items(wantToWatch) {
                Text(it, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = { onLogout() }) {
            Text("Se déconnecter")
        }
    }
}