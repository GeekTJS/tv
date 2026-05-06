package com.familytv.app.presentation.search

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.familytv.app.data.model.VodItem
import com.familytv.app.databinding.ActivitySearchBinding
import com.familytv.app.presentation.detail.DetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var resultAdapter: SearchResultAdapter

    private val searchHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
        loadHistory()
    }

    private fun setupUI() {
        resultAdapter = SearchResultAdapter { video ->
            navigateToDetail(video)
        }

        binding.resultRecyclerView.layoutManager = GridLayoutManager(this, 5)
        binding.resultRecyclerView.adapter = resultAdapter

        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                performSearch()
                true
            } else false
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
            searchHistory.clear()
            updateHistoryUI()
        }
    }

    private fun performSearch() {
        val keyword = binding.searchEditText.text.toString().trim()
        if (keyword.isNotEmpty()) {
            viewModel.saveHistory(keyword)
            searchHistory.add(0, keyword)
            updateHistoryUI()
            viewModel.search(keyword)
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            viewModel.getHistory().collectLatest { history ->
                searchHistory.clear()
                searchHistory.addAll(history)
                updateHistoryUI()
            }
        }
    }

    private fun updateHistoryUI() {
        binding.historyContainer.visibility = if (searchHistory.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun observeData() {
        viewModel.searchResults.observe(this) { results ->
            resultAdapter.submitList(results)
            binding.resultRecyclerView.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
            binding.emptyTextView.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }

        lifecycleScope.launch {
            viewModel.loadState.collectLatest { state ->
                binding.loadingProgressBar.visibility = if (state is SearchViewModel.LoadState.Loading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun navigateToDetail(video: VodItem) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("vodId", video.vodId)
            putExtra("vodName", video.vodName)
            putExtra("vodPic", video.vodPic)
        }
        startActivity(intent)
    }
}
