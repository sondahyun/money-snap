package com.example.tripline.data.repository

import com.example.tripline.data.dao.ExpenseDao
import com.example.tripline.data.entity.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    // 해당 하는 날의 모든 지출
    fun getExpensesByDate(date: String): Flow<List<Expense>> = expenseDao.getExpensesByDate(date)

    fun getExpensesByMonth(month: String): Flow<List<Expense>> = expenseDao.getExpensesByMonth(month)

    // 해당 하는 날의 총 지출
    fun getTotalExpenseByDate(date: String): Flow<Int> =  expenseDao.getTotalExpenseByDate(date)

    fun getTotalExpenseAll(): Flow<Int> = expenseDao.getTotalExpenseAll()

    // 지출 추가
    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    // 해당 하는 _id 의 지출 삭제
    suspend fun deleteExpenseById(id: Int) {
        expenseDao.deleteExpenseById(id)
    }

    // 해당 하는 date 의 모든 지출 삭제
    suspend fun deleteExpensesByDate(date: String) {
        expenseDao.deleteExpensesByDate(date)
    }

    // Delete an entire expense object
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}
