package com.innoprog.android.feature.edit.presentation

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.innoprog.android.R
import com.innoprog.android.base.BaseFragment
import com.innoprog.android.base.BaseViewModel
import com.innoprog.android.databinding.FragmentCreateEditContentBinding
import com.innoprog.android.di.AppComponentHolder
import com.innoprog.android.di.ScreenComponent
import com.innoprog.android.feature.edit.di.DaggerCreateEditContentComponent
import com.innoprog.android.feature.edit.domain.model.MediaAttachmentsModel
import com.innoprog.android.feature.training.courseInformation.presentation.CourseInformationFragment

class CreateEditContentFragment : BaseFragment<FragmentCreateEditContentBinding, BaseViewModel>() {

    private val args by navArgs<CreateEditContentFragmentArgs>()
    override val viewModel by injectViewModel<CreateEditContentViewModel>()

    private var mediaAttachAdapter: MediaAttachRecyclerAdapter? = null

    override fun diComponent(): ScreenComponent {
        val appComponent = AppComponentHolder.getComponent()
        return DaggerCreateEditContentComponent
            .builder()
            .appComponent(appComponent)
            .build()
    }

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                getRealPathFromUri(it)?.let { path ->
                    viewModel.addMediaToLoadList(path)
                }
            }
        }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateEditContentBinding {
        return FragmentCreateEditContentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.setEditorType(args.typeContent)

        binding.topBar.setLeftIconClickListener {
            viewModel.navigateUp()
        }

        binding.saveBV.setOnClickListener {
            // clickButtonSave()
        }

        binding.loadBV.setOnClickListener { loadMedia() }

    }

    private fun render(state: CreateEditContentState) {
        when (state) {
            is CreateEditContentState.CreateIdea -> {
                binding.topBar.setTitleText(getText(R.string.create_idea))
                binding.saveBV.setText(getString(R.string.publish))
                binding.inputTitle.setHintText(getString(R.string.title_of_idea))
                binding.inputText.setHintText(getString(R.string.text_idea))
                binding.groupProject.visibility = View.GONE

            }

            is CreateEditContentState.CreatePublication -> {
                binding.topBar.setTitleText(getText(R.string.create_publish))
                binding.saveBV.setText(getString(R.string.publish))
                binding.inputTitle.setHintText(getString(R.string.title_of_news))
                binding.inputText.setHintText(getString(R.string.text_publish))
                binding.groupProject.visibility = View.VISIBLE
            }

            is CreateEditContentState.EditPublication -> {
                binding.topBar.setTitleText(getText(R.string.edit_publish))
                binding.saveBV.setText(getString(R.string.save))
                binding.groupProject.visibility = View.VISIBLE
            }

            is CreateEditContentState.MediaAttachList -> {
                if (state.mediaAttachments != null) {
                    binding.rvMedia.visibility = View.VISIBLE
                    initMediaAttachList(state.mediaAttachments)

                } else {
                    binding.rvMedia.visibility = View.GONE
                }
            }

            is CreateEditContentState.Error -> showError(state.errorMassage)
        }
    }

    private fun loadMedia() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageAndVideo
            )
        )
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        var realPath: String? = null

        val cursor: Cursor? = requireContext()
            .contentResolver
            .query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                realPath = it.getString(columnIndex)
            }
        }
        cursor?.close()
        return realPath
    }

    private fun initMediaAttachList(mediaAttach: MediaAttachmentsModel) {
        mediaAttachAdapter = MediaAttachRecyclerAdapter(
            onDeleteClickListener = { viewModel.deleteMediaFromListAttachments(it) },
            onPlayClickListener = {
                viewModel.navigateTo(
                    R.id.videoPlayerFragment,
                    bundleOf(CourseInformationFragment.VIDEO_PLAYER_KEY to it)
                )
            }
        )
        mediaAttachAdapter!!.mediaList = mediaAttach.pathList
        binding.rvMedia.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedia.adapter = mediaAttachAdapter
        mediaAttachAdapter!!.notifyDataSetChanged()
    }

    private fun showError(errorMassage: String) {
        Toast.makeText(requireContext(), errorMassage, Toast.LENGTH_SHORT).show()
    }

}