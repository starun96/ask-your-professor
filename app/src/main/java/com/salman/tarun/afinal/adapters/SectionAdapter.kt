package com.salman.tarun.afinal.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.DatabaseUtils
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.salman.tarun.afinal.R
import com.salman.tarun.afinal.activities.MessageFeedActivity
import com.salman.tarun.afinal.activities.SendMessageActivity
import com.salman.tarun.afinal.helpers.Section
import com.salman.tarun.afinal.helpers.SectionDbHelper

/**
 * @param context the activity in which this adapter operates (and can start activites)
 * @param sectionList the data source list of sections
 * @param isAsk whether clicking a list item should take you to the question-sending activity or the section picking activity
 */
class SectionAdapter(private val context: Context, private var sectionList: MutableList<Section>, private val isAsk: Boolean) : RecyclerView.Adapter<SectionAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sectionItemLayout: ConstraintLayout = itemView.findViewById(R.id.sectionItemLayout)
        var textCourse: TextView = itemView.findViewById(R.id.textCourse)
        var textTime: TextView = itemView.findViewById(R.id.textTime)
        var textLocation: TextView = itemView.findViewById(R.id.textLocation)
        var textInstructor: TextView = itemView.findViewById(R.id.textInstructor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val sectionItemView = inflater.inflate(R.layout.recycler_item_section, parent, false)
        return ViewHolder(sectionItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sectionList[position]
        val chosenColor = if (position % 2 == 0) R.color.alternatingItemColor else R.color.white
        holder.sectionItemLayout.setBackgroundColor(context.resources.getColor(chosenColor, context.theme))
        val isStudent = context.getSharedPreferences(context.applicationContext.packageName + ".PREF_KEY", Context.MODE_PRIVATE).getBoolean("isStudent", true)
        // if isAsk is true, then clicking on a item takes user to the question sending activity

        if (isAsk && isStudent)
            holder.sectionItemLayout.setOnClickListener {
                /**
                 * Only allow student to join session if
                 * 1) section exists
                 * 2) section is active
                 * 3) student is authenticated (but we can leave that for later)
                 */
                val remoteDb = FirebaseDatabase.getInstance().reference
                remoteDb.child("sections").child(section.id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.child("active").value == true) {
                            val askIntent = Intent(context, SendMessageActivity::class.java)
                            askIntent.putExtra("section", section)
                            (context as Activity).startActivity(askIntent)
                        } else
                            Toast.makeText(context, "This section is not currently active.", Toast.LENGTH_LONG).show()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            }
        else if (isAsk && !isStudent) {
            holder.sectionItemLayout.setOnClickListener {
                val remoteDb = FirebaseDatabase.getInstance().reference
                val sectionRoot = remoteDb.child("sections").child(section.id.toString())

                sectionRoot.child("active").setValue(true)
                sectionRoot.child("humanId").setValue(section.humanID)
                sectionRoot.child("courseName").setValue(section.courseName)
                sectionRoot.child("instructor").setValue(section.instructor)
                sectionRoot.child("meetingTime").setValue(section.meetingTime)
                sectionRoot.child("location").setValue(section.location)

                val askIntent = Intent(context, MessageFeedActivity::class.java)
                askIntent.putExtra("section", section)
                (context as Activity).startActivity(askIntent)
            }
        }
        // if isAsk is false, then user goes back to home screen and sends the section object back
        else
            holder.sectionItemLayout.setOnClickListener {
                (context as Activity).run {
                    val computingID = getSharedPreferences(application.packageName + ".PREF_KEY", Context.MODE_PRIVATE).getString("computingID", "")
                    val readableDb = SectionDbHelper(context, computingID).readableDatabase
                    val sectionCount = DatabaseUtils.queryNumEntries(readableDb, "Sections", "id = ?", arrayOf(section.id.toString())).toInt()

                    if (sectionCount == 0) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("section", section)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    } else
                        Toast.makeText(context, "You already have this section in your list.", Toast.LENGTH_LONG).show()
                    readableDb.close()
                }
            }
        // sets the relevant section information to the row item
        holder.textCourse.text = "${section.humanID} ${section.courseName}"
        holder.textTime.text = section.meetingTime
        holder.textLocation.text = section.location
        holder.textInstructor.text = section.instructor
    }

    override fun getItemCount() = sectionList.size

    private fun update() {
        sectionList.sort()
        notifyDataSetChanged()
    }

    fun setList(sectionList: MutableList<Section>) {
        this.sectionList = sectionList
        update()
    }

    fun addSection(section: Section) {
        sectionList.add(section)
        update()
    }

    fun getSectionList(): List<Section> {
        return sectionList.toList()
    }

    fun getSize(): Int {
        return sectionList.size
    }


}
