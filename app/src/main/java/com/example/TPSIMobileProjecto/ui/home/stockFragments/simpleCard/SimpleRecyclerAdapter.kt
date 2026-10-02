package com.example.TPSIMobileProjecto.ui.home.stockFragments.simpleCard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.TPSIMobileProjecto.R
import com.squareup.picasso.Picasso
import retrofit.TickerSummary
import java.util.Locale

class SimpleRecyclerAdapter(private val context: Context, private var stockList: List<TickerSummary>) : RecyclerView.Adapter<SimpleRecyclerAdapter.MyViewHolder>() {
    private var clickListener: DetailedViewOnClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_simple_card, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = stockList[position]
        holder.price.text = String.format(Locale.US, "$%.2f", currentItem.current_price)
        holder.symbol.text = currentItem.symbol

        val isNegative = currentItem.change_percent < 0
        val isZero = currentItem.change_percent == 0.0

        val formattedPercent = if (currentItem.change_percent > 0) {
            String.format(Locale.US, "+%.2f%%", currentItem.change_percent)
        } else {
            String.format(Locale.US, "%.2f%%", currentItem.change_percent)
        }
        holder.percentage.text = formattedPercent

        val (textColorRes, bgRes, arrowSymbol) = when {
            isNegative -> Triple(R.color.stock_red_dark, R.drawable.bg_badge_negative, "▼")
            isZero -> Triple(R.color.text_secondary, R.drawable.bg_badge_neutral, "•")
            else -> Triple(R.color.stock_green_dark, R.drawable.bg_badge_positive, "▲")
        }

        val textColor = ContextCompat.getColor(context, textColorRes)
        holder.percentage.setTextColor(textColor)
        holder.percgraph.setTextColor(textColor)
        holder.percgraph.text = arrowSymbol
        holder.percgraph.rotation = 0f
        holder.badgeLayout?.setBackgroundResource(bgRes)

        // Load and display image using Picasso
        if (!currentItem.logo_url.isNullOrEmpty()) {
            Picasso.get()
                .load(currentItem.logo_url)
                .placeholder(R.drawable.ic_stock_placeholder)
                .error(R.drawable.ic_stock_placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_stock_placeholder)
        }

        holder.itemView.setOnClickListener {
            Log.e("Tag", "Clicked item $currentItem")
            clickListener?.onDetailedViewClick(currentItem)
        }
    }

    // This class defines the ViewHolder object for each item in the RecyclerView
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val symbol: TextView = itemView.findViewById(R.id.tvSymbol)
        val percentage : TextView = itemView.findViewById(R.id.tvPercentage)
        val percgraph   : TextView = itemView.findViewById(R.id.tvPercentageGraph)
        val badgeLayout : View? = itemView.findViewById(R.id.badgePercentLayout)
    }

    fun setDetailedItemClickListener(listener: DetailedViewOnClick) {
        clickListener = listener
    }

    interface DetailedViewOnClick {
        fun onDetailedViewClick(tickerSummary: TickerSummary)
    }
}