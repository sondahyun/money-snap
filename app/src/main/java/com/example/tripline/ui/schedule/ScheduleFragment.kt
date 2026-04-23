package com.example.tripline.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.tripline.MainActivity
import com.example.tripline.PrototypeScreenActivity
import com.example.tripline.R
import com.example.tripline.databinding.FragmentScheduleBinding
import com.example.tripline.ui.common.PdfShareBottomSheetFragment

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_locker)
        }

        binding.buttonShare.setOnClickListener {
            PdfShareBottomSheetFragment().show(childFragmentManager, "pdf_share")
        }

        binding.buttonMore.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                menu.add(0, 1, 0, "여행 정보 수정")
                menu.add(0, 2, 1, "준비물 체크리스트")
                menu.add(0, 3, 2, "날씨")
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> openScreen(PrototypeScreenActivity.Screen.TRIP_EDIT)
                        2 -> openScreen(PrototypeScreenActivity.Screen.CHECKLIST)
                        3 -> openScreen(PrototypeScreenActivity.Screen.WEATHER)
                    }
                    true
                }
                show()
            }
        }

        binding.buttonShowFullMap.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonEditDay1.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.SCHEDULE_EDIT)
        }
        binding.buttonEditDay2.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.SCHEDULE_EDIT)
        }
        binding.cardPlaceAirport.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_DETAIL)
        }
        binding.cardPlaceHotel.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_DETAIL)
        }
        binding.cardPlaceWaitan.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_DETAIL)
        }
        binding.cardPlaceRestaurant.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_DETAIL)
        }
        binding.buttonAddPlaceDay1.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonAddMemoDay1.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.MEMO_EDIT)
        }

        return binding.root
    }

    private fun openScreen(screen: PrototypeScreenActivity.Screen) {
        startActivity(PrototypeScreenActivity.intent(requireContext(), screen))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
