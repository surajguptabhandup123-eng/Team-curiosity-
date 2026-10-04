package com.example.data.local

import androidx.room.*
import com.example.data.model.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY status ASC, topicName ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE studentId = :studentId OR studentId IS NULL ORDER BY topicName ASC")
    fun getTopicsForStudent(studentId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE date = :date")
    fun getTopicsForDate(date: String): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("SELECT COUNT(*) FROM topics WHERE status = 'Completed'")
    fun getCompletedTopicsCount(): Flow<Int>
}
