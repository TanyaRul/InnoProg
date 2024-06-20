package com.innoprog.android.feature.edit.presentation

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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

    override val viewModel by injectViewModel<CreateEditContentViewModel>()

    private val args by navArgs<CreateEditContentFragmentArgs>()

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
        if (savedInstanceState == null) {
            viewModel.setEditorType(args.typeContent)
        }

        viewModel.getMediaAttachments()

        binding.topBar.setLeftIconClickListener {
            viewModel.navigateUp()
        }

        binding.loadBV.setOnClickListener { loadMedia() }

        binding.inputTitle.setMaxOfCharacters(MAX_TITLE_LENGTH)
        binding.inputText.setMaxOfCharacters(MAX_CONTENT_LENGTH)

    }

    private fun render(state: CreateEditContentState) {

        binding.inputTitle.addTextChangedListener(selectTextWatcher(state))
        binding.inputText.addTextChangedListener(selectTextWatcher(state))

        when (state) {
            is CreateEditContentState.CreateIdea -> {
                binding.topBar.setTitleText(getText(R.string.create_idea))
                binding.saveBV.setText(getString(R.string.publish))
                binding.inputTitle.setHintText(getString(R.string.title_of_idea))
                binding.inputText.setHintText(getString(R.string.text_idea))
                binding.groupProject.visibility = View.GONE
                binding.saveBV.setOnClickListener {
                    viewModel.saveNewIdea(
                        binding.inputTitle.getText(),
                        binding.inputText.getText()
                    ) { findNavController().popBackStack() }
                }

            }

            is CreateEditContentState.CreatePublication -> {
                binding.topBar.setTitleText(getText(R.string.create_publish))
                binding.saveBV.setText(getString(R.string.publish))
                binding.inputTitle.setHintText(getString(R.string.title_of_news))
                binding.inputText.setHintText(getString(R.string.text_publish))
                binding.groupProject.visibility = View.VISIBLE
                binding.saveBV.setOnClickListener {
                    viewModel.saveNewPublication(
                        binding.inputTitle.getText(),
                        binding.inputText.getText()
                    ) { findNavController().popBackStack() }
                }
            }

            is CreateEditContentState.EditPublication -> {
                binding.topBar.setTitleText(getText(R.string.edit_publish))
                binding.saveBV.setText(getString(R.string.save))
                binding.groupProject.visibility = View.VISIBLE
                binding.inputTitle.setHintText(getString(R.string.title_of_news))
                binding.inputText.setHintText(getString(R.string.text_publish))
                binding.inputTitle.setText(state.publication.title)
                binding.inputText.setText(state.publication.content)
                binding.saveBV.setOnClickListener {
                    viewModel.saveModifiedPublication(
                        binding.inputTitle.getText(),
                        binding.inputText.getText()
                    ) { findNavController().popBackStack() }
                }
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
            is CreateEditContentState.ProjectInfo -> {

                Glide.with(binding.ivProjectLogo)
                    .load(state.projectModel.logoUrl)
                    .placeholder(R.drawable.ic_placeholder_logo)
                    .into(binding.ivProjectLogo)
                binding.tvProjectName.text = state.projectModel.name
                binding.tvProjectArea.text = state.projectModel.area
            }
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
                val navOptions = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build()
                viewModel.navigateTo(
                    R.id.videoPlayerFragment,
                    bundleOf(CourseInformationFragment.VIDEO_PLAYER_KEY to it),
                    navOptions
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

    private fun selectTextWatcher(state: CreateEditContentState): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                when (state) {
                    is CreateEditContentState.EditPublication -> {
                        binding.saveBV.stateIsEnabled(
                            binding.inputTitle.getText().isNotBlank()
                                    && binding.inputText.getText().isNotBlank()
                                    && binding.inputText.getText() != state.publication.content
                                    && binding.inputTitle.getText() != state.publication.title
                        )
                    }

                    else -> {
                        binding.saveBV.stateIsEnabled(
                            binding.inputTitle.getText().isNotBlank() && binding.inputText.getText()
                                .isNotBlank()
                        )
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 255
        const val MAX_CONTENT_LENGTH = 10000
    }

}