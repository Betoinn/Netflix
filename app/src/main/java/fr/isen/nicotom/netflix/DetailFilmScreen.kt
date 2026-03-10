package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


@Composable
fun DetailFilmScreen(film: Film, onBack: () -> Unit) {
    val db     = FirebaseDatabase.getInstance().reference
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val black   = Color(0xFF141414)
    val red     = Color(0xFFE50914)
    val white   = Color.White
    val grey    = Color(0xFF8A8A9A)
    val surface = Color(0xFF2A2A2A)
    val gold    = Color(0xFFF5C518)

    // Statut actuel de l'utilisateur pour ce film
    var currentStatus by remember { mutableStateOf<String?>(null) }

    // Liste des users qui possèdent le film et veulent s'en débarrasser
    var ownersWantingToSell by remember { mutableStateOf<List<String>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var message   by remember { mutableStateOf("") }

    // --- Charger le statut actuel de l'utilisateur ---
    LaunchedEffect(film.id) {
        userId?.let { uid ->
            db.child("users").child(uid).child("films").child(film.id)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        currentStatus = snapshot.getValue(String::class.java)
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        // Charger les utilisateurs qui possèdent ce film et veulent s'en débarrasser
        db.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sellers = mutableListOf<String>()
                for (userSnapshot in snapshot.children) {
                    val status = userSnapshot.child("films").child(film.id)
                        .getValue(String::class.java)
                    if (status == "want_to_sell") {
                        val name = userSnapshot.child("name").getValue(String::class.java)
                            ?: "Utilisateur inconnu"
                        sellers.add(name)
                    }
                }
                ownersWantingToSell = sellers
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- Fonction pour mettre à jour le statut ---
    fun setStatus(newStatus: String) {
        userId ?: run {
            message = "Vous devez être connecté"
            return
        }
        isLoading = true

        // Si on reclique sur le même statut → on le supprime
        if (currentStatus == newStatus) {
            db.child("users").child(userId).child("films").child(film.id).removeValue()
                .addOnSuccessListener {
                    isLoading = false
                    message   = "Statut supprimé"
                }
                .addOnFailureListener {
                    isLoading = false
                    message   = "Erreur lors de la suppression"
                }
        } else {
            db.child("users").child(userId).child("films").child(film.id)
                .setValue(newStatus)
                .addOnSuccessListener {
                    isLoading = false
                    message   = "Statut mis à jour ✓"
                }
                .addOnFailureListener {
                    isLoading = false
                    message   = "Erreur lors de la mise à jour"
                }
        }
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
    ) {
        // Barre du haut avec bouton retour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Retour", color = red, fontSize = 14.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Titre du film ---
            item {
                Text(
                    text = film.titre,
                    color = white,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // --- Infos (année, saga) ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    film.annee?.let {
                        InfoRow(label = "📅 Année", value = it.toString(), grey = grey, white = white)
                    }
                    film.genre?.let {
                        InfoRow(label = "🎭 Genre", value = it, grey = grey, white = white)
                    }
                }
            }

            // --- Section : Mon statut ---
            item {
                Text(
                    text = "Mon statut",
                    color = gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // --- Boutons de statut ---
            item {
                val statusOptions = listOf(
                    Triple("watched",       "✅", "Vu"),
                    Triple("want_to_watch", "👁", "Je veux voir"),
                    Triple("own",           "📀", "Je possède"),
                    Triple("want_to_sell",  "🔁", "Je veux céder")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statusOptions.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (statusKey, emoji, label) ->
                                val isSelected = currentStatus == statusKey
                                Button(
                                    onClick = { setStatus(statusKey) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) red else surface
                                    )
                                ) {
                                    Text(
                                        text = "$emoji $label",
                                        color = if (isSelected) white else grey,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold
                                        else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Message de confirmation
            if (message.isNotEmpty()) {
                item {
                    Text(
                        text = message,
                        color = if (message.contains("Erreur")) Color(0xFFFF6B6B)
                        else Color(0xFF4CAF50),
                        fontSize = 13.sp
                    )
                }
            }

            // Chargement
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = red, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // --- Section : Personnes qui veulent céder ce film ---
            item {
                Text(
                    text = "🔁 Personnes qui veulent céder ce film",
                    color = gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (ownersWantingToSell.isEmpty()) {
                item {
                    Text(
                        text = "Personne ne veut céder ce film pour l'instant",
                        color = grey,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(ownersWantingToSell) { userName ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👤 $userName",
                            color = white,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Espace en bas
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// Petite ligne info "Label : Valeur"
@Composable
fun InfoRow(label: String, value: String, grey: Color, white: Color) {
    Row {
        Text(text = "$label : ", color = grey, fontSize = 14.sp)
        Text(text = value, color = white, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}