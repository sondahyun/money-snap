package com.example.tripline.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tripline.TriplineScreenActivity
import com.example.tripline.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        binding.buttonClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.mypageScrim.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val openSettings = {
            startActivity(
                TriplineScreenActivity.intent(
                    requireContext(),
                    TriplineScreenActivity.Screen.SETTINGS
                )
            )
        }

        binding.buttonSettingsTop.setOnClickListener { openSettings() }
        binding.rowProfileEdit.setOnClickListener { openSettings() }
        binding.rowAppSettings.setOnClickListener { openSettings() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
