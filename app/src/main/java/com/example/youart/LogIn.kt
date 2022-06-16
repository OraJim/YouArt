package com.example.youart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogIn : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var uMail: EditText
    lateinit var uPw: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
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
        uMail = findViewById(R.id.editTextLEmail)
        uPw = findViewById((R.id.editTextLPassword))
    }

    public fun logInUser(view: View){
        Log.d(LogIn.TAG, "Login in user")
        signIn(uMail.text.toString(), uPw.text.toString())
    }

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LogIn.TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LogIn.TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user != null){
            val intent = Intent(this,MainActivity::class.java).apply { }//InputUserInfo::class.java).apply { }
            intent.putExtra("Uname", "")
            startActivity(intent)
        }
    }


    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        // Do something in response to button
        val intent = Intent(this, register::class.java).apply { }
        startActivity(intent)
    }
    companion object {
        private const val TAG = "EmailPassword"
    }
}