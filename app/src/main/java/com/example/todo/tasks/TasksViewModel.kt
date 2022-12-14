package com.example.todo.tasks


import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.todo.data.Task
import com.example.todo.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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

    //onClick
    fun onTaskChecked(task: Task, isChecked:Boolean){
        viewModelScope.launch {
            //if we modify old item, DiffUtil will not pick up the changes
            taskDao.update(task.copy(completed = isChecked))
        }
    }

    //Swipe
    fun onTaskSwiped(task: Task){
        viewModelScope.launch {
            taskDao.delete(task)
        }
    }

    fun addTask(taskName:String, importance:Boolean){
        viewModelScope.launch{
            taskDao.insert(Task(name = taskName,important = importance))
        }
    }

    fun editTask(task:Task,taskName:String,importance: Boolean){
        viewModelScope.launch {
            taskDao.update(task.copy(name = taskName, important = importance))
        }
    }

    fun onUndoDeleteClick(task: Task){
        viewModelScope.launch{
            taskDao.insert(task)
        }
    }
    fun deleteAllTasks(){
        viewModelScope.launch {
            taskDao.deleteCompletedTasks()
        }
    }
}

enum class SortOrder {
    BY_NAME, BY_DATE
}