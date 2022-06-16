package com.example.youart

import com.google.firebase.auth.FirebaseUser

class Post {
    var id: String? = null
    var content: String? = null
    var nLikes: Int? = null
    var likes: List<String>? = null
    var author: UserModel? = null
    var hasLiked: Boolean? = null
    var hasFollowed: Boolean? = null
}