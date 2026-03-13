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

// ===================================================
// DetailFilmScreen
// Affiche les infos d'un film + cases à cocher statut
// Compatible avec ton MainActivity et UserSession
// ===================================================

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

    // --- Charger les statuts depuis Firebase ---
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

        // --- Cases à cocher ---
        Text(
            text = "Mon statut",
            color = Color.LightGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
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

        // --- Utilisateurs qui veulent céder ce film ---
        WantToSellSection(titre = titre)
    }
}

// ===================================================
// Case à cocher réutilisable
// ===================================================
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
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

// ===================================================
// Section : qui veut céder ce film ?
// ===================================================
@Composable
fun WantToSellSection(titre: String?) {
    val database = FirebaseDatabase.getInstance()
    var sellers by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(titre) {
        database.getReference("users").get().addOnSuccessListener { snapshot ->
            val result = mutableListOf<String>()
            for (userSnapshot in snapshot.children) {
                val wantToGetRid = userSnapshot
                    .child("films")
                    .child(titre ?: "")
                    .child("wantToGetRid")
                    .value == true

                if (wantToGetRid) {
                    result.add(userSnapshot.key ?: "Inconnu")
                }
            }
            sellers = result
        }
    }

    Text(
        text = "🔁 Personnes qui veulent céder ce film",
        color = Color.LightGray,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (sellers.isEmpty()) {
        Text(
            text = "Personne pour l'instant",
            color = Color.Gray,
            fontSize = 14.sp
        )
    } else {
        sellers.forEach { name ->
            Text(
                text = "👤 $name",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}