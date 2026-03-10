package fr.isen.nicotom.netflix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.isen.nicotom.netflix.ui.theme.NetflixTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            NetflixTheme {

                var selectedFilm by remember { mutableStateOf<Film?>(null) }
                var currentTab by remember { mutableStateOf("films") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    topBar = {
                        NetflixTopBar(
                            isDetailView = selectedFilm != null,
                            onBackClick = { selectedFilm = null }
                        )
                    },

                    bottomBar = {

                        if (selectedFilm == null) {

                            NavigationBar(
                                containerColor = Color(0xFF1A1A1A)
                            ) {

                                NavigationBarItem(
                                    selected = currentTab == "films",
                                    onClick = { currentTab = "films" },
                                    icon = { Text("🎬") },
                                    label = { Text("Films") }
                                )

                                NavigationBarItem(
                                    selected = currentTab == "profile",
                                    onClick = { currentTab = "profile" },
                                    icon = { Text("👤") },
                                    label = { Text("Profil") }
                                )

                            }
                        }
                    },

                    containerColor = Color.Black

                ) { innerPadding ->

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color.Black
                    ) {

                        if (selectedFilm != null) {

                            DetailFilmScreen(
                                titre = selectedFilm?.titre,
                                annee = selectedFilm?.annee?.toString()
                            )

                        } else {

                            when (currentTab) {

                                "films" -> {

                                    UniverseScreen(
                                        onFilmClick = { film ->
                                            selectedFilm = film
                                        }
                                    )

                                }

                                "profile" -> {

                                    ProfileScreen(
                                        onLogout = {
                                            startActivity(
                                                Intent(
                                                    this@MainActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                    )

                                }

                            }

                        }

                    }
                }

                BackHandler(enabled = selectedFilm != null) {
                    selectedFilm = null
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetflixTopBar(
    isDetailView: Boolean,
    onBackClick: () -> Unit
) {

    TopAppBar(
        title = {
            Text(
                text = if (isDetailView) "DÉTAILS" else "NETFLIX",
                color = Color.Red,
                fontWeight = FontWeight.ExtraBold
            )
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )

    )
}