package com.example.todo.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.todo.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(private val taskDao: TaskDao) : ViewModel() {

    val searchQuery = MutableStateFlow("") //holds single value (not a stream of values like Flow)

    val sortOrder = MutableStateFlow(SortOrder.BY_DATE) //default value
    val hideCompleted = MutableStateFlow(false)   //default value

    private val taskFlow = combine(searchQuery, sortOrder, hideCompleted) { //combine()  passes all there latest values
        query,sortOrder,hideCompleted ->
        Triple(query,sortOrder,hideCompleted)
    }
        .flatMapLatest { (query, sortOrder,hideCompleted) ->
        //flatMapLatest - whenever searchQuery changes runs the code below
        // and assigns new value to taskFlow
        // here we switch from one flow to another flow
        taskDao.getTasks(query,sortOrder,hideCompleted) // we could also write it.first, it.second and etc.
    }
    val tasks = taskFlow.asLiveData()

}

enum class SortOrder {
    BY_NAME, BY_DATE
}