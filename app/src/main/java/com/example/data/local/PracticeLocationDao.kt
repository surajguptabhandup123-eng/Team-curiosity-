package com.example.data.local

import androidx.room.*
import com.example.data.model.PracticeLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeLocationDao {
    @Query("SELECT * FROM practice_location WHERE id = 1 LIMIT 1")
    fun getPracticeLocation(): Flow<PracticeLocationEntity?>

    @Query("SELECT * FROM practice_location WHERE id = 1 LIMIT 1")
    suspend fun getPracticeLocationOnce(): PracticeLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPracticeLocation(location: PracticeLocationEntity)
}
