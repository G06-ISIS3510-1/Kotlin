package com.wheels.app.features.favoriteDrivers.presentation.ui

import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver

class FavoriteDriverAdapter(
    private val onDriverClick: (FavoriteDriver) -> Unit
) : ListAdapter<FavoriteDriver, FavoriteDriverAdapter.FavoriteDriverViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteDriverViewHolder {
        val context = parent.context
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(32, 24, 32, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val avatar = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(112, 112)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val name = TextView(context).apply { textSize = 18f }
        val stats = TextView(context).apply { textSize = 14f }
        val sync = TextView(context).apply { textSize = 12f }

        textContainer.addView(name)
        textContainer.addView(stats)
        textContainer.addView(sync)
        row.addView(avatar)
        row.addView(textContainer)

        return FavoriteDriverViewHolder(row, avatar, name, stats, sync, onDriverClick)
    }

    override fun onBindViewHolder(holder: FavoriteDriverViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavoriteDriverViewHolder(
        private val root: LinearLayout,
        private val avatar: ImageView,
        private val name: TextView,
        private val stats: TextView,
        private val sync: TextView,
        private val onDriverClick: (FavoriteDriver) -> Unit
    ) : RecyclerView.ViewHolder(root) {

        fun bind(driver: FavoriteDriver) {
            name.text = driver.driverName
            stats.text = "Rating %.1f | Trust %.0f | %d completed rides".format(
                driver.rating,
                driver.trustScore,
                driver.completedRides
            )
            sync.text = if (driver.pendingSync) "Pending sync" else "Synced"
            root.setOnClickListener { onDriverClick(driver) }

            Glide.with(avatar)
                .load(driver.profileImageUrl)
                .override(112, 112)
                .circleCrop()
                .into(avatar)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FavoriteDriver>() {
        override fun areItemsTheSame(oldItem: FavoriteDriver, newItem: FavoriteDriver): Boolean {
            return oldItem.driverId == newItem.driverId
        }

        override fun areContentsTheSame(oldItem: FavoriteDriver, newItem: FavoriteDriver): Boolean {
            return oldItem == newItem
        }
    }
}
