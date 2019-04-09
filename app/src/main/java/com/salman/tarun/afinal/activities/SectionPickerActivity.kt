package com.salman.tarun.afinal.activities

import LousListAPIClient
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.salman.tarun.afinal.R
import com.salman.tarun.afinal.adapters.SectionAdapter
import com.salman.tarun.afinal.helpers.LousListAPIInterface
import com.salman.tarun.afinal.helpers.LousSection
import com.salman.tarun.afinal.helpers.Section
import kotlinx.android.synthetic.main.activity_sectionpicker.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SectionPickerActivity : AppCompatActivity() {
    lateinit var adapter: SectionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sectionpicker)
        setResult(Activity.RESULT_CANCELED)
        sectionPickerLayout.setOnClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText)
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
        title = "Pick a Section"
        var sectionList = mutableListOf<Section>()
        if (savedInstanceState != null)
            sectionList = savedInstanceState.getParcelableArrayList("sectionList")
        // initializes recycler view
        adapter = SectionAdapter(this, sectionList, false)
        listSections.adapter = adapter
        listSections.layoutManager = LinearLayoutManager(this)
        // query Lou's List API for section with department id and course number, show list of sections with the queried course
        buttonPickSection.setOnClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText)
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)

            val departmentID = editDepartmentID.text.toString().trim()
            val courseNumber = editCourseNumber.text.toString()
            if (departmentID.isEmpty() || courseNumber.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_LONG).show()
            } else {
                val apiService = LousListAPIClient.client!!.create(LousListAPIInterface::class.java)
                val call = apiService.sectionList(departmentID, courseNumber)
                call.enqueue(object : Callback<List<LousSection>> {
                    override fun onResponse(call: Call<List<LousSection>>?, response: Response<List<LousSection>>?) {
                        if (response == null) {
                            Toast.makeText(this@SectionPickerActivity, "Response is null", Toast.LENGTH_LONG).show()
                        } else {
                            val lousSections = response.body()
                            if (lousSections.isEmpty())
                                Toast.makeText(this@SectionPickerActivity, "No sections exist with that department code or course number.", Toast.LENGTH_LONG).show()
                            else {
                                val sections = lousSections.map {
                                    val id = (it.courseID + it.section).toInt()
                                    val humanId = it.deptID + " " + it.courseNum
                                    Section(id, humanId, it.courseName ?: "Error", it.location
                                            ?: "Error", it.meetingTime ?: "Error", it.instructor
                                            ?: "Error")
                                }.toMutableList()
                                adapter.setList(sections)
                            }

                        }
                    }

                    override fun onFailure(call: Call<List<LousSection>>, t: Throwable) {
                        Toast.makeText(this@SectionPickerActivity, "JSON API Call failed", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList("sectionList", ArrayList<Section>(adapter.getSectionList()))
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}