package com.example.fairsplit.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.fairsplit.controller.AuthController
import com.example.fairsplit.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var ctrl: AuthController

    // --- Google Sign-In
    private lateinit var googleClient: GoogleSignInClient

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (!idToken.isNullOrBlank()) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Google sign-in failed (no token).", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                setLoading(false)
                Toast.makeText(this, "Google sign-in error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            setLoading(false)
            Toast.makeText(this, "Google sign-in cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---- Configure Google Sign-In (uses default_web_client_id from google-services.json)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.fairsplit.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        // ---- Auth controller callbacks
        ctrl = AuthController { action ->
            runOnUiThread {
                when (action) {
                    is AuthController.Action.Loading -> setLoading(action.on)
                    is AuthController.Action.Error ->
                        Toast.makeText(this, action.msg, Toast.LENGTH_SHORT).show()
                    is AuthController.Action.LoginSuccess -> {
                        startActivity(Intent(this, GroupsActivity::class.java))
                        finish()
                    }
                }
            }
        }

        // ---- Email/password login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val pass  = binding.etPassword.text?.toString()?.trim().orEmpty()

            var ok = true
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter a valid email"
                ok = false
            }
            if (pass.length < 6) {
                binding.etPassword.error = "Password min 6 chars"
                ok = false
            }
            if (!ok) return@setOnClickListener

            ctrl.login(email, pass)
        }

        // ---- Go to register
        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // ---- Forgot password
        binding.tvForgot.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnCompleteListener { t ->
                        val msg = if (t.isSuccessful)
                            "Password reset email sent."
                        else
                            t.exception?.localizedMessage ?: "Could not send reset email."
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
            } else {
                binding.etEmail.error = "Enter your email to reset"
            }
        }

        // ---- Google Sign-In button
        // Make sure you have a button with id `btnGoogle` in ActivityLoginBinding (activity_login.xml)
        binding.btnGoogle.setOnClickListener {
            setLoading(true)
            googleLauncher.launch(googleClient.signInIntent)
        }
    }

    // ----- Helpers -----
    private fun setLoading(on: Boolean) {
        binding.progress.visibility = if (on) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !on
        binding.btnGoRegister.isEnabled = !on
        binding.tvForgot.isEnabled = !on
        binding.etEmail.isEnabled = !on
        binding.etPassword.isEnabled = !on
        // If you added a Google button:
        try { binding.btnGoogle.isEnabled = !on } catch (_: Throwable) {}
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    startActivity(Intent(this, GroupsActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Google auth failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
