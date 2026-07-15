package com.yoon778.lexiloop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yoon778.lexiloop.data.local.dao.LexiLoopDao
import com.yoon778.lexiloop.data.local.entity.AuxiliaryMeaningEntity
import com.yoon778.lexiloop.data.local.entity.ErrorNoteEntity
import com.yoon778.lexiloop.data.local.entity.LearningItemEntity
import com.yoon778.lexiloop.data.local.entity.LearningProgressEntity
import com.yoon778.lexiloop.data.local.entity.SessionItemEntity
import com.yoon778.lexiloop.data.local.entity.StudySessionEntity

@Database(
    entities = [
        LearningItemEntity::class,
        AuxiliaryMeaningEntity::class,
        LearningProgressEntity::class,
        StudySessionEntity::class,
        SessionItemEntity::class,
        ErrorNoteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LexiLoopDatabase : RoomDatabase() {
    abstract fun dao(): LexiLoopDao

    companion object {
        fun create(context: Context): LexiLoopDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                LexiLoopDatabase::class.java,
                "lexiloop.db",
            ).build()
    }
}
