package com.salman.tarun.afinal.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.database.*
import com.salman.tarun.afinal.R
import com.salman.tarun.afinal.adapters.MessageAdapter
import com.salman.tarun.afinal.helpers.Section
import kotlinx.android.synthetic.main.activity_ask.*

/*
the activity responsible for allowing the student user to ask questions, view them, and send them to the instructor user
 */
class SendMessageActivity : AppCompatActivity() {
    private lateinit var remoteDb: DatabaseReference

    private lateinit var adapter: MessageAdapter
    private lateinit var stamperThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)
        title = "Send Questions to Your Professor"
        // extracting the computing ID
        val computingID = getSharedPreferences("${application.packageName}.PREF_KEY", Context.MODE_PRIVATE).getString("computingID", "")
        var messageList = mutableListOf<String>()
        if (savedInstanceState != null) {
            messageList = savedInstanceState.getStringArrayList("messageList")
        }

        // setting up recycler view
        adapter = MessageAdapter(this, messageList)
        listQuestions.adapter = adapter
        listQuestions.layoutManager = LinearLayoutManager(this)
        // set up remote database
        remoteDb = FirebaseDatabase.getInstance().reference
        // get section from intent
        val section = intent.getParcelableExtra<Section>("section")
        // button to send the message to the cloud and to the instructor user
        buttonSend.setOnClickListener {
            val message = editMessage.text.toString().trim()
            // pushing message to the database
            val messageRoot = remoteDb.child("messages").child(section.id.toString()).push()
            messageRoot.child("text").setValue(message)
            messageRoot.child("computingID").setValue(computingID)
            // updating the recycler view
            adapter.addMessage(message)
            // clearing the text box after sending the message
            editMessage.text.clear()
            // dismiss the keyboard upon sending
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText)
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

        remoteDb.child("sections").child(section.id.toString()).child("active").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == false) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }

        })

        stamperThread = Thread(Runnable {
            val threshold =7
            while (true) {
                val timestamp = System.currentTimeMillis() / 1000
                remoteDb.child("sections").child("112233445566").child("timestamp").addListenerForSingleValueEvent( object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val serverTimestamp = p0.getValue(String::class.java)?.toLong()
                        if (serverTimestamp != null && timestamp - serverTimestamp > threshold) {
                            Toast.makeText(this@SendMessageActivity, "Instructor not available", Toast.LENGTH_LONG).show()

                        }
                        Log.d("timestamp", "hello: " + serverTimestamp.toString())

                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                }

                )

                Thread.sleep(5000)
            }
        })

    }


    override fun onStart() {
        super.onStart()
        stamperThread.start()
    }
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putStringArrayList("messageList", ArrayList<String>(adapter.getMessageList()))
    }

}