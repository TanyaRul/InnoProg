package com.innoprog.android.uikit_sample

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.application.uikit_sample.R

class SampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val name: TextView = itemView.findViewById<TextView>(R.id.view_name)

    fun bind(item: ViewSample) {
        name.text = item.name
    }

}