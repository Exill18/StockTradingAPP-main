package com.example.TPSIMobileProjecto

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import retrofit.ChartData
import retrofit.CmcCryptoCurrency
import retrofit.News
import retrofit.RetrofitHelper
import retrofit.TickerDetails
import retrofit.TickerDetails_details
import retrofit.TickerSummary
import retrofit.retrofitInterface
import kotlin.math.round

class BackendData {
    private val tickersAPI = RetrofitHelper.getInstance().create(retrofitInterface::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    var tickersListDeferred: Deferred<List<String>>? = null

    // Cache local dos resultados da API durante a sessão
    private var cachedListings: List<CmcCryptoCurrency>? = null

    init {
        getSymbols()
    }

    // Método para obter a lista de símbolos
    fun getSymbols() {
        try {
            tickersListDeferred = coroutineScope.async {
                fetchListingsInternal().map { it.symbol }
            }
        } catch (e: Exception) {
            Log.e("BackendData", "Erro ao obter símbolos", e)
        }
    }

    private suspend fun fetchListingsInternal(): List<CmcCryptoCurrency> {
        if (cachedListings != null && cachedListings!!.isNotEmpty()) {
            return cachedListings!!
        }
        return try {
            val response = tickersAPI.getListings(limit = 50)
            if (response.isSuccessful) {
                val listings = response.body()?.data ?: emptyList()
                cachedListings = listings
                listings
            } else {
                Log.e("BackendData", "Falha no pedido getListings: ${response.code()} ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("BackendData", "Exceção ao obter listings da CoinMarketCap", e)
            emptyList()
        }
    }

    // Método assíncrono para aguardar a lista de símbolos
    suspend fun getTickersList(): List<String> {
        return tickersListDeferred?.await() ?: emptyList()
    }

    suspend fun fetchNews(): List<News> {
        return try {
            //Faz o pedido à API de News através do retrofit
            val response = tickersAPI.getNews(limit = 15)

            if (response.isSuccessful) {
                //Extrai a lista de artigos
                val articles = response.body()?.data ?: emptyList()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

                //Mapeia cada NewsArticle para o modelo News
                articles.map { article ->
                    val formattedDate = article.publishedOn?.let { timestamp ->
                        dateFormat.format(java.util.Date(timestamp * 1000))
                    } ?: "Recente"

                    News(
                        title = article.title ?: "Sem título",
                        description = article.body ?: "",
                        date = formattedDate,
                        image_url = article.imageurl ?: ""
                    )
                }
            } else {
                Log.e("BackendData", "Erro na API de notícias: ${response.code()} ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("BackendData", "Exceção ao obter notícias", e)
            emptyList()
        }
    }

    suspend fun fetchTickerDetailsList(): List<TickerDetails> {
        val listings = fetchListingsInternal()
        if (listings.isEmpty()) {
            Log.e("BackendData", "Lista de moedas vazia ao obter detalhes")
            return emptyList()
        }

        val detailsList = mutableListOf<TickerDetails>()

        // Obter símbolos separados por vírgula para consultar metadados
        val symbolsParam = listings.take(20).joinToString(",") { it.symbol }
        val metadataMap = try {
            val response = tickersAPI.getMetadata(symbol = symbolsParam)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyMap()
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("BackendData", "Exceção ao obter metadados da CoinMarketCap", e)
            emptyMap()
        }

        for (crypto in listings) {
            val quoteUsd = crypto.quote?.get("USD")
            val price = quoteUsd?.price ?: 0.0
            val percentChange = quoteUsd?.percentChange24h ?: 0.0

            val roundedPrice = (round(price * 100) / 100.0)
            val roundedPercentChange = (round(percentChange * 100) / 100.0)

            val metadataList = metadataMap[crypto.symbol]
            val metadata = metadataList?.firstOrNull()

            val logoUrl = metadata?.logo
                ?: "https://s2.coinmarketcap.com/static/img/coins/64x64/${crypto.id}.png"
            val category = metadata?.category?.replaceFirstChar { it.uppercase() } ?: "Criptomoeda"
            val description = metadata?.description ?: "${crypto.name} (${crypto.symbol}) disponível no mercado global de criptomoedas."

            // Gerar dados de gráfico baseados no preço atual
            val chartPoints = listOf(0.95, 0.97, 0.96, 0.98, 1.01, 0.99, 1.02, 1.00).map { (it * roundedPrice) }

            detailsList.add(
                TickerDetails(
                    symbol = crypto.symbol,
                    sector = category,
                    description = description,
                    CEO = "Descentralizado",
                    logo_url = logoUrl,
                    details = TickerDetails_details(
                        current_price = roundedPrice,
                        change_percent = roundedPercentChange
                    ),
                    chart_data = ChartData(October_2022 = chartPoints)
                )
            )
        }

        return detailsList
    }

    suspend fun fetchTickerSummary(): List<TickerSummary> {
        val listings = fetchListingsInternal()
        if (listings.isEmpty()) {
            Log.e("BackendData", "Falha ao obter lista de moedas para o resumo")
            return emptyList()
        }

        return listings.map { crypto ->
            val quoteUsd = crypto.quote?.get("USD")
            val price = quoteUsd?.price ?: 0.0
            val percentChange = quoteUsd?.percentChange24h ?: 0.0

            val roundedPrice = (round(price * 100) / 100.0)
            val roundedPercentChange = (round(percentChange * 100) / 100.0)

            val logoUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/${crypto.id}.png"

            TickerSummary(
                symbol = crypto.symbol,
                current_price = roundedPrice,
                change_percent = roundedPercentChange,
                logo_url = logoUrl
            )
        }
    }
}
