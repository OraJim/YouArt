package com.example.youart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var uName: EditText
    lateinit var uMail: EditText
    lateinit var uPw: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            //reload Main page to Real Main Page of Loged in User
            //reload();
        }
        uName = findViewById(R.id.editTextUserName)
        uMail = findViewById(R.id.editTextEmail)
        uPw = findViewById((R.id.editTextPassword))
    }

    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END create_user_with_email]
    }
    fun registerUser(v: View){
        Log.d(TAG, uName.text.toString())
        createAccount(uMail.text.toString(), uPw.text.toString())
    }


    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // Email Verification sent
            }
        // [END send_email_verification]
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.d(TAG, user.toString())
        if(user != null){
            val intent = Intent(this, InputUserInfo::class.java).apply { }
            intent.putExtra("Uname", uName.text.toString())
            startActivity(intent)
        }
    }

    private fun reload() {

    }
    companion object {
        private const val TAG = "EmailPassword"
    }


}