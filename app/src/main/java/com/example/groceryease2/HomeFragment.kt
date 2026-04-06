package com.example.groceryease2

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtWelcome: TextView
    private lateinit var btnAddCategory: ImageButton
    private lateinit var micBtn: ImageButton

    private lateinit var adapter: CategoryAdapter
    private val list = mutableListOf<CategoryModel>()

    private val defaultCategories = listOf(
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

    private val PICK_IMAGE = 101
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private var selectedImageUri: Uri? = null
    private var dialogImageView: ImageView? = null

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

        list.clear()
        list.addAll(defaultCategories)

        adapter = CategoryAdapter(list)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        fetchFirebaseCategories()

        tts = TextToSpeech(requireContext()) {}

        micBtn.setOnClickListener {

            startBlinkAnimation()

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1000)
                return@setOnClickListener
            }

            speak("Which category?")
        }

        btnAddCategory.setOnClickListener {
            openAddCategoryDialog()
        }

        return view
    }

    // 🟢 GREEN BLINK
    private fun startBlinkAnimation() {
        val anim = AlphaAnimation(0.3f, 1.0f)
        anim.duration = 300
        anim.repeatMode = AlphaAnimation.REVERSE
        anim.repeatCount = 5
        micBtn.startAnimation(anim)

        micBtn.setColorFilter(
            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
        )
    }

    // 🔥 BASE64
    private fun uriToBase64(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return ""
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return ""

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)

        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    // 🔥 FETCH (NO DUPLICATE)
    private fun fetchFirebaseCategories() {
        val ref = db.getReference("Categories")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                list.clear()
                list.addAll(defaultCategories)

                val addedNames = HashSet<String>()

                for (data in snapshot.children) {
                    val name = data.child("name").value?.toString() ?: ""
                    val base64 = data.child("image").value?.toString() ?: ""

                    if (!addedNames.contains(name)) {
                        list.add(CategoryModel(name = name, imageBase64 = base64))
                        addedNames.add(name)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔥 UPLOAD (NO DUPLICATE BUG)
    private fun uploadCategory(name: String, imageUri: Uri?) {

        val dialog = ProgressDialog(requireContext())
        dialog.setMessage("Uploading...")
        dialog.setCancelable(false)
        dialog.show()

        try {
            var base64Image = ""

            if (imageUri != null) {
                base64Image = uriToBase64(imageUri)
            }

            val ref = db.getReference("Categories")
            val id = ref.push().key ?: return

            val map = HashMap<String, Any>()
            map["name"] = name
            map["image"] = base64Image

            ref.child(id).setValue(map).addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Category Added", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            dialog.dismiss()
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    // 🟢 GREEN DIALOG UI
    private fun openAddCategoryDialog() {

        val green = ContextCompat.getColor(requireContext(), R.color.dark_green)

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
        }

        val editText = EditText(requireContext()).apply {
            hint = "Enter category name"
            setTextColor(green)
            setHintTextColor(green)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_green2)
        }

        val imageView = ImageView(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_gallery)
            layoutParams = LinearLayout.LayoutParams(250, 250)
            setPadding(0, 20, 0, 20)
        }

        dialogImageView = imageView

        val btnImage = Button(requireContext()).apply {
            text = "Select Image"
            setBackgroundColor(green)
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

            setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE)
            }
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
                    uploadCategory(name, selectedImageUri)
                    selectedImageUri = null
                } else {
                    Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(green)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(green)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            dialogImageView?.setImageURI(selectedImageUri)
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            startActivityForResult(intent, 200)
        }, 1500)
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.getReference("Users").child(uid).get().addOnSuccessListener {
            txtWelcome.text = "Welcome, ${it.child("shopName").value}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) tts.shutdown()
    }
}