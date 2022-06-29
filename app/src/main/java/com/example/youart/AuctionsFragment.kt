package com.example.youart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AuctionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuctionsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var auctions: ArrayList<Auction>? = null
    private var auctionsRv: RecyclerView? = null
    private var mBaseQuery =
        FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()?.child(Constants.FIREBASE_AUCTIONS)?.orderByChild(Constants.FIREBASE_ID_KEY)
    var options: FirebaseRecyclerOptions<Post> = FirebaseRecyclerOptions.Builder<Post>()
        .setQuery(mBaseQuery, Post::class.java)
        .build()
    var cometChatUser = Firebase.auth.currentUser
    var cometChatUserId = cometChatUser!!.uid
    private lateinit var mAdapter : FirebaseRecyclerAdapter<Auction, AuctionViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = getAdapter()
        cometChatUser = Firebase.auth.currentUser
        cometChatUserId = cometChatUser!!.uid
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auctions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auctionsRv = requireView().findViewById(R.id.auctionsRv)
        auctionsRv!!.layoutManager = LinearLayoutManager(this.context)
        // Init RecyclerView
        auctionsRv!!.apply {
            setHasFixedSize(true)
            adapter = mAdapter
        }
    }


    private fun getAdapter(): FirebaseRecyclerAdapter<Auction, AuctionViewHolder> {

        val options = FirebaseRecyclerOptions.Builder<Auction>()
            .setLifecycleOwner(this)
            .setQuery(getFirstQuery(), Auction::class.java)
            .build()

        return object : FirebaseRecyclerAdapter<Auction, AuctionViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
                return AuctionViewHolder(
                    layoutInflater.inflate(
                        R.layout.auction_item,
                        parent,
                        false
                    )
                )
            }

            override fun onBindViewHolder(holder: AuctionViewHolder, position: Int, auction: Auction) {
                //bind the Auction Objecto to the AuctionViewHolder
                holder.bind(auction)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }
    private fun getFirstQuery() = mBaseQuery.limitToFirst(5)
    private fun getLastQuery() = mBaseQuery.limitToLast(5)

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AuctionsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AuctionsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}