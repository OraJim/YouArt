package com.example.youart

import com.example.youart.UserModel

class Auction {
    var id: String? = null
    var content: String? = null
    var nInterests: Int? = null
    var interests: List<String>? = null
    var author: UserModel? = null
    var nBids: Int? = null
    var bids: List<String>? = null
    var highestBid :  Bid? = null
    var comment: String? = null
    var expires : String? =  null
}