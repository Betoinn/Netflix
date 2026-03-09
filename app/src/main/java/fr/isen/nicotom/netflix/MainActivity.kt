package fr.isen.nicotom.netflix

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import fr.isen.nicotom.netflix.ui.theme.NetflixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetflixTheme {
                // Scaffold avec fillMaxSize comme ton exemple
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        NetflixTopBar()
                    },
                    containerColor = Color.Black
                ) { innerPadding ->
                    // Application du padding interne pour ne pas chevaucher la TopBar
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color.Black
                    ) {
                        // Utilisation de données statiques (sans API)
                        UniverseScreen(onUniverseClick = { universeName ->
                            Toast.makeText(
                                this,
                                "Univers sélectionné : $universeName",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetflixTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "NETFLIX+",
                color = Color.Red,
                fontWeight = FontWeight.ExtraBold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    )
}