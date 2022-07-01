package com.example.youart
import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.ArrayList

class ProfilePageActivity : AppCompatActivity(), View.OnClickListener {

    private var profileUid: String? = ""

    private var authorAvatarIv: ImageView? = null
    private var postIv: ImageView? = null
    private var backIv: ImageView? = null
    private var videoIv: ImageView? = null
    private var logoutIv: ImageView? = null

    private var postBottomLine: View? = null
    private var videoBottomLine: View? = null

    private var nPostsTxt: TextView? = null
    private var nFollowersTxt: TextView? = null
    private var followTxt : TextView?=  null

    private var postRv: RecyclerView? = null
    private var mDatabase: DatabaseReference? = null
    lateinit var currentUser: FirebaseUser
    var profileUser: UserModel? = null
    lateinit var storage: FirebaseStorage
    private var storageReference: StorageReference? = null
    private var pDialog: ProgressDialog? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page_acitvity)
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        profileUid = intent?.getStringExtra("uid").toString()

        Log.d("TEST", profileUid!!)
        storage = Firebase.storage
        initViews()
        initEvents()
        initFirebaseDatabase()
        getProfile(profileUid, this)
    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.backIv -> goToHomeFeed()
            R.id.logoutIv -> logout()
          //  R.id.chatIv -> goToChat()
        }
    }
    private fun logout() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to logout ?")
            .setCancelable(false)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> handleLogout()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Logout")
        alert.show()
    }
    private fun handleLogout() {
        //todo
        auth.signOut();
        intent = Intent(this@ProfilePageActivity, LogIn::class.java)
        startActivity(intent)
    }
    private fun goToHomeFeed(){
        Log.d("GOHOOME", "TRYING")
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }
    private fun initViews() {
        val view = this
        backIv = findViewById(R.id.backIv)
        logoutIv = findViewById(R.id.logoutIv)
        logoutIv!!.setOnClickListener(this)
        authorAvatarIv = view.findViewById(R.id.authorAvatarIv)
        nPostsTxt = view.findViewById(R.id.nPostsTxt)
        nFollowersTxt = view.findViewById(R.id.nFollowersTxt)
        followTxt = view.findViewById(R.id.user_followTxt)
        postIv = view.findViewById(R.id.postIv)
        videoIv = view.findViewById(R.id.videoIv)
        postBottomLine = view.findViewById(R.id.postBottomLine)
        videoBottomLine = view.findViewById(R.id.videoBottomLine)
        postRv = view.findViewById(R.id.profilePostRv)

        videoBottomLine?.isVisible = false

        pDialog = ProgressDialog(view)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        backIv?.setOnClickListener(this)
        postIv?.setOnClickListener(View.OnClickListener {
            postBottomLine?.isVisible = true
            videoBottomLine?.isVisible = false
            getPosts(1)
        })
        videoIv?.setOnClickListener(View.OnClickListener {
            videoBottomLine?.isVisible = true
            postBottomLine?.isVisible = false
            getPosts(2)
        })
        followTxt?.setOnClickListener(View.OnClickListener {
            updateFollow(currentUser.uid)
        })
    }

    private fun updateFollow(cometChatUserId: String?) {
        if (cometChatUserId == null) {
            return
        }
        val user = profileUser
        val updatedFollow = ArrayList<String>()
        if (user?.followers == null || user?.followers!!.size == 0) {
            updatedFollow.add(cometChatUserId)
        } else if (user?.followers!!.contains(cometChatUserId)) {
            for (follower in user.followers!!) {
                if (!follower.equals(cometChatUserId)) {
                    updatedFollow.add(follower)
                }
            }
        } else if (!user?.followers!!.contains(cometChatUserId)) {
            for (follower in user.followers!!) {
                updatedFollow.add(follower)
            }
            updatedFollow.add(cometChatUserId)
            if (user.uid !== cometChatUserId) {
                val cometChatUser = Firebase.auth.currentUser
                val notificationId = UUID.randomUUID()
                val notificationMessage = cometChatUser!!.displayName + " has followed you"
                val notificationImage = cometChatUser!!.photoUrl
                val receiverId = user.uid
                val notification = Notification()
                notification.notificationMessage = notificationMessage
                notification.notificationImage = notificationImage.toString()
                notification.id = notificationId.toString()
                notification.receiverId = receiverId
                createNotification(notification)
            }
        }
        user?.followers = updatedFollow
        user?.nFollowers = updatedFollow.size
        mDatabase = Firebase.database.reference;
        mDatabase!!.child("users").child(user!!.uid!!).setValue(user)
    }
    private fun createNotification(notification: Notification) {
        if (notification?.id == null) {
            return;
        }
        mDatabase = Firebase.database.reference;
        mDatabase!!.child("notifications").child(notification.id!!).setValue(notification)
    }

    private fun getProfile(uid: String?, activity: Activity) {
        if(currentUser != null){
            mDatabase?.child(Constants.FIREBASE_USERS+"/"+uid)
                ?.addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.children.count() > 0) {
                            profileUser = dataSnapshot.getValue(UserModel::class.java)
                            Log.d("ImageTest", "ImageUri is:" + profileUser?.photoUrl.toString())
                            Log.d("ImageTest", "User is:" + profileUser)
                            storageReference =storage.getReferenceFromUrl(profileUser?.photoUrl.toString())//createUPicStoreReference()
                            val ONE_MEGABYTE: Long = 1024 * 1024
                            storageReference!!.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                                // Data for "images/island.jpg" is returned, use this as needed
                                Log.d("ImageTest", "ByteSize IT is:" + it.size)
                                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size);
                                Log.d("ImageTest", "ByteSize Pic is:" + it.size)
                                Glide.with(activity)
                                    .load(bmp)
                                    .circleCrop()
                                    .into(authorAvatarIv!!)
                                authorAvatarIv!!.setImageBitmap(bmp)
                                nPostsTxt?.text = if (profileUser!!.nPosts !== null) profileUser!!.nPosts.toString() else "0"
                                nFollowersTxt?.text = if (profileUser!!.nFollowers !== null) profileUser!!.nFollowers.toString() else "0"
                                if (currentUser.uid.equals(profileUser!!.uid)) {
                                    followTxt?.isVisible = false
                                } else {
                                    val updatedFollow = ArrayList<String>()
                                    if (profileUser?.followers == null || profileUser?.followers!!.size == 0) {
                                        followTxt?.text = "Follow"
                                    }else{
                                        followTxt?.text = if (profileUser?.followers!!.contains(currentUser.uid)) "Followed" else "Follow"
                                    }
                                }
                                getPosts(1)
                            }.addOnFailureListener {
                                // Handle any errors
                                Log.d("ImageTest", "Setting Image failed")
                            }
                        } else {
                            pDialog!!.dismiss()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        pDialog!!.dismiss()
                        Toast.makeText(
                            activity,
                            "Cannot fetch list of posts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun initRecyclerView(posts: ArrayList<Post>?) {
        if (posts == null) {
            return;
        }
        postRv!!.layoutManager = GridLayoutManager(this, 3)
        val adapter = this?.let { ProfilePostAdapter(it, posts) }
        postRv!!.adapter = adapter
        pDialog!!.dismiss()
    }
    fun getPosts(postCategory: Int){
        val cometChatUser = profileUser//currentUser
        val self = this
        if (cometChatUser != null) {
            pDialog!!.show()
            mDatabase?.child(Constants.FIREBASE_POSTS)?.orderByChild(Constants.FIREBASE_ID_KEY)
                ?.addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val posts = ArrayList<Post>()
                        if (dataSnapshot.children.count() > 0) {
                            for (postSnapshot in dataSnapshot.children) {
                                val post = postSnapshot.getValue(Post::class.java)
                                if (post != null && post.author!!.uid.equals(cometChatUser.uid)) {
                                    posts.add(post)
                                }
                            }
                        } else {
                            pDialog!!.dismiss()
                        }
                        initRecyclerView(posts)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        pDialog!!.dismiss()
                        Toast.makeText(
                            self,
                            "Cannot fetch list of posts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfilePageFragment.
         */
        /*
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            ProfilePageFragment().apply {
                arguments = Bundle().apply {
                    ARG_PARAM1 =  param1
                }
            }*/
    }
}