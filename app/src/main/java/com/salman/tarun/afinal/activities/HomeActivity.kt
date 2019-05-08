package com.salman.tarun.afinal.activities


import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.salman.tarun.afinal.R
import com.salman.tarun.afinal.adapters.SectionAdapter
import com.salman.tarun.afinal.helpers.Section
import com.salman.tarun.afinal.helpers.SectionDbHelper
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File


class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: SectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        title = "Your Sections"

        // init image
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val preferences = getSharedPreferences(application.packageName + ".PREF_KEY", Context.MODE_PRIVATE)
        // gets computing id, preparing for listing sections associated with the computing id
        val computingID = preferences.getString("computingID", "")
        val imagePath = preferences.getString("$computingID-userImagePath", "")
        val rotation = preferences.getFloat("$computingID-userImageRotation", 0.0f)

        if (imagePath.isNotEmpty()) {
            val imageFile = File(storageDir, imagePath.removePrefix("/my_images/"))
            val photoURI = FileProvider.getUriForFile(applicationContext, application.packageName + ".fileprovider", imageFile)

            imageUser.setImageURI(photoURI)
            imageUser.rotation = rotation
        }

        val localDb = SectionDbHelper(this, computingID)

        val writableDb = localDb.writableDatabase

        val sectionValues = ContentValues().apply {
            put("id", "112233445566")
            put("human_id", "CS 9999")
            put("course_name", "Test Class")
            put("instructor", "Tarun Saharya")
            put("location", "116 Chelsea Dr")
            put("meeting_time",  "2-3 PM")
        }
        writableDb?.insert("sections", null, sectionValues)
        localDb.close()

        var mySections = mutableListOf<Section>()

        if (savedInstanceState == null) {
            // getting sections from local database
            val localDb = SectionDbHelper(this, computingID)
            val readableDb = localDb.readableDatabase
            val cursor = readableDb.query(
                    "Sections",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            )
            // adding sections through iteration

            with(cursor) {
                while (moveToNext()) {
                    val itemId = getInt(getColumnIndexOrThrow("id"))
                    val itemHumanID = getString(getColumnIndexOrThrow("human_id"))
                    val itemCourseName = getString(getColumnIndexOrThrow("course_name"))
                    val itemInstructor = getString(getColumnIndexOrThrow("instructor"))
                    val itemLocation = getString(getColumnIndexOrThrow("location"))
                    val itemMeetingTime = getString(getColumnIndexOrThrow("meeting_time"))
                    val newSection = Section(itemId, itemHumanID, itemCourseName, itemLocation, itemMeetingTime, itemInstructor)
                    mySections.add(newSection)
                }
            }

            cursor.close()
            localDb.close()
        } else {
            mySections = savedInstanceState.getParcelableArrayList<Section>("sectionList").toMutableList()
        }

        // init user info
        textComputingID.text = computingID
        textSectionNum.text = mySections.size.toString()

        // initializing recycler view
        adapter = SectionAdapter(this, mySections, true)
        listSections.adapter = adapter
        listSections.layoutManager = LinearLayoutManager(this)

        if (mySections.isEmpty())
            textTip.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                SECTION_PICKER_ACTIVITY -> {
                    if (data != null) {
                        // gets the section from the section picker activity
                        val section = data.getParcelableExtra<Section>("section")
                        val preferences = getSharedPreferences(application.packageName + ".PREF_KEY", Context.MODE_PRIVATE)
                        val computingID = preferences.getString("computingID", "")
                        // see if section already exists in the database
                        val localDb = SectionDbHelper(this, computingID)
                        // need computing id to associate section with the user list
                        val isStudent = preferences.getBoolean("isStudent", true)
                        // save the section to the local database
                        val writableDb = localDb.writableDatabase

                        val sectionValues = ContentValues().apply {
                            put("id", section.id)
                            put("human_id", section.humanID)
                            put("course_name", section.courseName)
                            put("instructor", section.instructor)
                            put("location", section.location)
                            put("meeting_time", section.meetingTime)
                        }
                        writableDb?.insert("sections", null, sectionValues)
                        localDb.close()

                        // update recycler view
                        adapter.addSection(section)

                        // change tip text
                        textTip.visibility = View.GONE

                        // update sections num text view
                        textSectionNum.text = adapter.getSize().toString()

                        /**
                         * Add section to the database if
                         * 1) it doesn't already exist
                         * 2) user is an instructor (isStudent == false)
                         */

                        if (!isStudent) {
                            val remoteDb = FirebaseDatabase.getInstance().reference
                            var sectionExists = false
                            val sectionRoot = remoteDb.child("sections").child(section.id.toString())
                            sectionRoot.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@HomeActivity, "Cannot read from the section", Toast.LENGTH_LONG).show()
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    sectionExists = snapshot.exists()
                                    Log.d("sectionExists", sectionExists.toString())
                                }
                            })
                            if (!sectionExists) {
                                sectionRoot.child("active").setValue(false)
                            }
                        }
                    }

                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menu_button_add -> {
                // go to another activity to pick a section
                val intent = Intent(this, SectionPickerActivity::class.java)
                startActivityForResult(intent, SECTION_PICKER_ACTIVITY)
                true
            }
            R.id.menu_button_sign_out -> {
                // sign the user out
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList("sectionList", ArrayList<Section>(adapter.getSectionList()))
    }

    companion object {
        const val SECTION_PICKER_ACTIVITY = 0
    }

}

// pre-populated list of sections (not in final product)
/*val mobileSection = Section(0, "CS 4720", "Mobile Application Development", "MECH 341", "M/W/F 11:00 AM - 11:50 AM", "Mark Sherriff")
val ecommerceSection = Section(1, "CS 4753", "Electronic Commerce Technologies", "THOR E316", "M/W 3:30 PM - 4:45 PM", "Alfred Weaver")
val webSection = Section(2, "CS 4640", "Programming Languages for Web Applications", "THOR E316", "Tu/Th 2:00 PM - 3:15 PM", "Upsorn Praphamontripong")
val hciSection = Section(3, "CS 3205", "Human-Computer Interactions", "OLS 120", "Tu/Th 3:30 PM - 4:45 PM", "Mark Floryan")
val theorySection = Section(4, "CS 3102", "Theory of Computation", "GILM 130", "Tu/Th 12:30 PM - 1:45 PM", "Gabriel Robins")*/