package com.example.groceryease2

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var darkMode: SwitchCompat
    private lateinit var notification: SwitchCompat
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        darkMode = findViewById(R.id.darkMode)
        notification = findViewById(R.id.notification)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Load saved mode
        val isDark = prefs.getBoolean("dark_mode", false)
        darkMode.isChecked = isDark

        // Switch change listener
        darkMode.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                prefs.edit().putBoolean("dark_mode", true).apply()

            } else {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                prefs.edit().putBoolean("dark_mode", false).apply()

            }

        }
    }
}