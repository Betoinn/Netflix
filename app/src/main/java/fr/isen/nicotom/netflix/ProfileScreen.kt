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

// ===================================================
// ProfileScreen
// Affiche le profil de l'utilisateur connecté
// + sa liste de films avec leur statut
// + bouton de déconnexion
// ===================================================

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val db     = FirebaseDatabase.getInstance().reference
    val auth   = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Couleurs
    val black   = Color(0xFF141414)
    val red     = Color(0xFFE50914)
    val white   = Color.White
    val grey    = Color(0xFF8A8A9A)
    val surface = Color(0xFF2A2A2A)
    val gold    = Color(0xFFF5C518)

    // --- États ---
    var userName     by remember { mutableStateOf("") }
    var userEmail    by remember { mutableStateOf("") }
    var ownedFilms   by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    // Pair(titre du film, statut)
    var isLoading    by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // --- Charger le profil et les films de l'utilisateur ---
    LaunchedEffect(userId) {
        userId ?: run { isLoading = false; return@LaunchedEffect }

        // 1) Infos du profil
        db.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName  = snapshot.child("name").getValue(String::class.java) ?: ""
                    userEmail = snapshot.child("email").getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 2) Statuts des films + titres depuis /films
        db.child("users").child(userId).child("films")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userFilmStatuses = mutableMapOf<String, String>()
                    for (child in snapshot.children) {
                        userFilmStatuses[child.key!!] = child.value as String
                    }

                    if (userFilmStatuses.isEmpty()) {
                        ownedFilms = emptyList()
                        isLoading  = false
                        return
                    }

                    // Récupérer les titres depuis /films
                    db.child("films").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(filmsSnapshot: DataSnapshot) {
                            val result = mutableListOf<Pair<String, String>>()
                            for ((filmId, status) in userFilmStatuses) {
                                val titre = filmsSnapshot.child(filmId)
                                    .child("titre").getValue(String::class.java)
                                    ?: filmId // fallback sur l'id si pas trouvé
                                result.add(titre to status)
                            }
                            // Trier par statut puis par titre
                            ownedFilms = result.sortedWith(compareBy({ it.second }, { it.first }))
                            isLoading  = false
                        }
                        override fun onCancelled(error: DatabaseError) { isLoading = false }
                    })
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    // --- Dialogue de confirmation de déconnexion ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = surface,
            title = { Text("Déconnexion", color = white, fontWeight = FontWeight.Bold) },
            text  = { Text("Voulez-vous vraiment vous déconnecter ?", color = grey) },
            confirmButton = {
                TextButton(onClick = {
                    auth.signOut()
                    onLogout()
                }) {
                    Text("Oui", color = red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler", color = grey)
                }
            }
        )
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- Titre page ---
                item {
                    Text(
                        text = "Mon Profil",
                        color = red,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // --- Carte profil ---
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "👤 $userName",
                                color = white,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userEmail,
                                color = grey,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // --- Bouton déconnexion ---
                item {
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = surface)
                    ) {
                        Text("🚪 Se déconnecter", color = red, fontWeight = FontWeight.Bold)
                    }
                }

                // --- Titre section films ---
                item {
                    Text(
                        text = "Ma collection",
                        color = gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // --- Liste des films ---
                if (ownedFilms.isEmpty()) {
                    item {
                        Text(
                            text = "Vous n'avez encore aucun film dans votre collection.\nMarquez des films depuis leur page détail !",
                            color = grey,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    items(ownedFilms) { (titre, status) ->
                        ProfileFilmItem(
                            titre   = titre,
                            status  = status,
                            surface = surface,
                            white   = white,
                            grey    = grey
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// --- Carte d'un film dans le profil ---
@Composable
fun ProfileFilmItem(
    titre: String,
    status: String,
    surface: Color,
    white: Color,
    grey: Color
) {
    val (emoji, label, badgeColor) = when (status) {
        "watched"        -> Triple("✅", "Vu",            Color(0xFF2E7D32))
        "want_to_watch"  -> Triple("👁", "Je veux voir",  Color(0xFF1565C0))
        "own"            -> Triple("📀", "Je possède",    Color(0xFF6A1B9A))
        "want_to_sell"   -> Triple("🔁", "Je veux céder", Color(0xFFE65100))
        else             -> Triple("❓", status,          Color.Gray)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titre,
                color = white,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.25f)
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