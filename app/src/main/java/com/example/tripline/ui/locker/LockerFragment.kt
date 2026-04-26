package com.example.tripline.ui.locker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tripline.MainActivity
import com.example.tripline.PrototypeScreenActivity
import com.example.tripline.R
import com.example.tripline.databinding.FragmentLockerBinding

class LockerFragment : Fragment() {

    private var _binding: FragmentLockerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockerBinding.inflate(inflater, container, false)

        binding.buttonCreateTrip.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.TRIP_CREATE
                )
            )
        }

        binding.buttonLockerSearch.setOnClickListener {
            startActivity(
                PrototypeScreenActivity.intent(
                    requireContext(),
                    PrototypeScreenActivity.Screen.TRIP_SEARCH
                )
            )
        }

        val openSchedule = {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_schedule)
        }
        binding.tripRowShanghai.setOnClickListener { openSchedule() }
        binding.tripRowKyoto.setOnClickListener { openSchedule() }
        binding.tripRowTokyo.setOnClickListener { openSchedule() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
