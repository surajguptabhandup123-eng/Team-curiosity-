package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.PracticeLocationEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TopicEntity

@Database(
    entities = [
        StudentEntity::class,
        AttendanceEntity::class,
        ActivityEntity::class,
        TopicEntity::class,
        PracticeLocationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun activityDao(): ActivityDao
    abstract fun topicDao(): TopicDao
    abstract fun practiceLocationDao(): PracticeLocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "team_curiosity_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
