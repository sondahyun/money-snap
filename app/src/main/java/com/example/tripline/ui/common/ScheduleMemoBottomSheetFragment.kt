package com.example.tripline.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.tripline.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScheduleMemoBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_schedule_memo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val memoText = arguments?.getString(ARG_MEMO_TEXT).orEmpty()
        val memoTime = arguments?.getString(ARG_MEMO_TIME).orEmpty()

        view.findViewById<TextView>(R.id.textMemoContent)?.text =
            memoText.ifBlank { "메모 입력" }
        view.findViewById<TextView>(R.id.textMemoTime)?.text =
            memoTime.ifBlank { "시간 추가" }

        view.findViewById<View>(R.id.buttonMemoTimeAdd)?.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_MEMO_TEXT = "memo_text"
        private const val ARG_MEMO_TIME = "memo_time"

        fun newInstance(memoText: String, memoTime: String = ""): ScheduleMemoBottomSheetFragment {
            return ScheduleMemoBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MEMO_TEXT, memoText)
                    putString(ARG_MEMO_TIME, memoTime)
                }
            }
        }
    }
}
