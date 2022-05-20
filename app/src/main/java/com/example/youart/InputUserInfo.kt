package com.example.youart

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

class InputUserInfo : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var auth: FirebaseAuth
    lateinit var uName: EditText
    lateinit var storage: FirebaseStorage
    private var storageReference: StorageReference? = null
    lateinit var currentUser: FirebaseUser
    lateinit var uImage: ImageView
    var profileUri: Uri? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_user_info)
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        uName = findViewById(R.id.editTextUname)
        uName.setText(intent.getStringExtra("Uname"))
        uImage = findViewById((R.id.imageView))
        storage = Firebase.storage
        //CreateStoreReference()
    }

    private fun CreateStoreReference(): StorageReference? {
        val storageRef = storage.reference
        var imagesRef: StorageReference? = storageRef.child("images")
        var picRef : StorageReference? = storageRef.child("images/"+currentUser.uid+"/")
        Log.d("Picture", picRef.toString())
        return picRef
    }
    fun clickedProfileButton(v: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //permission already granted
                pickImageFromGallery()
            }
        }
        else{
            //system OS is < Marshmallow
            pickImageFromGallery()
        }
    }
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        //startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    fun clickedSaveButton(v: View) {
        val storageRef = storage.reference
        var imagesRef: StorageReference? = storageRef.child("images")
       // var file = Uri.fromFile(File(uImage.getTag().toString()))
        var picRef : StorageReference = storageRef.child("images/"+currentUser.uid)
        //upload the image to DataStore
        val uploadTask = picRef.putFile(profileUri!!)
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("ImageTest", "Failed uploading Image")
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("ImageTest", "Succesfully uploaded Image")
        }

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            picRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("ImageTest", "ImageUri is:" + downloadUri)
                val profileUpdates = userProfileChangeRequest {
                    displayName = uName.text.toString()
                    photoUri = downloadUri//Uri.parse("https://example.com/jane-q-user/profile.jpg")
                }
                currentUser!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("UserUpdate", "User profile updated.")
                        }
                    }
            } else {
                // Handle failures
                // ...
            }
        }

    }
    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }
    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup granted
                    pickImageFromGallery()
                    //permission from popup denied
                    //Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
       // if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }
            Log.d("UpdatePic", "Updating Pic")
            uImage.setImageURI(data?.data)
            //uImage.setTag(data?.data)
            profileUri = data?.data
           // Log.d("UpdatePic", getRealPathFromURI(profileUri))
        }
    }
}