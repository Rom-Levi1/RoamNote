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
import dev.romle.roamnoteapp.data.TripsRepository
import dev.romle.roamnoteapp.databinding.ActivitySignUpBinding
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySignUpBinding

    private val authRepository = AuthRepository()

    private val tripsRepo = TripsRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.registerBTN.setOnClickListener {
            val email = binding.signupEmailEditText.text.toString()
            val password = binding.signupPasswordEditText.text.toString()
            val username = binding.signupUsernameEditText.text.toString()

            if (username.isBlank()) {
                Toast.makeText(this, "Username cannot be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tripsRepo.isUsernameAvailable(username) { isAvailable ->
                if (!isAvailable) {
                    Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
                    return@isUsernameAvailable
                }

                lifecycleScope.launch {
                    val success = authRepository.register(email, password)
                    if (success) {
                        tripsRepo.addUsername(username)

                        Toast.makeText(
                            this@SignUpActivity,
                            "Registered & logged in! successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

            binding.registeredTXT.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, LogInActivity::class.java))
                finish()
            }
        }
    }
}