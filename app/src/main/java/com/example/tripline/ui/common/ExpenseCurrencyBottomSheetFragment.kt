package com.example.tripline.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tripline.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ExpenseCurrencyBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_expense_currency, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.rowCurrencyKrw)?.setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.rowCurrencyCny)?.setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.rowCurrencyUsd)?.setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.buttonCurrencyAdd)?.setOnClickListener { dismiss() }
    }
}
