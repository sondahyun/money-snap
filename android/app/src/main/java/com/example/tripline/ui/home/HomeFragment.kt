package com.example.tripline.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tripline.MainActivity
import com.example.tripline.PrototypeScreenActivity
import com.example.tripline.R
import com.example.tripline.TriplineApplication
import com.example.tripline.databinding.FragmentHomeBinding
import com.example.tripline.ui.expense.ExpenseViewModel
import com.example.tripline.ui.expense.ExpenseViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: TransactionAdapter
    private val hasCurrentTripMock = true

    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((requireActivity().application as TriplineApplication).expenseRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // RecyclerView 설정
        adapter = TransactionAdapter(emptyList())
        binding.todayList.layoutManager = LinearLayoutManager(requireContext())
        binding.todayList.adapter = adapter

        // 오늘 날짜 가져오기
        val todayDate = getTodayDate()

        // UI에 오늘 날짜 설정
        setTodayDateUI()

        // 오늘의 지출 요약 계산
        calculateExpenseSummary(todayDate)

        // 오늘의 거래 데이터 로드 및 정렬
        loadTransactions(todayDate)
        renderHomeState(hasCurrentTripMock)

        binding.addButton.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.EXPENSE_ENTRY
                )
            )
        }

        binding.buttonMypage.setOnClickListener {
            (activity as? MainActivity)?.openMyPage()
        }

        binding.buttonCreateTripEmpty.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.TRIP_CREATE
                )
            )
        }

        binding.buttonImportOcrEmpty.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.OCR_IMPORT
                )
            )
        }

        binding.currentTripSection.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_schedule)
        }

        binding.nextScheduleSection.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_schedule)
        }

        binding.todayWeatherSection.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.WEATHER
                )
            )
        }

        return binding.root
    }

    private fun renderHomeState(hasCurrentTrip: Boolean) {
        binding.homeEmptySection.visibility = if (hasCurrentTrip) View.GONE else View.VISIBLE

        val contentVisibility = if (hasCurrentTrip) View.VISIBLE else View.GONE
        binding.currentTripSection.visibility = contentVisibility
        binding.currentTripDivider.visibility = contentVisibility
        binding.nextScheduleSection.visibility = contentVisibility
        binding.nextScheduleDivider.visibility = contentVisibility
        binding.routeOverviewSection.visibility = contentVisibility
        binding.routeOverviewDivider.visibility = contentVisibility
        binding.todayContextSection.visibility = contentVisibility
        binding.todayContextDivider.visibility = contentVisibility
        binding.todaySummarySection.visibility = contentVisibility
        binding.todaySummaryDivider.visibility = contentVisibility
        binding.addButton.visibility = contentVisibility
        binding.todayRecordsSection.visibility = contentVisibility
    }

    private fun setTodayDateUI() {
        val calendar = Calendar.getInstance()
        binding.calMonth.text = "${calendar.get(Calendar.MONTH) + 1}월"
        binding.calDay.text = "${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun calculateExpenseSummary(date: String) {
        expenseViewModel.getTotalExpenseByDate(date).observe(viewLifecycleOwner) { totalExpense ->
            val formattedExpense = "₩${totalExpense ?: 0}"
            binding.todayExpense.text = formattedExpense
            Log.d("HomeFragment", "Total Expense: $formattedExpense")
        }

        expenseViewModel.getTotalExpenseAll().observe(viewLifecycleOwner) { totalExpense ->
            val formattedExpense = "₩${totalExpense ?: 0}"
            binding.todayIncome.text = formattedExpense
            Log.d("HomeFragment", "Total Cumulative Expense: $formattedExpense")
        }
    }

    private fun loadTransactions(date: String) {
        expenseViewModel.getExpensesByDate(date).observe(viewLifecycleOwner) { expenses ->
            Log.d("HomeFragment", "Expenses: $expenses")

            val transactionItems = expenses.map { TransactionItem.ExpenseItem(it) }
                .sortedByDescending { it.date }

            adapter = TransactionAdapter(transactionItems)
            binding.todayList.adapter = adapter
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
