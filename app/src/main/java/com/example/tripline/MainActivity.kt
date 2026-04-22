package com.example.tripline

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tripline.databinding.ActivityMainBinding
import com.example.tripline.ui.calendar.CalendarFragment
import com.example.tripline.ui.funds.FundsFragment
import com.example.tripline.ui.home.HomeFragment
import com.example.tripline.ui.locker.LockerFragment
import com.example.tripline.ui.mypage.MypageFragment
import com.example.tripline.ui.schedule.ScheduleFragment

class MainActivity : AppCompatActivity() {

    private companion object {
        const val MY_PAGE_BACK_STACK = "my_page_panel"
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()
        supportFragmentManager.addOnBackStackChangedListener {
            updateOverlayVisibility()
        }
        updateOverlayVisibility()

        val navigateTo = intent.getStringExtra("navigate_to")

        if (savedInstanceState == null) {
            val initialTab = when (navigateTo) {
                "HomeFragment" -> R.id.fragment_home
                else -> R.id.fragment_home
            }
            navigateToTab(initialTab)
        } else {
            updateOverlayVisibility()
        }
    }

    private fun showFragment(
        fragment: Fragment,
        containerId: Int = R.id.main_container,
        addToBackStack: Boolean = false,
        backStackName: String? = null
    ) {
        supportFragmentManager.beginTransaction().apply {
            replace(containerId, fragment)
            if (addToBackStack) addToBackStack(backStackName)
        }.commit()
    }

    private fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            closeMyPagePanel()

            when (item.itemId) {
                R.id.fragment_home -> {
                    showFragment(HomeFragment())
                    true
                }

                R.id.fragment_locker -> {
                    showFragment(LockerFragment())
                    true
                }

                R.id.fragment_schedule -> {
                    showFragment(ScheduleFragment())
                    true
                }

                R.id.fragment_calendar -> {
                    showFragment(CalendarFragment())
                    true
                }

                R.id.fragment_funds -> {
                    showFragment(FundsFragment())
                    true
                }

                else -> false
            }
        }
    }

    fun navigateToTab(itemId: Int) {
        binding.bottomNavigationView.selectedItemId = itemId
    }

    fun openMyPage() {
        if (supportFragmentManager.findFragmentById(R.id.overlay_container) != null) {
            return
        }

        binding.overlayContainer.visibility = View.VISIBLE
        showFragment(
            MypageFragment(),
            containerId = R.id.overlay_container,
            addToBackStack = true,
            backStackName = MY_PAGE_BACK_STACK
        )
    }

    private fun closeMyPagePanel() {
        if (supportFragmentManager.findFragmentById(R.id.overlay_container) != null) {
            supportFragmentManager.popBackStack(
                MY_PAGE_BACK_STACK,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            supportFragmentManager.executePendingTransactions()
        }

        updateOverlayVisibility()
    }

    private fun updateOverlayVisibility() {
        binding.overlayContainer.visibility =
            if (supportFragmentManager.findFragmentById(R.id.overlay_container) != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }
}
