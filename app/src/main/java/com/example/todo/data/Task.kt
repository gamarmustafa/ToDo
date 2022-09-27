package com.example.todo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "task_table")
@Parcelize
data class Task(
    val name:String,
    val important: Boolean = false,
    val completed:Boolean = false,
    val created:Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id:Int = 0
):Parcelable{
    val createdDateFormatted:String
        get() = SimpleDateFormat("yyyy-MM-dd , HH:mm:ss aa",Locale.getDefault()).format(created)
}
