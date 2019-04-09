package com.salman.tarun.afinal.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.salman.tarun.afinal.R
import kotlinx.android.synthetic.main.activity_signin.*


class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        title = "Sign In"
        // prepares authentication, takes you to home screen if successful
        buttonSignIn.setOnClickListener {

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText)
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            // input email and password
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            // Firebase authentication
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, {
                if (it.isSuccessful) {
                    it.exception
                    val user = auth.currentUser
                    var computingID = ""
                    if (user != null) {
                        // extract computing id from UVA email address
                        computingID = email.split('@')[0].toLowerCase()
                        var isStudent = false
                        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@SignInActivity, "Could not listen for the event.", Toast.LENGTH_LONG).show()
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                isStudent = snapshot.child(computingID).value as Boolean
                                // save computing id in shared preferences for future use
                                val sharedPref = getSharedPreferences("${application.packageName}.PREF_KEY", Context.MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("computingID", computingID)
                                editor.putBoolean("isStudent", isStudent)
                                editor.apply()
                                // authentication succeeded message
                                Toast.makeText(this@SignInActivity, "Authentication succeeded, $computingID.", Toast.LENGTH_LONG).show()
                                // go to home screen (with all the user's sections)
                                val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                                startActivity(intent)
                            }
                        })

                    }
                } else {
                    Toast.makeText(this, "Sign-in failed. The e-mail or password is incorrect, or the account does not exist.", Toast.LENGTH_LONG).show()
                }
            })
        }
        // go to sign up screen to create an account
        buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onBackPressed() {

    }


}
