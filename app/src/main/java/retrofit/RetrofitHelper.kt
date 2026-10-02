package retrofit

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    val baseUrl = "http://tuxdroid.pythonanywhere.com/"

    private val fallbackInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = null
        var shouldFallback = false

        try {
            response = chain.proceed(request)
            if (!response.isSuccessful) {
                shouldFallback = true
            }
        } catch (e: Exception) {
            shouldFallback = true
        }

        if (shouldFallback) {
            response?.close()
            val path = request.url().encodedPath()
            val jsonResponse = getMockResponseForPath(path)
            val mediaType = MediaType.parse("application/json")
            val body = ResponseBody.create(mediaType, jsonResponse)

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build()
        } else {
            response!!
        }
    }

    private fun getMockResponseForPath(path: String): String {
        return when {
            path.endsWith("/api/symbols") -> {
                """["AAPL", "GOOGL", "MSFT", "NVDA", "NFLX", "INTC", "TSLA", "AMZN"]"""
            }
            path.contains("/api/symbol/summary/") -> {
                val symbol = path.substringAfterLast("/api/symbol/summary/").trimEnd('/')
                getMockSummaryJson(symbol)
            }
            path.contains("/api/symbol/details/") -> {
                val symbol = path.substringAfterLast("/api/symbol/details/").trimEnd('/')
                getMockDetailsJson(symbol)
            }
            path.endsWith("/api/news") -> {
                getMockNewsJson()
            }
            else -> "{}"
        }
    }

    private fun getMockSummaryJson(symbol: String): String {
        val (price, change, logo) = when (symbol.uppercase()) {
            "NVDA" -> Triple(150.10, 0.30, "https://logo.clearbit.com/nvidia.com")
            "AAPL" -> Triple(150.30, 0.50, "https://logo.clearbit.com/apple.com")
            "NFLX" -> Triple(150.90, 0.00, "https://logo.clearbit.com/netflix.com")
            "GOOGL" -> Triple(150.00, 0.20, "https://logo.clearbit.com/google.com")
            "INTC" -> Triple(150.90, -0.10, "https://logo.clearbit.com/intel.com")
            "MSFT" -> Triple(150.70, 0.90, "https://logo.clearbit.com/microsoft.com")
            "TSLA" -> Triple(150.70, 0.90, "https://logo.clearbit.com/tesla.com")
            "AMZN" -> Triple(150.40, 0.50, "https://logo.clearbit.com/amazon.com")
            else -> Triple(100.00, 0.00, "")
        }
        return """
            {
                "symbol": "$symbol",
                "current_price": $price,
                "change_percent": $change,
                "logo_url": "$logo"
            }
        """.trimIndent()
    }

    private fun getMockDetailsJson(symbol: String): String {
        val (ceo, sector, desc, price, change, logo) = when (symbol.uppercase()) {
            "NVDA" -> Tuple6("Jensen Huang", "Semiconductors", "Nvidia Corporation is an American multinational technology company known for designing graphics processing units (GPUs).", 150.10, 0.30, "https://logo.clearbit.com/nvidia.com")
            "AAPL" -> Tuple6("Tim Cook", "Technology", "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories.", 150.30, 0.50, "https://logo.clearbit.com/apple.com")
            "NFLX" -> Tuple6("Ted Sarandos", "Entertainment", "Netflix, Inc. provides subscription-based streaming entertainment services with films and television series.", 150.90, 0.00, "https://logo.clearbit.com/netflix.com")
            "GOOGL" -> Tuple6("Sundar Pichai", "Communication Services", "Alphabet Inc. offers products and platforms including Search, YouTube, Android, Google Cloud, and hardware.", 150.00, 0.20, "https://logo.clearbit.com/google.com")
            "INTC" -> Tuple6("Pat Gelsinger", "Semiconductors", "Intel Corporation designs and manufactures microprocessors and semiconductor chips for global markets.", 150.90, -0.10, "https://logo.clearbit.com/intel.com")
            "MSFT" -> Tuple6("Satya Nadella", "Technology", "Microsoft Corporation develops and supports personal software, cloud computing services, and enterprise solutions.", 150.70, 0.90, "https://logo.clearbit.com/microsoft.com")
            "TSLA" -> Tuple6("Elon Musk", "Automotive", "Tesla, Inc. designs, develops, manufactures, sells, and leases electric vehicles and energy storage systems.", 150.70, 0.90, "https://logo.clearbit.com/tesla.com")
            "AMZN" -> Tuple6("Andy Jassy", "Consumer Cyclical", "Amazon.com, Inc. focuses on retail, cloud services (AWS), online advertising, and digital streaming.", 150.40, 0.50, "https://logo.clearbit.com/amazon.com")
            else -> Tuple6("N/A", "General", "Company overview information.", 100.00, 0.00, "")
        }

        val chartData = "[100.0, 101.2, 102.5, 104.0, 100.0, 101.5, 103.0, 105.2, 107.0, 110.0, 113.5, 100.0, 102.0, 106.0, 109.0, 113.0, 117.0, 121.5, 100.0, 103.5, 107.0, 111.0, 115.0, 119.0, 122.0, 125.0, 130.0, 135.0, 142.0, 148.0, $price]"

        return """
            {
                "symbol": "$symbol",
                "sector": "$sector",
                "CEO": "$ceo",
                "description": "$desc",
                "logo_url": "$logo",
                "details": {
                    "current_price": $price,
                    "change_percent": $change
                },
                "chart_data": {
                    "October_2022": $chartData
                }
            }
        """.trimIndent()
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
    )

    private fun getMockNewsJson(): String {
        return """
            [
                {
                    "title": "Wall Street closes strong with record weekly momentum",
                    "date": "2026-10-02",
                    "description": "Major indices closed higher this week following solid earnings and economic data that lifted market sentiment across tech and energy sectors.",
                    "image_url": "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800&q=80"
                },
                {
                    "title": "Tech giants announce new artificial intelligence breakthroughs",
                    "date": "2026-10-01",
                    "description": "Leading technology companies continue to invest heavily in cloud infrastructure and next-generation AI hardware, driving semiconductor rally.",
                    "image_url": "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&q=80"
                },
                {
                    "title": "Global markets respond positively to central bank statements",
                    "date": "2026-09-30",
                    "description": "Investors expressed optimism as macroeconomic indicators pointed towards stabilization and continued consumer spending resilience.",
                    "image_url": "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=800&q=80"
                }
            ]
        """.trimIndent()
    }

    fun getInstance(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(fallbackInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
