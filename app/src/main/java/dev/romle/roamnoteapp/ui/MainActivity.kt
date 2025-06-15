package dev.romle.roamnoteapp.ui

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_trips,
                R.id.navigation_day_log,
                R.id.navigation_map,
                R.id.navigation_Forum
            )
        )
        navView.setupWithNavController(navController)

        initViews()


    }

    private fun initViews() {
        binding.mainArrowBack.setOnClickListener{
            startActivity(Intent(this,MenuActivity::class.java))
            finish()
        }
        val target = intent.getStringExtra("target_fragment")
        when (target) {
            "trips" -> binding.navView.selectedItemId = R.id.navigation_trips
            "dailylog" -> binding.navView.selectedItemId = R.id.navigation_day_log
            "map" -> binding.navView.selectedItemId = R.id.navigation_map
            "forum" -> binding.navView.selectedItemId = R.id.navigation_Forum
        }
    }
}