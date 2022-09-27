package com.example.todo.tasks

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.data.Task
import com.example.todo.databinding.ActivityMainBinding
import com.example.todo.databinding.DialogAddBinding
import com.example.todo.databinding.DialogEditBinding
import com.example.todo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),TasksAdapter.OnItemClickListener {

    val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView:SearchView
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tasksAdapter = TasksAdapter(this)
        supportActionBar?.title = "Tasks"
        binding.apply {
            rvTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            //Swipe
            ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean { return false } // onMove method is for drag&drop. we don't need it here

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                    Snackbar.make(binding.root, "Task deleted",Snackbar.LENGTH_LONG)
                        .setAction("UNDO"){
                            //UI shouldn't handle logic part. that's why we delegate deletion to ViewModel
                            viewModel.onUndoDeleteClick(task)
                        }.show()
                }
            }).attachToRecyclerView(rvTasks)

            //Adding tasks
            fabAddTasks.setOnClickListener {
                val addDialog = Dialog(this@MainActivity,R.style.Theme_Dialog)
                addDialog.setCanceledOnTouchOutside(true)
                val addBinding= DialogAddBinding.inflate(layoutInflater)
                addDialog.setContentView(addBinding.root)
                //addBinding?.etAddNote?.showKeyboard()
                addBinding.tvAdd.setOnClickListener {

                    val taskText = addBinding.etAddNote.text.toString()
                    val importance = addBinding.checkBoxImportant.isChecked
                    if (taskText.isNotEmpty()) {

                            viewModel.addTask(taskText,importance)
                            Toast.makeText(applicationContext, "Task added", Toast.LENGTH_SHORT).show()
                            addDialog.dismiss()

                    } else {
                        Toast.makeText(applicationContext, "A task cannot be blank!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                addBinding.tvCancel.setOnClickListener {
                    addDialog.dismiss()
                }
                addDialog.show()
            }
        }

        viewModel.tasks.observe(this) {
            if(it.isNotEmpty()){
                binding.rvTasks.visibility = View.VISIBLE
                binding.tvNoTask.visibility = View.GONE
                binding.ivNoTask.visibility = View.GONE
            }
            else{
                binding.rvTasks.visibility = View.GONE
                binding.tvNoTask.visibility = View.VISIBLE
                binding.ivNoTask.visibility = View.VISIBLE
            }
            tasksAdapter.submitList(it)
        }
    }

    //Editing task
    override fun onItemClick(task: Task) {
        val editDialog = Dialog(this,R.style.Theme_Dialog)
        editDialog.setCanceledOnTouchOutside(true)
        val editBinding = DialogEditBinding.inflate(layoutInflater)
        editDialog.setContentView(editBinding.root)

        editBinding.apply {
            etAddNote.setText(task.name)
            checkBoxImportant.isChecked = task.important
            editBinding.created.text = task.createdDateFormatted
        }

        editBinding.tvUpdate.setOnClickListener {
            val currentTask = editBinding.etAddNote.text.toString()
            val importance = editBinding.checkBoxImportant.isChecked
            if(currentTask.isNotEmpty()){
                viewModel.editTask(task,currentTask,importance)
                Toast.makeText(applicationContext, "Task Updated", Toast.LENGTH_SHORT).show()
                editDialog.dismiss()
            }else{
                Toast.makeText(applicationContext, "A task cannot be blank!", Toast.LENGTH_SHORT)
                .show()
            }
        }

        editBinding.tvCancel.setOnClickListener {
            editDialog.dismiss()
        }
        editDialog.show()


    }
    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        //delegating work to ViewModel
        viewModel.onTaskChecked(task,isChecked)
    }

    //Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tasks, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if(pendingQuery != null && pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false )
        } // to put the query in the search view when screen rotated
        // when screen rotated, searchView disappears for a unknown reason

        searchView.onQueryTextChanged {
            //we pass query from the SearchView to ViewModel
            viewModel.searchQuery.value = it
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_date_created -> {
                viewModel.sortOrder.value = SortOrder.BY_DATE
                true
            }
            R.id.action_sort_by_name -> {
                viewModel.sortOrder.value = SortOrder.BY_NAME
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.hideCompleted.value = item.isChecked
                true
            }
            R.id.action_delete_all_completed_tasks -> {
                viewModel.deleteAllTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)
    }

}