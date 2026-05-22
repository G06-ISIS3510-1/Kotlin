package com.wheels.app.features.favoriteDrivers.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wheels.app.features.favoriteDrivers.presentation.viewmodel.FavoriteDriversViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteDriversActivity : ComponentActivity() {

    private val viewModel: FavoriteDriversViewModel by viewModels()
    private lateinit var adapter: FavoriteDriverAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var errorText: TextView
    private lateinit var analyticsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = FavoriteDriverAdapter { driver ->
            startActivity(FavoriteDriverDetailActivity.createIntent(this, driver.driverId))
        }

        setContentView(buildContentView())
        viewModel.observeFavorites()
        viewModel.loadMostFavoritedDrivers()
        observeState()
    }

    private fun buildContentView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "Favorite Drivers"
            textSize = 24f
        }
        val addDemoButton = Button(this).apply {
            text = "Add demo favorite"
            setOnClickListener { viewModel.addDemoFavorite() }
        }
        progressBar = ProgressBar(this)
        emptyText = TextView(this).apply {
            text = "No favorite drivers yet."
            visibility = View.GONE
        }
        errorText = TextView(this).apply {
            visibility = View.GONE
        }
        val analyticsTitle = TextView(this).apply {
            text = "Most Favorited Drivers"
            textSize = 18f
            setPadding(0, 24, 0, 8)
        }
        analyticsText = TextView(this).apply {
            text = "Loading favorite driver analytics..."
            textSize = 14f
            setPadding(0, 0, 0, 16)
        }
        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@FavoriteDriversActivity)
            adapter = this@FavoriteDriversActivity.adapter
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        root.addView(title)
        root.addView(addDemoButton)
        root.addView(progressBar)
        root.addView(errorText)
        root.addView(analyticsTitle)
        root.addView(analyticsText)
        root.addView(emptyText)
        root.addView(recyclerView)
        return root
    }

    private fun observeState() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    emptyText.visibility = if (!state.isLoading && state.drivers.isEmpty()) View.VISIBLE else View.GONE
                    errorText.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
                    errorText.text = state.errorMessage.orEmpty()
                    analyticsText.text = if (state.mostFavoritedDrivers.isEmpty()) {
                        "No favorite analytics events yet. Events are stored in favorite_driver_events for Looker Studio."
                    } else {
                        state.mostFavoritedDrivers.joinToString(separator = "\n") { summary ->
                            "${summary.driverName}: ${summary.favoriteCount} saves"
                        }
                    }
                    adapter.submitList(state.drivers)
                }
            }
        }
    }

    companion object {
        fun createIntent(context: android.content.Context): Intent {
            return Intent(context, FavoriteDriversActivity::class.java)
        }
    }
}
