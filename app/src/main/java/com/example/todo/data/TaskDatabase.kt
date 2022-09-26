package com.example.todo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        //here we use provider to break the circular dependency
        override fun onCreate(db: SupportSQLiteDatabase) {
            //onCreate() happens after build()
            super.onCreate(db)
            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Welcome to the To Do app!",important = true))
                dao.insert(Task("You can swipe in either direction to delete the task"))
            }

        }
    }
}