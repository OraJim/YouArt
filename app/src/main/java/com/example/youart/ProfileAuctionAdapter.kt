package com.example.youart

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.youart.R
import com.example.youart.DetailActivity
import com.example.youart.Post

class ProfileAuctionAdapter(
    private val context: Context,
    private val auctions: List<Auction>,
) : RecyclerView.Adapter<ProfileAuctionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val auction = auctions[position]
        holder.postContentVv.isVisible = false
        holder.postContentIv.isVisible = true
        Glide.with(context)
            .load(auction.content)
            .into(holder.postContentIv);
        holder.postItemFl.setOnClickListener(View.OnClickListener {
            goToAuction(auction,it)
        })
    }

    override fun getItemCount(): Int {
        return auctions.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val postContentIv: ImageView = itemView.findViewById(R.id.postContentIv)
        val postContentVv: VideoView = itemView.findViewById(R.id.postContentVv)
        val postItemFl: FrameLayout = itemView.findViewById(R.id.postItemFl)
    }

    private fun goToAuction(auction: Auction?, view : View) {
        if (auction?.content == null) {
            return
        }
        /*
        val activity : AppCompatActivity = view.context as AppCompatActivity
        val fragment = AuctionItemFragment.newInstance(auction.id!!, "")
        activity?.supportFragmentManager?.beginTransaction()!!.replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
            .commit()*/
        val intent = Intent(this.context, MainActivity::class.java)
        intent.putExtra("auctionUid", auction.id)
        this.context.startActivity(intent)
    }
}