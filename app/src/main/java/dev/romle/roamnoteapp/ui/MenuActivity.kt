package dev.romle.roamnoteapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.data.AuthRepository
import dev.romle.roamnoteapp.data.TripsRepository
import dev.romle.roamnoteapp.databinding.ActivityMenuBinding
import dev.romle.roamnoteapp.model.SessionManager

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    private val auth = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }


    private fun goToMain(target: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("target_fragment", target)
        startActivity(intent)
        finish()
    }

    private fun initViews() {
        TripsRepository().loadTrips { trips ->
            SessionManager.currentUser?.trips?.clear()
            SessionManager.currentUser?.trips?.addAll(trips)
            Log.d("MainActivity", "Trips loaded into session: ${trips.size}")
        }
        binding.menuTxtLogOut.setOnClickListener {
            auth.logout()
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.menuBTNTrips.setOnClickListener { goToMain("trips") }
        binding.menuBTNDailylog.setOnClickListener { goToMain("dailylog") }
        binding.menuBTNMap.setOnClickListener { goToMain("map") }
        binding.menuBTNForum.setOnClickListener { goToMain("forum") }
    }
}