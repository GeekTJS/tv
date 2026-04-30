package com.familytv.app.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.familytv.app.data.model.Category
import com.familytv.app.data.model.VodItem
import com.familytv.app.databinding.ActivityHomeBinding
import com.familytv.app.presentation.detail.DetailActivity
import com.familytv.app.presentation.search.SearchActivity
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private val categories = listOf(
        Category(1, "电影"),
        Category(2, "连续剧"),
        Category(3, "综艺"),
        Category(4, "动漫"),
        Category(14, "国产剧"),
        Category(15, "韩剧"),
        Category(16, "欧美剧"),
        Category(22, "日剧"),
        Category(25, "泰剧")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryTabs()
        setupBanner()
        setupRecommendList()
        setupClickListeners()
        
        viewModel.loadBannerVideos()
    }

    private fun setupCategoryTabs() {
        binding.tabLayout.removeAllTabs()
        categories.forEachIndexed { index, category ->
            val tab = binding.tabLayout.newTab().apply {
                text = category.name
                tag = category
            }
            binding.tabLayout.addTab(tab)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val category = tab.tag as Category
                viewModel.loadVideosByCategory(category.id, 1)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        if (categories.isNotEmpty()) {
            viewModel.loadVideosByCategory(categories[0].id, 1)
        }
    }

    private fun setupBanner() {
        viewModel.banners.observe(this) { banners ->
            if (banners.isNotEmpty()) {
                binding.bannerViewPager.adapter = BannerAdapter(banners) { video ->
                    navigateToDetail(video)
                }
            }
        }
    }

    private fun setupRecommendList() {
        val adapter = VideoListAdapter { video ->
            navigateToDetail(video)
        }
        
        binding.recommendRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recommendRecyclerView.adapter = adapter

        viewModel.videoList.observe(this) { videos ->
            adapter.submitList(videos)
        }

        lifecycleScope.launch {
            viewModel.loadState.collectLatest { state ->
                binding.loadingView.visibility = if (state is HomeViewModel.LoadState.Loading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
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
