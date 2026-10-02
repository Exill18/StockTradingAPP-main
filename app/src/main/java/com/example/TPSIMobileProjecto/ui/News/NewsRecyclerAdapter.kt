package com.example.TPSIMobileProjecto.ui.News

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.TPSIMobileProjecto.R
import com.squareup.picasso.Picasso
import retrofit.News

class NewsRecyclerAdapter(private var stockList: List<News>) : RecyclerView.Adapter<NewsRecyclerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_news_card, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val newsItem = stockList[position]
        holder.date.text = newsItem.date
        holder.description.text = newsItem.description
        holder.title.text = newsItem.title

        holder.description.maxLines = 3
        holder.readMoreButton.text = "Read More"

        holder.description.post {
            val layout = holder.description.layout
            if (layout != null) {
                val lines = layout.lineCount
                if (lines > 0) {
                    val ellipsisCount = layout.getEllipsisCount(lines - 1)
                    if (ellipsisCount > 0) {
                        holder.readMoreButton.visibility = View.VISIBLE
                        holder.readMoreButton.setOnClickListener {
                            if (holder.description.maxLines == 3) {
                                holder.description.maxLines = Int.MAX_VALUE
                                holder.readMoreButton.text = "Read Less"
                            } else {
                                holder.description.maxLines = 3
                                holder.readMoreButton.text = "Read More"
                            }
                        }
                    } else {
                        holder.readMoreButton.visibility = View.GONE
                    }
                }
            }
        }

        // Load and display image using Picasso
        if (!newsItem.image_url.isNullOrEmpty()) {
            Picasso.get().load(newsItem.image_url)
                .placeholder(R.drawable.ic_news_placeholder)
                .error(R.drawable.ic_news_placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_news_placeholder)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val description: TextView = itemView.findViewById(R.id.tvDesc)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val imageView: ImageView = itemView.findViewById(R.id.ivImage)
        val readMoreButton: Button = itemView.findViewById(R.id.btnReadMore)
    }
}
