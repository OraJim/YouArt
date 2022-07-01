package com.example.youart

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "auctionUid"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AuctionItemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuctionItemFragment : Fragment(), MainActivity.NoticeDialogListener {
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
    private var auctionTimerTxt : TextView? = null
    private var auctionnBidsTxt : TextView? = null
    private var auctionTitleTxt : TextView? = null
    private var auctionDescribTxt : TextView? = null
    private var auctionAuthor : UserModel? = null
    private var auctionItem : Auction? = null
    private var auctionValueTxt : TextView? = null
    private var bidButton : Button? = null
    private var countDownTimer : CountDownTimer?  = null
    private var countdownRunning = false

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
        auctionTimerTxt = view.findViewById(R.id.item_timeToGo)
        auctionAuthorTxt = view.findViewById(R.id.item_auctionAuthor)
        auctionValueTxt = view.findViewById(R.id.item_auctionPrice)
        auctionTitleTxt = view.findViewById(R.id.item_AuctionTitle)
        auctionDescribTxt = view.findViewById(R.id.item_Message)
        authorImage = view.findViewById(R.id.item_profileView)
        bidButton = view.findViewById((R.id.bid_button))
        auctionnBidsTxt = view.findViewById((R.id.item_nBids))


        pDialog = ProgressDialog(this.context)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }
   private fun initEvents() {
       bidButton?.setOnClickListener(View.OnClickListener {
           val newFragment = BidDialogFragment()
           newFragment.setTargetFragment(this,0)
           newFragment.show( activity?.supportFragmentManager!! , "game")
       })

    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment, bidVal : String) {
        // User touched the dialog's positive button
        Log.d("DIALOGBACK", dialog.toString())
        Log.d("DIALOGBACL", bidVal)
        dialog.dismiss()
        var highestVal = auctionItem!!.highestBid!!.value!!.filter { it.isDigit()}
        val numberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.setMaximumFractionDigits(0);
        highestVal = numberFormat.format(highestVal.toFloat())
        var currentBid = bidVal.filter { it.isDigit()}
        currentBid = numberFormat.format(currentBid.toFloat())
        if(highestVal>currentBid){
            Toast.makeText(
                this@AuctionItemFragment.context,
                "Your Bid was too low",
                Toast.LENGTH_LONG
            ).show()
        }else {
            postBid(bidVal)
        }
    }

    fun postBid(bidVal : String){
        if (currentUser != null) {
            val author = UserModel()
            author.uid = currentUser.uid
            author.photoUrl = currentUser.photoUrl.toString()
            author.displayName = currentUser.displayName

            val startBid = Bid()
            startBid.author = author
            startBid.id = UUID.randomUUID().toString()
            startBid.value = bidVal
            startBid.auctionId = auctionUid

            mDatabase = Firebase.database.reference;
            mDatabase?.child("bids")?.child(startBid.id!!)?.setValue(startBid)

            mDatabase?.child("auctions")?.child(auctionUid!!)?.get()?.addOnSuccessListener {
                pDialog!!.dismiss()
                val auction = it.getValue(Auction::class.java)
                var list: MutableList<String> = mutableListOf<String>()
                if(auction!!.bids != null){
                    list = auction!!.bids as MutableList<String>
                }
                auction!!.nBids = if (auction.nBids != null) auction.nBids!!.plus(1) else 1
                if (auction.nBids!! >= 1) {
                    list.add(startBid.id!!)
                    auction.bids = list
                }
                else { auction.bids  = listOf(startBid.id!!)}
                var highestVal = auction!!.highestBid!!.value!!.filter { it.isDigit()}
                val numberFormat = NumberFormat.getCurrencyInstance()
                numberFormat.setMaximumFractionDigits(0);
                highestVal = numberFormat.format(highestVal.toFloat())
                val currentBid = bidVal.filter { it.isDigit()}
                if(highestVal < numberFormat.format(currentBid.toFloat())){
                    auction.highestBid = startBid
                }
                mDatabase!!.child("auctions").child(auctionUid!!).setValue(auction)
            }?.addOnFailureListener {
                pDialog!!.dismiss()
            }
            Toast.makeText(
                this@AuctionItemFragment.context,
                "Your Bid was placed successfully",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this@AuctionItemFragment.context,
                "Cannot place your bid",
                Toast.LENGTH_LONG
            ).show()
            pDialog!!.dismiss()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
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
        auctionnBidsTxt!!.text = auctionItem!!.nBids!!.toString() + " Bids"

        if(countdownRunning && countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        //1 second as countDown interval
        //TODO calc Miliseconds until end from auction.expires
        Log.d("TESTCOUNTER", auctionItem!!.expires.toString())
        val current = LocalDateTime.now()
        val  dateFormat = "dd/MM/yyyy HH:mm z"
        val END_TIME_FORMATTER = DateTimeFormatter.ofPattern(dateFormat)
        //Instance with given zone auctionItem!!.expires
        //DATE_TIME_FORMATTER.timeZone = TimeZone.getTimeZone("UTC")
        val endDate = LocalDateTime.parse(auctionItem!!.expires, END_TIME_FORMATTER)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val curFormatted = current.format(formatter)
        var difSeconds : Long = 0
        Log.d("TESTCOUNTER", endDate.format(formatter) + "" + curFormatted)
        if(endDate.isAfter(current)){
            //calc time min
            difSeconds = endDate.toEpochSecond(ZoneOffset.UTC) - current.toEpochSecond(ZoneOffset.UTC)
        }
        countDownTimer = object : CountDownTimer(difSeconds*1000, 1000) {

            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {
                //"seconds remaining: " +
                val days  =  TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished-TimeUnit.DAYS.toMillis(days));
                val minutes = TimeUnit.MILLISECONDS.toMinutes((millisUntilFinished - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours)));
                val seconds = TimeUnit.MILLISECONDS.toSeconds((millisUntilFinished- TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours)-TimeUnit.MINUTES.toMillis(minutes)));
                var txt = days.toString() + "D " + hours.toString() + "H "  + minutes.toString()+ "m " +seconds.toString() +" s"
                if(days>0){
                    txt = days.toString() + "D " + hours.toString() + "H"
                }else if(hours > 0){
                    txt = hours.toString() + "H "  + minutes.toString()+ "m"
                }else{
                    txt = minutes.toString()+ "m " +seconds.toString() +"s"
                }
                auctionTimerTxt!!.setText(txt)
                countdownRunning = true
            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                auctionTimerTxt!!.setText("over")
                countdownRunning = false
            }
        }
        countDownTimer!!.start()

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