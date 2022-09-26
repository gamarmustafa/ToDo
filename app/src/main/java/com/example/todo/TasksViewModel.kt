package com.example.todo

import androidx.lifecycle.ViewModel
import com.example.todo.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(private val taskDao: TaskDao) :ViewModel(){
}