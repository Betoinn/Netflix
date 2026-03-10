package fr.isen.nicotom.netflix

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    private lateinit var btnTabLogin: Button
    private lateinit var btnTabRegister: Button
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: com.google.android.material.textfield.TextInputEditText
    private lateinit var etEmail: com.google.android.material.textfield.TextInputEditText
    private lateinit var etPassword: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMain()
            return
        }

        btnTabLogin   = findViewById(R.id.btnTabLogin)
        btnTabRegister = findViewById(R.id.btnTabRegister)
        tilName       = findViewById(R.id.tilName)
        etName        = findViewById(R.id.etName)
        etEmail       = findViewById(R.id.etEmail)
        etPassword    = findViewById(R.id.etPassword)
        btnSubmit     = findViewById(R.id.btnSubmit)
        tvError       = findViewById(R.id.tvError)
        progressBar   = findViewById(R.id.progressBar)

        btnTabLogin.setOnClickListener {
            isLoginMode = true
            tilName.visibility = View.GONE
            btnSubmit.text = "Se connecter"
            btnTabLogin.backgroundTintList = getColorStateList(R.color.gold)
            btnTabRegister.backgroundTintList = getColorStateList(R.color.surface)
            clearError()
        }

        btnTabRegister.setOnClickListener {
            isLoginMode = false
            tilName.visibility = View.VISIBLE
            btnSubmit.text = "Créer un compte"
            btnTabRegister.backgroundTintList = getColorStateList(R.color.gold)
            btnTabLogin.backgroundTintList = getColorStateList(R.color.surface)
            clearError()
        }

        btnSubmit.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showError("Veuillez remplir tous les champs")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError("Le mot de passe doit avoir au moins 6 caractères")
                return@setOnClickListener
            }

            if (isLoginMode) {
                login(email, password)
            } else {
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    showError("Veuillez entrer un nom d'affichage")
                    return@setOnClickListener
                }
                register(email, password, name)
            }
        }
    }

    private fun login(email: String, password: String) {
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                goToMain()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                showError(getFirebaseErrorMessage(e.message))
            }
    }

    private fun register(email: String, password: String, name: String) {
        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user!!.uid

                val userProfile = mapOf(
                    "name"  to name,
                    "email" to email
                )
                database.child("users").child(userId).setValue(userProfile)
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        showError("Compte créé mais erreur profil : ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                showError(getFirebaseErrorMessage(e.message))
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !loading
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun clearError() {
        tvError.visibility = View.GONE
    }


    private fun getFirebaseErrorMessage(msg: String?): String {
        return when {
            msg == null                          -> "Erreur inconnue"
            "no user record"     in msg          -> "Aucun compte avec cet email"
            "password is invalid" in msg         -> "Mot de passe incorrect"
            "email address is already" in msg    -> "Cet email est déjà utilisé"
            "badly formatted"    in msg          -> "Format d'email invalide"
            "network error"      in msg          -> "Pas de connexion internet"
            else                                 -> "Erreur : $msg"
        }
    }
}