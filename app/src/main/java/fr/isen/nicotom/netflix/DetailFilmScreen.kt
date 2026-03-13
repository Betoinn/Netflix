package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase

@Composable
fun DetailFilmScreen(
    titre: String?,
    annee: String?,
    currentTab: String,
    onTabChange: (String) -> Unit
) {

    val database = FirebaseDatabase.getInstance()
    val userId = UserSession.currentUser ?: return

    var watched by remember { mutableStateOf(false) }
    var wantToWatch by remember { mutableStateOf(false) }
    var owned by remember { mutableStateOf(false) }
    var wantToGetRid by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        val ref = database.getReference("users")
            .child(userId)
            .child("films")
            .child(titre ?: "")

        ref.get().addOnSuccessListener {

            watched = it.child("watched").value == true
            wantToWatch = it.child("wantToWatch").value == true
            owned = it.child("owned").value == true
            wantToGetRid = it.child("wantToGetRid").value == true

        }
    }

    fun updateFirebase(field: String, value: Boolean) {

        database.getReference("users")
            .child(userId)
            .child("films")
            .child(titre ?: "")
            .child(field)
            .setValue(value)

    }

    Scaffold(

        containerColor = Color.Black,

        bottomBar = {

            NavigationBar(
                containerColor = Color(0xFF1A1A1A)
            ) {

                NavigationBarItem(
                    selected = currentTab == "films",
                    onClick = { onTabChange("films") },
                    icon = { Text("🎬") },
                    label = { Text("Films") }
                )

                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { onTabChange("profile") },
                    icon = { Text("👤") },
                    label = { Text("Profil") }
                )

            }

        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .padding(24.dp),

            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = titre ?: "Titre inconnu",
                color = Color.White,
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Date de sortie : ${annee ?: "N/A"}",
                color = Color.Red,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            CheckboxRow("Watched", watched) {
                watched = it
                updateFirebase("watched", it)
            }

            CheckboxRow("Want to watch", wantToWatch) {
                wantToWatch = it
                updateFirebase("wantToWatch", it)
            }

            CheckboxRow("Own on DVD / BluRay", owned) {
                owned = it
                updateFirebase("owned", it)
            }

            CheckboxRow("Want to get rid of", wantToGetRid) {
                wantToGetRid = it
                updateFirebase("wantToGetRid", it)
            }

        }

    }

}

@Composable
fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp
        )

    }

}