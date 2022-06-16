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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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
var ARG_PARAM1 : FirebaseUser? = null
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfilePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilePageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: FirebaseUser? = null

    private var authorAvatarIv: ImageView? = null
    private var postIv: ImageView? = null
    private var videoIv: ImageView? = null

    private var postBottomLine: View? = null
    private var videoBottomLine: View? = null

    private var nPostsTxt: TextView? = null
    private var nFollowersTxt: TextView? = null

    private var postRv: RecyclerView? = null
    lateinit var currentUser: FirebaseUser
    lateinit var storage: FirebaseStorage
    private var storageReference: StorageReference? = null
    private var pDialog: ProgressDialog? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        arguments?.let {
            param1 = ARG_PARAM1
        }*/
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
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
        //initFirebaseDatabase()
        getProfile()
        getPosts(1)
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

    private fun getProfile() {
        if(currentUser != null){
            Log.d("ImageTest", "ImageUri is:" + currentUser.photoUrl.toString())
            Log.d("ImageTest", "User is:" + currentUser)
            storageReference =storage.getReferenceFromUrl(currentUser.photoUrl.toString())//createUPicStoreReference()
            val ONE_MEGABYTE: Long = 1024 * 1024
            storageReference!!.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Data for "images/island.jpg" is returned, use this as needed
                Log.d("ImageTest", "ByteSize IT is:" + it.size)
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size);
                Log.d("ImageTest", "ByteSize Pic is:" + it.size)
                Glide.with(this)
                    .load(bmp)
                    .circleCrop()
                    .into(authorAvatarIv!!)
                authorAvatarIv!!.setImageBitmap(bmp)
            }.addOnFailureListener {
                // Handle any errors
                Log.d("ImageTest", "Setting Image failed")
            }

        }
    }
    fun getPosts(postCategory: Int){
        Log.d("GETPOSTS", "TODO")
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
        fun newInstance(param1: FirebaseUser) =
            ProfilePageFragment().apply {
                arguments = Bundle().apply {
                    ARG_PARAM1 =  param1
                }
            }*/
    }
}