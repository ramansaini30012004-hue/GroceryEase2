package com.example.groceryease2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtWelcome: TextView
    private lateinit var btnAddCategory: ImageButton
    private lateinit var micBtn: ImageButton

    private lateinit var adapter: CategoryAdapter
    private val list = mutableListOf<CategoryModel>()

    private val PICK_IMAGE = 101

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private var selectedImageUri: Uri? = null
    private var dialogImageView: ImageView? = null

    // 🎤 VOICE
    private var step = 0
    private var categoryName = ""
    private var productName = ""
    private var quantity = ""
    private var unit = ""
    private var price = ""

    private lateinit var tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.categoryRecycler)
        txtWelcome = view.findViewById(R.id.txtWelcome)
        btnAddCategory = view.findViewById(R.id.btnAddCategory)
        micBtn = view.findViewById(R.id.micBtn)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        loadUserName()

        tts = TextToSpeech(requireContext()) {}

        // ✅ DEFAULT CATEGORIES
        list.addAll(
            listOf(
                CategoryModel("Vegetables", imageResId = R.drawable.vegetales),
                CategoryModel("Fruits", imageResId = R.drawable.fruits),
                CategoryModel("Spices", imageResId = R.drawable.spices),
                CategoryModel("Dairy", imageResId = R.drawable.dairy),
                CategoryModel("Oils", imageResId = R.drawable.oils),
                CategoryModel("Bakery", imageResId = R.drawable.bakery),
                CategoryModel("Household", imageResId = R.drawable.household),
                CategoryModel("Pulses", imageResId = R.drawable.pulses),
                CategoryModel("Beverages", imageResId = R.drawable.beverages),
                CategoryModel("Snacks", imageResId = R.drawable.snacks)
            )
        )

        adapter = CategoryAdapter(list)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        micBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1000)
                return@setOnClickListener
            }

            step = 1
            speak("Which category?")
        }

        btnAddCategory.setOnClickListener {
            openAddCategoryDialog()
        }

        return view
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            startActivityForResult(intent, 200)
        }, 1500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {

            val text = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )?.get(0)?.lowercase() ?: return

            when (step) {

                1 -> {
                    categoryName = text.replaceFirstChar { it.uppercase() }
                    step = 2
                    speak("Product name?")
                }

                2 -> {
                    productName = text
                    step = 3
                    speak("Quantity?")
                }

                3 -> {
                    quantity = text.filter { it.isDigit() }.ifEmpty { "1" }
                    step = 4
                    speak("Unit?")
                }

                4 -> {
                    unit = text
                    step = 5
                    speak("Price?")
                }

                5 -> {
                    price = text.filter { it.isDigit() }.ifEmpty { "0" }

                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 101)
                }
            }
        }

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            saveProduct(data?.data)
        }

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            dialogImageView?.setImageURI(selectedImageUri)
        }
    }

    private fun saveProduct(uri: Uri?) {

        val category = categoryName.trim().replaceFirstChar { it.uppercase() }

        val ref = db.getReference("Products").child(category)
        val id = ref.push().key ?: return

        val map = HashMap<String, Any>()
        map["name"] = productName
        map["quantity"] = quantity
        map["unit"] = unit
        map["price"] = price
        map["image"] = uri?.toString() ?: ""

        ref.child(id).setValue(map)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product Added", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ UPDATED GREEN DIALOG
    private fun openAddCategoryDialog() {

        val darkGreen = ContextCompat.getColor(requireContext(), R.color.dark_green)

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 30, 40, 30)

        val editText = EditText(requireContext())
        editText.hint = "Enter category name"

        // ✅ TEXT COLOR GREEN
        editText.setTextColor(darkGreen)
        editText.setHintTextColor(darkGreen)

        // ✅ BORDER GREEN
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_green2)

        val imageView = ImageView(requireContext())
        imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        imageView.layoutParams = LinearLayout.LayoutParams(250, 250)
        editText.textCursorDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.cursor_green)

        dialogImageView = imageView

        val btnImage = Button(requireContext())
        btnImage.text = "Select Image"

        // ✅ BUTTON GREEN
        btnImage.setBackgroundColor(darkGreen)
        btnImage.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        btnImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        layout.addView(editText)
        layout.addView(imageView)
        layout.addView(btnImage)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Category")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    list.add(CategoryModel(name))
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return

        db.getReference("Users").child(uid).get()
            .addOnSuccessListener {
                txtWelcome.text = "Welcome, ${it.child("shopName").value}"
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.shutdown()
        }
    }
}