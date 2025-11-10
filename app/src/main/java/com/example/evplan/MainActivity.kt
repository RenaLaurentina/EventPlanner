package com.example.evplan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.evplan.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupEvents()
    }

    private fun setupEvents() {
        binding.tombolLoginDenganGoogle.setOnClickListener {
            loginWithGoogle()
        }
    }

    private fun loginWithGoogle() {
        val request = prepareGoogleSignInRequest()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("GOOGLE_SIGNIN", "Credential error: ${e.localizedMessage}")
                Toast.makeText(
                    this@MainActivity,
                    "Gagal mendapatkan kredensial Google. Coba lagi.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("GOOGLE_SIGNIN", "Unexpected error: ${e.localizedMessage}")
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareGoogleSignInRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            // client ID kamu
            .setServerClientId("259925292147-uauldqmg2bfqm34q61gu03q217i6l4i9.apps.googleusercontent.com")
            .setFilterByAuthorizedAccounts(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun handleSignIn(credential: Credential) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                        toEventListPage()
                    } else {
                        Log.e("FIREBASE_AUTH", "Login gagal: ${task.exception?.localizedMessage}")
                        Toast.makeText(
                            this,
                            "Login gagal: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("SIGNIN_ERROR", "handleSignIn gagal: ${e.localizedMessage}")
            Toast.makeText(this, "Gagal login: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun toEventListPage() {
        try {
            val intent = Intent(this, EventActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("INTENT_ERROR", "Tidak bisa buka EventActivity: ${e.localizedMessage}")
            Toast.makeText(this, "Gagal buka halaman event.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            toEventListPage()
        }
    }
}
