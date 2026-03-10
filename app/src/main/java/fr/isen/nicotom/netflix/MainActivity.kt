package fr.isen.nicotom.netflix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.nicotom.netflix.ui.theme.NetflixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NetflixTheme {
                MainApp(
                    onLogout = {
                        // Déconnexion → retour au login
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

// ===================================================
// Navigation entre les écrans
// ===================================================

sealed class Screen {
    object Universe : Screen()                   // liste des univers/films
    object Profile  : Screen()                   // profil utilisateur
    data class Detail(val film: Film) : Screen() // détail d'un film
}

@Composable
fun MainApp(onLogout: () -> Unit) {
    val black = Color(0xFF141414)
    val red   = Color(0xFFE50914)
    val white = Color.White

    // Écran actuellement affiché (Universe par défaut)
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Universe) }

    Scaffold(
        containerColor = black,
        topBar = {
            Surface(color = black.copy(alpha = 0.95f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NETFLIX+",
                        color = red,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        },
        bottomBar = {
            // Barre de navigation cachée sur l'écran détail
            if (currentScreen !is Screen.Detail) {
                NavigationBar(containerColor = Color(0xFF1A1A1A)) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Universe,
                        onClick  = { currentScreen = Screen.Universe },
                        icon     = { Text("🎬", fontSize = 20.sp) },
                        label    = { Text("Films", color = white, fontSize = 11.sp) },
                        colors   = NavigationBarItemDefaults.colors(
                            indicatorColor = red.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Profile,
                        onClick  = { currentScreen = Screen.Profile },
                        icon     = { Text("👤", fontSize = 20.sp) },
                        label    = { Text("Profil", color = white, fontSize = 11.sp) },
                        colors   = NavigationBarItemDefaults.colors(
                            indicatorColor = red.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {

                is Screen.Universe -> {
                    UniverseScreen(
                        onFilmClick = { film ->
                            currentScreen = Screen.Detail(film)
                        }
                    )
                }

                is Screen.Profile -> {
                    ProfileScreen(onLogout = onLogout)
                }

                is Screen.Detail -> {
                    DetailFilmScreen(
                        film   = screen.film,
                        onBack = { currentScreen = Screen.Universe }
                    )
                }
            }
        }
    }
}