package com.example.youart

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AuctionViewHolder (itemView: View)  : RecyclerView.ViewHolder(itemView) {
    var auctionMessage: TextView = itemView.findViewById(R.id.post_Message)
    var personName: TextView? = itemView.findViewById(R.id.post_AuthorName)
    var personPhoto: ImageView? = itemView.findViewById(R.id.post_profileView)

    fun bind(auction: Auction) {
        personName!!.text = auction.author!!.displayName
        auctionMessage.text = "Already " + auction.nBids + " Bids set"
        Glide.with(itemView)
            .load(auction.author?.photoUrl)
            .circleCrop()
            .into(personPhoto!!);

    }
}