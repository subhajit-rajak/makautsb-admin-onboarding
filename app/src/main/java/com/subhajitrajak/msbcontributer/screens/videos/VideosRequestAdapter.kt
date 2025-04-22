package com.subhajitrajak.msbcontributer.screens.videos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.msbcontributer.databinding.ItemUploadRequestsBinding
import com.subhajitrajak.msbcontributer.models.VideosModel

class VideosRequestAdapter(
    private val context: Context,
    private val list: ArrayList<VideosModel>,
    private val onClick: (String) -> Unit,
    private val onAccept: (String) -> Unit,
    private val onReject: (String) -> Unit
) : RecyclerView.Adapter<VideosRequestAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemUploadRequestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: VideosModel) {
            val id = playlist.playlistId!!
            binding.bookName.text = id
            binding.topicName.visibility = View.GONE
            binding.branch.visibility = View.GONE
            binding.semester.visibility = View.GONE
            binding.contributorName.visibility = View.GONE
            binding.contributorEmail.visibility = View.GONE

            binding.root.setOnClickListener {
                onClick(id)
            }
            binding.acceptButton.setOnClickListener {
                onAccept(id)
            }
            binding.rejectButton.setOnClickListener {
                onReject(id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUploadRequestsBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}