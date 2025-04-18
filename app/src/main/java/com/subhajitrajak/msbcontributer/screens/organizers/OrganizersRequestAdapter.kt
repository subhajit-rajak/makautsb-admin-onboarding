package com.subhajitrajak.msbcontributer.screens.organizers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.msbcontributer.databinding.ItemUploadRequestsBinding
import com.subhajitrajak.msbcontributer.models.BooksModel

class OrganizersRequestAdapter(
    private val context: Context,
    private val list: ArrayList<BooksModel>,
    private val onClick: (String) -> Unit,
    private val onAccept: (BooksModel) -> Unit,
    private val onReject: (BooksModel) -> Unit
) : RecyclerView.Adapter<OrganizersRequestAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemUploadRequestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BooksModel) {
            binding.bookName.text = book.bookName
            binding.topicName.visibility = View.GONE
            binding.branch.text = book.branch
            binding.semester.text = "Sem - " + book.semester
            binding.contributorName.text = "Contributor: " + book.contributor
            binding.contributorEmail.text = "Email: " + book.contributorEmail

            binding.root.setOnClickListener {
                onClick(book.bookPDF)
            }
            binding.acceptButton.setOnClickListener {
                onAccept(book)
            }
            binding.rejectButton.setOnClickListener {
                onReject(book)
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