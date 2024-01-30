package com.fajar.absensi.ui.absen

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Criteria
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fajar.absensi.databinding.ActivityAbsenBinding
import com.fajar.absensi.model.Presensi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class AbsenActivity:AppCompatActivity() {

    private lateinit var mBitmap: Bitmap
    private lateinit var binding: ActivityAbsenBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val firestore = Firebase.firestore
    private var storage = Firebase.storage.reference
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch the camera
                setCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance().reference.child("photo/")

        setUploadData()
        retrieveData()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.apply {
            imageSelfie.setOnClickListener {
                setCamera()
            }

            inputLokasi.setOnClickListener {
                setCurrentLocation()
            }

            inputTanggal.setOnClickListener {
                setCurrentTime()
            }

        }

    }

    private fun setCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the CAMERA permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        } else {
            // Permission already granted, launch the camera
            val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(camera, 200)
        }
    }


    private fun retrieveData(){

        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get().addOnSuccessListener {
            val name = it.get("name")
            val idPegawai = it.get("idPegawai")

            binding.apply {
                inputNama.setText(name.toString())
                inputIdPegawai.setText(idPegawai.toString())

            }

        }

    }

    private fun setCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the LOCATION permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        } else {
            // Permission already granted, get the current location
            getLocation()
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider: String? = locationManager.getBestProvider(criteria, true)

            if (provider != null) {
                try {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        // Use Geocoder to get address details
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val street = address.thoroughfare ?: ""
                                val sublocality = address.subLocality ?: ""
                                val locality = address.locality ?: ""
                                val postalCode = address.postalCode ?: ""
                                val country = address.countryName ?: ""

                                val locationString = "$street, $sublocality, $locality, $postalCode, $country"

                                // Update your UI or set the location to the inputLokasi text field
                                binding.inputLokasi.setText(locationString)
                            } else {
                                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "SecurityException: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Location provider not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request the LOCATION permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
    }

    private fun setCurrentTime(){

        val tanggalAbsen = Calendar.getInstance()
        val date =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                tanggalAbsen[Calendar.YEAR] = year
                tanggalAbsen[Calendar.MONTH] = monthOfYear
                tanggalAbsen[Calendar.DAY_OF_MONTH] = dayOfMonth
                val strFormatDefault = "HH:mm dd MMMM yyyy"
                val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                binding.inputTanggal.setText(simpleDateFormat.format(tanggalAbsen.time))
            }
        DatePickerDialog(
            this@AbsenActivity, date,
            tanggalAbsen[Calendar.YEAR],
            tanggalAbsen[Calendar.MONTH],
            tanggalAbsen[Calendar.DAY_OF_MONTH]
        ).show()

    }

    private fun setUploadData() {
        binding.btnAbsen.setOnClickListener {
            uploadData(){
                Log.d("test", it.toString())
            }
        }
    }

    private fun uploadData(state: (Boolean) -> Unit) {
        val name = binding.inputNama.text.toString().trim()
        val idPegawai = binding.inputIdPegawai.text.toString().trim()
        val selfieImageUri: Uri? = getImageUriFromImageView(binding.imageSelfie)  // Helper function to get Uri from ImageView
        val tanggal = binding.inputTanggal.text.toString().trim()
        val lokasi = binding.inputLokasi.text.toString().trim()
        val keterangan = binding.inputKeterangan.text.toString().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val id = UUID.randomUUID().toString()

                    // Upload selfie image to Firebase Storage
                    val storageRef = storage.child("presensi/selfie/$id.jpg")
                    storageRef.putFile(selfieImageUri!!)
                        .addOnSuccessListener {
                            // Get download URL of the uploaded image
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                val photoUrl = uri.toString()

                                firestore.collection("user").document(userId)
                                    .get().addOnSuccessListener { userSnapshot ->
                                        val employeeName = userSnapshot.getString("name")
                                        val employeeId = userId

                                        // Create a product object
                                        val product = Presensi(
                                            id,
                                            name,
                                            idPegawai,
                                            photoUrl,  // Use the download URL of the image
                                            tanggal,
                                            lokasi,
                                            keterangan,
                                            employeeName,
                                            employeeId
                                        )

                                        // Upload product data to the subcollection "presensi" under the "user" collection
                                        firestore.collection("user").document(userId)
                                            .collection("presensi").document(id)
                                            .set(product)
                                            .addOnSuccessListener {
                                                state(true)
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("Firestore", "Error adding document to subcollection: $exception")
                                                state(false)
                                            }
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase Storage", "Error uploading selfie image: $exception")
                            state(false)
                        }

                } catch (e: Exception) {
                    state(false)
                    return@launch
                }
            }
        } else {
            state(false)
            Log.e("Firestore", "User ID null")
        }
    }

    // Helper function to get Uri from ImageView
    private fun getImageUriFromImageView(imageView: ImageView): Uri? {
        val drawable = imageView.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        val tempFile = File.createTempFile("tempImage", ".jpg")
        tempFile.deleteOnExit()

        try {
            val fileOutputStream = FileOutputStream(tempFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Uri.fromFile(tempFile)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 250){
            binding.imageSelfie.setImageURI(data?.data)

            val uri : Uri?= data?.data
            mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
        else if(requestCode == 200 && resultCode == Activity.RESULT_OK){
            mBitmap = data?.extras?.get("data") as Bitmap
            binding.imageSelfie.setImageBitmap(mBitmap)
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}