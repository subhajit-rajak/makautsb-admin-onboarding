package com.subhajitrajak.msbcontributer.screens.books

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.subhajitrajak.msbcontributer.databinding.FragmentBooksBinding
import com.subhajitrajak.msbcontributer.models.BooksModel
import com.subhajitrajak.msbcontributer.utils.Constants.ACCEPTED
import com.subhajitrajak.msbcontributer.utils.Constants.BOOKS_DATA
import com.subhajitrajak.msbcontributer.utils.Constants.PENDING
import com.subhajitrajak.msbcontributer.utils.Constants.REJECTED
import com.subhajitrajak.msbcontributer.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.msbcontributer.utils.removeWithAnim
import com.subhajitrajak.msbcontributer.utils.showToast
import com.subhajitrajak.msbcontributer.utils.showWithAnim

class BooksFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private val list = ArrayList<BooksModel>()
    private lateinit var binding: FragmentBooksBinding
    private lateinit var adapter: BooksRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBooksBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        val booksRef = database.getReference(UPLOAD_REQUESTS).child(BOOKS_DATA)
        booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (data in snapshot.children) {
                    val book = data.getValue(BooksModel::class.java)
                    book?.let {
                        if (it.status == PENDING) {
                            list.add(it)
                        }
                    }
                }
                if (isAdded) {
                    setupAdapter()
                }

                if (list.isEmpty()) {
                    binding.mErrorHolder.showWithAnim()
                    binding.rvNotes.removeWithAnim()
                } else {
                    binding.mErrorHolder.removeWithAnim()
                    binding.rvNotes.showWithAnim()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupAdapter() {
        adapter = BooksRequestAdapter(
            requireContext(),
            list,
            onClick = { url->
                openPdfFile(fileUrl = url)
            },
            onAccept = { book ->
                onAccept(book)
                adapter.notifyDataSetChanged()
            },
            onReject = { book ->
                onReject(book)
                adapter.notifyDataSetChanged()
            }
        )
        binding.rvNotes.adapter = adapter
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
    }


    private fun openPdfFile(fileUrl: String) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(fileUrl)
        )
        startActivity(browserIntent)
    }

    private fun onReject(book: BooksModel) {
        val bookRef = database.getReference(UPLOAD_REQUESTS).child(BOOKS_DATA).child(book.id)

        val bookName = book.bookName
        val authorName = book.authorName

        bookRef.child("status").setValue(REJECTED)
            .addOnSuccessListener {
                val pdfFileName = if (authorName != null) {
                    "$bookName-$authorName.pdf"
                } else {
                    "$bookName.pdf"
                }
                val fileRef = FirebaseStorage.getInstance().reference.child(pdfFileName)
                fileRef.delete()
                    .addOnSuccessListener {
                        showToast(requireContext(), "Storage: Delete successful")
                    }
                    .addOnFailureListener { exception ->
                        showToast(requireContext(), "Error: ${exception.message}")
                    }
                showToast(requireContext(), "Book rejected")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Error: ${it.message}")
            }
    }

    private fun onAccept(book: BooksModel) {
        val bookDataRef = database.getReference(BOOKS_DATA)
        val bookRef = bookDataRef.child(book.id)

        val newBook = mapOf(
            "id" to book.id,
            "bookName" to book.bookName,
            "authorName" to book.authorName,
            "preview" to book.preview,
            "bookPDF" to book.bookPDF,
            "contributor" to book.contributor,
            "contributorEmail" to book.contributorEmail,
            "type" to book.type,
            "status" to ACCEPTED
        )

        bookRef.setValue(newBook)
            .addOnSuccessListener {
                showToast(requireContext(), "Upload successful")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Failed to upload data")
            }

        // setting accepted status in node upload requests
        val uploadBookRef = database.getReference(UPLOAD_REQUESTS).child(BOOKS_DATA).child(book.id)
        uploadBookRef.child("status").setValue(ACCEPTED)
            .addOnSuccessListener {
                showToast(requireContext(), "Book accepted")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Error: ${it.message}")
            }
    }
}