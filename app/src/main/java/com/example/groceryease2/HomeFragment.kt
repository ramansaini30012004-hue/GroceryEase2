package com.example.groceryease2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            dialogImageView?.setImageURI(selectedImageUri)
        }

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = result?.get(0)?.lowercase()?.trim()

            if (!spoken.isNullOrEmpty()) {
                filterCategory(spoken)
            }
        }
    }

    private fun filterCategory(spoken: String) {
        var found = false

        for (item in list) {
            val name = item.name.lowercase()

            if (name.contains(spoken) || spoken.contains(name)) {
                item.isSelected = true
                found = true
            } else {
                item.isSelected = false
            }
        }

        if (!found) {
            Toast.makeText(requireContext(), "Category not found", Toast.LENGTH_SHORT).show()
        }

        adapter.notifyDataSetChanged()
    }

    private fun uriToBase64(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return ""
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return ""

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream)

        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    // 🔥 UPDATED DIALOG
    private fun openAddCategoryDialog() {

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)

        val editText = EditText(requireContext())
        editText.hint = "Category Name"

        editText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        editText.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), android.R.color.black)

        val imageView = ImageView(requireContext())
        imageView.setImageDrawable(null)

        val btnSelect = Button(requireContext())
        btnSelect.text = "Select Image"
        btnSelect.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        // ✅ DARK GREEN (#1B5E20)
        btnSelect.setBackgroundColor(Color.parseColor("#1B5E20"))

        layout.addView(editText)
        layout.addView(imageView)
        layout.addView(btnSelect)

        dialogImageView = imageView

        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Category")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->

                val name = editText.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val base64 = selectedImageUri?.let { uriToBase64(it) } ?: ""

                val ref = db.getReference("Categories").push()
                val map = HashMap<String, Any>()
                map["name"] = name
                map["image"] = base64

                ref.setValue(map)

                Toast.makeText(requireContext(), "Category Added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchFirebaseCategories() {
        val ref = db.getReference("Categories")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val tempList = mutableListOf<CategoryModel>()
                tempList.addAll(defaultCategories)

                val addedNames = HashSet<String>()

                for (data in snapshot.children) {
                    val name = data.child("name").value?.toString() ?: ""
                    val base64 = data.child("image").value?.toString() ?: ""

                    if (name.isNotEmpty() && !addedNames.contains(name)) {
                        tempList.add(CategoryModel(name = name, imageBase64 = base64))
                        addedNames.add(name)
                    }
                }

                list.clear()
                list.addAll(tempList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return

        db.getReference("Users").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("shopName").value?.toString() ?: "User"
                    txtWelcome.text = "Welcome, $name"
                }

                override fun onCancelled(error: DatabaseError) {
                    txtWelcome.text = "Welcome"
                }
            })
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            startActivityForResult(intent, 200)
        }, 1200)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) tts.shutdown()
    }
}