package com.example.todo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ToDoApplication :Application() {
    // setup that's necessary to activate dagger/hilt
}