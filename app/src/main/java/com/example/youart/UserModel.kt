package com.example.youart

import android.net.Uri

class UserModel {
    var uid: String? = null
    var displayName: String? = null
    var email: String? = null
    var photoUrl: String? = null
    var followers: List<String>? = null
    var nFollowers: Int? = null
    var nPosts: Int? = null
}