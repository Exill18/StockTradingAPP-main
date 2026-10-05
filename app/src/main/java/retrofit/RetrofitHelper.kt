package retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    // CoinMarketCap API Base URL
    val baseUrl = "https://pro-api.coinmarketcap.com/"

    // Coloque a sua chave de API da CoinMarketCap aqui (ex: "b54bcf4d-1234-4321-...")
    var apiKey = "8d01847b-f4de-4f98-bba5-a65b952da44a"

    fun getInstance(): Retrofit {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-CMC_PRO_API_KEY", apiKey)
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
