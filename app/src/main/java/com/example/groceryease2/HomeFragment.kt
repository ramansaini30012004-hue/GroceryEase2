package com.example.groceryease2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtWelcome: TextView
    private lateinit var micBtn: ImageButton

    private val SPEECH_REQUEST = 100
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.categoryRecycler)
        txtWelcome = view.findViewById(R.id.txtWelcome)
        micBtn = view.findViewById(R.id.micBtn)

        auth = FirebaseAuth.getInstance()

        // 🔥 LOAD SHOP NAME FROM FIREBASE
        loadUserName()

        // CATEGORY LIST
        val list = listOf(
            CategoryModel("Vegetables", R.drawable.vegetales),
            CategoryModel("Fruits", R.drawable.fruits),
            CategoryModel("Spices", R.drawable.spices),
            CategoryModel("Dairy", R.drawable.dairy),
            CategoryModel("Oils", R.drawable.oils),
            CategoryModel("Bakery", R.drawable.bakery),
            CategoryModel("Household", R.drawable.household),
            CategoryModel("Pulses", R.drawable.pulses),
            CategoryModel("Beverages", R.drawable.beverages),
            CategoryModel("Snacks", R.drawable.snacks)
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = CategoryAdapter(list)

        // 🎤 MIC BUTTON
        micBtn.setOnClickListener {

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak product name and price"
            )

            startActivityForResult(intent, SPEECH_REQUEST)
        }

        return view
    }

    // 🔥 FETCH NAME
    private fun loadUserName() {

        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val name = snapshot.child("shopName").value.toString()

                    txtWelcome.text = "Welcome, $name"
                } else {
                    txtWelcome.text = "Welcome"
                }
            }
            .addOnFailureListener {
                txtWelcome.text = "Welcome"
            }
    }

    // 🎤 VOICE RESULT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST && resultCode == Activity.RESULT_OK) {

            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceText = result?.get(0) ?: ""

            processVoiceCommand(voiceText)
        }
    }

    // 🎤 VOICE COMMAND PROCESS
    private fun processVoiceCommand(text: String) {

        val words = text.lowercase(Locale.getDefault()).split(" ")

        var product = ""
        var price = ""

        for (i in words.indices) {

            if (words[i] == "product") {
                product = words.getOrNull(i + 1) ?: ""
            }

            if (words[i] == "price") {
                price = words.getOrNull(i + 1) ?: ""
            }
        }

        if (product.isNotEmpty() && price.isNotEmpty()) {

            // TODO: Firebase save
            println("Product: $product Price: $price")
        }
    }
}