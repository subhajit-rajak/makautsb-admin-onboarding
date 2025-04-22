package com.subhajitrajak.msbcontributer.screens.organizers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.subhajitrajak.msbcontributer.databinding.FragmentOrganizersBinding
import com.subhajitrajak.msbcontributer.models.BooksModel
import com.subhajitrajak.msbcontributer.utils.Constants.ACCEPTED
import com.subhajitrajak.msbcontributer.utils.Constants.APP_DATA
import com.subhajitrajak.msbcontributer.utils.Constants.BOOK_LIST
import com.subhajitrajak.msbcontributer.utils.Constants.HOME
import com.subhajitrajak.msbcontributer.utils.Constants.ORGANIZERS_DATA
import com.subhajitrajak.msbcontributer.utils.Constants.PENDING
import com.subhajitrajak.msbcontributer.utils.Constants.REJECTED
import com.subhajitrajak.msbcontributer.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.msbcontributer.utils.getBranchCode
import com.subhajitrajak.msbcontributer.utils.showToast

class OrganizersFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private val list = ArrayList<BooksModel>()
    private lateinit var binding: FragmentOrganizersBinding
    private lateinit var adapter: OrganizersRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrganizersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        val notesRef = database.getReference(UPLOAD_REQUESTS).child(ORGANIZERS_DATA)
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
        adapter = OrganizersRequestAdapter(
            requireContext(),
            list,
            onClick = { url ->
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
        binding.rvOrganizers.adapter = adapter
        binding.rvOrganizers.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun onReject(book: BooksModel) {
        val bookRef = database.getReference(UPLOAD_REQUESTS).child(ORGANIZERS_DATA).child(book.id)

        val bookName = book.bookName

        bookRef.child("status").setValue(REJECTED)
            .addOnSuccessListener {
                val pdfFileName = "$bookName.pdf"
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
        val branchCode = getBranchCode(book.branch!!)
        val branchRef = database.getReference(APP_DATA).child(HOME)
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
                    showToast(requireContext(), "Upload successful")
                }
                .addOnFailureListener {
                    showToast(requireContext(), "Failed to upload data")
                }
        }

        // setting accepted status in node upload requests
        val uploadBookRef =
            database.getReference(UPLOAD_REQUESTS).child(ORGANIZERS_DATA).child(book.id)
        uploadBookRef.child("status").setValue(ACCEPTED)
            .addOnSuccessListener {
                showToast(requireContext(), "Book accepted")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Error: ${it.message}")
            }
    }


    private fun openPdfFile(fileUrl: String) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(fileUrl)
        )
        startActivity(browserIntent)
    }
}