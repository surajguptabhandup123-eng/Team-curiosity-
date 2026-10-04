package com.example.data.local

import androidx.room.*
import com.example.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, time DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY time DESC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC, time DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getAttendanceForStudentOnDate(studentId: String, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, time DESC")
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Present'")
    fun getPresentCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Late'")
    fun getLateCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Absent'")
    fun getAbsentCountForDate(date: String): Flow<Int>
}
