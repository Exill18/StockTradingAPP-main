package com.example.TPSIMobileProjecto.ui.News

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.TPSIMobileProjecto.R
import com.example.TPSIMobileProjecto.databinding.FragmentNewsBinding

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private lateinit var noDataFoundTV : TextView
    private lateinit var newsViewModel: NewsViewModel
    lateinit var progressBar: ProgressBar

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e("Lifecycle", "NewsFragment onCreateView()")
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newsViewModel = ViewModelProvider(this).get(NewsViewModel::class.java)

        progressBar = view.findViewById(R.id.progressBarNews)
        showProgressBar()

        noDataFoundTV = view.findViewById(R.id.tvNewsFound)
        noDataFoundTV.visibility = View.GONE

        val recyclerView: RecyclerView = view.findViewById(R.id.newsrecyleview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        newsViewModel.newsList.observe(viewLifecycleOwner) { newsList ->
            if (newsList.isNullOrEmpty()) {
                noDataFoundTV.visibility = View.VISIBLE
            } else {
                noDataFoundTV.visibility = View.GONE
                recyclerView.adapter = NewsRecyclerAdapter(newsList)
            }
            hideProgressBar()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.e("Lifecycle", "NewsFragment onDestroyView()")
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }
}