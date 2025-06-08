package com.subhajitrajak.msbcontributer.screens.books

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.subhajitrajak.msbcontributer.databinding.ItemUploadRequestsBooksBinding
import com.subhajitrajak.msbcontributer.models.BooksModel

class BooksRequestAdapter(
    private val context: Context,
    private val list: ArrayList<BooksModel>,
    private val onClick: (String) -> Unit,
    private val onAccept: (BooksModel) -> Unit,
    private val onReject: (BooksModel) -> Unit
) : RecyclerView.Adapter<BooksRequestAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemUploadRequestsBooksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BooksModel) {
            binding.apply {
                bookName.text = book.bookName
                authorName.text = "Author: " + book.authorName
                contributorName.text = "Contributor: " + book.contributor
                contributorEmail.text = "Email: " + book.contributorEmail

                Glide.with(context).load(book.preview).into(imageView)

                root.setOnClickListener {
                    onClick(book.bookPDF)
                }
                acceptButton.setOnClickListener {
                    onAccept(book)
                }
                rejectButton.setOnClickListener {
                    onReject(book)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUploadRequestsBooksBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}