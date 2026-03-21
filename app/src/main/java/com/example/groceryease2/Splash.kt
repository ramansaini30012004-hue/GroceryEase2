package com.example.groceryease2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class Splash : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({

            if (auth.currentUser != null) {

                startActivity(
                    Intent(this, BottomNavigationActivity::class.java)
                )

            } else {

                startActivity(
                    Intent(this, RegisterActivity::class.java)
                )
            }

            finish()

        }, 2000)
    }
}