package fr.isen.nicotom.netflix

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nicotom.netflix.ui.theme.NetflixTheme

class LoginActivity : ComponentActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            NetflixTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        NetflixTopBar(isDetailView = false, onBackClick = {})
                    },
                    containerColor = Color.Black
                ) { innerPadding ->

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color.Black
                    ) {

                        LoginContent(
                            onLogin = { user, pass -> login(user, pass) },
                            onRegister = { user, pass -> register(user, pass) }
                        )

                    }
                }
            }
        }
    }

    private fun login(username: String, pass: String) {

        if (username.isEmpty()) {
            Toast.makeText(this, "Entrez un nom d'utilisateur", Toast.LENGTH_SHORT).show()
            return
        }

        usersRef.child(username).get().addOnSuccessListener { snapshot ->

            if (snapshot.exists()) {

                val dbPassword = snapshot.child("password").getValue(String::class.java)

                if (dbPassword == pass) {

                    // utilisateur connecté
                    UserSession.currentUser = username

                    Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {

            Toast.makeText(this, "Erreur Firebase", Toast.LENGTH_LONG).show()

        }
    }

    private fun register(username: String, pass: String) {

        if (username.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val user = mapOf(
            "password" to pass
        )

        usersRef.child(username).setValue(user).addOnSuccessListener {

            Toast.makeText(this, "Compte créé pour $username", Toast.LENGTH_SHORT).show()

        }
    }
}

@Composable
fun LoginContent(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "NETFLIX+",
            color = Color.Red,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLogin(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("S'IDENTIFIER", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { onRegister(username, password) }) {
            Text("Pas encore de compte ? Inscrivez-vous", color = Color.LightGray)
        }
    }
}