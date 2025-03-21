package com.example.notetakingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Date = Date()
) {
    fun getPreviewContent(): String {
        return if (content.length > 100) {
            content.substring(0, 97) + "..."
        } else {
            content
        }
    }
    
    companion object {
        fun createEmpty(): Note {
            return Note(
                title = "",
                content = "",
                timestamp = Date()
            )
        }
    }
}