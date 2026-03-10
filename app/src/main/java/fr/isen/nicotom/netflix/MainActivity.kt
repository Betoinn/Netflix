package fr.isen.nicotom.netflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    topBar = {
                        NetflixTopBar(
                            isDetailView = selectedFilm != null,
                            onBackClick = { selectedFilm = null }
                        )
                    },

                    containerColor = Color.Black

                ) { innerPadding ->

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color.Black
                    ) {

                        if (selectedFilm == null) {

                            UniverseScreen(
                                onFilmClick = { film ->
                                    selectedFilm = film
                                }
                            )

                        } else {

                            DetailFilmScreen(
                                titre = selectedFilm?.titre,
                                annee = selectedFilm?.annee?.toString()
                            )

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