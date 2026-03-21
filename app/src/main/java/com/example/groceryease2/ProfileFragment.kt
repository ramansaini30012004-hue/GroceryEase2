package com.example.groceryease2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import android.location.Geocoder
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var shopName: EditText
    private lateinit var phone: EditText
    private lateinit var address: EditText

    private lateinit var saveButton: Button
    private lateinit var logout: Button
    private lateinit var settings: LinearLayout
    private lateinit var orderHistory: LinearLayout
    private lateinit var profileImage: CircleImageView
    private lateinit var getLocation: Button

    private lateinit var auth: FirebaseAuth
    private var selectedBitmap: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        shopName = view.findViewById(R.id.ushopname)
        phone = view.findViewById(R.id.phone)
        address = view.findViewById(R.id.address)

        saveButton = view.findViewById(R.id.editProfile)
        logout = view.findViewById(R.id.logout)
        settings = view.findViewById(R.id.settings)
        orderHistory = view.findViewById(R.id.orderHistory)
        profileImage = view.findViewById(R.id.profileImage)
        getLocation = view.findViewById(R.id.getLocation)

        auth = FirebaseAuth.getInstance()

        loadUserData()

        // Profile image select
        profileImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,100)

        }

        // Get location
        getLocation.setOnClickListener {

            val fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())

            if(ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                return@setOnClickListener
            }

            fusedLocation.lastLocation.addOnSuccessListener { location ->

                if(location != null){

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    val addresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )

                    val addressText = addresses?.get(0)?.getAddressLine(0)

                    address.setText(addressText)

                }

            }

        }

        // Save profile
        saveButton.setOnClickListener {

            saveProfile()

        }

        // Logout
        logout.setOnClickListener {

            auth.signOut()
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
            requireActivity().finish()

        }

        // Open settings
        settings.setOnClickListener {

            startActivity(Intent(requireContext(), SettingsActivity::class.java))

        }

        // Order history
        orderHistory.setOnClickListener {

            Toast.makeText(context,"Order History Coming Soon",Toast.LENGTH_SHORT).show()

        }

        return view
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)

        if(requestCode == 100 && resultCode == Activity.RESULT_OK){

            val uri:Uri? = data?.data

            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,uri)

            selectedBitmap = Bitmap.createScaledBitmap(bitmap,400,400,false)

            profileImage.setImageBitmap(selectedBitmap)

        }
    }

    private fun imageToBase64(bitmap:Bitmap):String{

        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG,60,baos)

        val bytes = baos.toByteArray()

        return Base64.encodeToString(bytes,Base64.DEFAULT)

    }

    private fun base64ToBitmap(base64:String):Bitmap{

        val bytes = Base64.decode(base64,Base64.DEFAULT)

        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)

    }

    private fun saveProfile(){

        val uid = auth.currentUser?.uid ?: return

        val map = HashMap<String,Any>()

        map["shopName"] = shopName.text.toString()
        map["phone"] = phone.text.toString()
        map["address"] = address.text.toString()

        if(selectedBitmap != null){
            map["image"] = imageToBase64(selectedBitmap!!)
        }

        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .updateChildren(map)
            .addOnSuccessListener {

                Toast.makeText(context,"Profile Saved",Toast.LENGTH_SHORT).show()

            }

    }

    private fun loadUserData(){

        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .get()
            .addOnSuccessListener {

                shopName.setText(it.child("shopName").value?.toString())
                phone.setText(it.child("phone").value?.toString())
                address.setText(it.child("address").value?.toString())

                val img = it.child("image").value?.toString()

                if(!img.isNullOrEmpty()){

                    val bitmap = base64ToBitmap(img)

                    profileImage.setImageBitmap(bitmap)

                }

            }
    }
}