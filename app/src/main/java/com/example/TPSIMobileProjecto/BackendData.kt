package com.example.TPSIMobileProjecto

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import retrofit.News
import retrofit.RetrofitHelper
import retrofit.TickerDetails
import retrofit.TickerSummary
import retrofit.retrofitInterface
import retrofit2.Response

class BackendData {
    private val tickersAPI = RetrofitHelper.getInstance().create(retrofitInterface::class.java)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var tickersListDeferred: Deferred<List<String>>? = null

    init {
        getSymbols()
    }

    // Function to get all symbols
    fun getSymbols() {
        tickersListDeferred = coroutineScope.async {
            try {
                val symbolsResponse = tickersAPI.getSymbols()
                if (symbolsResponse.isSuccessful) {
                    symbolsResponse.body() ?: emptyList()
                } else {
                    listOf("AAPL", "GOOGL", "MSFT", "NVDA", "NFLX", "INTC", "TSLA", "AMZN")
                }
            } catch (e: Exception) {
                Log.e("BackendData", "Error fetching symbols: ${e.message}")
                listOf("AAPL", "GOOGL", "MSFT", "NVDA", "NFLX", "INTC", "TSLA", "AMZN")
            }
        }
    }

    // Function to call async and await all symbols to pass onto other functions
    suspend fun getTickersList(): List<String> {
        return try {
            tickersListDeferred?.await() ?: listOf("AAPL", "GOOGL", "MSFT", "NVDA", "NFLX", "INTC", "TSLA", "AMZN")
        } catch (e: Exception) {
            Log.e("BackendData", "Error awaiting tickers: ${e.message}")
            listOf("AAPL", "GOOGL", "MSFT", "NVDA", "NFLX", "INTC", "TSLA", "AMZN")
        }
    }

    suspend fun fetchNews(): List<News> {
        return try {
            val newsResponse: Response<List<News>> = tickersAPI.getNews()
            if (newsResponse.isSuccessful) {
                newsResponse.body() ?: emptyList()
            } else {
                Log.e("BackendData", "Failed to get news: ${newsResponse.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("BackendData", "Exception fetching news: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchTickerDetailsList(): List<TickerDetails> {
        return try {
            val tickersList = getTickersList()
            if (tickersList.isEmpty()) {
                Log.e("BackendData", "No tickers available to fetch details")
                return emptyList()
            }

            val symbolDetailsList = mutableListOf<TickerDetails>()
            for (ticker in tickersList) {
                try {
                    val responseSymbolDetail: Response<TickerDetails> = tickersAPI.getSymbolDetails(ticker)
                    if (responseSymbolDetail.isSuccessful) {
                        val tickerDetails = responseSymbolDetail.body()
                        if (tickerDetails != null) {
                            symbolDetailsList.add(tickerDetails)
                        }
                    } else {
                        Log.e("BackendData", "Failed to fetch details for ticker: $ticker")
                    }
                } catch (e: Exception) {
                    Log.e("BackendData", "Exception fetching details for $ticker: ${e.message}")
                }
            }
            symbolDetailsList
        } catch (e: Exception) {
            Log.e("BackendData", "Error in fetchTickerDetailsList: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchTickerSummary(): List<TickerSummary> {
        return try {
            val symbolSummaryList = mutableListOf<TickerSummary>()
            val tickersList = getTickersList()

            if (tickersList.isEmpty()) {
                Log.e("BackendData", "No tickers available to fetch summary")
                return emptyList()
            }

            for (ticker in tickersList) {
                try {
                    val responseSymbolSummary: Response<TickerSummary> = tickersAPI.getSymbolSummary(ticker)
                    if (responseSymbolSummary.isSuccessful) {
                        val tickerSummary = responseSymbolSummary.body()
                        if (tickerSummary != null) {
                            symbolSummaryList.add(tickerSummary)
                        }
                    } else {
                        Log.e("BackendData", "Failed to fetch summary for ticker: $ticker")
                    }
                } catch (e: Exception) {
                    Log.e("BackendData", "Exception fetching summary for $ticker: ${e.message}")
                }
            }
            symbolSummaryList
        } catch (e: Exception) {
            Log.e("BackendData", "Error in fetchTickerSummary: ${e.message}")
            emptyList()
        }
    }
}
