package com.example.youart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
/**
 * Activity called when clicked on a post
 * used tho show PostDetail View
 * needs to be implemented yet
 */
class DetailActivity : AppCompatActivity() {
    private var postContentIv: ImageView? = null
    private var postContentVv: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        initViews()
    }

    private fun initViews() {
        postContentIv = findViewById(R.id.postContentIv)
        postContentVv = findViewById(R.id.postContentVv)
        val postContent = intent?.getStringExtra("postContent").toString()
        val postCategory = intent?.getIntExtra("postCategory", 0)
       if (postCategory == 1) {
            postContentIv?.isVisible = true
            postContentVv?.isVisible = false
            Glide.with(this)
                .load(postContent)
                .into(postContentIv!!);
        }
    }
}