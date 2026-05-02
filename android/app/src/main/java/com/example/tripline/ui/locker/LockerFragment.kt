package com.example.tripline.ui.locker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.tripline.MainActivity
import com.example.tripline.R
import com.example.tripline.TriplineScreenActivity
import com.example.tripline.TriplineUiStateStore
import com.example.tripline.databinding.FragmentLockerBinding
import com.example.tripline.ui.common.TripDeleteConfirmBottomSheetFragment

class LockerFragment : Fragment() {

    private var _binding: FragmentLockerBinding? = null
    private val binding get() = _binding!!
    private var openedTripActions: TripActionViews? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockerBinding.inflate(inflater, container, false)

        binding.buttonCreateTrip.setOnClickListener {
            startActivity(
                TriplineScreenActivity.intent(
                    requireContext(),
                    TriplineScreenActivity.Screen.TRIP_CREATE
                )
            )
        }

        binding.buttonLockerSearch.setOnClickListener {
            startActivity(
                TriplineScreenActivity.intent(
                    requireContext(),
                    TriplineScreenActivity.Screen.TRIP_SEARCH
                )
            )
        }

        val openSchedule = {
            TriplineUiStateStore.selectExistingTrip(requireContext())
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_schedule)
            Unit
        }
        bindTripActions(
            row = binding.tripRowShanghai,
            content = binding.tripContentShanghai,
            moreButton = binding.buttonTripMoreShanghai,
            actionPanel = binding.tripActionPanelShanghai,
            editButton = binding.buttonTripEditShanghai,
            deleteButton = binding.buttonTripDeleteShanghai,
            openSchedule = openSchedule
        )
        bindTripActions(
            row = binding.tripRowKyoto,
            content = binding.tripContentKyoto,
            moreButton = binding.buttonTripMoreKyoto,
            actionPanel = binding.tripActionPanelKyoto,
            editButton = binding.buttonTripEditKyoto,
            deleteButton = binding.buttonTripDeleteKyoto,
            openSchedule = openSchedule
        )
        bindTripActions(
            row = binding.tripRowTokyo,
            content = binding.tripContentTokyo,
            moreButton = binding.buttonTripMoreTokyo,
            actionPanel = binding.tripActionPanelTokyo,
            editButton = binding.buttonTripEditTokyo,
            deleteButton = binding.buttonTripDeleteTokyo,
            openSchedule = openSchedule
        )

        return binding.root
    }

    private fun bindTripActions(
        row: View,
        content: View,
        moreButton: View,
        actionPanel: View,
        editButton: View,
        deleteButton: View,
        openSchedule: () -> Unit
    ) {
        val actions = TripActionViews(content, moreButton, actionPanel)

        row.setOnClickListener {
            if (openedTripActions == actions) {
                collapseTripActions(actions)
            } else {
                openSchedule()
            }
        }

        moreButton.setOnClickListener {
            if (openedTripActions == actions) {
                collapseTripActions(actions)
            } else {
                openedTripActions?.let(::collapseTripActions)
                expandTripActions(actions)
            }
        }

        editButton.setOnClickListener {
            collapseTripActions(actions)
            startActivity(
                TriplineScreenActivity.intent(
                    requireContext(),
                    TriplineScreenActivity.Screen.TRIP_EDIT
                )
            )
        }

        deleteButton.setOnClickListener {
            collapseTripActions(actions)
            TripDeleteConfirmBottomSheetFragment().show(childFragmentManager, "trip_delete_confirm")
        }
    }

    private fun expandTripActions(actions: TripActionViews) {
        openedTripActions = actions

        actions.content.animate()
            .translationX(-dp(14).toFloat())
            .setDuration(180L)
            .setInterpolator(DecelerateInterpolator())
            .start()

        actions.moreButton.animate()
            .alpha(0f)
            .translationX(-dp(18).toFloat())
            .setDuration(120L)
            .withEndAction {
                actions.moreButton.visibility = View.GONE
                actions.moreButton.alpha = 1f
                actions.moreButton.translationX = 0f
            }
            .start()

        actions.actionPanel.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationX = dp(34).toFloat()
            animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(180L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun collapseTripActions(actions: TripActionViews) {
        if (openedTripActions == actions) {
            openedTripActions = null
        }

        actions.content.animate()
            .translationX(0f)
            .setDuration(160L)
            .setInterpolator(DecelerateInterpolator())
            .start()

        actions.actionPanel.animate()
            .alpha(0f)
            .translationX(dp(34).toFloat())
            .setDuration(130L)
            .withEndAction {
                actions.actionPanel.visibility = View.GONE
            }
            .start()

        actions.moreButton.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationX = -dp(12).toFloat()
            animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(150L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        openedTripActions = null
        _binding = null
    }

    private data class TripActionViews(
        val content: View,
        val moreButton: View,
        val actionPanel: View
    )
}
