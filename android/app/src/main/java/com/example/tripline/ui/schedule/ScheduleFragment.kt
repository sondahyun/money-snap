package com.example.tripline.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.tripline.MainActivity
import com.example.tripline.PrototypeScreenActivity
import com.example.tripline.R
import com.example.tripline.databinding.FragmentScheduleBinding
import com.example.tripline.ui.common.PdfShareBottomSheetFragment
import com.example.tripline.ui.common.ScheduleMemoBottomSheetFragment
import com.example.tripline.ui.common.SchedulePlaceBottomSheetFragment

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val hasScheduleMock = true

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
                menu.add(0, 3, 2, "여행 캘린더")
                menu.add(0, 4, 3, "날씨")
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> openScreen(PrototypeScreenActivity.Screen.TRIP_EDIT)
                        2 -> openScreen(PrototypeScreenActivity.Screen.CHECKLIST)
                        3 -> openScreen(PrototypeScreenActivity.Screen.TRIP_CALENDAR)
                        4 -> openScreen(PrototypeScreenActivity.Screen.WEATHER)
                    }
                    true
                }
                show()
            }
        }

        binding.mapPreviewContainer.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.TRIP_ROUTE_MAP)
        }
        binding.buttonEditDay1.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.SCHEDULE_EDIT)
        }
        binding.cardPlaceAirport.setOnClickListener {
            openPlaceSheet(
                placeName = "상하이 홍차오 국제공항",
                placeMeta = "교통시설 · 홍차오 공항 주변",
                placeHours = "운영시간 00:00 - 24:00"
            )
        }
        binding.cardPlaceHotel.setOnClickListener {
            openPlaceSheet(
                placeName = "캄파닐레 호텔 상하이 번드",
                placeMeta = "3성급 · 인민 광장 주변",
                placeHours = "체크인 15:00"
            )
        }
        binding.cardMemoDay1.setOnClickListener {
            openMemoSheet("해 질 무렵 와이탄 쪽으로 이동하기", "18:00")
        }
        binding.cardPlaceWaitan.setOnClickListener {
            openPlaceSheet(
                placeName = "와이탄",
                placeMeta = "관광명소 · 인민 광장 주변 · 예약가능",
                placeHours = "영업시간 09:00 - 22:00"
            )
        }
        binding.cardPlaceRestaurant.setOnClickListener {
            openPlaceSheet(
                placeName = "남상만두 예원 점",
                placeMeta = "음식점 · 인민 광장 주변 · 예약가능",
                placeHours = "영업시간 08:30 - 21:00"
            )
        }
        binding.cardMemoDay2.setOnClickListener {
            openMemoSheet("비 오면 근처 실내 일정으로 바로 변경", "")
        }
        binding.buttonAddPlaceDay1.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonAddMemoDay1.setOnClickListener {
            openMemoSheet("메모 입력", "")
        }
        binding.buttonAddPlaceDay2.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonAddMemoDay2.setOnClickListener {
            openMemoSheet("메모 입력", "")
        }
        binding.buttonScheduleEmptyCreateTrip.setOnClickListener {
            openScreen(PrototypeScreenActivity.Screen.TRIP_CREATE)
        }
        binding.buttonScheduleEmptyOpenLocker.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_locker)
        }

        renderScheduleState(hasScheduleMock)

        return binding.root
    }

    private fun renderScheduleState(hasSchedule: Boolean) {
        binding.scheduleContentSection.isVisible = hasSchedule
        binding.scheduleEmptySection.isVisible = !hasSchedule
        binding.buttonShare.isVisible = hasSchedule
    }

    private fun openScreen(screen: PrototypeScreenActivity.Screen) {
        startActivity(PrototypeScreenActivity.intent(requireContext(), screen))
    }

    private fun openMemoSheet(memoText: String, memoTime: String) {
        ScheduleMemoBottomSheetFragment.newInstance(
            memoText = memoText,
            memoTime = memoTime
        ).show(childFragmentManager, "schedule_memo")
    }

    private fun openPlaceSheet(
        placeName: String,
        placeMeta: String,
        placeHours: String
    ) {
        SchedulePlaceBottomSheetFragment.newInstance(
            placeName = placeName,
            placeMeta = placeMeta,
            placeHours = placeHours
        ).show(childFragmentManager, "schedule_place")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
