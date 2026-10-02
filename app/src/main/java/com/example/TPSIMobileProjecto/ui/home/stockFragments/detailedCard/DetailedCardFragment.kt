package com.example.TPSIMobileProjecto.ui.home.stockFragments.detailedCard

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.example.TPSIMobileProjecto.R
import com.example.TPSIMobileProjecto.ui.home.stockFragments.simpleCard.SimpleCardFragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.squareup.picasso.Picasso
import retrofit.TickerDetails
import retrofit.TickerSummary
import java.util.Locale

class DetailedCardFragment(val chosenTicker : TickerSummary) : Fragment() {
    lateinit var progressBar: ProgressBar
    lateinit var noDataFoundtv: TextView
    private var tickerDetailedList: MutableList<TickerDetails> = mutableListOf()
    lateinit var lineGraphView: GraphView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detailed_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("Lifecycle", "DetailedFragment onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(this).get(DetailedCardViewModel::class.java)

        noDataFoundtv = view.findViewById(R.id.tvDetailedNotFound)
        noDataFoundtv.visibility = View.GONE

        progressBar = view.findViewById(R.id.progressBarDetailed)
        showProgressBar()

        val button: Button = view.findViewById(R.id.btnHome)
        button.text = ""
        button.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.display_fragment, SimpleCardFragment(mutableListOf(), false))
                    .commit()
            }
        }

        viewModel.symbolDetailsList.observe(viewLifecycleOwner) { symbolDetails ->
            tickerDetailedList.clear()
            tickerDetailedList.addAll(symbolDetails)
            val ticker = tickerDetailedList.find { it.symbol == chosenTicker.symbol }
            Log.e("tag", "Ticker detailed = ${ticker.toString()}")

            val price: TextView = view.findViewById(R.id.tvPriceDetailed)
            val symbol: TextView = view.findViewById(R.id.tvSymbolDetailed)
            val percentage: TextView = view.findViewById(R.id.tvPercentDetailed)
            val sector: TextView = view.findViewById(R.id.tvSectorDetailed)
            val imageView: ImageView = view.findViewById(R.id.imageviewDetailed)
            val statSector: TextView? = view.findViewById(R.id.tvStatSector)
            val statCEO: TextView? = view.findViewById(R.id.tvStatCEO)
            val companyDesc: TextView? = view.findViewById(R.id.tvCompanyDescription)
            val badgeLayout: View? = view.findViewById(R.id.badgePercentDetailed)
            val arrowView: TextView? = view.findViewById(R.id.tvPercentArrow)
            val aboutTitle: TextView? = view.findViewById(R.id.tvAboutTitle)

            lineGraphView = view.findViewById(R.id.idGraphView)
            lineGraphView.visibility = View.GONE

            if (ticker != null) {
                populateGraph(ticker, lineGraphView)
                sector.text = ticker.sector
                statSector?.text = ticker.sector
                statCEO?.text = ticker.CEO.ifEmpty { "N/A" }
                companyDesc?.text = ticker.description.ifEmpty { "No company description available." }
                aboutTitle?.text = "About ${ticker.symbol}"

                price.text = String.format(Locale.US, "$%.2f", ticker.details.current_price)
                symbol.text = ticker.symbol

                val isNegative = ticker.details.change_percent < 0
                val isZero = ticker.details.change_percent == 0.0

                val formattedPercent = if (ticker.details.change_percent > 0) {
                    String.format(Locale.US, "+%.2f%%", ticker.details.change_percent)
                } else {
                    String.format(Locale.US, "%.2f%%", ticker.details.change_percent)
                }
                percentage.text = formattedPercent

                val (colorRes, bgRes, arrow) = when {
                    isNegative -> Triple(R.color.stock_red_dark, R.drawable.bg_badge_negative, "▼")
                    isZero -> Triple(R.color.text_secondary, R.drawable.bg_badge_neutral, "•")
                    else -> Triple(R.color.stock_green_dark, R.drawable.bg_badge_positive, "▲")
                }
                val textColor = ContextCompat.getColor(requireContext(), colorRes)
                percentage.setTextColor(textColor)
                arrowView?.setTextColor(textColor)
                arrowView?.text = arrow
                badgeLayout?.setBackgroundResource(bgRes)

                if (!ticker.logo_url.isNullOrEmpty()) {
                    Picasso.get()
                        .load(ticker.logo_url)
                        .placeholder(R.drawable.ic_stock_placeholder)
                        .error(R.drawable.ic_stock_placeholder)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_stock_placeholder)
                }
            } else {
                noDataFoundtv.visibility = View.VISIBLE
            }
            hideProgressBar()
        }
    }

    fun populateGraph(ticker: TickerDetails, graph: GraphView) {
        val chartDataList = ticker.chart_data.October_2022
        if (chartDataList.isEmpty()) {
            graph.visibility = View.GONE
            return
        }

        val graphList = mutableListOf<DataPoint>()
        for ((index, value) in chartDataList.withIndex()) {
            graphList.add(DataPoint(index.toDouble(), value))
        }

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(graphList.toTypedArray())

        val isNegative = ticker.details.change_percent < 0
        val lineColorRes = if (isNegative) R.color.stock_red else R.color.stock_green
        val resolvedLineColor = ContextCompat.getColor(requireContext(), lineColorRes)

        series.color = resolvedLineColor
        series.thickness = 6
        series.isDrawBackground = true
        val fillColor = if (isNegative) {
            Color.argb(35, 239, 68, 68)
        } else {
            Color.argb(35, 16, 185, 129)
        }
        series.backgroundColor = fillColor

        // Style the viewport and grid
        graph.viewport.setXAxisBoundsManual(true)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(graphList.size.toDouble() - 1)

        graph.viewport.isScrollable = true
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)
        graph.viewport.setScrollableY(true)

        val strokeColor = ContextCompat.getColor(requireContext(), R.color.card_stroke)
        val textTertiaryColor = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
        graph.gridLabelRenderer.gridColor = strokeColor
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.gridLabelRenderer.horizontalLabelsColor = textTertiaryColor
        graph.gridLabelRenderer.verticalLabelsColor = textTertiaryColor
        graph.gridLabelRenderer.textSize = 28f
        graph.gridLabelRenderer.numHorizontalLabels = 5
        graph.gridLabelRenderer.numVerticalLabels = 4
        graph.gridLabelRenderer.reloadStyles()

        graph.removeAllSeries()
        graph.addSeries(series)
        graph.visibility = View.VISIBLE
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }
}
