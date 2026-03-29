package com.example.groceryease2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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
    private lateinit var saveButton: Button
    private lateinit var logout: Button
    private lateinit var profileImage: CircleImageView
    private lateinit var getLocation: Button

    private lateinit var auth: FirebaseAuth
    private var selectedBitmap: Bitmap? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val AUTOCOMPLETE_REQUEST_CODE = 101

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Google Places
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBpfZ7UKwP2KOuoI1Mv7YG1OO76g-lNtqA")
        }

        auth = FirebaseAuth.getInstance()
        initViews(view)
        loadUserData()

        // 1. Image Picker
        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        // 2. Google Places Autocomplete
        address.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        // 3. Manual GPS Location
        getLocation.setOnClickListener {
            fetchGPSLocation()
        }

        saveButton.setOnClickListener { saveProfile() }

        logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
            requireActivity().finish()
        }

        return view
    }

    private fun initViews(view: View) {
        shopName = view.findViewById(R.id.ushopname)
        phone = view.findViewById(R.id.phone)
        address = view.findViewById(R.id.address)
        latText = view.findViewById(R.id.latitude)
        longText = view.findViewById(R.id.longitude)
        saveButton = view.findViewById(R.id.editProfile)
        logout = view.findViewById(R.id.logout)
        profileImage = view.findViewById(R.id.profileImage)
        getLocation = view.findViewById(R.id.getLocation)
    }

    private fun fetchGPSLocation() {
        val fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Places result
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            address.setText(place.address)
            latitude = place.latLng?.latitude
            longitude = place.latLng?.longitude
            latText.setText(latitude.toString())
            longText.setText(longitude.toString())
        }

        // Handle Image result
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val inputStream = requireActivity().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
                profileImage.setImageBitmap(selectedBitmap)
            }
        }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val map = hashMapOf<String, Any>(
            "shopName" to shopName.text.toString(),
            "phone" to phone.text.toString(),
            "address" to address.text.toString()
        )
        latitude?.let { map["latitude"] = it }
        longitude?.let { map["longitude"] = it }
        selectedBitmap?.let { map["image"] = imageToBase64(it) }

        FirebaseDatabase.getInstance().getReference("Users").child(uid).updateChildren(map)
            .addOnSuccessListener { Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show() }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("Users").child(uid).get().addOnSuccessListener {
            shopName.setText(it.child("shopName").value?.toString())
            phone.setText(it.child("phone").value?.toString())
            address.setText(it.child("address").value?.toString())
            latitude = it.child("latitude").value?.toString()?.toDoubleOrNull()
            longitude = it.child("longitude").value?.toString()?.toDoubleOrNull()
            latText.setText(latitude?.toString())
            longText.setText(longitude?.toString())
            it.child("image").value?.toString()?.let { b64 ->
                profileImage.setImageBitmap(base64ToBitmap(b64))
            }
        }
    }

    private fun imageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}