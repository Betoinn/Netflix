package fr.isen.nicotom.netflix

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : Activity() {

    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            login()
        }

        registerButton.setOnClickListener {
            register()
        }
    }

    private fun login() {

        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        usersRef.child(username).get().addOnSuccessListener {

            if (it.exists()) {

                val dbPassword = it.child("password").value.toString()

                if (dbPassword == password) {

                    Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {

                    Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show()

                }

            } else {

                Toast.makeText(this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show()

            }

        }
    }

    private fun register() {

        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        if (username.isEmpty() || password.isEmpty()) {

            Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
            return

        }

        val user = mapOf(
            "email" to "$username@mail.com",
            "password" to password
        )

        usersRef.child(username).setValue(user).addOnSuccessListener {

            Toast.makeText(this, "Compte créé", Toast.LENGTH_SHORT).show()

        }
    }
}