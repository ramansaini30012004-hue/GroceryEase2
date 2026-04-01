package com.example.groceryease2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.groceryease2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // ✅ AUTO LOGIN CHECK (FIXED WITH FIREBASE)
        if (auth.currentUser != null) {

            val uid = auth.currentUser?.uid

            FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid!!)
                .get()
                .addOnSuccessListener { snapshot ->

                    val intent = Intent(this, BottomNavigationActivity::class.java)

                    if (snapshot.exists()) {
                        intent.putExtra("openProfile", false) // HOME
                    } else {
                        intent.putExtra("openProfile", true) // PROFILE
                    }

                    startActivity(intent)
                    finish()
                }
        }

        // ✅ REGISTER
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
                .addOnCompleteListener {

                    if (it.isSuccessful) {

                        // ✅ NEW USER → DIRECT PROFILE
                        val intent = Intent(this, BottomNavigationActivity::class.java)
                        intent.putExtra("openProfile", true)

                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // ✅ LOGIN
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
                .addOnCompleteListener {

                    if (it.isSuccessful) {

                        val uid = auth.currentUser?.uid

                        FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(uid!!)
                            .get()
                            .addOnSuccessListener { snapshot ->

                                val intent = Intent(this, BottomNavigationActivity::class.java)

                                if (snapshot.exists()) {
                                    // ✅ Profile already filled
                                    intent.putExtra("openProfile", false) // HOME
                                } else {
                                    // ❌ Profile not filled
                                    intent.putExtra("openProfile", true) // PROFILE
                                }

                                startActivity(intent)
                                finish()
                            }

                    } else {
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}