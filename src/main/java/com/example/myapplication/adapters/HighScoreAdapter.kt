package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.HighScore
import android.content.Intent
import android.graphics.Color
import android.net.Uri

class HighScoreAdapter(
    private val scores: List<HighScore>,
    private val onItemClick: (Double, Double) -> Unit
) : RecyclerView.Adapter<HighScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.item_rank_text)
        val distanceText: TextView = view.findViewById(R.id.item_distance_text)
        val locationText: TextView = view.findViewById(R.id.item_location_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_high_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.rankText.text = "${position + 1}."
        holder.distanceText.text = "Distance: ${score.distance}"

        if (score.latitude != 0.0 && score.longitude != 0.0) {
            holder.locationText.text = "View Location"
            holder.locationText.setTextColor(Color.BLUE)
            holder.locationText.paint.isUnderlineText = true
            holder.locationText.setOnClickListener {
                val uri = Uri.parse("geo:${score.latitude},${score.longitude}?q=${score.latitude},${score.longitude}(High+Score+Location)")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                it.context.startActivity(intent)
            }
        } else {
            holder.locationText.text = "Location: Unknown"
            holder.locationText.setOnClickListener(null)
            holder.locationText.setTextColor(Color.BLACK)
            holder.locationText.paint.isUnderlineText = false
        }

        holder.itemView.setOnClickListener {
            onItemClick(score.latitude, score.longitude)
        }
    }


    override fun getItemCount(): Int = scores.size

    private fun getLocationName(lat: Double, lng: Double): String {
        return if (lat == 0.0 && lng == 0.0) "Unknown" else "($lat, $lng)"
    }
}
