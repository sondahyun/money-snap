package com.example.tripline.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tripline.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    // 해당 하는 날의 모든 지출
    @Query("SELECT * FROM expenses WHERE date = :date")
    fun getExpensesByDate(date: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE substr(date, 1, 7) = :month ORDER BY date DESC")
    fun getExpensesByMonth(month: String): Flow<List<Expense>>

    // 해당 하는 날의 총 지출
    @Query("SELECT SUM(amount) FROM expenses WHERE date = :date")
    fun getTotalExpenseByDate(date: String): Flow<Int>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenseAll(): Flow<Int>

    // 해당 하는 _id 의 지출 삭제
    @Query("DELETE FROM expenses WHERE _id = :id")
    suspend fun deleteExpenseById(id: Int): Int

    // 해당 하는 date 의 모든 지출 삭제
    @Query("DELETE FROM expenses WHERE date = :date")
    suspend fun deleteExpensesByDate(date: String): Int

    // Delete an entire expense object
    @Delete
    suspend fun deleteExpense(expense: Expense): Int

}
