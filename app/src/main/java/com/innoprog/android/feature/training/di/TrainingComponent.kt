package com.innoprog.android.feature.training.di

import com.innoprog.android.di.ScreenComponent
import dagger.Component

@Component(
    modules = [TrainingModule::class, AbstractTrainingModule::class]
)
interface TrainingComponent : ScreenComponent
