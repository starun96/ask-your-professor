package com.salman.tarun.afinal.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import com.google.firebase.database.*
import com.salman.tarun.afinal.adapters.MessageAdapter
import com.salman.tarun.afinal.helpers.Section
import kotlinx.android.synthetic.main.activity_ask.*
import com.salman.tarun.afinal.R


import android.app.*



class MessageFeedActivity : AppCompatActivity() {
    lateinit var adapter: MessageAdapter
    lateinit var remoteDb: DatabaseReference
    lateinit var section: Section
    lateinit var stamperThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_feed)
        title = "Student Inquiries"
        // recycler view
        var questionList = mutableListOf<String>()

        if (savedInstanceState != null) {
            questionList = savedInstanceState.getStringArrayList("messageList").toMutableList()

        }


        // database
        remoteDb = FirebaseDatabase.getInstance().reference
        Log.d("before", "sandwich")
        section = intent.getParcelableExtra("section")
        Log.d("section", section.toString())
        val sectionMessagesRoot = remoteDb.child("messages").child(section.id.toString())
        // add new messages to list as soon as they come
        sectionMessagesRoot.addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                adapter.addMessage(snapshot.child("text").value.toString())
            }

        })
        adapter = MessageAdapter(this, questionList)
        listQuestions.adapter = adapter
        listQuestions.layoutManager = LinearLayoutManager(this)

        val stamper = Runnable{
            while (true) {
                Log.d("HEYLOOKATME", "tag")
                val timestamp = System.currentTimeMillis() / 1000
                remoteDb.child("sections").child("112233445566").child("timestamp").setValue(timestamp.toString())
                Thread.sleep(5000)
            }
        }


        stamperThread = Thread(stamper)
    }


    override fun onStart() {
        super.onStart()
        stamperThread.start()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                // use same behavior as back button
                buildAlert()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun clearMessages() {
        remoteDb.child("messages").child(section.id.toString()).removeValue()
        // render the section session inactive
        remoteDb.child("sections").child(section.id.toString()).child("active").setValue(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearMessages()
    }

    override fun onBackPressed() {
        buildAlert()
    }

    private fun buildAlert() {
        val builder = AlertDialog.Builder(this)
        builder.run {
            setTitle("End Session")
            setMessage("Are you sure you want to end the lecture session?")
            setPositiveButton("End Session", { _, _ ->
                clearMessages()
                finish()
            })
            setNegativeButton("Cancel", { _, _ -> })
            create().show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putStringArrayList("messageList", ArrayList(adapter.getMessageList()))
    }


}