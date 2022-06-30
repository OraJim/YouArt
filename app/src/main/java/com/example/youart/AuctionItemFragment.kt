package com.example.youart

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "auctionUid"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AuctionItemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuctionItemFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var auctionUid: String? = null
    private var param2: String? = null
    private var mDatabase: DatabaseReference? = null
    lateinit var currentUser: FirebaseUser
    lateinit var storage: FirebaseStorage
    private var storageReference: StorageReference? = null
    private var pDialog: ProgressDialog? = null
    private lateinit var auth: FirebaseAuth
    private var auctionImage : ImageView? = null
    private var authorImage : ImageView? = null
    private var auctionAuthorTxt : TextView? = null
    private var auctionTitleTxt : TextView? = null
    private var auctionDescribTxt : TextView? = null
    private var auctionAuthor : UserModel? = null
    private var auctionItem : Auction? = null
    private var auctionValueTxt : TextView? = null
    private var bidButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auctionUid = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        Log.d("ONCREATE", currentUser.uid)
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auction_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initEvents()
        initFirebaseDatabase()
        getAuction(auctionUid)
    }

    private fun initViews() {
        val view = this.requireView()
        auctionImage = view.findViewById(R.id.item_auctionItemPicture)
        auctionAuthorTxt = view.findViewById(R.id.item_auctionAuthor)
        auctionValueTxt = view.findViewById(R.id.item_auctionPrice)
        auctionTitleTxt = view.findViewById(R.id.item_AuctionTitle)
        auctionDescribTxt = view.findViewById(R.id.item_Message)
        authorImage = view.findViewById(R.id.item_profileView)
        bidButton = view.findViewById((R.id.bid_button))

        pDialog = ProgressDialog(this.context)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }
   private fun initEvents() {
       bidButton?.setOnClickListener(View.OnClickListener {
           //TODO open bid dialog
           val newFragment = BidDialogFragment()
           newFragment.show( activity?.supportFragmentManager!! , "game")
       })
    }
    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }
    private fun getAuction(uid: String?){
        Log.d("GETAUCTION", "ENTER")
        if(currentUser != null){
            mDatabase?.child(Constants.FIREBASE_AUCTIONS+"/"+uid)
                ?.addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.children.count() > 0) {
                           updateGui(dataSnapshot.getValue(Auction::class.java))
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

    private fun updateGui(item: Auction?) {
        val self = this
        auctionItem = item
        auctionAuthor = auctionItem!!.author
        Log.d("ImageTest", "ImageUri is:" + auctionItem?.content.toString())
        Log.d("ImageTest", "User is:" + auctionAuthor)
        auctionTitleTxt!!.text = auctionItem!!.title
        auctionAuthorTxt!!.text = auctionAuthor!!.displayName
        auctionDescribTxt!!.text = auctionItem!!.comment
        auctionValueTxt!!.text = auctionItem!!.highestBid!!.value

        storageReference =storage.getReferenceFromUrl(auctionItem?.content.toString())//createUPicStoreReference()
        val ONE_MEGABYTE: Long = 1024 * 1024
        storageReference!!.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            Log.d("ImageTest", "ByteSize IT is:" + it.size)
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size);
            Log.d("ImageTest", "ByteSize Pic is:" + it.size)
            Glide.with(self)
                .load(bmp)
                .into(auctionImage!!)
            auctionImage!!.setImageBitmap(bmp)
        }.addOnFailureListener {
            // Handle any errors
            Log.d("ImageTest", "Setting Image failed")
        }
        storageReference =storage.getReferenceFromUrl(auctionAuthor?.photoUrl.toString())//createUPicStoreReference()
        storageReference!!.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size);
            Glide.with(self)
                .load(bmp)
                .into(authorImage!!)
            authorImage!!.setImageBitmap(bmp)
        }.addOnFailureListener {
            // Handle any errors
            Log.d("ImageTest", "Setting Image failed")
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AuctionItemFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AuctionItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}