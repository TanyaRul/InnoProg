package com.innoprog.android.feature.projects.create.presentation

import androidx.lifecycle.viewModelScope
import com.innoprog.android.base.BaseViewModel
import com.innoprog.android.feature.projects.create.domain.CreateProjectUseCase
import com.innoprog.android.feature.projects.create.presentation.model.FillAboutProjectEvent
import com.innoprog.android.feature.projects.create.presentation.model.FillAboutProjectState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateProjectViewModel @Inject constructor(private val useCase: CreateProjectUseCase) :
    BaseViewModel() {

    private val _state = MutableStateFlow(FillAboutProjectState())
    val state: StateFlow<FillAboutProjectState> get() = _state.asStateFlow()
    private var direction: String? = null
    private var logoUrl: String? = null
    private var attachments = mutableListOf<String>()

    fun obtainEvent(event: FillAboutProjectEvent) {
        when (event) {
            is FillAboutProjectEvent.PickPhoto -> {
                _state.update { it.copy(pinedLogoUri = event.uri) }
            }

            is FillAboutProjectEvent.UnPinePhoto -> {
                _state.update { it.copy(pinedLogoUri = null) }
            }
        }
    }

    fun downloadLogo(url: String) {
        logoUrl = url
    }

    fun setDirection(direction: String) {
        this.direction = direction
    }

    fun attachDocument(url: String) {
        attachments.add(url)
    }

    fun createProject() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.createProject()
        }
    }
}
