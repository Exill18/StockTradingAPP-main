package com.example.TPSIMobileProjecto.ui.home.stockFragments.simpleCard

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.TPSIMobileProjecto.R
import com.example.TPSIMobileProjecto.ui.home.stockFragments.checklist.ChecklistFragment
import com.example.TPSIMobileProjecto.ui.home.stockFragments.detailedCard.DetailedCardFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit.TickerSummary

class SimpleCardFragment(val watchList: MutableList<TickerSummary>, var isFromWatchlist: Boolean) : Fragment(), SimpleRecyclerAdapter.DetailedViewOnClick {

    private var noDataTV: TextView? = null
    private var emptyStateLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("Lifecycle", "SimpleFragment onViewCreated()")
        noDataTV = view.findViewById(R.id.tvSimpleNoData)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        recyclerView = view.findViewById(R.id.recycleView)

        // Only retrieve from persistent storage on cold start
        if (!isDataLoaded && !isFromWatchlist) {
            retrieveData()
            isDataLoaded = true
        }

        updateEmptyState()

        val button: Button = view.findViewById(R.id.btnChecklist)
        button.text = getString(R.string.edit_watchlist)
        button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.display_fragment, ChecklistFragment(watchList))
                .addToBackStack("SimpleFragment")
                .commit()
        }

        val btnEmptyAdd: View? = view.findViewById(R.id.btnEmptyAdd)
        btnEmptyAdd?.setOnClickListener {
            button.performClick()
        }
    }

    private fun updateEmptyState() {
        if (watchList.isEmpty()) {
            emptyStateLayout?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            emptyStateLayout?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        updateEmptyState()

        val itemAdapter = SimpleRecyclerAdapter(requireContext(), watchList)
        val rv: RecyclerView? = view?.findViewById(R.id.recycleView)
        rv?.let {
            itemAdapter.setDetailedItemClickListener(this)
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = itemAdapter
        }
    }

    override fun onStop() {
        super.onStop()
        saveData()
    }

    private fun saveData() {
        val ctx = context ?: return
        val sharedPreferences = ctx.getSharedPreferences("editList", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()
        val json = gson.toJson(watchList)

        editor.putString("tickerList", json)
        editor.putBoolean("isListInitialized", true)
        editor.apply()
    }

    // Retrieve the data using SharedPreferences and Gson
    private fun retrieveData() {
        val ctx = context ?: return
        val sharedPreferences = ctx.getSharedPreferences("editList", Context.MODE_PRIVATE)
        val isInitialized = sharedPreferences.getBoolean("isListInitialized", false)
        val json = sharedPreferences.getString("tickerList", null)

        if (isInitialized && json != null) {
            val gson = Gson()
            val type = object : TypeToken<List<TickerSummary>>() {}.type
            val savedList: List<TickerSummary> = gson.fromJson(json, type) ?: emptyList()
            watchList.clear()
            watchList.addAll(savedList)
        }
        updateEmptyState()
    }

    override fun onDetailedViewClick(tickerSummary: TickerSummary) {
        Log.e("Tag", "Arrived on SimpleFragment")

        parentFragmentManager.beginTransaction()
            .replace(R.id.display_fragment, DetailedCardFragment(tickerSummary))
            .addToBackStack(null)
            .commit()
    }
}