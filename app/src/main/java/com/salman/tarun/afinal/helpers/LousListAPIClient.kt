import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Helper Retrofit client for accessing Lou's List JSON API
 */
object LousListAPIClient {
    val BASE_URL = "http://stardock.cs.virginia.edu/louslist/Courses/view/"
    private var retrofit: Retrofit? = null

    val client: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofit
        }

}