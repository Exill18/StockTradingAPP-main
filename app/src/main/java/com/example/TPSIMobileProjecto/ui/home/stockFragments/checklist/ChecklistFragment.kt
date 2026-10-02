package com.example.TPSIMobileProjecto.ui.home.stockFragments.checklist

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.TPSIMobileProjecto.R
import com.example.TPSIMobileProjecto.ui.home.stockFragments.simpleCard.SimpleCardFragment
import com.google.gson.Gson
import retrofit.TickerSummary

class ChecklistFragment(val watchList: MutableList<TickerSummary>) : Fragment(), ChecklistRecyclerAdapter.ChecklistItemClickListener {

    private lateinit var itemAdapter: ChecklistRecyclerAdapter
    private lateinit var noDataTV: TextView
    val symbolsSummaryList = mutableListOf<TickerSummary>()
    lateinit var progressBar: ProgressBar
    val viewModel: ChecklistViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("Lifecycle", "CheckListFragment onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        noDataTV = view.findViewById(R.id.tvCheckListNoData)
        noDataTV.visibility = View.GONE

        val button: Button = view.findViewById(R.id.btnSimple)
        val buttonRefresh: Button = view.findViewById(R.id.buttonRefresh)
        button.text = ""
        button.setOnClickListener {
            saveWatchList()
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.display_fragment, SimpleCardFragment(watchList, true))
                    .commit()
            }
        }

        progressBar = view.findViewById(R.id.progressBar)
        showProgressBar()

        viewModel.symbolSummaryList.observe(viewLifecycleOwner) { summaryListFromVM ->
            Log.e("Fetch", summaryListFromVM.toString())
            symbolsSummaryList.clear()
            if (summaryListFromVM != null) {
                symbolsSummaryList.addAll(summaryListFromVM)
            }

            itemAdapter = ChecklistRecyclerAdapter(requireContext(), symbolsSummaryList, watchList)
            val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)
            itemAdapter.setChecklistItemClickListener(this)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = itemAdapter
            hideProgressBar()
            noDataTV.visibility = if (symbolsSummaryList.isEmpty()) View.VISIBLE else View.GONE

            view.findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    filter(p0.toString())
                }
            })
        }

        buttonRefresh.setOnClickListener {
            showProgressBar()
            viewModel.refreshData(true)
        }
    }

    fun filter(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            val filteredTickers = symbolsSummaryList.filter {
                it.symbol.contains(trimmed, ignoreCase = true)
            }
            itemAdapter.filter(filteredTickers)
            noDataTV.visibility = if (filteredTickers.isEmpty()) View.VISIBLE else View.GONE
        } else {
            itemAdapter.filter(symbolsSummaryList)
            noDataTV.visibility = if (symbolsSummaryList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("Lifecycle", "CheckListFragment onStart()")
        viewModel.refreshData(true)
    }

    override fun onStop() {
        super.onStop()
        saveWatchList()
    }

    override fun onAddButtonClick(tickerSummary: TickerSummary) {
        if (!watchList.any { it.symbol == tickerSummary.symbol }) {
            watchList.add(tickerSummary)
            saveWatchList()
        }
    }

    override fun onRemoveButtonClick(tickerSummary: TickerSummary) {
        watchList.removeAll { it.symbol == tickerSummary.symbol }
        saveWatchList()
    }

    private fun saveWatchList() {
        val ctx = context ?: return
        val sharedPreferences = ctx.getSharedPreferences("editList", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(watchList)
        editor.putString("tickerList", json)
        editor.putBoolean("isListInitialized", true)
        editor.apply()
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }
}
