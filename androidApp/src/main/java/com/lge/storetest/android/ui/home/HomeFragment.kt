package com.lge.storetest.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.lge.storetest.android.R
import com.lge.storetest.android.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    // UI components
    private lateinit var titleEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var refreshButton: Button
    private lateinit var saveButton: Button
    private lateinit var nextPostButton: Button
    private lateinit var prevPostButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Factory would ideally be provided by DI (for example, using kotlin-inject)
        val viewModelFactory = HomeViewModelFactory()
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupViews()
        observeViewModel()
        setupListeners()

        return root
    }

    private fun setupViews() {
        binding.apply {
            titleEditText = editTextTitle
            bodyEditText = editTextBody
            loadingIndicator = progressBar
            errorText = textError
            refreshButton = buttonRefresh
            saveButton = buttonSave
            nextPostButton = buttonNextPost
            prevPostButton = buttonPrevPost
        }
    }

    private fun observeViewModel() {
        // Observe the title text
        homeViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }

        // Observe the post state flow
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.postState.collect { state ->
                when (state) {
                    is PostUiState.Loading -> {
                        loadingIndicator.isVisible = true
                        errorText.isVisible = false
                        titleEditText.isVisible = false
                        bodyEditText.isVisible = false
                    }
                    is PostUiState.Success -> {
                        loadingIndicator.isVisible = false
                        errorText.isVisible = false
                        titleEditText.isVisible = true
                        bodyEditText.isVisible = true

                        // Update UI with post data
                        titleEditText.setText(state.post.title)
                        bodyEditText.setText(state.post.body)
                    }
                    is PostUiState.Error -> {
                        loadingIndicator.isVisible = false
                        errorText.isVisible = true
                        titleEditText.isVisible = false
                        bodyEditText.isVisible = false

                        errorText.text = state.message
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        // 2. UI button to force server refresh
        refreshButton.setOnClickListener {
            homeViewModel.refreshFromServer()
        }

        // 4. Update post in DB only
        saveButton.setOnClickListener {
            homeViewModel.updatePostInDb(
                title = titleEditText.text.toString(),
                body = bodyEditText.text.toString(),
                userId = null // Keep the existing userId
            )
        }

        // 3. Navigate to next/prev post (simulating screen transition)
        var currentPostId = 1

        nextPostButton.setOnClickListener {
            currentPostId++
            homeViewModel.setPostId(currentPostId)
        }

        prevPostButton.setOnClickListener {
            if (currentPostId > 1) {
                currentPostId--
                homeViewModel.setPostId(currentPostId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Factory for providing dependencies to the ViewModel
class HomeViewModelFactory : ViewModelProvider.Factory {
    // In a real app, this would be injected
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Get the repository through App Component or other DI mechanism
            val appComponent = (requireActivity().application as MyApplication).appComponent
            val postRepository = appComponent.providePostRepository()
            return HomeViewModel(postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}