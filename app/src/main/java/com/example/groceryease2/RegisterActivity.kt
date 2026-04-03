package com.example.groceryease2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.groceryease2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 🔥 UI hide initially (IMPORTANT)
        binding.root.visibility = View.GONE

        // ✅ FAST AUTO LOGIN CHECK
        val user = auth.currentUser

        if (user != null) {
            // Already logged in → go to Home
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("openProfile", false)
            startActivity(intent)
            finish()
            return
        } else {
            // Not logged in → show UI
            binding.root.visibility = View.VISIBLE
        }

        // ================= REGISTER =================
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
                binding.passwordTv.error = "Min 6 characters required"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, BottomNavigationActivity::class.java)
                    intent.putExtra("openProfile", true) // new user
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }

        // ================= LOGIN =================
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
                .addOnSuccessListener {

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, BottomNavigationActivity::class.java)
                    intent.putExtra("openProfile", false)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }
}