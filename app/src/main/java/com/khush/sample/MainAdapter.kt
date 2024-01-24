package com.khush.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(private val items: List<String>) :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private var itemClickListener: ((String, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    fun setOnItemClickListener(itemClickListener: ((String, Int) -> Unit)) {
        this.itemClickListener = itemClickListener
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tv_name)
        fun bind(item: String, pos: Int) {
            tv.text = item
            itemView.setOnClickListener {
                itemClickListener?.invoke(item, pos)
            }
        }
    }
}