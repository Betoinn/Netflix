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
    annee: String?
) {
    val database = FirebaseDatabase.getInstance()
    val userId   = UserSession.currentUser ?: return

    // --- États des cases à cocher ---
    var watched      by remember { mutableStateOf(false) }
    var wantToWatch  by remember { mutableStateOf(false) }
    var owned        by remember { mutableStateOf(false) }
    var wantToGetRid by remember { mutableStateOf(false) }

    // --- Listes des autres utilisateurs ---
    var usersWhoOwn         by remember { mutableStateOf(listOf<String>()) }
    var usersWhoWantToSell  by remember { mutableStateOf(listOf<String>()) }

    // --- Charger les statuts de l'utilisateur connecté ---
    LaunchedEffect(titre) {
        val ref = database.getReference("users")
            .child(userId)
            .child("films")
            .child(titre ?: "")

        ref.get().addOnSuccessListener { snapshot ->
            watched      = snapshot.child("watched").value == true
            wantToWatch  = snapshot.child("wantToWatch").value == true
            owned        = snapshot.child("owned").value == true
            wantToGetRid = snapshot.child("wantToGetRid").value == true
        }

        // --- Charger les autres utilisateurs ---
        database.getReference("users").get().addOnSuccessListener { snapshot ->
            val ownList     = mutableListOf<String>()
            val sellList    = mutableListOf<String>()

            for (userSnapshot in snapshot.children) {
                val username = userSnapshot.key ?: continue

                // On ne s'affiche pas soi-même
                if (username == userId) continue

                val filmSnapshot = userSnapshot.child("films").child(titre ?: "")

                if (filmSnapshot.child("owned").value == true) {
                    ownList.add(username)
                }
                if (filmSnapshot.child("wantToGetRid").value == true) {
                    sellList.add(username)
                }
            }

            usersWhoOwn        = ownList
            usersWhoWantToSell = sellList
        }
    }

    // --- Sauvegarder un statut dans Firebase ---
    fun updateFirebase(field: String, value: Boolean) {
        database.getReference("users")
            .child(userId)
            .child("films")
            .child(titre ?: "")
            .child(field)
            .setValue(value)
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {

        // Titre du film
        Text(
            text = titre ?: "Titre inconnu",
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Année de sortie
        Text(
            text = "Date de sortie : ${annee ?: "N/A"}",
            color = Color.Red,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Mon statut ---
        Text(
            text = "Mon statut",
            color = Color.LightGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CheckboxRow("✅ Watched", watched) {
            watched = it
            updateFirebase("watched", it)
        }
        CheckboxRow("👁 Want to watch", wantToWatch) {
            wantToWatch = it
            updateFirebase("wantToWatch", it)
        }
        CheckboxRow("📀 Own on DVD / BluRay", owned) {
            owned = it
            updateFirebase("owned", it)
        }
        CheckboxRow("🔁 Want to get rid of", wantToGetRid) {
            wantToGetRid = it
            updateFirebase("wantToGetRid", it)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Utilisateurs qui possèdent ce film ---
        Text(
            text = "📀 Utilisateurs qui possèdent ce film",
            color = Color.LightGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (usersWhoOwn.isEmpty()) {
            Text("Personne pour l'instant", color = Color.DarkGray, fontSize = 14.sp)
        } else {
            usersWhoOwn.forEach { name ->
                Text("👤 $name", color = Color.White, fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Utilisateurs qui veulent s'en débarrasser ---
        Text(
            text = "🔁 Utilisateurs qui veulent céder ce film",
            color = Color.LightGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (usersWhoWantToSell.isEmpty()) {
            Text("Personne pour l'instant", color = Color.DarkGray, fontSize = 14.sp)
        } else {
            usersWhoWantToSell.forEach { name ->
                Text("👤 $name", color = Color.White, fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

// Case à cocher réutilisable
@Composable
fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Red,
                uncheckedColor = Color.Gray
            )
        )
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}