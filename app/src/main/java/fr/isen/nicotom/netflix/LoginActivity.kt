package fr.isen.nicotom.netflix

/*import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.nicotom.netflix.ui.theme.NetflixTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Si déjà connecté → aller directement à MainActivity
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            NetflixTheme {
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val auth     = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    // --- États du formulaire ---
    var isLoginMode  by remember { mutableStateOf(true) }
    var name         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var errorMsg     by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    // Couleurs
    val black   = Color(0xFF141414)
    val red     = Color(0xFFE50914)
    val white   = Color.White
    val grey    = Color(0xFF8A8A9A)
    val surface = Color(0xFF2A2A2A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Titre ---
            Text(
                text = "NETFLIX+",
                color = red,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Votre collection de films",
                color = grey,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            // --- Onglets Connexion / Inscription ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = { isLoginMode = true; errorMsg = "" },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoginMode) red else surface
                    )
                ) {
                    Text("Connexion", color = white, fontSize = 13.sp)
                }
                Button(
                    onClick = { isLoginMode = false; errorMsg = "" },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isLoginMode) red else surface
                    )
                ) {
                    Text("Inscription", color = white, fontSize = 13.sp)
                }
            }

            // --- Champ Nom (inscription seulement) ---
            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom d'affichage", color = grey) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = white,
                        unfocusedTextColor = white,
                        focusedBorderColor = red,
                        unfocusedBorderColor = grey
                    ),
                    singleLine = true
                )
            }

            // --- Champ Email ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Adresse e-mail", color = grey) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = white,
                    unfocusedTextColor = white,
                    focusedBorderColor = red,
                    unfocusedBorderColor = grey
                ),
                singleLine = true
            )

            // --- Champ Mot de passe ---
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe", color = grey) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(
                            text = if (showPassword) "Cacher" else "Voir",
                            color = grey,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = white,
                    unfocusedTextColor = white,
                    focusedBorderColor = red,
                    unfocusedBorderColor = grey
                ),
                singleLine = true
            )

            // --- Message d'erreur ---
            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Bouton principal ou chargement ---
            if (isLoading) {
                CircularProgressIndicator(color = red)
            } else {
                Button(
                    onClick = {
                        // Validation
                        if (email.isBlank() || password.isBlank()) {
                            errorMsg = "Veuillez remplir tous les champs"
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMsg = "Le mot de passe doit avoir au moins 6 caractères"
                            return@Button
                        }
                        if (!isLoginMode && name.isBlank()) {
                            errorMsg = "Veuillez entrer un nom d'affichage"
                            return@Button
                        }

                        isLoading = true
                        errorMsg  = ""

                        if (isLoginMode) {
                            // CONNEXION
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener { onLoginSuccess() }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMsg  = firebaseError(e.message)
                                }
                        } else {
                            // INSCRIPTION
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener { result ->
                                    val uid = result.user!!.uid
                                    database.child("users").child(uid).setValue(
                                        mapOf("name" to name, "email" to email)
                                    )
                                        .addOnSuccessListener { onLoginSuccess() }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMsg  = "Compte créé mais erreur : ${e.message}"
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMsg  = firebaseError(e.message)
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = red)
                ) {
                    Text(
                        text = if (isLoginMode) "Se connecter" else "Créer un compte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = white
                    )
                }
            }
        }
    }
}*/

// Traduit les erreurs Firebase en français
fun firebaseError(msg: String?): String = when {
    msg == null                        -> "Erreur inconnue"
    "no user record"     in msg        -> "Aucun compte avec cet email"
    "password is invalid" in msg       -> "Mot de passe incorrect"
    "email address is already" in msg  -> "Cet email est déjà utilisé"
    "badly formatted"    in msg        -> "Format d'email invalide"
    "network error"      in msg        -> "Pas de connexion internet"
    else                               -> "Erreur : $msg"
}