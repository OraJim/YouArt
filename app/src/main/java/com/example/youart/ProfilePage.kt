package com.example.youart

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class ProfilePage : AppCompatActivity() {
    lateinit var uName: EditText
    private lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    private var storageReference: StorageReference? = null
    lateinit var currentUser: FirebaseUser
    lateinit var uImage: ImageView
    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        uName = findViewById(R.id.editTextUnameProfile)
        uName.setText(currentUser.displayName)
        uImage = findViewById((R.id.imageView))
        storage = Firebase.storage
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar);
        Log.d("ImageTest", "ImageUri is:" + currentUser.photoUrl.toString())
        storageReference =storage.getReferenceFromUrl(currentUser.photoUrl.toString())//createUPicStoreReference()
        val ONE_MEGABYTE: Long = 1024 * 1024
        storageReference!!.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            Log.d("ImageTest", "ByteSize IT is:" + it.size)
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size);
            Log.d("ImageTest", "ByteSize Pic is:" + it.size)
            uImage.setImageBitmap(bmp)
        }.addOnFailureListener {
            // Handle any errors
            Log.d("ImageTest", "Setting Image failed")
        }
    }

    // Menu icons are inflated just as they were with actionbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.getItemId()) {
            R.id.editUser -> {
                editUserInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    public fun editUserInfo(){
        Log.d("Change to edit", "Edit userInformationView")
        val intent = Intent(this,InputUserInfo::class.java).apply { }//InputUserInfo::class.java).apply { }
        startActivity(intent)
    }

    private fun createUPicStoreReference(): StorageReference? {
        val storageRef = storage.reference
        var imagesRef: StorageReference? = storageRef.child("images")
        var picRef : StorageReference? = storageRef.child("images/"+currentUser.uid+"/")
        Log.d("Picture", picRef.toString())
        return picRef
    }
}