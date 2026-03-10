package fr.isen.nicotom.netflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import fr.isen.nicotom.netflix.ui.theme.NetflixTheme

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NetflixTheme {
                Text("Login Screen")
            }
        }
    }
}

