package com.example.groceryease2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.groceryease2.databinding.ActivityBottomNavigationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.button_green)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment_activity_bottom_navigation
            ) as NavHostFragment

        val navController = navHostFragment.navController

        val sp = getSharedPreferences("user", MODE_PRIVATE)
        val openProfile = intent.getBooleanExtra("openProfile", false)

        // ✅ AUTO HANDLE NAVIGATION (BEST PRACTICE)
        navView.setupWithNavController(navController)

        // ✅ START SCREEN FIX
        if (openProfile) {
            navController.navigate(R.id.nav_profile)
        } else {
            navController.navigate(R.id.nav_home)
        }

        // ✅ LOCK SYSTEM
        navView.setOnItemSelectedListener { item ->

            val completed = sp.getBoolean("profileCompleted", false)

            if (!completed && item.itemId != R.id.nav_profile) {
                Toast.makeText(this, "Complete Profile First", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }

            // Let NavigationUI handle navigation
            when (item.itemId) {
                R.id.nav_home -> navController.navigate(R.id.nav_home)
                R.id.nav_notifications -> navController.navigate(R.id.nav_notifications)
                R.id.nav_profile -> navController.navigate(R.id.nav_profile)
            }

            true
        }
    }
}