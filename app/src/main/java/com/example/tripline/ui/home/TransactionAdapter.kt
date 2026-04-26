package com.example.tripline.ui.home

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tripline.R
import com.example.tripline.data.entity.Expense
import com.example.tripline.data.entity.Income
import com.example.tripline.databinding.TodayListExpenseBinding
import com.example.tripline.databinding.TodayListIncomeBinding

sealed class TransactionItem {
    data class IncomeItem(val income: Income) : TransactionItem()
    data class ExpenseItem(val expense: Expense) : TransactionItem()

    val date: String
        get() = when (this) {
            is IncomeItem -> this.income.date
            is ExpenseItem -> this.expense.date
        }
}

class TransactionAdapter(private val items: List<TransactionItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_INCOME = 1
        private const val VIEW_TYPE_EXPENSE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionItem.IncomeItem -> VIEW_TYPE_INCOME
            is TransactionItem.ExpenseItem -> VIEW_TYPE_EXPENSE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_INCOME -> IncomeViewHolder(
                TodayListIncomeBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_EXPENSE -> ExpenseViewHolder(
                TodayListExpenseBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IncomeViewHolder -> holder.bind((items[position] as TransactionItem.IncomeItem).income)
            is ExpenseViewHolder -> holder.bind((items[position] as TransactionItem.ExpenseItem).expense)
        }
    }

    override fun getItemCount() = items.size

    class IncomeViewHolder(private val binding: TodayListIncomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(income: Income) {
            binding.incomeAmount.text = "+₩${income.amount}"
            binding.incomePlace.text = income.place
            binding.incomeType.text = "환불/정산"
            binding.incomeDate.text = income.date.replace("-", ".")
            binding.incomeIcon.setImageResource(resolveIncomeIcon(income))
            if (income.description.isNullOrBlank()) {
                binding.incomeDescription.visibility = View.GONE
            } else {
                binding.incomeDescription.visibility = View.VISIBLE
                binding.incomeDescription.text = income.description
            }
        }
    }

    class ExpenseViewHolder(private val binding: TodayListExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.expenseAmount.text = "-₩${expense.amount}"
            binding.expensePlace.text = expense.place ?: expense.category
            binding.expenseCategory.text = expense.category
            binding.expenseDate.text = expense.date.replace("-", ".")
            binding.expenseIcon.setImageResource(resolveExpenseIcon(expense))
            if (expense.description.isNullOrBlank()) {
                binding.expenseDescription.visibility = View.GONE
            } else {
                binding.expenseDescription.visibility = View.VISIBLE
                binding.expenseDescription.text = expense.description
            }
        }
    }
}

private fun resolveExpenseIcon(expense: Expense): Int {
    val bucket = listOfNotNull(expense.category, expense.place, expense.description)
        .joinToString(" ")
        .lowercase()

    return when {
        bucket.contains("카드") || bucket.contains("visa") || bucket.contains("master") || bucket.contains("신용") ->
            R.drawable.ic_tripline_payment_card
        bucket.contains("환전") || bucket.contains("현금") || bucket.contains("atm") || bucket.contains("cash") ->
            R.drawable.ic_tripline_payment_cash
        bucket.contains("pay") || bucket.contains("페이") || bucket.contains("머니") || bucket.contains("alipay") || bucket.contains("카카오") || bucket.contains("토스") ->
            R.drawable.ic_tripline_payment_wallet
        else -> R.drawable.ic_tripline_payment_card
    }
}

private fun resolveIncomeIcon(income: Income): Int {
    val bucket = listOfNotNull(income.place, income.description).joinToString(" ").lowercase()
    return when {
        bucket.contains("환불") || bucket.contains("정산") || bucket.contains("pay") || bucket.contains("페이") ->
            R.drawable.ic_tripline_payment_wallet
        bucket.contains("현금") || bucket.contains("atm") || bucket.contains("cash") ->
            R.drawable.ic_tripline_payment_cash
        else -> R.drawable.ic_tripline_payment_card
    }
}
