package dev.romle.roamnoteapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

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
        binding.menuBTNTrips.setOnClickListener { goToMain("trips") }
        binding.menuBTNDailylog.setOnClickListener { goToMain("dailylog") }
        binding.menuBTNMap.setOnClickListener { goToMain("map") }
        binding.menuBTNForum.setOnClickListener { goToMain("forum") }
    }
}