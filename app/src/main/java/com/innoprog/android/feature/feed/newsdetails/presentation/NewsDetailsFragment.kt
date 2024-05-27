package com.innoprog.android.feature.feed.newsdetails.presentation

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.innoprog.android.R
import com.innoprog.android.base.BaseFragment
import com.innoprog.android.base.BaseViewModel
import com.innoprog.android.databinding.FragmentNewsDetailsBinding
import com.innoprog.android.di.AppComponentHolder
import com.innoprog.android.di.ScreenComponent
import com.innoprog.android.feature.feed.newsdetails.di.DaggerNewsDetailsComponent
import com.innoprog.android.feature.feed.newsdetails.domain.models.CommentModel
import com.innoprog.android.feature.feed.newsdetails.domain.models.NewsDetailsModel
import com.innoprog.android.feature.imagegalleryadapter.ImageGalleryAdapter
import com.innoprog.android.uikit.ImageLoadingType
import okhttp3.internal.format
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NewsDetailsFragment : BaseFragment<FragmentNewsDetailsBinding, BaseViewModel>() {

    override val viewModel by injectViewModel<NewsDetailsViewModel>()
    private var galleryAdapter: ImageGalleryAdapter? = null
    private var commentsAdapter: CommentsAdapter? = null

    override fun diComponent(): ScreenComponent {
        val appComponent = AppComponentHolder.getComponent()
        return DaggerNewsDetailsComponent.builder()
            .appComponent(appComponent)
            .build()
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewsDetailsBinding {
        return FragmentNewsDetailsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUiListeners()

        viewModel.screenState.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        val args: NewsDetailsFragmentArgs by navArgs()
        val newsId = args.newsId

        viewModel.getNewsDetails(newsId)
    }

    private fun setUiListeners() {
        binding.apply {
            newsTopBar.setLeftIconClickListener {
                viewModel.navigateUp()
            }

            newsTopBar.setRightIconClickListener {
                Toast.makeText(requireContext(), "Добавлено/удалено из избранного", Toast.LENGTH_SHORT)
                    .show()
            }

            btnShowAll.setOnClickListener {
                if (tvPublicationContent.maxLines == TV_MAX_LINES) {
                    tvPublicationContent.maxLines = Int.MAX_VALUE
                    btnShowAll.isVisible = false
                }
            }
        }
    }

    private fun initImageGallery() {
        val images = listOf(
            R.drawable.news_sample,
            R.drawable.course_logo_sample,
            R.drawable.news_sample,
            R.drawable.course_logo_sample,
            R.drawable.news_sample,
        )

        galleryAdapter = ImageGalleryAdapter(images)
        binding.viewPager.adapter = galleryAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position -> }.attach()
    }

    private fun updateUI(state: NewsDetailsScreenState) {
        when (state) {
            is NewsDetailsScreenState.Loading -> showLoading()
            is NewsDetailsScreenState.Content -> state.newsDetails?.let { showContent(it) }
            is NewsDetailsScreenState.Error -> showError()
        }
    }

    private fun showLoading() {
        Toast.makeText(requireContext(), "Загрузка", Toast.LENGTH_SHORT).show()
    }

    private fun showError() {
        Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show()
    }

    private fun showContent(newsDetails: NewsDetailsModel) {
        binding.apply {
            initImageGallery()

            tvPublicationTitle.text = newsDetails.title
            tvPublicationContent.text = newsDetails.content
            tvNewsComments.text = newsDetails.commentsCount.toString()
            newsLikesView.setLikeCount(newsDetails.likesCount)
            tvNewsPublicationDate.text = getFormattedDate(newsDetails.publishedAt)

            val avatarUrl = newsDetails.author.avatarUrl
            val placeholderResId = com.innoprog.android.uikit.R.drawable.ic_person
            loadAvatar(avatarUrl, placeholderResId)

            tvNewsAuthorName.text = newsDetails.author.name

            val newsAuthorPosition =
                StringBuilder().append(newsDetails.author.company.role).append(" в ")
                    .append(newsDetails.author.company.companyName)
                    .toString()
            tvNewsAuthorPosition.text = newsAuthorPosition

            tvComments.text = format(getString(R.string.comments), newsDetails.commentsCount)

            if (newsDetails.comments != null) {
                rvComments.isVisible = true
                val commentsList = newsDetails.comments
                initRecyclerView(commentsList)
            } else {
                rvComments.isVisible = false
                tvNoCommentsPlaceholder.isVisible = true
            }
        }
    }

    private fun getFormattedDate(inputDate: String): String {
        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy в HH:mm", Locale("ru"))
        val dateTime = LocalDateTime.parse(inputDate, inputFormatter)
        return dateTime.format(outputFormatter)
    }

    private fun loadAvatar(avatarUrl: String?, placeholderResId: Int) {
        val imageType =
            avatarUrl?.let {
                ImageLoadingType.ImageNetwork(
                    it,
                    placeholderResId = placeholderResId
                )
            }
        if (imageType != null) {
            binding.newsAuthorAvatar.loadImage(imageType)
        }
    }

    private fun initRecyclerView(commentsList: List<CommentModel>) {
        commentsAdapter =
            CommentsAdapter(commentsList, object : CommentsAdapter.OnClickListener {
                override fun onItemClick(
                    position: Int,
                    comment: CommentModel,
                    context: Context
                ) {
                    val itemView = binding.rvComments.layoutManager?.findViewByPosition(position)
                    itemView?.setBackgroundColor(Color.parseColor("#F0F0F0"))
                    itemView?.findViewById<TextView>(R.id.tvDeleteComment)?.visibility =
                        View.VISIBLE
                }
            })

        binding.rvComments.adapter = commentsAdapter
    }

    companion object {
        const val TV_MAX_LINES = 6
    }
}