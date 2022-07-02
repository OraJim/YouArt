package com.example.youart

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


/**
 * Feed Fragment with RecyclerView
 * Inits PostItems via PostAdapter with FirebaseRecyclerAdapter
 */
class FeedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var postRv: RecyclerView? = null

    private var pDialog: ProgressDialog? = null
    private var mDatabase: DatabaseReference? = null
    private var posts: ArrayList<Post>? = null
    private var adapter: PostAdapter? = null
    private var lastFocused : View? = null
    private var lastInstanceState: Parcelable? = null
    private var firstInit : Boolean = true
    private var mBaseQuery =
        FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()?.child(Constants.FIREBASE_POSTS)?.orderByChild(Constants.FIREBASE_ID_KEY)
    var options: FirebaseRecyclerOptions<Post> = FirebaseRecyclerOptions.Builder<Post>()
        .setQuery(mBaseQuery, Post::class.java)
        .build()
    var cometChatUser = Firebase.auth.currentUser
    var cometChatUserId = cometChatUser!!.uid
    private lateinit var mAdapter : FirebaseRecyclerAdapter<Post, PostAdapter.ViewHolder>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = getAdapter()
        cometChatUser = Firebase.auth.currentUser
        cometChatUserId = cometChatUser!!.uid
        /*
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }
    private fun getAdapter(): FirebaseRecyclerAdapter<Post, PostAdapter.ViewHolder> {

        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setLifecycleOwner(this)
            .setQuery(getFirstQuery(), Post::class.java)
            .build()

        return object : FirebaseRecyclerAdapter<Post, PostAdapter.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
                return PostAdapter.ViewHolder(
                    layoutInflater.inflate(
                        R.layout.post_item,
                        parent,
                        false
                    )
                )
            }

            override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int, post: Post) {
                //check if user has liked Post to show active heart Icon
                hasLiked(post,cometChatUserId)
                Glide.with(this@FeedFragment.requireContext())
                    .load(post.author?.photoUrl)
                    .circleCrop()
                    .into(holder.authorAvatarIv);
                holder.authorNameTxt.text = post.author?.displayName
                holder.likeCountTxt.text = post.nLikes.toString() + " Likes"
                if (cometChatUserId.equals(post.author!!.uid)) {
                    holder.followTxt.isVisible = false
                    holder.dot.isVisible = false
                } else {
                    holder.followTxt.text = if (post.hasFollowed === true) "Followed" else "Follow"
                }
                holder.postContentIv.isVisible = true
                Glide.with(this@FeedFragment.requireContext())
                    .load(post.content)
                    .into(holder.postContentIv);
                holder.postContentVv.isVisible = false

                val heartIcon = if (post.hasLiked == true) R.drawable.heart_active else R.drawable.heart
                Glide.with(this@FeedFragment.requireContext())
                    .load(heartIcon)
                    .into(holder.heartIv);
                holder.followTxt.setOnClickListener(View.OnClickListener {
                    toggleFollow(post)
                })
                holder.heartIv.setOnClickListener(View.OnClickListener {
                    toggleLike(post, holder, it)
                })
                holder.postContentIv.setOnClickListener(View.OnClickListener {
                   // goToDetail(post)
                })
                holder.postContentVv.setOnClickListener(View.OnClickListener {
                   // goToDetail(post)
                })
                holder.authorAvatarIv.setOnClickListener(View.OnClickListener {
                    loadUserProfile(post.author!!)
                })
                holder.authorNameTxt.setOnClickListener(View.OnClickListener {
                    loadUserProfile(post.author!!)
                })
            }
        }
    }

    private fun loadUserProfile(user: UserModel){
        Log.d("LoadUSerProfile", user.uid!!)
        /*
        val bundle = Bundle()
        bundle.putString("uid",user.uid)
        val fragment = ProfilePageFragment()
        fragment.arguments = bundle
        val transaction = feedFragment.requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
        transaction.disallowAddToBackStack()
        transaction.commit()*/
        val intent = Intent(this.context, ProfilePageActivity::class.java)
        intent.putExtra("uid", user.uid)
        this.requireContext().startActivity(intent)
    }
    private fun updateFollow(post: Post?, cometChatUserId: String?) {
        if (post == null || cometChatUserId == null) {
            return
        }
        val parent = this.context
        mDatabase?.child("users")?.child(post.author?.uid!!)?.get()?.addOnSuccessListener {
            val user = it.getValue(UserModel::class.java)
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
                if (post.author!!.uid !== cometChatUserId) {
                    val cometChatUser = Firebase.auth.currentUser
                    val notificationId = UUID.randomUUID()
                    val notificationMessage = cometChatUser!!.displayName + " has followed you"
                    val notificationImage = cometChatUser!!.photoUrl
                    val receiverId = post.author!!.uid
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
            mDatabase!!.child("users").child(post.author!!.uid!!).setValue(user)
           // feedFragment.getPosts()
        }?.addOnFailureListener {
            Log.d("error",it.toString())
        }
    }
    private fun createNotification(notification: Notification) {
        if (notification?.id == null) {
            return;
        }
        mDatabase = Firebase.database.reference;
        mDatabase!!.child("notifications").child(notification.id!!).setValue(notification)
    }

    private fun toggleFollow(post: Post?) {
        if (post == null) {
            return;
        }
        updateFollow(post, cometChatUserId)
    }

    private fun updateLikes(post: Post?, cometChatUserId: String?,  viewHolder : PostAdapter.ViewHolder, context:View) {
        if (post == null || cometChatUserId == null) {
            return
        }
        val updatedLikes = ArrayList<String>()
        if (post.likes == null || post.likes!!.size == 0) {
            updatedLikes.add(cometChatUserId)
        } else if (post.hasLiked == true) {
            val heartIcon = R.drawable.heart
            Glide.with(context)
                .load(heartIcon)
                .into(viewHolder!!.heartIv);
            for (like in post.likes!!) {
                if (!like.equals(cometChatUserId)) {
                    updatedLikes.add(like)
                }
            }
        } else if (post.hasLiked == false) {
            val heartIcon = R.drawable.heart_active
            Glide.with(context)
                .load(heartIcon)
                .into(viewHolder!!.heartIv);
            for (like in post.likes!!) {
                updatedLikes.add(like)
            }
            updatedLikes.add(cometChatUserId)
            if (post.author!!.uid !== cometChatUserId) {
                val cometChatUser = Firebase.auth.currentUser
                val notificationId = UUID.randomUUID()
                val notificationMessage = cometChatUser!!.displayName + " has liked your post"
                val notificationImage = cometChatUser!!.photoUrl.toString()
                val receiverId = post.author!!.uid
                val notification = Notification()
                notification.notificationMessage = notificationMessage
                notification.notificationImage = notificationImage
                notification.id = notificationId.toString()
                notification.receiverId = receiverId
                createNotification(notification)
            }
        }
        post.likes = updatedLikes
        post.nLikes = updatedLikes.size
        mDatabase = Firebase.database.reference;
        mDatabase!!.child("posts").child(post.id!!).setValue(post)
    }

    private fun toggleLike(post: Post?, holder : PostAdapter.ViewHolder, view: View) {
        if (post == null) {
            return;
        }
        //check if user has liked post
        hasLiked(post, cometChatUserId)
        updateLikes(post, cometChatUserId, holder, view)
    }

    private fun getFirstQuery() = mBaseQuery.limitToFirst(5)
    private fun getLastQuery() = mBaseQuery.limitToLast(5)

    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        //initFirebaseDatabase()
        //getPosts()
    }

    private fun initViews() {
        pDialog = ProgressDialog(this.context)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)

        postRv = requireView().findViewById(R.id.postRv)
        postRv!!.layoutManager = LinearLayoutManager(this.context)

        // lastFirstVisiblePosition = postRv.layoutManager.find
        val cometChatUser = Firebase.auth.currentUser
        val cometChatUserId = cometChatUser!!.uid
        postRv!!.adapter = mAdapter
        postRv!!.layoutManager!!.onRestoreInstanceState(lastInstanceState)

        pDialog!!.dismiss()
    }

    private fun initFirebaseDatabase() {
        mDatabase =
            FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun initRecyclerView(posts: ArrayList<Post>?) {
        if (posts == null || posts.size == 0) {
            return;
        }
        if(!firstInit){
            return;
        }
        firstInit = false;
        postRv!!.layoutManager = LinearLayoutManager(this.context)

       // lastFirstVisiblePosition = postRv.layoutManager.find
        val cometChatUser = Firebase.auth.currentUser
        val cometChatUserId = cometChatUser!!.uid
        adapter = this.context?.let { PostAdapter(this, mDatabase!!, it, posts, cometChatUserId) }
        postRv!!.adapter = mAdapter
        postRv!!.layoutManager!!.onRestoreInstanceState(lastInstanceState)

        pDialog!!.dismiss()
    }

    private fun hasLiked(post: Post?, id: String?) {
        if (post?.likes == null || post?.likes?.size === 0 || id == null) {
            post?.hasLiked = false
            return;
        }
        for (like in post.likes!!) {
            if (like.equals(id)) {
                post.hasLiked = true;
                return;
            }
        }
        post.hasLiked = false
    }

    private fun hasFollowed(index: Int?, post: Post?, id: String?) {
        if (post?.author == null || post.author?.uid == null || id == null) {
            return;
        }
        val userId = post.author?.uid
        mDatabase?.child("users")?.child(userId!!)?.get()?.addOnSuccessListener {
            val user = it.getValue(UserModel::class.java)
            if (user?.followers == null || user.followers?.size == 0) {
                post.hasFollowed = false
            } else {
                for (follower in user.followers!!) {
                    if (follower.equals(id)) {
                        post.hasFollowed = true
                    }
                }
            }
            posts!!.set(index!!, post)
            if (adapter != null) {
                adapter!!.notifyDataSetChanged()
            }
        }?.addOnFailureListener {
        }
    }

    private fun updateAllFollow() {
        val cometChatUser = Firebase.auth.currentUser
        val cometChatUserId = cometChatUser!!.uid
        for ((index, post) in posts!!.withIndex()) {
            hasFollowed(index, post, cometChatUserId)
        }
    }

    fun getPosts() {
        val cometChatUser = Firebase.auth.currentUser
        if (cometChatUser != null) {
            pDialog!!.show()
            mDatabase?.child(Constants.FIREBASE_POSTS)?.orderByChild(Constants.FIREBASE_ID_KEY)
                ?.addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        posts = ArrayList()
                        if (dataSnapshot.children.count() > 0) {
                            for (postSnapshot in dataSnapshot.children) {
                                val post = postSnapshot.getValue(Post::class.java)
                                if (post != null) {
                                    hasLiked(post, cometChatUser.uid)
                                    posts!!.add(post)
                                }
                            }
                            if(!firstInit){
                                adapter!!.notifyDataSetChanged()
                            }
                            initRecyclerView(posts)
                            updateAllFollow()
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedFragment().apply {
                /*
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }
}