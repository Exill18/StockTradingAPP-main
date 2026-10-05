package retrofit

import com.google.gson.annotations.SerializedName

// Response for /v1/cryptocurrency/listings/latest
data class CmcListingsResponse(
    val data: List<CmcCryptoCurrency>?,
    val status: CmcStatus?
)

data class CmcCryptoCurrency(
    val id: Int,
    val name: String,
    val symbol: String,
    val quote: Map<String, CmcQuoteUSD>?
)

data class CmcQuoteUSD(
    val price: Double,
    @SerializedName("percent_change_24h")
    val percentChange24h: Double,
    @SerializedName("market_cap")
    val marketCap: Double?
)

// Response for /v2/cryptocurrency/info
data class CmcInfoResponse(
    val data: Map<String, List<CmcMetadata>>?,
    val status: CmcStatus?
)

data class CmcMetadata(
    val id: Int,
    val name: String,
    val symbol: String,
    val category: String?,
    val description: String?,
    val logo: String?
)

data class CmcStatus(
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("error_message")
    val errorMessage: String?
)

/*Resposta principal da api/news*/
data class CryptoCompareNewsResponse(
    @SerializedName("Type")
    val type: Int?,
    @SerializedName("Message")
    val message: String?,
    @SerializedName("Data")
    val data: List<CryptoCompareNewsArticle>?
)

data class CryptoCompareNewsArticle(
    val id: String?,
    val guid: String?,
    @SerializedName("published_on")
    val publishedOn: Long?,
    val imageurl: String?,
    val title: String?,
    val url: String?,
    val source: String?,
    val body: String?,
    val tags: String?,
    val categories: String?,
    @SerializedName("source_info")
    val sourceInfo: CryptoCompareSourceInfo?
)

data class CryptoCompareSourceInfo(
    val name: String?,
    val img: String?,
    val lang: String?
)