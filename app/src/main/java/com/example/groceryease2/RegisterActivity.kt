package com.example.groceryease2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.groceryease2.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, BottomNavigationActivity::class.java))
            finish()
        }

        // REGISTER
        binding.btnRegister.setOnClickListener {

            val email = binding.emailTv.text.toString().trim()
            val password = binding.passwordTv.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailTv.error = "Enter Email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordTv.error = "Enter Password"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.passwordTv.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        // EMAIL SAVE
                        val sp = getSharedPreferences("user", MODE_PRIVATE)
                        sp.edit().putString("email", email).apply()

                        Toast.makeText(
                            this,
                            "Account Created Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, BottomNavigationActivity::class.java))
                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // LOGIN
        binding.btnLogin.setOnClickListener {

            val email = binding.emailTv.text.toString().trim()
            val password = binding.passwordTv.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailTv.error = "Enter Email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordTv.error = "Enter Password"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        // EMAIL SAVE
                        val sp = getSharedPreferences("user", MODE_PRIVATE)
                        sp.edit().putString("email", email).apply()

                        Toast.makeText(
                            this,
                            "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, BottomNavigationActivity::class.java))
                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}