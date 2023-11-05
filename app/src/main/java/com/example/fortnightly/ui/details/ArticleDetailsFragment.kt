package com.example.fortnightly.ui.details

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fortnightly.R
import com.example.fortnightly.databinding.FragmentArticleDetailsBinding
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleDetailsFragment : Fragment(R.layout.fragment_article_details) {

    private val viewModel: ArticleDetailsViewModel by viewModels()

    private val args: ArticleDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 300L
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentArticleDetailsBinding.bind(view)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_open_in_browser -> {
                    openArticleUrl(args.articleUrl)
                    true
                }

                else -> false
            }
        }
    }

    private fun openArticleUrl(articleUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl))
        startActivity(intent)
    }
}