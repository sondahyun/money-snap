package com.example.tripline.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tripline.PrototypeScreenActivity
import com.example.tripline.TriplineApplication
import com.example.tripline.databinding.FragmentCalendarBinding
import com.example.tripline.ui.expense.ExpenseViewModel
import com.example.tripline.ui.expense.ExpenseViewModelFactory
import com.example.tripline.ui.home.TransactionAdapter
import com.example.tripline.ui.home.TransactionItem
import com.example.tripline.ui.income.IncomeViewModel
import com.example.tripline.ui.income.IncomeViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.text.NumberFormat

class CalendarFragment : Fragment(), CalendarAdapter.OnItemListener {
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var selectedDate: LocalDate
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var isWeekMode = false
    private var monthExpensesLiveData: LiveData<List<com.example.tripline.data.entity.Expense>>? = null
    private val monthExpensesObserver = Observer<List<com.example.tripline.data.entity.Expense>> { expenses ->
        val total = expenses.sumOf { it.amount }
        val topCategory = expenses
            .groupBy { it.category.ifBlank { "기타" } }
            .mapValues { (_, values) -> values.sumOf { it.amount } }
            .maxByOrNull { it.value }
            ?.key
            ?: "기록 없음"

        binding.tvMonthTotal.text = "₩${NumberFormat.getNumberInstance().format(total)}"
        binding.tvMonthTopCategory.text =
            if (expenses.isEmpty()) "아직 지출 없음" else "최다 소비 $topCategory"
    }

    private val incomeViewModel: IncomeViewModel by viewModels {
        IncomeViewModelFactory((requireActivity().application as TriplineApplication).incomeRepository)
    }

    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((requireActivity().application as TriplineApplication).expenseRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        selectedDate = LocalDate.now()
        transactionAdapter = TransactionAdapter(emptyList())
        binding.selectedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.selectedRecyclerView.adapter = transactionAdapter
        bottomSheetBehavior = BottomSheetBehavior.from(binding.calendarBottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            skipCollapsed = false
            isDraggable = true
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> updateCalendarMode(true)
                    BottomSheetBehavior.STATE_COLLAPSED -> updateCalendarMode(false)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0.28f && !isWeekMode) {
                    updateCalendarMode(true)
                } else if (slideOffset <= 0.12f && isWeekMode) {
                    updateCalendarMode(false)
                }
            }
        })

        setMonthView()

        // 기본으로 오늘 날짜를 선택
        onItemClick(selectedDate)

        binding.btnPreviousMonth.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            setMonthView()
        }

        binding.btnNextMonth.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            setMonthView()
        }

        binding.buttonCalendarAddExpense.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.EXPENSE_ENTRY
                )
            )
        }

        return binding.root
    }

    private fun setMonthView() {
        binding.tvMonth.text = monthYearFromDate(selectedDate)
        updateMonthSummary(selectedDate)
        val daysInMonth = generateDaysInMonthArray(selectedDate)

        val calendarAdapter = CalendarAdapter(
            daysInMonth,
            incomeViewModel,
            expenseViewModel,
            viewLifecycleOwner,
            selectedDate,
            this
        )
        val layoutManager = GridLayoutManager(requireContext(), 7)
        binding.calendarRecyclerView.layoutManager = layoutManager
        binding.calendarRecyclerView.adapter = calendarAdapter
        setWeekView()
    }

    private fun setWeekView() {
        val weekDays = generateDaysInWeekArray(selectedDate)
        val weekAdapter = CalendarAdapter(
            weekDays,
            incomeViewModel,
            expenseViewModel,
            viewLifecycleOwner,
            selectedDate,
            this
        )
        binding.weekCalendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.weekCalendarRecyclerView.adapter = weekAdapter
    }

    private fun generateDaysInMonthArray(date: LocalDate): List<LocalDate?> {
        val daysInMonthArray = mutableListOf<LocalDate?>()

        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstOfMonth = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7

        for (i in 1 until dayOfWeek) {
            daysInMonthArray.add(null)
        }

        for (day in 1..daysInMonth) {
            daysInMonthArray.add(LocalDate.of(selectedDate.year, selectedDate.month, day))
        }

        while (daysInMonthArray.size % 7 != 0) {
            daysInMonthArray.add(null)
        }

        return daysInMonthArray
    }

    private fun generateDaysInWeekArray(date: LocalDate): List<LocalDate?> {
        val startOffset = date.dayOfWeek.value % 7
        val startOfWeek = date.minusDays(startOffset.toLong())
        return (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("M월")
        return date.format(formatter)
    }

    private fun updateMonthSummary(date: LocalDate) {
        val monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        monthExpensesLiveData?.removeObservers(viewLifecycleOwner)
        monthExpensesLiveData = expenseViewModel.getExpensesByMonth(monthKey)
        monthExpensesLiveData?.observe(viewLifecycleOwner, monthExpensesObserver)
    }

    override fun onItemClick(date: LocalDate?) {
        if (date != null) {
            selectedDate = date
            val formattedDate = date.format(DateTimeFormatter.ofPattern("d일 E요일"))
            binding.tvSelectedDay.text = formattedDate

            loadTransactionsForDate(date.toString())
            if (isWeekMode) {
                setWeekView()
            } else {
                setMonthView()
            }
        }
    }

    private fun updateCalendarMode(showWeekOnly: Boolean) {
        if (isWeekMode == showWeekOnly) return
        isWeekMode = showWeekOnly
        binding.calendarRecyclerView.visibility = if (showWeekOnly) View.GONE else View.VISIBLE
        binding.weekCalendarRecyclerView.visibility = if (showWeekOnly) View.VISIBLE else View.GONE
        if (showWeekOnly) {
            setWeekView()
        }
    }

    private fun loadTransactionsForDate(date: String) {
        incomeViewModel.getIncomesByDate(date).observe(viewLifecycleOwner) { incomes ->
            expenseViewModel.getExpensesByDate(date).observe(viewLifecycleOwner) { expenses ->
                val transactionItems = (incomes.map { TransactionItem.IncomeItem(it) } +
                        expenses.map { TransactionItem.ExpenseItem(it) })
                    .sortedByDescending { it.date }

                transactionAdapter = TransactionAdapter(transactionItems)
                binding.selectedRecyclerView.adapter = transactionAdapter
                binding.tvCalendarDetailMeta.text =
                    "${transactionItems.size}건 · ${
                        NumberFormat.getNumberInstance().format(expenses.sumOf { it.amount })
                    }원"
            }
        }
    }
}
