package com.subhajitrajak.msbcontributer.screens.videos

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import com.subhajitrajak.msbcontributer.databinding.FragmentVideosBinding
import com.subhajitrajak.msbcontributer.models.VideosModel
import com.subhajitrajak.msbcontributer.utils.Constants.ACCEPTED
import com.subhajitrajak.msbcontributer.utils.Constants.PENDING
import com.subhajitrajak.msbcontributer.utils.Constants.REJECTED
import com.subhajitrajak.msbcontributer.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.msbcontributer.utils.Constants.VIDEOS
import com.subhajitrajak.msbcontributer.utils.Constants.VIDEOS_DATA
import com.subhajitrajak.msbcontributer.utils.removeWithAnim
import com.subhajitrajak.msbcontributer.utils.showToast
import com.subhajitrajak.msbcontributer.utils.showWithAnim

class VideosFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private val list = ArrayList<VideosModel>()
    private lateinit var binding: FragmentVideosBinding
    private lateinit var adapter: VideosRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideosBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        val notesRef = database.getReference(UPLOAD_REQUESTS).child(VIDEOS_DATA)
        notesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (data in snapshot.children) {
                    val book = data.getValue(VideosModel::class.java)
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
                    binding.rvVideos.removeWithAnim()
                } else {
                    binding.mErrorHolder.removeWithAnim()
                    binding.rvVideos.showWithAnim()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupAdapter() {
        adapter = VideosRequestAdapter(
            requireContext(),
            list,
            onClick = { id->
                showPlaylistInYoutube(id)
            },
            onAccept = { id ->
                onAccept(id)
                adapter.notifyDataSetChanged()
            },
            onReject = { id ->
                onReject(id)
                adapter.notifyDataSetChanged()
            }
        )
        binding.rvVideos.adapter = adapter
        binding.rvVideos.layoutManager = LinearLayoutManager(requireContext())
    }


    private fun showPlaylistInYoutube(playlistId: String) {
        val url = "https://www.youtube.com/playlist?list=$playlistId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.google.android.youtube")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback to browser
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(fallbackIntent)
        }
    }

    private fun onReject(id: String) {
        val uploadRequestRef = database.getReference(UPLOAD_REQUESTS).child(VIDEOS_DATA).child(id)

        uploadRequestRef.child("status").setValue(REJECTED)
            .addOnSuccessListener {

                showToast(requireContext(), "Videos rejected")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Error: ${it.message}")
            }
    }

    private fun onAccept(id: String) {
        val videosDataRef = database.getReference(VIDEOS_DATA)
        val videosRef = videosDataRef.child(id)

        val newVideo = mapOf(
            "playlistId" to id,
            "type" to VIDEOS,
            "status" to ACCEPTED
        )

        videosRef.setValue(newVideo)
            .addOnSuccessListener {
                showToast(requireContext(), "Upload successful")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Failed to upload data")
            }

        // setting accepted status in node upload requests
        val uploadVideosRef = database.getReference(UPLOAD_REQUESTS).child(VIDEOS_DATA).child(id)
        uploadVideosRef.child("status").setValue(ACCEPTED)
            .addOnSuccessListener {
                showToast(requireContext(), "Videos accepted")
            }
            .addOnFailureListener {
                showToast(requireContext(), "Error: ${it.message}")
            }
    }
}