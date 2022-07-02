package com.example.youart

import android.app.ProgressDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
// TODO: Do Home and Auction Fragment
// Redesign Input ProfileInfo & Profile Page
//if no Post show default add Post Icon
//Bottom Bar needs Create Post Option (Galerie and Kamera Picker)
//Profile Page needs Gallerie Option
//Home Page Display Other Posts
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [ProfilePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilePageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var profileUid: String? = ""

    private var authorAvatarIv: ImageView? = null
    private var postIv: ImageView? = null
    private var videoIv: ImageView? = null

    private var postBottomLine: View? = null
    private var videoBottomLine: View? = null

    private var nPostsTxt: TextView? = null
    private var nFollowersTxt: TextView? = null

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

        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        profileUid = arguments?.getString("uid")

        Log.d("TEST", profileUid!!)
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_profile_page, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initEvents()
        initFirebaseDatabase()
        getProfile(profileUid)
        //getPosts(1)
    }
    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun initViews() {
        val view = this.requireView()
        authorAvatarIv = view.findViewById(R.id.authorAvatarIv)
        nPostsTxt = view.findViewById(R.id.nPostsTxt)
        nFollowersTxt = view.findViewById(R.id.nFollowersTxt)
        postIv = view.findViewById(R.id.postIv)
        videoIv = view.findViewById(R.id.videoIv)
        postBottomLine = view.findViewById(R.id.postBottomLine)
        videoBottomLine = view.findViewById(R.id.videoBottomLine)
        postRv = view.findViewById(R.id.profilePostRv)

        videoBottomLine?.isVisible = false

        pDialog = ProgressDialog(this.context)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
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
    }

    private fun getProfile(uid: String?) {
        val self = this
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
                                Glide.with(self)
                                    .load(bmp)
                                    .circleCrop()
                                    .into(authorAvatarIv!!)
                                authorAvatarIv!!.setImageBitmap(bmp)
                                nPostsTxt?.text = if (profileUser!!.nPosts !== null) profileUser!!.nPosts.toString() else "0"
                                nFollowersTxt?.text = if (profileUser!!.nFollowers !== null) profileUser!!.nFollowers.toString() else "0"
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
                            context,
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
        postRv!!.layoutManager = GridLayoutManager(this.context, 3)
        val adapter = this.context?.let { ProfilePostAdapter(it, posts) }
        postRv!!.adapter = adapter
        pDialog!!.dismiss()
    }
    private fun initAuctionRecyclerView(auctions: ArrayList<Auction>?) {
        if (auctions == null) {
            return;
        }
        postRv!!.layoutManager = GridLayoutManager(this.context, 3)
        val adapter = this.context?.let { ProfileAuctionAdapter(it, auctions) }
        postRv!!.adapter = adapter
        pDialog!!.dismiss()
    }
    fun getPosts(postCategory: Int){
        val cometChatUser = profileUser//currentUser
        if (cometChatUser != null) {
            if(postCategory == 1) {
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
                                context,
                                "Cannot fetch list of posts",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }else if(postCategory==2){
                pDialog!!.show()
                mDatabase?.child(Constants.FIREBASE_AUCTIONS)?.orderByChild(Constants.FIREBASE_ID_KEY)
                    ?.addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val auctions = ArrayList<Auction>()
                            if (dataSnapshot.children.count() > 0) {
                                for (auctionSnapshot in dataSnapshot.children) {
                                    val auction = auctionSnapshot.getValue(Auction::class.java)
                                    if (auction != null && auction.author!!.uid.equals(cometChatUser.uid)) {
                                        auctions.add(auction)
                                    }
                                }
                            } else {
                                pDialog!!.dismiss()
                            }
                            initAuctionRecyclerView(auctions)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            pDialog!!.dismiss()
                            Toast.makeText(
                                context,
                                "Cannot fetch list of posts",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
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