package com.example.tripline.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tripline.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChecklistItemActionsBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_checklist_item_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.buttonEditChecklistItem)?.setOnClickListener {
            dismiss()
        }
        view.findViewById<View>(R.id.buttonDeleteChecklistItem)?.setOnClickListener {
            dismiss()
        }
    }
}
