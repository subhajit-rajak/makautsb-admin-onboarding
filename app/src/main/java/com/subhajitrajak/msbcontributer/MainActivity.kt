package com.subhajitrajak.msbcontributer

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.subhajitrajak.msbcontributer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var selectedPdfUri: Uri
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var progressDialog: ProgressDialog
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        database = firebaseDatabase.getReference("AppData").child("Home")
        storage = FirebaseStorage.getInstance().reference

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading")
        progressDialog.setMessage("Please wait while the PDF is being uploaded.")
        progressDialog.setCancelable(false)

        val branchList = arrayOf("CSE", "IT", "ECE", "ME", "CE", "EE")
        val adapter= ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, branchList)
        val autoCompleteTextView = binding.listOfBranches
        autoCompleteTextView.setAdapter(adapter)

        binding.apply {
            buttonChooseFile.setOnClickListener {
                choosePdf()
            }

            buttonUpload.setOnClickListener {
                val code=when(autoCompleteTextView.text.toString()){
                    "CSE"->"0"
                    "IT"->"1"
                    "ECE"->"2"
                    "ME"->"3"
                    "CE"->"4"
                    "EE"->"5"
                    else -> {
                        Toast.makeText(this@MainActivity, "Select Branch", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                uploadPdf(code, autoCompleteTextView.text.toString())
            }
        }
    }
    private fun choosePdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedPdfUri = data?.data ?: return
        }
    }

    private fun uploadPdf(branch: String, branchName:String) {
        val bookName = binding.editTextBookName.text.toString()
        val semester = binding.editTextSemester.text.toString()

        val sanitizedBookName = bookName.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
        val fileName = "${sanitizedBookName}.pdf"
        val fileRef = storage.child(fileName)

        progressDialog.show()

        fileRef.putFile(selectedPdfUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val pdfUrl = uri.toString()
                    val newBook = mapOf(
                        "bookName" to bookName,
                        "bookPDF" to pdfUrl,
                        "semester" to semester
                    )

                    database.child(branch).child("booksList").get().addOnSuccessListener { snapshot ->
                        val booksList = snapshot.children.mapNotNull { it.getValue<Map<String, Any>>() }.toMutableList()
                        booksList.add(newBook)

                        val branchData = mapOf(
                            "booksList" to booksList,
                            "branch" to branchName
                        )

                        database.child(branch).setValue(branchData)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Failed to upload data", Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to fetch existing books", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload PDF", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val PICK_PDF_REQUEST = 1
    }
}