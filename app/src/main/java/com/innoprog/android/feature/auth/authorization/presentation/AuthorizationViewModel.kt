package com.innoprog.android.feature.auth.authorization.presentation

import android.content.Context
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.innoprog.android.R
import com.innoprog.android.base.BaseViewModel
import com.innoprog.android.feature.auth.authorization.domain.AuthorisationUseCase
import com.innoprog.android.feature.auth.authorization.domain.model.UserData
import com.innoprog.android.util.ErrorType
import com.innoprog.android.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthorizationViewModel @Inject constructor(
    private val useCase: AuthorisationUseCase,
    private val context: Context
) :
    BaseViewModel() {

    private val stateLiveData = MutableLiveData<Pair<UserData?, String?>>()
    fun observeState(): LiveData<Pair<UserData?, String?>> = stateLiveData
    fun verify(inputLogin: String, inputPassword: String) {
        if (inputLogin.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(inputLogin)
                .matches() && inputPassword.isNotEmpty()
        ) {
            viewModelScope.launch {
                useCase.verify(inputLogin, inputPassword).collect {
                    when (it) {
                        is Resource.Success -> stateLiveData.postValue(Pair(it.data, null))
                        is Resource.Error -> menageError(it.errorType)
                    }
                }
            }
        } else menageError(ErrorType.UNEXPECTED)
    }

    private fun menageError(errorType: ErrorType){
        when(errorType){
            ErrorType.NOT_FOUND -> stateLiveData.postValue(Pair(null, getString(context,R.string.autorisation_no_internet)))
            ErrorType.BAD_REQUEST -> stateLiveData.postValue(Pair(null, getString(context,R.string.autorisation_no_internet)))
            else -> stateLiveData.postValue( Pair(null, getString(context, R.string.autorisation_bad_data)))
        }
    }
}
