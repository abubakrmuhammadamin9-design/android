package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val photoPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val rating: Int = 3 // Priority/Rating 1-5
)
