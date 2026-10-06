package retrofit

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Headers

interface retrofitInterface {

    @GET("v1/cryptocurrency/listings/latest")
    suspend fun getListings(
        @Query("limit") limit: Int = 100,
        @Query("convert") convert: String = "USD"
    ): Response<CmcListingsResponse>

    @GET("v2/cryptocurrency/info")
    suspend fun getMetadata(
        @Query("symbol") symbol: String
    ): Response<CmcInfoResponse>

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    @GET("https://min-api.cryptocompare.com/data/v2/news/?lang=EN")
    suspend fun getNews(
        @Query("sortOrder") sortOrder: String = "popular",
        @Query("limit") limit: Int = 10,
        @Query("api_key") apiKey: String = RetrofitHelper.cryptoCompareApiKey
    ): Response<CryptoCompareNewsResponse>
}
