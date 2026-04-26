package com.example.tripline

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripline.ui.common.ChecklistItemActionsBottomSheetFragment
import com.example.tripline.ui.common.ExpenseCurrencyBottomSheetFragment
import com.example.tripline.ui.common.PdfShareBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PrototypeScreenActivity : AppCompatActivity() {

    enum class Screen(@LayoutRes val layoutRes: Int) {
        TRIP_CREATE(R.layout.activity_trip_create),
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
        WEATHER(R.layout.activity_trip_weather),
        TRIP_CALENDAR(R.layout.activity_trip_calendar),
        TRIP_ROUTE_MAP(R.layout.activity_trip_route_map),
        PLACE_DETAIL(R.layout.fragment_place_detail),
        PLACE_SEARCH(R.layout.fragment_place_search),
        SCHEDULE_EDIT(R.layout.fragment_schedule_edit),
        SETTINGS(R.layout.activity_settings)
    }

    companion object {
        private const val EXTRA_SCREEN = "extra_screen"

        fun intent(context: Context, screen: Screen): Intent {
            return Intent(context, PrototypeScreenActivity::class.java).apply {
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
                finishOnClick(R.id.buttonTripCreateBack)
                openTabOnClick(R.id.buttonTripCreateSave, R.id.fragment_schedule)
            }

            Screen.TRIP_EDIT -> {
                finishOnClick(R.id.buttonTripEditBack, R.id.buttonTripEditSave)
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
                showMockActionOnClick(
                    R.id.buttonExpenseDateSelect,
                    "날짜 선택은 다음 단계에서 연결할게요."
                )
                showMockActionOnClick(
                    R.id.buttonExpensePaymentSelect,
                    "결제수단 선택은 다음 단계에서 연결할게요."
                )
                showMockActionOnClick(
                    R.id.buttonExpenseCategoryMore,
                    "추가 카테고리는 다음 단계에서 연결할게요."
                )
                showMockActionOnClick(
                    R.id.buttonExpensePlaceClear,
                    "장소 연결 해제는 다음 단계에서 연결할게요."
                )
                showMockActionOnClick(
                    R.id.buttonExpenseEntryPhotoAdd,
                    "사진 첨부는 다음 단계에서 연결할게요."
                )
                showMockActionOnClick(
                    R.id.buttonExpenseEntryMore,
                    "추가 옵션은 다음 단계에서 연결할게요."
                )
                showMockActionOnClick(
                    R.id.buttonExpenseEntrySaveBottom,
                    "완료 저장은 다음 단계에서 연결할게요."
                )
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
                showMockActionOnClick(R.id.buttonAddTopCategory, "새 카테고리 추가는 다음 단계에서 연결할게요.")
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
                showMockActionOnClick(
                    R.id.buttonOcrCamera,
                    "카메라 OCR 연결은 다음 단계에서 붙일게요."
                )
                showMockActionOnClick(
                    R.id.buttonOcrGallery,
                    "앨범 OCR 연결은 다음 단계에서 붙일게요."
                )
                openScreenOnClick(R.id.buttonOcrConfirm, Screen.SCHEDULE_EDIT)
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
                showMockActionOnClick(R.id.rowProfileEdit, "프로필 편집은 다음 단계에서 연결할게요.")
                openTabOnClick(R.id.buttonLogout, R.id.fragment_home)
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

    private fun showMockActionOnClick(@IdRes id: Int, message: String) {
        findViewById<View?>(id)?.setOnClickListener {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
