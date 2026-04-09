package com.example.groceryease2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.groceryease2.databinding.ActivityBottomNavigationBinding

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment_activity_bottom_navigation
            ) as NavHostFragment

        val navController = navHostFragment.navController

        binding.navView.setupWithNavController(navController)

        val openProfile = intent.getBooleanExtra("openProfile", false)

        if (openProfile) {
            navController.navigate(R.id.nav_profile)
        } else {
            navController.navigate(R.id.nav_home)
        }
    }
}
