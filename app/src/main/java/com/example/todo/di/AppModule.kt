package com.example.todo.di

import android.app.Application
import androidx.room.Room
import com.example.todo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(app: Application, callback:TaskDatabase.Callback) =
        Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration().addCallback(callback).build()

    @Provides
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
    // we need a scope as long as the application lives
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
// if we ever have two coroutine scopes in our app, dagger should know which one to inject
// with creating our annotations, we can inform dagger about different scopes
//as we have only one scope, we didn't have to add this annotation, but it's a good practice
