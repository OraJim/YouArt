package com.example.youart

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat

class AuctionViewHolder (itemView: View)  : RecyclerView.ViewHolder(itemView) {
    var auctionTitle : TextView = itemView.findViewById((R.id.post_AuctionTitle))
    var auctionPrice : TextView = itemView.findViewById((R.id.post_auctionPrice))
    var auctionMessage: TextView = itemView.findViewById(R.id.post_Message)
    var personName: TextView? = itemView.findViewById(R.id.post_AuctionTitle)
    var personPhoto: ImageView? = itemView.findViewById(R.id.post_profileView)
    var auctionListItem : ConstraintLayout? = itemView.findViewById((R.id.auction_list_item))

    fun bind(auction: Auction) {
        val numberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.setMaximumFractionDigits(0);
        var convertet = numberFormat.format(auction.highestBid?.value.toString().filter{it.isDigit()}.toFloat())
        auctionTitle!!.text = auction.title
        auctionPrice!!.text = convertet
        personName!!.text = auction.author!!.displayName
        auctionMessage.text = "Already " + auction.nBids + " Bids set"
        Glide.with(itemView)
            .load(auction.content)
            .circleCrop()
            .into(personPhoto!!);

    }
}