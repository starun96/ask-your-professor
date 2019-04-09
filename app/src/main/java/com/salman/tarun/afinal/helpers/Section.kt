package com.salman.tarun.afinal.helpers

import android.os.Parcel
import android.os.Parcelable

/**
 * A section is composed of the following fields:
 * @param id distinguishes this section from every other section, even those from other courses
 * @param humanID human-readable ID for the course, e.g. CS 4720 or APMA 3100
 * @param courseName name of the course
 * @param location rough description of the location of the section
 * @param instructor the instructor of the section
 */
data class Section(var id: Int, var humanID: String, var courseName: String, var location: String, var meetingTime: String, var instructor: String) : Comparable<Section>, Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(humanID)
        dest.writeString(courseName)
        dest.writeString(location)
        dest.writeString(meetingTime)
        dest.writeString(instructor)
    }

    override fun toString(): String {
        return "id: $id, humanId: $humanID, courseName: $courseName, location: $location, meetingTime: $meetingTime, instructor: $instructor \n"
    }

    override fun describeContents() = 0

    /**
     * A section is to be sorted by ascending humanID
     */
    override fun compareTo(other: Section): Int {
        return humanID.compareTo(other.humanID)
    }

    companion object CREATOR : Parcelable.Creator<Section> {
        override fun createFromParcel(parcel: Parcel): Section {
            return Section(parcel)
        }

        override fun newArray(size: Int): Array<Section?> {
            return arrayOfNulls(size)
        }
    }

}
