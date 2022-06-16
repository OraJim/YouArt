package com.example.youart

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class register : AppCompatActivity(), View.OnClickListener  {
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var userAvatarIv: ImageView? = null
    private var userAvatarTxt: TextView? = null
    private var fullnameEdt: EditText? = null
    private var emailEdt: EditText? = null
    private var passwordEdt: EditText? = null
    private var confirmPasswordEdt: EditText? = null
    private var registerBtn: Button? = null

    private var pDialog: ProgressDialog? = null

    private var uploadedUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        initViews()
        initEvents()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }

    }

    private fun initViews() {
        userAvatarIv = findViewById(R.id.userAvatarIv);
        userAvatarTxt = findViewById(R.id.userAvatarTxt);
        fullnameEdt = findViewById(R.id.fullnameEdt);
        emailEdt = findViewById(R.id.emailEdt);
        passwordEdt = findViewById(R.id.passwordEdt);
        confirmPasswordEdt = findViewById(R.id.confirmPasswordEdt);
        registerBtn = findViewById(R.id.registerBtn);

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        registerBtn?.setOnClickListener(this);
        userAvatarIv?.setOnClickListener(this);
    }
    private fun chooseImage(){
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
        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)
        intent.putExtra("aspectX", 16)
        intent.putExtra("aspectY", 9)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        //startActivityForResult(intent, IMAGE_PICK_CODE)
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
            val uri = data?.data
            uploadedUri = uri.toString()
            if (uri != null) {
                val imageBitmap = uriToBitmap(uri)
                Glide.with(this)
                    .load(imageBitmap)
                    .circleCrop()
                    .into(userAvatarIv!!);
                userAvatarTxt?.isVisible = false
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun validate(fullName: String?, email: String?, password: String?, confirmPassword: String?): Boolean {
        if (uploadedUri == null || uploadedUri.equals(EMPTY_STRING)) {
            Toast.makeText(this@register, "Please upload your avatar", Toast.LENGTH_LONG).show();
            return false;
        }
        if (fullName == null || fullName.equals(EMPTY_STRING)) {
            Toast.makeText(this@register, "Please input your full name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (email == null || email.equals(EMPTY_STRING)) {
            Toast.makeText(this@register, "Please input your email", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password == null || password.equals(EMPTY_STRING)) {
            Toast.makeText(this@register, "Please input your password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (confirmPassword == null || confirmPassword.equals(EMPTY_STRING)) {
            Toast.makeText(this@register, "Please input your confirm password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this@register, "Your password and confirm password must be matched", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private fun goToLoginActivity() {
        intent = Intent(this@register, MainActivity::class.java)
        startActivity(intent)
    }

    private fun insertFirebaseDatabase(userId: String?, fullname: String?, email: String?, avatar: String?) {
        val userModel = UserModel()
        userModel.uid = userId
        userModel.displayName = fullname
        userModel.email = email
        userModel.photoUrl = avatar
        database = Firebase.database.reference;
        database.child("users").child(userId!!).setValue(userModel)
    }

    private fun createFirebaseAccount(fullname: String?, email: String?, password: String?, avatar: String?) {
        if (email != null && password != null) {
            auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = UUID.randomUUID()
                        insertFirebaseDatabase(userId.toString(), fullname, email, avatar)
                    } else {
                        pDialog!!.dismiss()
                        Toast.makeText(this@register, "Cannot create your account, please try again", Toast.LENGTH_LONG).show();
                    }
                }
        } else {
            pDialog!!.dismiss()
            Toast.makeText(this@register, "Please provide your email and password", Toast.LENGTH_LONG).show();
        }
    }

    private fun uploadUserAvatar(fullname: String?, email: String?, password: String?) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID(1, 1)
        val avatarRef = storageRef.child("users/" + uuid + ".jpeg")
        userAvatarIv?.isDrawingCacheEnabled = true
        userAvatarIv?.buildDrawingCache()
        val bitmap = (userAvatarIv?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = avatarRef.putBytes(data)
        uploadTask.addOnFailureListener {
            pDialog!!.dismiss()
            Toast.makeText(this, "Cannot upload your avatar", Toast.LENGTH_LONG).show();
        }.addOnSuccessListener { taskSnapshot ->
            avatarRef.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri ->
                if (uri != null) {
                    this.createFirebaseAccount(fullname, email, password, uri.toString())
                }
            })
        }
    }

    private fun register() {
        val fullName = fullnameEdt!!.text.toString().trim()
        val email = emailEdt!!.text.toString().trim()
        val password = passwordEdt!!.text.toString().trim()
        val confirmPassword = confirmPasswordEdt!!.text.toString().trim()
        if (validate(fullName, email, password, confirmPassword)) {
            pDialog!!.show()
            uploadUserAvatar(fullName, email, password)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.userAvatarIv -> chooseImage()
            R.id.registerBtn -> register()
            else -> {}
        }
    }

    private fun reload() {

    }
    companion object {
        private const val TAG = "EmailPassword"
        private val IMAGE_PICK_CODE = 1000
            //Permission code
        private val PERMISSION_CODE = 1001
        const val EMPTY_STRING = ""
    }


}