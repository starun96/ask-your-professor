package com.salman.tarun.afinal.helpers


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Query by department id and course number
 */
interface LousListAPIInterface {

    @GET("{dept}/{num}?json")
    fun sectionList(@Path("dept") dept: String, @Path("num") num: String): Call<List<LousSection>>

}
