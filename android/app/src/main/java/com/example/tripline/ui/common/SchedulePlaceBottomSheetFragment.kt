package com.example.tripline.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.tripline.TriplineScreenActivity
import com.example.tripline.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SchedulePlaceBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_schedule_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeName = arguments?.getString(ARG_PLACE_NAME).orEmpty()
        val placeMeta = arguments?.getString(ARG_PLACE_META).orEmpty()
        val placeHours = arguments?.getString(ARG_PLACE_HOURS).orEmpty()

        view.findViewById<TextView>(R.id.textPlaceSheetTitle)?.text = placeName
        view.findViewById<TextView>(R.id.textPlaceSheetMeta)?.text = placeMeta
        view.findViewById<TextView>(R.id.textPlaceSheetHours)?.text = placeHours

        view.findViewById<View>(R.id.buttonPlaceSheetDetail)?.setOnClickListener {
            openPlaceDetail()
        }
        view.findViewById<View>(R.id.buttonPlaceDetailBottom)?.setOnClickListener {
            openPlaceDetail()
        }
        view.findViewById<View>(R.id.buttonPlaceExpenseAdd)?.setOnClickListener {
            startActivity(
                TriplineScreenActivity.intent(
                    requireContext(),
                    TriplineScreenActivity.Screen.EXPENSE_ENTRY
                )
            )
            dismiss()
        }
        view.findViewById<View>(R.id.buttonPlaceTimeAdd)?.setOnClickListener {
            val manager = parentFragmentManager
            dismiss()
            Handler(Looper.getMainLooper()).post {
                ScheduleTimeBottomSheetFragment.newInstance("18:00")
                    .show(manager, "schedule_time")
            }
        }
        view.findViewById<View>(R.id.buttonPlaceMemoAdd)?.setOnClickListener {
            val manager = parentFragmentManager
            dismiss()
            Handler(Looper.getMainLooper()).post {
                ScheduleMemoBottomSheetFragment.newInstance(
                    memoText = "메모 입력",
                    memoTime = ""
                ).show(manager, "schedule_memo")
            }
        }
    }

    private fun openPlaceDetail() {
        startActivity(
            TriplineScreenActivity.intent(
                requireContext(),
                TriplineScreenActivity.Screen.PLACE_DETAIL
            )
        )
        dismiss()
    }

    companion object {
        private const val ARG_PLACE_NAME = "place_name"
        private const val ARG_PLACE_META = "place_meta"
        private const val ARG_PLACE_HOURS = "place_hours"

        fun newInstance(
            placeName: String,
            placeMeta: String,
            placeHours: String
        ): SchedulePlaceBottomSheetFragment {
            return SchedulePlaceBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PLACE_NAME, placeName)
                    putString(ARG_PLACE_META, placeMeta)
                    putString(ARG_PLACE_HOURS, placeHours)
                }
            }
        }
    }
}
