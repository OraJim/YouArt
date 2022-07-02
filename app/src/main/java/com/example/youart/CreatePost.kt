package com.example.youart

import android.Manifest
import android.app.Activity
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
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.widget.LinearLayoutCompat
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
import kotlin.collections.ArrayList
/**
 * Activity for Creating a new Post Entry to the Firebase Database
 */
class CreatePost : AppCompatActivity() , View.OnClickListener {
    private val PICK_IMAGE_REQUEST = 71
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    private var uploadContainer: LinearLayoutCompat? = null
    private var postIv: ImageView? = null
    private var videoView: VideoView? = null
    private var createPostBtn: Button? = null

    private var pDialog: ProgressDialog? = null

    private lateinit var database: DatabaseReference
    private var uploadedUri: Uri? = null
    private var isVideo: Boolean? = false
    private lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    private val PERMISSION_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        initViews()
        initEvents()
    }

    private fun initViews() {
        postIv = findViewById(R.id.postIv)
        uploadContainer = findViewById(R.id.uploadContainer)
        createPostBtn = findViewById(R.id.createPostBtn)
        videoView = findViewById(R.id.videoView)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        postIv!!.setOnClickListener(this)
        videoView!!.setOnClickListener(this)
        createPostBtn!!.setOnClickListener(this)
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
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        //startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            if (uri != null) {
                uploadContainer?.isVisible= false
                videoView?.isVisible = false
                postIv?.isVisible = true;
                val imageBitmap = uriToBitmap(uri)
                Glide.with(this)
                    .load(imageBitmap)
                    .centerCrop()
                    .into(postIv!!);
                uploadContainer?.isVisible = false
                uploadedUri = uri
                isVideo = false
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this,MainActivity::class.java).apply { }//InputUserInfo::class.java).apply { }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun createFirebasePost(uuid: String?, postContent: String?) {
        if (currentUser != null) {
            val author = UserModel()
            author.uid = currentUser.uid
            author.photoUrl = currentUser.photoUrl.toString()
            author.displayName = currentUser.displayName

            val post = Post()
            post.id = uuid
            post.content = postContent
            post.likes = ArrayList()
            post.nLikes = 0
            post.author = author

            database = Firebase.database.reference;
            database.child("posts").child(uuid!!).setValue(post)

            database?.child("users")?.child(currentUser.uid)?.get()?.addOnSuccessListener {
                pDialog!!.dismiss()
                val user = it.getValue(UserModel::class.java)
                user!!.nPosts = if (user.nPosts != null) user.nPosts!!.plus(1) else 1
                database.child("users").child(currentUser.uid!!).setValue(user)
            }?.addOnFailureListener {
                pDialog!!.dismiss()
            }
            Toast.makeText(
                this@CreatePost,
                "Your post was created successfully",
                Toast.LENGTH_LONG
            ).show()
            goToMainActivity()
        } else {
            Toast.makeText(
                this@CreatePost,
                "Cannot load your cometchat account",
                Toast.LENGTH_LONG
            ).show()
            pDialog!!.dismiss()
        }
    }

    private fun getUploadedImage(): ByteArray {
        postIv?.isDrawingCacheEnabled = true
        postIv?.buildDrawingCache()
        val bitmap = (postIv?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun uploadPostContent() {
        if (uploadedUri == null) {
            Toast.makeText(this@CreatePost, "Please upload the post image or video", Toast.LENGTH_LONG).show()
            return
        }
        pDialog!!.show()
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val refLink = "posts/" + uuid + ".jpeg"
        val postRef = storageRef.child(refLink)
        val uploadTask = postRef.putBytes(getUploadedImage())
        uploadTask.addOnFailureListener {
            pDialog!!.dismiss()
            Toast.makeText(this, "Cannot upload your post", Toast.LENGTH_LONG).show();
        }.addOnSuccessListener { taskSnapshot ->
            postRef.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri ->
                if (uri != null) {

                    this.createFirebasePost(uuid.toString(), uri.toString())
                }else{
                    Toast.makeText(this@CreatePost, "Can't Create Firebase Post", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun createPost() {
        if (currentUser != null) {
            uploadPostContent()
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.videoView -> chooseImage()
            R.id.postIv -> chooseImage()
            R.id.createPostBtn -> createPost()
        }
    }

}