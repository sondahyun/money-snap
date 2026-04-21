package com.example.tripline.ui.calendar

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.tripline.R
import com.example.tripline.databinding.CalendarDayItemBinding
import com.example.tripline.ui.expense.ExpenseViewModel
import com.example.tripline.ui.income.IncomeViewModel
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<LocalDate?>,
    private val incomeViewModel: IncomeViewModel,
    private val expenseViewModel: ExpenseViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onItemListener: OnItemListener
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition: Int = days.indexOf(LocalDate.now()) // 오늘 날짜 기본 선택

    interface OnItemListener {
        fun onItemClick(date: LocalDate?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = CalendarDayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        holder.bind(date, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onItemListener.onItemClick(date)
        }
    }

    override fun getItemCount(): Int = days.size

    inner class CalendarViewHolder(private val binding: CalendarDayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: LocalDate?, isSelected: Boolean) {
            if (date == null) {
                binding.tvCellDay.text = ""
                binding.tvDayPlus.visibility = View.GONE
                binding.tvDayMinus.visibility = View.GONE
                binding.root.setBackgroundResource(0)
                return
            }

            binding.tvCellDay.text = date.dayOfMonth.toString()
            binding.tvDayPlus.visibility = View.VISIBLE
            binding.tvDayMinus.visibility = View.VISIBLE

            binding.root.setBackgroundResource(if (isSelected) R.drawable.bg_selected_date else R.drawable.bg_tripline_day)
            val dayTextColor = if (isSelected) R.color.white else R.color.tripline_text_primary
            val plusTextColor = if (isSelected) R.color.white else R.color.tripline_teal
            val minusTextColor = if (isSelected) R.color.white else R.color.tripline_coral
            binding.tvCellDay.setTextColor(ContextCompat.getColor(binding.root.context, dayTextColor))
            binding.tvDayPlus.setTextColor(ContextCompat.getColor(binding.root.context, plusTextColor))
            binding.tvDayMinus.setTextColor(ContextCompat.getColor(binding.root.context, minusTextColor))

            incomeViewModel.getTotalIncomeByDate(date.toString())
                .observe(lifecycleOwner) { totalIncome ->
                    binding.tvDayPlus.text = "+${formatCurrency(totalIncome)}"
                }

            expenseViewModel.getTotalExpenseByDate(date.toString())
                .observe(lifecycleOwner) { totalExpense ->
                    binding.tvDayMinus.text = "-${formatCurrency(totalExpense)}"
                }
        }

        private fun formatCurrency(amount: Int?): String {
            return if (amount != null) {
                String.format("%,d", amount)
            } else {
                "0"
            }
        }
    }
}
