package com.subhajitrajak.msbcontributer.screens.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.msbcontributer.databinding.FragmentNotesBinding
import com.subhajitrajak.msbcontributer.models.BooksModel
import com.subhajitrajak.msbcontributer.utils.Constants.ACCEPTED
import com.subhajitrajak.msbcontributer.utils.Constants.BOOK_LIST
import com.subhajitrajak.msbcontributer.utils.Constants.HOME
import com.subhajitrajak.msbcontributer.utils.Constants.NOTES_DATA
import com.subhajitrajak.msbcontributer.utils.Constants.PENDING
import com.subhajitrajak.msbcontributer.utils.Constants.REJECTED
import com.subhajitrajak.msbcontributer.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.msbcontributer.utils.getBranchCode

class NotesFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private val list = ArrayList<BooksModel>()
    private lateinit var binding: FragmentNotesBinding
    private lateinit var adapter: NotesRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        val notesRef = database.getReference(UPLOAD_REQUESTS).child(NOTES_DATA)
        notesRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupAdapter() {
        adapter = NotesRequestAdapter(
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
        val bookRef = database.getReference(UPLOAD_REQUESTS).child(NOTES_DATA).child(book.id)

        bookRef.child("status").setValue(REJECTED)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Book rejected", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onAccept(book: BooksModel) {
        val branchCode = getBranchCode(book.branch!!)
        val branchRef = database.getReference(NOTES_DATA).child(HOME)
        val bookRef = branchRef.child(branchCode).child(BOOK_LIST)

        val newBook = mapOf(
            "id" to book.id,
            "semester" to book.semester,
            "bookName" to book.bookName,
            "topicName" to book.topicName,
            "bookPDF" to book.bookPDF,
            "contributor" to book.contributor,
            "contributorEmail" to book.contributorEmail,
            "type" to book.type,
            "branch" to book.branch,
            "status" to ACCEPTED
        )

        bookRef.get().addOnSuccessListener { snapshot ->
            val booksList = snapshot.children.mapNotNull { it.value }.toMutableList()
            booksList.add(newBook)

            val branchData = mapOf(
                "booksList" to booksList,
                "branch" to book.branch
            )

            branchRef.child(branchCode).setValue(branchData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload data", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        // setting accepted status in node upload requests
        val uploadBookRef =
            database.getReference(UPLOAD_REQUESTS).child(NOTES_DATA).child(book.id)
        uploadBookRef.child("status").setValue(ACCEPTED)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Book accepted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}