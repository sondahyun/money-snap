package com.example.tripline.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.tripline.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScheduleTimeBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_schedule_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val time = arguments?.getString(ARG_TIME).orEmpty()
        view.findViewById<TextView>(R.id.textScheduleTimeValue)?.text = time.ifBlank { "18:00" }
        view.findViewById<View>(R.id.buttonScheduleTimeSave)?.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_TIME = "time"

        fun newInstance(time: String = ""): ScheduleTimeBottomSheetFragment {
            return ScheduleTimeBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TIME, time)
                }
            }
        }
    }
}
