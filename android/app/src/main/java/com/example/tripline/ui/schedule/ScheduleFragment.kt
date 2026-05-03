package com.example.tripline.ui.schedule

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.tripline.MainActivity
import com.example.tripline.TriplineScreenActivity
import com.example.tripline.R
import com.example.tripline.TriplineUiStateStore
import com.example.tripline.databinding.FragmentScheduleBinding
import com.example.tripline.ui.common.PdfShareBottomSheetFragment
import com.example.tripline.ui.common.ScheduleMemoBottomSheetFragment
import com.example.tripline.ui.common.SchedulePlaceBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private var isMapPreviewCollapsed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        val navigateBack = {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_locker)
        }
        binding.buttonBack.setOnClickListener { navigateBack() }
        binding.buttonSchedulePinnedBack.setOnClickListener { navigateBack() }

        val openShareSheet = {
            PdfShareBottomSheetFragment().show(childFragmentManager, "pdf_share")
        }
        binding.buttonShare.setOnClickListener { openShareSheet() }
        binding.buttonSchedulePinnedShare.setOnClickListener { openShareSheet() }

        val openRouteMap = {
            openScreen(TriplineScreenActivity.Screen.TRIP_ROUTE_MAP)
        }
        binding.buttonScheduleMapMode.setOnClickListener { openRouteMap() }
        binding.buttonSchedulePinnedMapMode.setOnClickListener { openRouteMap() }

        binding.buttonMore.setOnClickListener(::showTripMenu)
        binding.buttonSchedulePinnedMore.setOnClickListener(::showTripMenu)

        binding.mapPreviewContainer.setOnClickListener {
            openRouteMap()
        }
        binding.buttonScheduleMapCollapse.setOnClickListener {
            setMapPreviewCollapsed(!isMapPreviewCollapsed)
        }
        binding.buttonEditDay1.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.SCHEDULE_EDIT)
        }
        binding.cardPlaceAirport.setOnClickListener {
            openPlaceSheet(
                placeName = "상하이 홍차오 국제공항",
                placeMeta = "관광명소 · 인민 광장 주변",
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
                placeMeta = "관광명소 · 인민 광장 주변",
                placeHours = "영업시간 09:00 - 22:00"
            )
        }
        binding.cardPlaceRestaurant.setOnClickListener {
            openPlaceSheet(
                placeName = "남상만두 예원 점",
                placeMeta = "음식점 · 예원 주변",
                placeHours = "영업시간 08:30 - 21:00"
            )
        }
        binding.cardMemoDay2.setOnClickListener {
            openMemoSheet("비 오면 근처 실내 일정으로 바로 변경", "")
        }
        binding.buttonAddPlaceDay1.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonAddFlightDay1.setOnClickListener {
            showFlightDepartureSheet()
        }
        binding.buttonHeaderAddFlight.setOnClickListener {
            showFlightDepartureSheet()
        }
        binding.buttonAddLodgingDay1.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.LODGING_SEARCH)
        }
        binding.buttonHeaderAddLodging.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.LODGING_SEARCH)
        }
        binding.buttonOpenTripLedger.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.TRIP_LEDGER)
        }
        binding.buttonHeaderOpenTripLedger.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.TRIP_LEDGER)
        }
        binding.buttonHeaderOpenChecklist.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.CHECKLIST)
        }
        binding.buttonAddMemoDay1.setOnClickListener {
            openMemoSheet("메모 입력", "")
        }
        binding.buttonAddPlaceDay2.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonAddMemoDay2.setOnClickListener {
            openMemoSheet("메모 입력", "")
        }
        binding.buttonScheduleNoTripCreate.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.TRIP_CREATE)
        }
        binding.buttonScheduleNoTripOpenLocker.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.fragment_locker)
        }
        binding.buttonScheduleEmptyCreateTrip.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.PLACE_SEARCH)
        }
        binding.buttonScheduleEmptyOpenLocker.setOnClickListener {
            openScreen(TriplineScreenActivity.Screen.OCR_IMPORT)
        }

        binding.scheduleScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            updateExpandedMeta(scrollY)
        }
        updateExpandedMeta(0)
        renderScheduleState(
            hasCurrentTrip = TriplineUiStateStore.hasCurrentTrip(requireContext()),
            hasSchedule = TriplineUiStateStore.hasSchedule(requireContext())
        )

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        _binding?.let {
            renderScheduleState(
                hasCurrentTrip = TriplineUiStateStore.hasCurrentTrip(requireContext()),
                hasSchedule = TriplineUiStateStore.hasSchedule(requireContext())
            )
        }
    }

    private fun renderScheduleState(hasCurrentTrip: Boolean, hasSchedule: Boolean) {
        val hasScheduleContent = hasCurrentTrip && hasSchedule
        binding.scheduleContentSection.isVisible = hasScheduleContent
        binding.scheduleNoTripSection.isVisible = !hasCurrentTrip
        binding.scheduleEmptySection.isVisible = hasCurrentTrip && !hasSchedule
        binding.buttonShare.isVisible = hasScheduleContent
        binding.buttonScheduleMapMode.isVisible = hasScheduleContent
        binding.buttonSchedulePinnedShare.isVisible = hasScheduleContent
        binding.buttonSchedulePinnedMapMode.isVisible = hasScheduleContent
        binding.textSchedulePinnedTitle.isVisible = hasCurrentTrip
        binding.textSchedulePinnedMeta.isVisible = hasCurrentTrip
        binding.buttonSchedulePinnedMore.isVisible = hasCurrentTrip
    }

    private fun updateExpandedMeta(scrollY: Int) {
        val progress = (scrollY / dp(150).toFloat()).coerceIn(0f, 1f)

        binding.scheduleExpandedMetaSection.apply {
            alpha = 1f - progress * 0.85f
            scaleY = 1f - progress * 0.12f
            translationY = -dp(18) * progress
        }

        binding.textTripTitleTop.alpha = 0.45f + progress * 0.55f
        binding.textTripMetaTop.alpha = 0.35f + progress * 0.65f
        binding.textSchedulePinnedTitle.alpha = progress
        binding.textSchedulePinnedMeta.alpha = progress
    }

    private fun showTripMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menu.add(0, 1, 0, "여행 정보 수정")
            menu.add(0, 2, 1, "준비물 체크리스트")
            menu.add(0, 3, 2, "여행 캘린더")
            menu.add(0, 4, 3, "날씨")
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> openScreen(TriplineScreenActivity.Screen.TRIP_EDIT)
                    2 -> openScreen(TriplineScreenActivity.Screen.CHECKLIST)
                    3 -> openScreen(TriplineScreenActivity.Screen.TRIP_CALENDAR)
                    4 -> openScreen(TriplineScreenActivity.Screen.WEATHER)
                }
                true
            }
            show()
        }
    }

    private fun setMapPreviewCollapsed(collapsed: Boolean) {
        isMapPreviewCollapsed = collapsed
        binding.mapPreviewContainer.animate().cancel()
        binding.imageScheduleMapCollapse.animate().cancel()

        binding.imageScheduleMapCollapse.animate()
            .rotation(if (collapsed) 180f else 0f)
            .setDuration(160)
            .start()

        if (collapsed) {
            binding.mapPreviewContainer.animate()
                .alpha(0f)
                .translationY(-dp(12).toFloat())
                .setDuration(180)
                .withEndAction {
                    _binding?.mapPreviewContainer?.isVisible = false
                }
                .start()
            return
        }

        binding.mapPreviewContainer.apply {
            alpha = 0f
            translationY = -dp(12).toFloat()
            isVisible = true
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start()
        }
    }

    private fun openScreen(screen: TriplineScreenActivity.Screen) {
        startActivity(TriplineScreenActivity.intent(requireContext(), screen))
    }

    private fun showFlightDepartureSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(28), dp(28), dp(32))
        }

        content.addView(
            TextView(requireContext()).apply {
                text = "출발일"
                textSize = 16f
                typeface = resources.getFont(R.font.pretendardbold)
                setTextColor(resources.getColor(R.color.schedule_minimal_subtle, null))
            }
        )

        listOf("day1 2.15/일", "day2 2.16/월", "day3 2.17/화", "day4 2.18/수")
            .forEach { label ->
                content.addView(createFlightSheetRow(label) {
                    dialog.dismiss()
                    openScreen(TriplineScreenActivity.Screen.FLIGHT_AIRLINE)
                })
            }

        content.addView(createFlightSheetRow("예약한 항공편 추가하기") {
            dialog.dismiss()
            openScreen(TriplineScreenActivity.Screen.FLIGHT_AIRLINE)
        })

        dialog.setContentView(content)
        dialog.show()
    }

    private fun createFlightSheetRow(label: String, onClick: () -> Unit): TextView {
        return TextView(requireContext()).apply {
            text = label
            gravity = Gravity.CENTER_VERTICAL
            textSize = 19f
            typeface = resources.getFont(R.font.pretendardbold)
            setTextColor(resources.getColor(R.color.schedule_minimal_text, null))
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(66)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
