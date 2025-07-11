package dev.romle.roamnoteapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import dev.romle.roamnoteapp.data.AuthRepository
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.ActivityLoginBinding
import dev.romle.roamnoteapp.model.SessionManager
import dev.romle.roamnoteapp.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginBTN.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            lifecycleScope.launch {
                val (success, user) = withContext(Dispatchers.IO) {
                    val result = authRepository.login(email, password)
                    val currentUser = if (result) authRepository.getCurrentUser() else null
                    result to currentUser
                }

                if (success && user != null) {
                    SessionManager.currentUser = User.Builder()
                        .uid(user.uid)
                        .mail(user.email ?: "")
                        .build()

                    Toast.makeText(this@LogInActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LogInActivity, MenuActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LogInActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.registerTXT.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

}
