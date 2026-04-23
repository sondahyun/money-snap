package com.example.tripline.ui.funds

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tripline.PrototypeScreenActivity
import com.example.tripline.R
import com.example.tripline.databinding.FragmentFundsBinding

class FundsFragment : Fragment() {

    private var _binding: FragmentFundsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFundsBinding.inflate(inflater, container, false)

        binding.buttonFundsAddExpense.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.EXPENSE_ENTRY)
        }
        binding.rowFundsPlaceWaitan.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.EXPENSE_DETAIL)
        }
        binding.rowFundsPlaceExchange.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.EXPENSE_DETAIL)
        }
        binding.buttonFundsPlaceTab.setOnClickListener { showPlaceMode() }
        binding.buttonFundsCategoryTab.setOnClickListener { showCategoryMode() }

        showPlaceMode()

        return binding.root
    }

    private fun showPlaceMode() {
        updateTabSelection(isPlaceSelected = true)
        binding.textFundsListTitle.text = "장소별 지출"
        binding.textFundsRow1Title.text = "와이탄"
        binding.textFundsRow1Meta.text = "지출 4건"
        binding.textFundsRow1Amount.text = "₩132,800"
        binding.textFundsRow2Title.text = "환전"
        binding.textFundsRow2Meta.text = "지출 1건"
        binding.textFundsRow2Amount.text = "₩200,000"
    }

    private fun showCategoryMode() {
        updateTabSelection(isPlaceSelected = false)
        binding.textFundsListTitle.text = "상위 카테고리별 지출"
        binding.textFundsRow1Title.text = "식비"
        binding.textFundsRow1Meta.text = "지출 4건"
        binding.textFundsRow1Amount.text = "₩132,800"
        binding.textFundsRow2Title.text = "환전"
        binding.textFundsRow2Meta.text = "지출 1건"
        binding.textFundsRow2Amount.text = "₩200,000"
    }

    private fun updateTabSelection(isPlaceSelected: Boolean) {
        val blue = ContextCompat.getColor(requireContext(), R.color.schedule_minimal_blue)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val textSecondary = ContextCompat.getColor(requireContext(), R.color.tripline_text_secondary)

        binding.buttonFundsPlaceTab.setBackgroundResource(
            if (isPlaceSelected) R.drawable.bg_tripline_pill
            else R.drawable.bg_tripline_secondary_action_neutral
        )
        binding.buttonFundsPlaceTab.backgroundTintList =
            if (isPlaceSelected) ColorStateList.valueOf(blue) else null
        binding.buttonFundsPlaceTab.setTextColor(if (isPlaceSelected) white else textSecondary)

        binding.buttonFundsCategoryTab.setBackgroundResource(
            if (isPlaceSelected) R.drawable.bg_tripline_secondary_action_neutral
            else R.drawable.bg_tripline_pill
        )
        binding.buttonFundsCategoryTab.backgroundTintList =
            if (isPlaceSelected) null else ColorStateList.valueOf(blue)
        binding.buttonFundsCategoryTab.setTextColor(if (isPlaceSelected) textSecondary else white)
    }

    private fun openScreen(screen: PrototypeScreenActivity.Screen) {
        startActivity(PrototypeScreenActivity.intent(requireContext(), screen))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
