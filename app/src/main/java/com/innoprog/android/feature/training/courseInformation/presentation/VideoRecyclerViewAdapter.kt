package com.innoprog.android.feature.training.courseInformation.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.innoprog.android.R
import com.innoprog.android.databinding.ItemTrainingVideoBinding
import com.innoprog.android.feature.training.courseInformation.domain.model.CourseInformationVideoModel

class VideoAdapter(
    private val context: Context,
    private val onVideoClickListener: (String) -> Unit
) : Adapter<VideoViewHolder>() {

    var items = listOf<CourseInformationVideoModel>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return VideoViewHolder(
            ItemTrainingVideoBinding.inflate(layoutInflater, parent, false), context, onVideoClickListener
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class VideoViewHolder(
    private val binding: ItemTrainingVideoBinding,
    private val context: Context,
    private val onVideoClickListener: (String) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: CourseInformationVideoModel) {
        binding.videoPlaceholderIV.setOnClickListener {
            onVideoClickListener.invoke(item.videoURL)
        }
        binding.videoDescriptionTV.text = item.videoDescription

        Glide.with(context)
            .load(item.videoURL)
            .fitCenter()
            .placeholder(R.drawable.video_sample)
            .into(binding.videoPlaceholderIV)
    }
}