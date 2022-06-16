package com.example.youart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfilePage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var currentUser: FirebaseUser
    lateinit var toolbar: Toolbar
    lateinit var bottomNav : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)
        auth = Firebase.auth
        val curUser = auth.currentUser
        if(curUser != null){
            currentUser = curUser
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        val profileFragment = ProfilePageFragment()
        val feedFragment = FeedFragment()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar);
        bottomNav = findViewById(R.id.bottomNavigationView) as BottomNavigationView
        bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home->setCurrentFragment(feedFragment)
               // R.id.message->setCurrentFragment(secondFragment)
                R.id.profile->setCurrentFragment(profileFragment)
               // R.id.auction->setCurrentFragment(thirdFragment)


            }
            true
        }

    }

    // Menu icons are inflated just as they were with actionbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.getItemId()) {
            R.id.editUser -> {
                editUserInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }

    public fun editUserInfo(){
        Log.d("Change to edit", "Edit userInformationView")
        val intent = Intent(this,InputUserInfo::class.java).apply { }//InputUserInfo::class.java).apply { }
        startActivity(intent)
    }

    private fun createUPicStoreReference(): StorageReference? {
        val storageRef = storage.reference
        var imagesRef: StorageReference? = storageRef.child("images")
        var picRef : StorageReference? = storageRef.child("images/"+currentUser.uid+"/")
        Log.d("Picture", picRef.toString())
        return picRef
    }
}