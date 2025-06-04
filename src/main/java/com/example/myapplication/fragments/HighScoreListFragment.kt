package com.example.myapplication.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.HighScoreAdapter
import com.example.myapplication.model.HighScore
import com.example.myapplication.utilities.SharedPrefManager
import com.example.myapplication.viewmodel.SharedHighScoreViewModel

class HighScoreListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val sharedViewModel: SharedHighScoreViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_high_score_list, container, false)
        recyclerView = view.findViewById(R.id.highscore_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val scores = SharedPrefManager.loadHighScores(requireContext())
        Log.d("DEBUG_SCORES", "Loaded scores: ${scores.size}")

        val adapter = HighScoreAdapter(scores) { lat, lng ->
            sharedViewModel.setLocation(lat, lng)
        }

        recyclerView.adapter = adapter

        return view
    }
}
