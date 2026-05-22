package com.wheels.app.features.favoriteDrivers.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import com.wheels.app.features.favoriteDrivers.presentation.viewmodel.FavoriteDriversViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteDriverDetailActivity : ComponentActivity() {

    private val viewModel: FavoriteDriversViewModel by viewModels()
    private lateinit var avatar: ImageView
    private lateinit var name: TextView
    private lateinit var stats: TextView
    private lateinit var sync: TextView
    private lateinit var removeButton: Button
    private var driverId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        driverId = intent.getStringExtra(EXTRA_DRIVER_ID).orEmpty()

        setContentView(buildContentView())
        viewModel.observeDriver(driverId)
        observeState()
    }

    private fun buildContentView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 48, 32, 32)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        avatar = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(220, 220)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        name = TextView(this).apply {
            textSize = 24f
            gravity = Gravity.CENTER
        }
        stats = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
        }
        sync = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
        }
        removeButton = Button(this).apply {
            text = "Remove from favorites"
            setOnClickListener {
                if (driverId.isNotBlank()) {
                    viewModel.unfavoriteDriver(driverId)
                    finish()
                }
            }
        }

        root.addView(avatar)
        root.addView(name)
        root.addView(stats)
        root.addView(sync)
        root.addView(removeButton)
        return root
    }

    private fun observeState() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val driver = state.selectedDriver
                    if (driver == null) {
                        name.text = "Favorite driver not found"
                        stats.text = ""
                        sync.text = state.errorMessage.orEmpty()
                        removeButton.visibility = View.GONE
                    } else {
                        renderDriver(driver)
                    }
                }
            }
        }
    }

    private fun renderDriver(driver: FavoriteDriver) {
        name.text = driver.driverName
        stats.text = "Rating %.1f\nTrust score %.0f\n%d completed rides".format(
            driver.rating,
            driver.trustScore,
            driver.completedRides
        )
        sync.text = if (driver.pendingSync) "Pending sync" else "Synced"
        removeButton.visibility = View.VISIBLE
        Glide.with(avatar)
            .load(driver.profileImageUrl)
            .override(220, 220)
            .circleCrop()
            .into(avatar)
    }

    companion object {
        private const val EXTRA_DRIVER_ID = "extra_driver_id"

        fun createIntent(context: Context, driverId: String): Intent {
            return Intent(context, FavoriteDriverDetailActivity::class.java)
                .putExtra(EXTRA_DRIVER_ID, driverId)
        }
    }
}
