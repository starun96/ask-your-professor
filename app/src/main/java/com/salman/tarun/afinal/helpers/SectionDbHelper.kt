package com.salman.tarun.afinal.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * The database helper for writing to and from the local database
 */
class SectionDbHelper(context: Context, databaseName: String) : SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {

        const val DATABASE_VERSION = 2

        const val SQL_CREATE_ENTRIES = "CREATE TABLE Sections (\n" +
                "    id INTEGER PRIMARY KEY,\n" +
                "    human_id VARCHAR(20) NOT NULL,\n" +
                "    course_name VARCHAR(255) NOT NULL,\n" +
                "    instructor VARCHAR(255) NOT NULL,\n" +
                "    location VARCHAR(255) NOT NULL,\n" +
                "    meeting_time VARCHAR(255) NOT NULL\n" +
                ");"
        const val SQL_DELETE_ENTRIES = "DELETE FROM Sections;"

    }
}