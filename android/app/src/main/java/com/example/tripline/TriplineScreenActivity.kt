package com.example.tripline

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripline.ui.common.ChecklistItemActionsBottomSheetFragment
import com.example.tripline.ui.common.ExpenseCurrencyBottomSheetFragment
import com.example.tripline.ui.common.PdfShareBottomSheetFragment
import com.example.tripline.ui.common.TripDeleteConfirmBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class TriplineScreenActivity : AppCompatActivity() {

    enum class Screen(@LayoutRes val layoutRes: Int) {
        TRIP_CREATE(R.layout.activity_trip_create),
        TRIP_CREATE_DATE(R.layout.activity_trip_create_date),
        TRIP_CREATE_STYLE(R.layout.activity_trip_create_style),
        TRIP_EDIT(R.layout.activity_trip_edit),
        TRIP_SEARCH(R.layout.activity_trip_search),
        CHECKLIST(R.layout.activity_trip_checklist),
        CHECKLIST_EDIT(R.layout.activity_trip_checklist_edit),
        CHECKLIST_ITEM_ADD(R.layout.activity_trip_checklist_item_add),
        EXPENSE_ENTRY(R.layout.activity_expense_entry),
        EXPENSE_DETAIL(R.layout.activity_expense_detail),
        CATEGORY_MANAGE(R.layout.activity_category_manage),
        MEMO_EDIT(R.layout.activity_memo_edit),
        OCR_IMPORT(R.layout.activity_ocr_import),
        OCR_REVIEW(R.layout.activity_ocr_review),
        WEATHER(R.layout.activity_trip_weather),
        TRIP_CALENDAR(R.layout.activity_trip_calendar),
        TRIP_ROUTE_MAP(R.layout.activity_trip_route_map),
        FLIGHT_AIRLINE(R.layout.activity_flight_airline),
        FLIGHT_NUMBER(R.layout.activity_flight_number),
        FLIGHT_FORM(R.layout.activity_flight_form),
        AIRPORT_SEARCH(R.layout.activity_airport_search),
        LODGING_SEARCH(R.layout.activity_lodging_search),
        LODGING_DATE(R.layout.activity_lodging_date),
        TRIP_LEDGER(R.layout.activity_trip_ledger),
        PLACE_DETAIL(R.layout.fragment_place_detail),
        PLACE_SEARCH(R.layout.fragment_place_search),
        SCHEDULE_EDIT(R.layout.fragment_schedule_edit),
        PROFILE_EDIT(R.layout.activity_profile_edit),
        SETTINGS(R.layout.activity_settings)
    }

    companion object {
        private const val EXTRA_SCREEN = "extra_screen"
        private const val EXTRA_FLIGHT_FORM_READY = "extra_flight_form_ready"

        fun intent(context: Context, screen: Screen): Intent {
            return Intent(context, TriplineScreenActivity::class.java).apply {
                putExtra(EXTRA_SCREEN, screen.name)
            }
        }
    }

    private val screen: Screen by lazy {
        intent.getStringExtra(EXTRA_SCREEN)
            ?.let(Screen::valueOf)
            ?: Screen.TRIP_CREATE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(screen.layoutRes)
        applySystemBarInsets()
        bindScreen()
    }

    private fun applySystemBarInsets() {
        val content = findViewById<ViewGroup>(android.R.id.content)
        val root = content.getChildAt(0) ?: return
        val initialTop = root.paddingTop
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                initialTop + systemBars.top,
                view.paddingRight,
                initialBottom + systemBars.bottom
            )
            insets
        }

        ViewCompat.requestApplyInsets(root)
    }

    private fun bindScreen() {
        when (screen) {
            Screen.TRIP_CREATE -> {
                bindTripCreateCityRows()
                finishOnClick(R.id.buttonTripCreateBack)
                showCityOrderOnClick(R.id.buttonTripCityOrder)
                openScreenOnClick(R.id.buttonTripCreateNext, Screen.TRIP_CREATE_DATE)
            }

            Screen.TRIP_CREATE_DATE -> {
                finishOnClick(R.id.buttonTripDateBack)
                openScreenOnClick(R.id.buttonTripDateDone, Screen.TRIP_CREATE_STYLE)
            }

            Screen.TRIP_CREATE_STYLE -> {
                finishOnClick(R.id.buttonTripStyleBack)
                openTabOnClick(R.id.buttonTripStyleDone, R.id.fragment_schedule)
                openTabOnClick(R.id.buttonTripStyleSkip, R.id.fragment_schedule)
            }

            Screen.TRIP_EDIT -> {
                finishOnClick(R.id.buttonTripEditBack, R.id.buttonTripEditSave)
                showTripDeleteOnClick(R.id.buttonTripEditDelete)
            }

            Screen.TRIP_SEARCH -> {
                finishOnClick(R.id.buttonTripSearchBack)
                openTabOnClick(R.id.tripSearchResultShanghai, R.id.fragment_schedule)
                openTabOnClick(R.id.tripSearchResultKyoto, R.id.fragment_schedule)
                openTabOnClick(R.id.tripSearchResultTokyo, R.id.fragment_schedule)
            }

            Screen.CHECKLIST -> {
                finishOnClick(R.id.buttonChecklistBack)
                openScreenOnClick(R.id.buttonChecklistEdit, Screen.CHECKLIST_EDIT)
            }

            Screen.CHECKLIST_EDIT -> {
                finishOnClick(R.id.buttonChecklistEditBack, R.id.buttonChecklistEditDone)
                openScreenOnClick(R.id.buttonChecklistItemAdd, Screen.CHECKLIST_ITEM_ADD)
                openScreenOnClick(R.id.buttonChecklistCategoryAdd, Screen.CATEGORY_MANAGE)
                showChecklistActionsOnClick(
                    R.id.buttonChecklistCategoryMenu,
                    R.id.buttonChecklistItemMenu1
                )
            }

            Screen.CHECKLIST_ITEM_ADD -> {
                finishOnClick(
                    R.id.buttonChecklistItemAddBack,
                    R.id.buttonChecklistItemAddSaveTop,
                    R.id.buttonChecklistItemSaveBottom
                )
            }

            Screen.EXPENSE_ENTRY -> {
                finishOnClick(R.id.buttonExpenseEntryBack)
                findViewById<View?>(R.id.buttonExpenseCurrency)?.setOnClickListener {
                    ExpenseCurrencyBottomSheetFragment().show(
                        supportFragmentManager,
                        "expense_currency"
                    )
                }
                showOptionSheetOnClick(
                    R.id.buttonExpenseDateSelect,
                    "날짜 선택",
                    listOf("여행준비", "day 1 2.15/일", "day 2 2.16/월", "day 3 2.17/화", "day 4 2.18/수")
                )
                showOptionSheetOnClick(
                    R.id.buttonExpensePaymentSelect,
                    "결제수단",
                    listOf("현금", "카드", "간편결제", "정산 예정")
                )
                showOptionSheetOnClick(
                    R.id.buttonExpenseCategoryMore,
                    "카테고리 선택",
                    listOf("숙소", "항공", "교통", "관광", "식비", "쇼핑", "기타")
                )
                showOptionSheetOnClick(
                    R.id.buttonExpensePlaceSelect,
                    "장소 연결",
                    listOf("장소 없이 저장", "상하이 홍차오 국제공항", "캄파닐레 호텔 상하이 번드", "와이탄")
                )
                showOptionSheetOnClick(
                    R.id.buttonExpenseEntryPhotoAdd,
                    "사진 첨부",
                    listOf("카메라로 촬영", "앨범에서 선택", "첨부하지 않음")
                )
                showOptionSheetOnClick(
                    R.id.buttonExpenseEntryMore,
                    "추가 옵션",
                    listOf("반복 지출 아님", "환불/정산으로 기록", "영수증 OCR로 입력")
                )
                finishOnClick(R.id.buttonExpenseEntrySaveBottom)
            }

            Screen.EXPENSE_DETAIL -> {
                finishOnClick(R.id.buttonExpenseDetailBack, R.id.buttonExpenseDetailDelete)
                openScreenOnClick(
                    R.id.buttonExpenseDetailEdit,
                    Screen.EXPENSE_ENTRY
                )
                openScreenOnClick(
                    R.id.buttonExpenseDetailEditBottom,
                    Screen.EXPENSE_ENTRY
                )
            }

            Screen.CATEGORY_MANAGE -> {
                finishOnClick(R.id.buttonCategoryManageBack)
                showOptionSheetOnClick(
                    R.id.buttonAddTopCategory,
                    "새 카테고리 추가",
                    listOf("숙소", "항공", "교통", "관광", "식비", "쇼핑", "기타")
                )
            }

            Screen.MEMO_EDIT -> {
                finishOnClick(
                    R.id.buttonMemoBack,
                    R.id.buttonMemoSaveTop,
                    R.id.buttonMemoSaveBottom
                )
            }

            Screen.OCR_IMPORT -> {
                finishOnClick(R.id.buttonOcrBack, R.id.buttonOcrCancel)
                openScreenOnClick(R.id.buttonOcrCamera, Screen.OCR_REVIEW)
                openScreenOnClick(R.id.buttonOcrGallery, Screen.OCR_REVIEW)
            }

            Screen.OCR_REVIEW -> {
                finishOnClick(R.id.buttonOcrReviewBack)
                openScreenOnClick(R.id.buttonOcrReviewConfirm, Screen.SCHEDULE_EDIT)
            }

            Screen.WEATHER -> {
                finishOnClick(R.id.buttonWeatherBack)
            }

            Screen.TRIP_CALENDAR -> {
                finishOnClick(R.id.buttonTripCalendarBack)
                openScreenOnClick(R.id.buttonTripCalendarAddExpense, Screen.EXPENSE_ENTRY)
                bindBottomSheet(R.id.tripCalendarBottomSheet)
            }

            Screen.TRIP_ROUTE_MAP -> {
                finishOnClick(R.id.buttonTripRouteMapBack)
            }

            Screen.FLIGHT_AIRLINE -> {
                finishOnClick(R.id.buttonFlightAirlineBack)
                openScreenOnClick(R.id.buttonFlightAirlineDone, Screen.FLIGHT_NUMBER)
                openScreenOnClick(
                    R.id.buttonAirlineKorean,
                    R.id.buttonAirlineAsiana,
                    R.id.buttonAirlineJeju,
                    R.id.buttonAirlineJin,
                    R.id.buttonAirlineTway,
                    R.id.buttonAirlineAirSeoul,
                    R.id.buttonAirlineAirBusan,
                    R.id.buttonAirlineEastar,
                    screen = Screen.FLIGHT_NUMBER
                )
            }

            Screen.FLIGHT_NUMBER -> {
                finishOnClick(R.id.buttonFlightNumberBack)
                openScreenOnClick(R.id.buttonFlightNumberDone, Screen.FLIGHT_FORM)
            }

            Screen.FLIGHT_FORM -> {
                finishOnClick(R.id.buttonFlightFormBack)
                showFlightDateSheetOnClick(R.id.textFlightDepartureDate, "출발일")
                showFlightDateSheetOnClick(R.id.textFlightArrivalDate, "도착일")
                showTimePickerOnClick(R.id.textFlightDepartureTime, 0, 21)
                showTimePickerOnClick(R.id.textFlightArrivalTime, 0, 22)
                openScreenOnClick(R.id.textFlightDepartureAirport, Screen.AIRPORT_SEARCH)
                openScreenOnClick(R.id.textFlightArrivalAirport, Screen.AIRPORT_SEARCH)
                openTabOnClick(R.id.buttonFlightFormDone, R.id.fragment_schedule)

                if (intent.getBooleanExtra(EXTRA_FLIGHT_FORM_READY, false)) {
                    markFlightFormReady()
                }
            }

            Screen.AIRPORT_SEARCH -> {
                finishOnClick(R.id.buttonAirportSearchBack)
                openReadyFlightFormOnClick(
                    R.id.buttonAirportAtlanta,
                    R.id.buttonAirportBarcelona,
                    R.id.buttonAirportBogota,
                    R.id.buttonAirportJakarta
                )
            }

            Screen.LODGING_SEARCH -> {
                finishOnClick(R.id.buttonLodgingSearchBack)
                openScreenOnClick(
                    R.id.buttonLodgingSelect1,
                    R.id.buttonLodgingSelect2,
                    R.id.buttonLodgingSelect3,
                    R.id.buttonLodgingSelect4,
                    screen = Screen.LODGING_DATE
                )
                openScreenOnClick(R.id.buttonLodgingManualAdd, Screen.LODGING_DATE)
            }

            Screen.LODGING_DATE -> {
                finishOnClick(R.id.buttonLodgingDateBack)
                openTabOnClick(R.id.buttonLodgingDateDone, R.id.fragment_schedule)
            }

            Screen.TRIP_LEDGER -> {
                finishOnClick(R.id.buttonTripLedgerBack)
                openScreenOnClick(
                    R.id.buttonLedgerAddPreparation,
                    R.id.buttonLedgerAddDay1,
                    R.id.buttonLedgerAddDay2,
                    R.id.buttonLedgerAddDay3,
                    screen = Screen.EXPENSE_ENTRY
                )
            }

            Screen.PLACE_DETAIL -> {
                finishOnClick(R.id.buttonPlaceDetailBack, R.id.buttonAddPlaceToSchedule)
            }

            Screen.PLACE_SEARCH -> {
                finishOnClick(
                    R.id.buttonPlaceSearchBack,
                    R.id.buttonPlaceSelect1,
                    R.id.buttonPlaceSelect2,
                    R.id.buttonPlaceSelect3
                )
            }

            Screen.SCHEDULE_EDIT -> {
                finishOnClick(R.id.buttonEditBack, R.id.buttonEditDone)
                openScreenOnClick(R.id.buttonImportOcr, Screen.OCR_IMPORT)
            }

            Screen.SETTINGS -> {
                finishOnClick(R.id.buttonSettingsBack)
                openScreenOnClick(R.id.rowProfileEdit, Screen.PROFILE_EDIT)
                openTabOnClick(R.id.buttonLogout, R.id.fragment_home)
            }

            Screen.PROFILE_EDIT -> {
                finishOnClick(R.id.buttonProfileEditBack, R.id.buttonProfileSave)
            }
        }
    }

    private fun bindBottomSheet(@IdRes id: Int) {
        val sheet = findViewById<LinearLayout?>(id) ?: return
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            skipCollapsed = false
            isDraggable = true
        }
    }

    private fun finishOnClick(@IdRes vararg ids: Int) {
        ids.forEach { id ->
            findViewById<View?>(id)?.setOnClickListener { finish() }
        }
    }

    private fun openScreenOnClick(@IdRes id: Int, screen: Screen) {
        findViewById<View?>(id)?.setOnClickListener {
            startActivity(intent(this, screen))
        }
    }

    private fun openScreenOnClick(@IdRes vararg ids: Int, screen: Screen) {
        ids.forEach { id ->
            openScreenOnClick(id, screen)
        }
    }

    private fun openReadyFlightFormOnClick(@IdRes vararg ids: Int) {
        ids.forEach { id ->
            findViewById<View?>(id)?.setOnClickListener {
                startActivity(
                    intent(this, Screen.FLIGHT_FORM).apply {
                        putExtra(EXTRA_FLIGHT_FORM_READY, true)
                    }
                )
            }
        }
    }

    private fun openTabOnClick(@IdRes id: Int, @IdRes itemId: Int) {
        findViewById<View?>(id)?.setOnClickListener {
            startActivity(MainActivity.intentForTab(this, itemId))
            finish()
        }
    }

    private fun showChecklistActionsOnClick(@IdRes vararg ids: Int) {
        ids.forEach { id ->
            findViewById<View?>(id)?.setOnClickListener {
                ChecklistItemActionsBottomSheetFragment().show(
                    supportFragmentManager,
                    "checklist_item_actions"
                )
            }
        }
    }

    private fun showTripDeleteOnClick(@IdRes id: Int) {
        findViewById<View?>(id)?.setOnClickListener {
            TripDeleteConfirmBottomSheetFragment().show(
                supportFragmentManager,
                "trip_delete_confirm"
            )
        }
    }

    private fun showOptionSheetOnClick(
        @IdRes id: Int,
        title: String,
        options: List<String>
    ) {
        findViewById<View?>(id)?.setOnClickListener {
            showOptionSheet(title, options)
        }
    }

    private fun showOptionSheet(title: String, options: List<String>) {
        val dialog = BottomSheetDialog(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(26), dp(24), dp(26))
        }

        content.addView(
            TextView(this).apply {
                text = title
                textSize = 19f
                typeface = resources.getFont(R.font.pretendardbold)
                setTextColor(getColor(R.color.schedule_minimal_text))
            }
        )

        options.forEach { option ->
            content.addView(
                TextView(this).apply {
                    text = option
                    gravity = Gravity.CENTER_VERTICAL
                    textSize = 18f
                    typeface = resources.getFont(R.font.pretendardbold)
                    setTextColor(getColor(R.color.schedule_minimal_text))
                    setOnClickListener { dialog.dismiss() }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(58)
                    )
                }
            )
        }

        dialog.setContentView(content)
        dialog.show()
    }

    private fun bindTripCreateCityRows() {
        val container = findViewById<LinearLayout?>(R.id.tripCreateCityList) ?: return
        container.removeAllViews()

        listOf(
            CityOption("도쿄", "도쿄, 하코네, 요코하마, 가마쿠라", "도쿄", R.color.schedule_minimal_blue_soft, true),
            CityOption("후쿠오카", "후쿠오카, 유후인, 벳푸, 기타큐슈", "후쿠\n오카", R.color.tripline_green_soft, true),
            CityOption("오사카", "오사카, 교토, 고베, 나라", "오사카", R.color.tripline_coral_soft, true),
            CityOption("가고시마", "가고시마, 이부스키, 기리시마", "가고\n시마", R.color.schedule_minimal_blue_soft, false),
            CityOption("시즈오카", "시즈오카, 후지노미야, 이토, 하마마쓰", "시즈\n오카", R.color.schedule_minimal_blue_soft, false),
            CityOption("나고야", "나고야, 다카야마, 시라카와고, 게로", "나고\n야", R.color.schedule_minimal_blue_soft, false)
        ).forEach { city ->
            container.addView(createTripCreateCityRow(city))
        }
    }

    private fun createTripCreateCityRow(city: CityOption): LinearLayout {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60)
            )

            addView(
                TextView(context).apply {
                    text = city.badge
                    gravity = Gravity.CENTER
                    textSize = 10f
                    typeface = resources.getFont(R.font.pretendardbold)
                    setTextColor(getColor(R.color.schedule_minimal_text))
                    setBackgroundResource(R.drawable.bg_tripline_pill)
                    backgroundTintList = ColorStateList.valueOf(getColor(city.badgeColor))
                    layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
                }
            )

            addView(
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = dp(12)
                    }

                    addView(
                        TextView(context).apply {
                            text = city.name
                            textSize = 16f
                            typeface = resources.getFont(R.font.pretendardbold)
                            setTextColor(getColor(R.color.schedule_minimal_text))
                        }
                    )

                    addView(
                        TextView(context).apply {
                            text = city.description
                            textSize = 12f
                            typeface = resources.getFont(R.font.pretendardmedium)
                            setTextColor(getColor(R.color.tripline_text_hint))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = dp(3)
                            }
                        }
                    )
                }
            )

            addView(
                TextView(context).apply {
                    text = if (city.selected) "취소" else "선택"
                    gravity = Gravity.CENTER
                    textSize = 12f
                    typeface = resources.getFont(R.font.pretendardbold)
                    setTextColor(
                        getColor(
                            if (city.selected) R.color.schedule_minimal_blue else R.color.schedule_minimal_text
                        )
                    )
                    setBackgroundResource(R.drawable.bg_tripline_secondary_action)
                    layoutParams = LinearLayout.LayoutParams(dp(52), dp(30))
                }
            )
        }
    }

    private fun showFlightDateSheetOnClick(@IdRes id: Int, title: String) {
        val target = findViewById<TextView?>(id) ?: return
        target.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(28), dp(28), dp(28), dp(32))
            }

            content.addView(
                TextView(this).apply {
                    text = title
                    textSize = 16f
                    typeface = resources.getFont(R.font.pretendardbold)
                    setTextColor(getColor(R.color.schedule_minimal_subtle))
                }
            )

            listOf(
                "day1 5.7/목" to "2026.5.7",
                "day2 5.8/금" to "2026.5.8",
                "day3 5.9/토" to "2026.5.9",
                "day4 5.10/일" to "2026.5.10",
                "day5 5.11/월" to "2026.5.11"
            ).forEach { (label, value) ->
                content.addView(
                    TextView(this).apply {
                        text = label
                        gravity = Gravity.CENTER_VERTICAL
                        textSize = 19f
                        typeface = resources.getFont(R.font.pretendardbold)
                        setTextColor(getColor(R.color.schedule_minimal_text))
                        setOnClickListener {
                            target.text = value
                            target.setTextColor(getColor(R.color.schedule_minimal_text))
                            dialog.dismiss()
                        }
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(66)
                        )
                    }
                )
            }

            dialog.setContentView(content)
            dialog.show()
        }
    }

    private fun showTimePickerOnClick(@IdRes id: Int, hour: Int, minute: Int) {
        val target = findViewById<TextView?>(id) ?: return
        target.setOnClickListener {
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                target.text = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                target.setTextColor(getColor(R.color.schedule_minimal_text))
            }, hour, minute, true).show()
        }
    }

    private fun markFlightFormReady() {
        val filledColor = getColor(R.color.schedule_minimal_text)
        findViewById<TextView?>(R.id.textFlightDepartureAirport)?.apply {
            text = "엘프라트 국제공항(BCN)"
            setTextColor(filledColor)
        }
        findViewById<TextView?>(R.id.textFlightDepartureTime)?.apply {
            text = "00:21"
            setTextColor(filledColor)
        }
        findViewById<TextView?>(R.id.textFlightArrivalAirport)?.apply {
            text = "하츠필드 잭슨 애틀랜타 국제공항(ATL)"
            setTextColor(filledColor)
        }
        findViewById<TextView?>(R.id.textFlightArrivalDate)?.apply {
            text = "2026.5.8"
            setTextColor(filledColor)
        }
        findViewById<TextView?>(R.id.textFlightArrivalTime)?.apply {
            text = "00:22"
            setTextColor(filledColor)
        }
    }

    private fun showCityOrderOnClick(@IdRes id: Int) {
        findViewById<View?>(id)?.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(24), dp(28), dp(24), dp(24))
            }

            content.addView(
                TextView(this).apply {
                    text = "순서 변경"
                    textSize = 18f
                    typeface = resources.getFont(R.font.pretendardbold)
                    setTextColor(getColor(R.color.schedule_minimal_text))
                }
            )

            listOf("도쿄", "후쿠오카", "오사카").forEach { city ->
                content.addView(createOrderRow(city))
            }

            content.addView(
                TextView(this).apply {
                    text = "완료"
                    gravity = Gravity.CENTER
                    textSize = 16f
                    typeface = resources.getFont(R.font.pretendardbold)
                    setTextColor(getColor(R.color.white))
                    setBackgroundResource(R.drawable.bg_tripline_action)
                    setOnClickListener { dialog.dismiss() }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                    ).apply {
                        topMargin = dp(18)
                    }
                }
            )

            dialog.setContentView(content)
            dialog.show()
        }
    }

    private fun createOrderRow(city: String): LinearLayout {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(68)
            )

            addView(
                TextView(context).apply {
                    text = city
                    textSize = 20f
                    typeface = resources.getFont(R.font.pretendardmedium)
                    setTextColor(getColor(R.color.schedule_minimal_text))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
            )

            addView(
                TextView(context).apply {
                    text = "↕"
                    gravity = Gravity.CENTER
                    textSize = 20f
                    setTextColor(getColor(R.color.tripline_text_hint))
                    layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
                }
            )

            addView(
                TextView(context).apply {
                    text = "×"
                    gravity = Gravity.CENTER
                    textSize = 24f
                    setTextColor(getColor(R.color.tripline_text_hint))
                    layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
                }
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private data class CityOption(
        val name: String,
        val description: String,
        val badge: String,
        val badgeColor: Int,
        val selected: Boolean
    )
}
