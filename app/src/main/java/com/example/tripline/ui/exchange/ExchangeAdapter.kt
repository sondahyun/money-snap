package com.example.tripline.ui.exchange

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tripline.R
import com.example.tripline.databinding.ItemExchangeRateBinding

class ExchangeAdapter(private var exchanges: List<ExchangeRateItemUiModel>) :
    RecyclerView.Adapter<ExchangeAdapter.ExchangeViewHolder>() {

    inner class ExchangeViewHolder(private val binding: ItemExchangeRateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exchange: ExchangeRateItemUiModel) {
            binding.countryName.text = exchange.currencyName
            binding.currencyName.text = exchange.currencyCode
            binding.exchangeRate.text = exchange.currentRateText
            binding.changeLabel.text = exchange.comparisonLabelText

            when (exchange.changeDirection) {
                ChangeDirection.UP -> {
                    val upColor = binding.root.context.getColor(R.color.tripline_market_up)
                    binding.changeValue.setTextColor(upColor)
                    binding.changePercent.setTextColor(upColor)
                    binding.changeContainer.visibility = View.VISIBLE
                }

                ChangeDirection.DOWN -> {
                    val downColor = binding.root.context.getColor(R.color.tripline_market_down)
                    binding.changeValue.setTextColor(downColor)
                    binding.changePercent.setTextColor(downColor)
                    binding.changeContainer.visibility = View.VISIBLE
                }

                ChangeDirection.FLAT -> {
                    val flatColor = binding.root.context.getColor(R.color.tripline_text_secondary)
                    binding.changeValue.setTextColor(flatColor)
                    binding.changePercent.setTextColor(flatColor)
                    binding.changeContainer.visibility = View.VISIBLE
                }

                ChangeDirection.NONE -> {
                    binding.changeContainer.visibility = View.GONE
                }
            }

            binding.changeValue.text = exchange.changeValueText
            binding.changePercent.text = exchange.changePercentText.orEmpty()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeViewHolder {
        val binding = ItemExchangeRateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExchangeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExchangeViewHolder, position: Int) {
        holder.bind(exchanges[position])
    }

    override fun getItemCount(): Int = exchanges.size

    // 데이터 업데이트 메서드
    fun updateData(newExchanges: List<ExchangeRateItemUiModel>) {
        exchanges = newExchanges
        notifyDataSetChanged()
    }
}
