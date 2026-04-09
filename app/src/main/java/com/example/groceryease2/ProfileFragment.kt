package com.example.groceryease2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var shopName: EditText
    private lateinit var phone: EditText
    private lateinit var address: EditText
    private lateinit var latText: EditText
    private lateinit var longText: EditText
    private lateinit var saveBtn: Button
    private lateinit var logout: Button
    private lateinit var profileImage: CircleImageView
    private lateinit var getLocation: Button

    private lateinit var auth: FirebaseAuth
    private var selectedBitmap: Bitmap? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        shopName = view.findViewById(R.id.ushopname)
        phone = view.findViewById(R.id.phone)
        address = view.findViewById(R.id.address)
        latText = view.findViewById(R.id.latitude)
        longText = view.findViewById(R.id.longitude)
        saveBtn = view.findViewById(R.id.editProfile)
        logout = view.findViewById(R.id.logout)
        profileImage = view.findViewById(R.id.profileImage)
        getLocation = view.findViewById(R.id.getLocation)

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        getLocation.setOnClickListener { fetchLocation() }
        saveBtn.setOnClickListener { saveProfile() }

        logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        if (!isDataLoaded) {
            loadFromCache()
            loadFromFirebase()
            isDataLoaded = true
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            selectedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
            profileImage.setImageBitmap(selectedBitmap)
        }
    }

    private fun fetchLocation() {

        val fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude

                latText.setText(latitude.toString())
                longText.setText(longitude.toString())

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
                address.setText(addresses?.get(0)?.getAddressLine(0))
            }
        }
    }

    private fun saveProfile() {

        val uid = auth.currentUser?.uid ?: return

        val name = shopName.text.toString().trim()
        val phoneTxt = phone.text.toString().trim()
        val addressTxt = address.text.toString().trim()

        if (name.isEmpty()) {
            shopName.error = "Enter shop name"
            return
        }

        if (selectedBitmap == null) {
            Toast.makeText(context, "Select image", Toast.LENGTH_SHORT).show()
            return
        }

        val map = HashMap<String, Any>()
        map["shopName"] = name
        map["phone"] = phoneTxt
        map["address"] = addressTxt
        map["latitude"] = latitude ?: 0.0
        map["longitude"] = longitude ?: 0.0
        map["image"] = imageToBase64(selectedBitmap!!)

        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .setValue(map)
            .addOnSuccessListener {

                saveToCache(name, phoneTxt, addressTxt, map["image"].toString())

                // ✅ FIX: profileCompleted TRUE
                val pref = requireContext().getSharedPreferences("user", 0)
                pref.edit().putBoolean("profileCompleted", true).apply()

                Toast.makeText(context, "Profile Saved", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFromFirebase() {

        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val name = snapshot.child("shopName").value.toString()
                    val phoneTxt = snapshot.child("phone").value.toString()
                    val addressTxt = snapshot.child("address").value.toString()
                    val lat = snapshot.child("latitude").value.toString()
                    val lng = snapshot.child("longitude").value.toString()
                    val image = snapshot.child("image").value.toString()

                    shopName.setText(name)
                    phone.setText(phoneTxt)
                    address.setText(addressTxt)
                    latText.setText(lat)
                    longText.setText(lng)

                    if (image.isNotEmpty()) {
                        val bytes = Base64.decode(image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        profileImage.setImageBitmap(bitmap)
                        selectedBitmap = bitmap
                    }

                    saveToCache(name, phoneTxt, addressTxt, image)

                    // ✅ ALSO mark completed if data exists
                    val pref = requireContext().getSharedPreferences("user", 0)
                    pref.edit().putBoolean("profileCompleted", true).apply()
                }
            }
    }

    private fun saveToCache(name: String, phone: String, address: String, image: String) {
        val pref = requireContext().getSharedPreferences("user", 0)
        pref.edit()
            .putString("name", name)
            .putString("phone", phone)
            .putString("address", address)
            .putString("image", image)
            .apply()
    }

    private fun loadFromCache() {
        val pref = requireContext().getSharedPreferences("user", 0)

        shopName.setText(pref.getString("name", ""))
        phone.setText(pref.getString("phone", ""))
        address.setText(pref.getString("address", ""))

        val img = pref.getString("image", "")

        if (!img.isNullOrEmpty()) {
            val bytes = Base64.decode(img, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profileImage.setImageBitmap(bitmap)
            selectedBitmap = bitmap
        }
    }

    private fun imageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }
}