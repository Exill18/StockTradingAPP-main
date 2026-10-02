package com.example.TPSIMobileProjecto.ui.home.stockFragments.checklist

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.TPSIMobileProjecto.R
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import retrofit.TickerSummary
import java.util.Locale

class ChecklistRecyclerAdapter(
    private val context: Context,
    private var stockList: List<TickerSummary>,
    private var addedItems: List<TickerSummary>
) : RecyclerView.Adapter<ChecklistRecyclerAdapter.MyViewHolder>() {

    private var clickListener: ChecklistItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_checklist_card, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = stockList[position]
        holder.price.text = String.format(Locale.US, "$%.2f", item.current_price)
        holder.symbol.text = item.symbol

        val formattedPercent = if (item.change_percent > 0) {
            String.format(Locale.US, "+%.2f%%", item.change_percent)
        } else {
            String.format(Locale.US, "%.2f%%", item.change_percent)
        }
        holder.percentage.text = formattedPercent

        val colorRes = if (item.change_percent < 0) R.color.stock_red_dark else R.color.stock_green_dark
        holder.percentage.setTextColor(ContextCompat.getColor(context, colorRes))

        if (!item.logo_url.isNullOrEmpty()) {
            Picasso.get()
                .load(item.logo_url)
                .placeholder(R.drawable.ic_stock_placeholder)
                .error(R.drawable.ic_stock_placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_stock_placeholder)
        }

        val isInWatchList = addedItems.any { it.symbol == item.symbol }
        updateButtonState(holder.addButton, isInWatchList)

        holder.addButton.setOnClickListener {
            val isNowInWatchList = addedItems.any { it.symbol == item.symbol }
            if (isNowInWatchList) {
                clickListener?.onRemoveButtonClick(item)
                updateButtonState(holder.addButton, false)
            } else {
                clickListener?.onAddButtonClick(item)
                updateButtonState(holder.addButton, true)
            }
        }
    }

    private fun updateButtonState(button: Button, isInWatchList: Boolean) {
        if (button is MaterialButton) {
            if (isInWatchList) {
                button.text = context.getString(R.string.remove)
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.stock_red))
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                button.text = context.getString(R.string.add)
                button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary))
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        } else {
            if (isInWatchList) {
                button.text = context.getString(R.string.remove)
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.stock_red))
            } else {
                button.text = context.getString(R.string.add)
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val price: TextView = itemView.findViewById(R.id.tvPriceChecklist)
        val symbol: TextView = itemView.findViewById(R.id.tvSymbolChecklist)
        val percentage: TextView = itemView.findViewById(R.id.tvPercentageChecklist)
        val addButton: Button = itemView.findViewById(R.id.btnAddChecklist)
        val imageView: ImageView = itemView.findViewById(R.id.imgViewLogoChecklist)
    }

    fun setChecklistItemClickListener(listener: ChecklistItemClickListener) {
        clickListener = listener
    }

    interface ChecklistItemClickListener {
        fun onAddButtonClick(tickerSummary: TickerSummary)
        fun onRemoveButtonClick(tickerSummary: TickerSummary)
    }

    fun filter(filteredList: List<TickerSummary>) {
        this.stockList = filteredList
        notifyDataSetChanged()
    }
}
