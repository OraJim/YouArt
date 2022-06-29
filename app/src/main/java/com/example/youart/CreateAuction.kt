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
import android.util.Log
import android.view.View
import android.widget.*
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

class CreateAuction : AppCompatActivity() , View.OnClickListener {
    private val PICK_IMAGE_REQUEST = 71
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    private var uploadContainer: LinearLayoutCompat? = null
    private var postIv: ImageView? = null
    private var backIv: ImageView? = null
    private var postIvTxt: TextView? = null
    private var auctionDescribTxt: TextView? = null
    private var auctionValue: EditText? = null
    private var expirationDate: EditText? = null
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
        setContentView(R.layout.activity_create_auction)
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
        postIv = findViewById(R.id.auctionImageIv)
        auctionDescribTxt = findViewById(R.id.auctionMessage)
        auctionValue = findViewById(R.id.startBid)
        expirationDate = findViewById(R.id.expireDate)
        postIvTxt = findViewById(R.id.auctionImgTxt)
        uploadContainer = findViewById(R.id.uploadContainer)
        createPostBtn = findViewById(R.id.createAuctionBtn)
        backIv = findViewById(R.id.backIv)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }
    private fun initEvents() {
        uploadContainer!!.setOnClickListener(this)
        createPostBtn!!.setOnClickListener(this)
        backIv!!.setOnClickListener(this)


    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.uploadContainer -> chooseImage()
            R.id.createAuctionBtn -> createPost()
            R.id.backIv -> goToMainActivity()
        }
    }
    private fun createFirebasePost(uuid: String?, postContent: String?) {
        if (currentUser != null) {
            val author = UserModel()
            author.uid = currentUser.uid
            author.photoUrl = currentUser.photoUrl.toString()
            author.displayName = currentUser.displayName

            val auction = Auction()
            auction.id = uuid
            auction.content = postContent
            auction.interests = ArrayList()
            auction.bids = ArrayList()
            auction.nInterests = 0
            auction.nBids = 0
            auction.author = author
            auction.comment = auctionDescribTxt!!.text.toString().trim()
            auction.expires = expirationDate!!.text.toString().trim()
            val startBid = Bid()
            startBid.author = author
            startBid.id = UUID.randomUUID().toString()
            startBid.value = auctionValue!!.text.toString().toInt()
            auction.highestBid = startBid

            database = Firebase.database.reference;
            database.child("auctions").child(uuid!!).setValue(auction)

            database?.child("users")?.child(currentUser.uid)?.get()?.addOnSuccessListener {
                pDialog!!.dismiss()
                val user = it.getValue(UserModel::class.java)
                val list: MutableList<String> = user!!.auctions as MutableList<String>
                user!!.nAuctions = if (user.nAuctions != null) user.nAuctions!!.plus(1) else 1
                if (user.nAuctions!! >= 1) {
                    list.add(auction.id!!)
                    user.auctions = list
                    }
                else { user.auctions  = listOf(auction.id!!)}
                database.child("users").child(currentUser.uid!!).setValue(user)
            }?.addOnFailureListener {
                pDialog!!.dismiss()
            }
            Toast.makeText(
                this@CreateAuction,
                "Your post was created successfully",
                Toast.LENGTH_LONG
            ).show()
            goToMainActivity()
        } else {
            Toast.makeText(
                this@CreateAuction,
                "Cannot create your post",
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
            Toast.makeText(this@CreateAuction, "Please upload the post image or video", Toast.LENGTH_LONG).show()
            return
        }
        pDialog!!.show()
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val refLink = "auctions/" + uuid + ".jpeg"
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
                    Toast.makeText(this@CreateAuction, "Can't Create Firebase Post", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    private fun createPost() {
        Log.d("UploadTEST", "funzt")
        if (currentUser != null) {
            uploadPostContent()
        }
    }
    private fun chooseImage() {
        Log.d("IMGTEST", "funzt")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            if (uri != null) {
               // uploadContainer?.isVisible= false
                postIv?.isVisible = true;
                val imageBitmap = uriToBitmap(uri)
                Glide.with(this)
                    .load(imageBitmap)
                    .centerCrop()
                    .into(postIv!!);
                //uploadContainer?.isVisible = false
                uploadedUri = uri
                isVideo = false
                postIvTxt?.isVisible = false
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
}