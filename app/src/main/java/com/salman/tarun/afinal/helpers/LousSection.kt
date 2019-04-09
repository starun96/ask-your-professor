package com.salman.tarun.afinal.helpers

import java.util.*


/**
 *
 * Assignment Notes: Helper class provided as an object to hold
 * a record coming out of the SQLite database.  Nothing should
 * be edited here.
 *
 */

class LousSection {

    /**
     *
     * @return
     * The courseID
     */
    /**
     *
     * @param courseID
     * The courseID
     */
    var courseID: String? = null
    /**
     *
     * @return
     * The departmentID
     */
    /**
     *
     * @param departmentID
     * The departmentID
     */
    var department: String? = null
    /**
     *
     * @return
     * The departmentID
     */
    /**
     *
     * @param departmentID
     * The departmentID
     */
    var deptID: String? = null
    /**
     *
     * @return
     * The courseNum
     */
    /**
     *
     * @param courseNum
     * The courseNum
     */
    var courseNum: String? = null
    /**
     *
     * @return
     * The courseName
     */
    /**
     *
     * @param courseName
     * The courseName
     */
    var courseName: String? = null
    /**
     *
     * @return
     * The semester
     */
    /**
     *
     * @param semester
     * The semester
     */
    var semester: String? = null
    /**
     *
     * @return
     * The section
     */
    /**
     *
     * @param section
     * The section
     */
    var section: String? = null
    /**
     *
     * @return
     * The meetingType
     */
    /**
     *
     * @param meetingType
     * The meetingType
     */
    var meetingType: String? = null
    /**
     *
     * @return
     * The units
     */
    /**
     *
     * @param units
     * The units
     */
    var units: String? = null
    /**
     *
     * @return
     * The status
     */
    /**
     *
     * @param status
     * The status
     */
    var status: String? = null
    /**
     *
     * @return
     * The seatsTaken
     */
    /**
     *
     * @param seatsTaken
     * The seatsTaken
     */
    var seatsTaken: String? = null
    /**
     *
     * @return
     * The seatsOffered
     */
    /**
     *
     * @param seatsOffered
     * The seatsOffered
     */
    var seatsOffered: String? = null
    /**
     *
     * @return
     * The instructor
     */
    /**
     *
     * @param instructor
     * The instructor
     */
    var instructor: String? = null
    /**
     *
     * @return
     * The meetingTime
     */
    /**
     *
     * @param meetingTime
     * The meetingTime
     */
    var meetingTime: String? = null
    /**
     *
     * @return
     * The location
     */
    /**
     *
     * @param location
     * The location
     */
    var location: String? = null
    /**
     *
     * @return
     * The lat
     */
    /**
     *
     * @param lat
     * The lat
     */
    var lat: String? = null
    /**
     *
     * @return
     * The lon
     */
    /**
     *
     * @param lon
     * The lon
     */
    var lon: String? = null
    private val additionalProperties = HashMap<String, Any>()

    fun getAdditionalProperties(): Map<String, Any> {
        return this.additionalProperties
    }

    fun setAdditionalProperty(name: String, value: Any) {
        this.additionalProperties[name] = value
    }

    override fun toString(): String {
        return deptID + courseNum + ": " + courseName
    }

}