package com.example.youart

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class MainActivity : AppCompatActivity(), View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var currentUser: FirebaseUser
    private var plusIv: ImageView? = null
    private var chatIv: ImageView? = null
    private var logoutIv: ImageView? = null
    private var bottomNavigationView: BottomNavigationView? = null
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, bidVal: String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    private var pDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        initViews()
        initEvents()
        initFragment(savedInstanceState)
    }

    private fun initViews() {
        plusIv = findViewById(R.id.plusIv)
        chatIv = findViewById(R.id.chatIv)
        logoutIv = findViewById(R.id.logoutIv)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        plusIv!!.setOnClickListener(this)
        chatIv!!.setOnClickListener(this)
        logoutIv!!.setOnClickListener(this)
        bottomNavigationView!!.setOnNavigationItemSelectedListener(this)
    }
    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment = FeedFragment()
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
    }

    private fun goToLoginActivity() {
        intent = Intent(this, LogIn::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (currentUser == null) {
            goToLoginActivity();
        }
    }
    private fun goToCreatePost() {
        intent = Intent(this@MainActivity, CreatePost::class.java)
        startActivity(intent)
    }
    private fun logout() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to logout ?")
            .setCancelable(false)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> handleLogout()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Logout")
        alert.show()
    }
    private fun handleLogout() {
        //todo
    }
    private fun goToChat() {
        intent = Intent(this@MainActivity, CreateAuction::class.java)
        startActivity(intent)
    }
    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.plusIv -> goToCreatePost()
            R.id.logoutIv -> logout()
            R.id.chatIv -> goToChat()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val bundle = Bundle()
        bundle.putString("uid",currentUser.uid)
        when (item.itemId) {
            R.id.home -> {
                val fragment = FeedFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
            R.id.profile -> {
                val fragment = ProfilePageFragment()
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
            R.id.notification -> {
                val fragment = NotificationFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
            R.id.auction -> {
                val fragment = AuctionsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
        }
        return false
    }

    private fun createUPicStoreReference(): StorageReference? {
        val storageRef = storage.reference
        var imagesRef: StorageReference? = storageRef.child("images")
        var picRef : StorageReference? = storageRef.child("images/"+currentUser.uid+"/")
        Log.d("Picture", picRef.toString())
        return picRef
    }
}