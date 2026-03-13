package fr.isen.nicotom.netflix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@Composable
fun DetailFilmScreen(
    titre: String?,
    annee: String?
) {
    val database = FirebaseDatabase.getInstance()
    val userId   = UserSession.currentUser ?: return
    val scope    = rememberCoroutineScope()

    var watched      by remember { mutableStateOf(false) }
    var wantToWatch  by remember { mutableStateOf(false) }
    var owned        by remember { mutableStateOf(false) }
    var wantToGetRid by remember { mutableStateOf(false) }

    var posterUrl   by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf<String?>(null) }
    var isLoadingTmdb by remember { mutableStateOf(true) }

    var usersWhoOwn        by remember { mutableStateOf(listOf<String>()) }
    var usersWhoWantToSell by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(titre) {

        database.getReference("users")
            .child(userId)
            .child("films")
            .child(titre ?: "")
            .get()
            .addOnSuccessListener { snapshot ->
                watched      = snapshot.child("watched").value == true
                wantToWatch  = snapshot.child("wantToWatch").value == true
                owned        = snapshot.child("owned").value == true
                wantToGetRid = snapshot.child("wantToGetRid").value == true
            }

        database.getReference("users").get().addOnSuccessListener { snapshot ->
            val ownList  = mutableListOf<String>()
            val sellList = mutableListOf<String>()
            for (userSnapshot in snapshot.children) {
                val username = userSnapshot.key ?: continue
                if (username == userId) continue
                val filmSnap = userSnapshot.child("films").child(titre ?: "")
                if (filmSnap.child("owned").value == true)        ownList.add(username)
                if (filmSnap.child("wantToGetRid").value == true) sellList.add(username)
            }
            usersWhoOwn        = ownList
            usersWhoWantToSell = sellList
        }

        scope.launch {
            try {
                val response = TmdbApi.service.searchMovie(
                    apiKey = TmdbApi.API_KEY,
                    query  = titre ?: ""
                )
                val film = response.results.firstOrNull()
                posterUrl   = film?.posterUrl()
                description = film?.overview?.ifEmpty { "Aucune description disponible." }
                    ?: "Aucune description disponible."
            } catch (e: Exception) {
                description = "Impossible de charger la description."
            } finally {
                isLoadingTmdb = false
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        if (isLoadingTmdb) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = "Affiche de $titre",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎬", fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = titre ?: "Titre inconnu",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "📅 Date de sortie : ${annee ?: "N/A"}",
            color = Color.Red,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Synopsis",
            color = Color.LightGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = description ?: "Chargement...",
            color = Color.Gray,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Mon statut",
            color = Color.LightGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CheckboxRow("✅ Watched", watched) {
            watched = it; updateFirebase("watched", it)
        }
        CheckboxRow("👁 Want to watch", wantToWatch) {
            wantToWatch = it; updateFirebase("wantToWatch", it)
        }
        CheckboxRow("📀 Own on DVD / BluRay", owned) {
            owned = it; updateFirebase("owned", it)
        }
        CheckboxRow("🔁 Want to get rid of", wantToGetRid) {
            wantToGetRid = it; updateFirebase("wantToGetRid", it)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "📀 Users who own this movie",
            color = Color.LightGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
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

        Text(
            text = "🔁 Users who whant to get rid of it",
            color = Color.LightGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
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

        Spacer(modifier = Modifier.height(40.dp))
    }
}

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