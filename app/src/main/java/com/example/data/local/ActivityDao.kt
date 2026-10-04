package com.example.data.local

import androidx.room.*
import com.example.data.model.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY date DESC, activityId DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE date = :date ORDER BY activityId DESC")
    fun getActivitiesForDate(date: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE studentId = :studentId ORDER BY date DESC, activityId DESC")
    fun getActivitiesForStudent(studentId: String): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT COUNT(*) FROM activities WHERE date = :date")
    fun getActivityCountForDate(date: String): Flow<Int>
}
