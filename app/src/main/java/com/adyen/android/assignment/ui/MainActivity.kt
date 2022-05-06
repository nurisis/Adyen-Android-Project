package com.adyen.android.assignment.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.android.assignment.databinding.ActivityMainBinding
import com.adyen.android.assignment.ui.state.MainState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var venuesListAdapter: VenuesListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        observeStates()

        viewModel.fetchNearByVenues(latitude = null, longitude = null)
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun handleState(state: MainState) {
        when (state) {
            is MainState.Uninitialized -> {
                // todo@nurisis: 상태 처리
            }
            is MainState.Loading -> {
                // todo@nurisis: 상태 처리
            }
            is MainState.Error -> {
                // todo@nurisis: 상태 처리
            }
            is MainState.ShowVenues -> {
                venuesListAdapter?.submitList(state.list)
            }
        }
    }

    private fun initViews() {
        venuesListAdapter = VenuesListAdapter {
            // todo@nurisis: 클릭 처리
            Toast.makeText(this, "Click: ${it.name}", Toast.LENGTH_SHORT).show()
        }

        binding.venuesRecyclerView.adapter = venuesListAdapter
    }

}
