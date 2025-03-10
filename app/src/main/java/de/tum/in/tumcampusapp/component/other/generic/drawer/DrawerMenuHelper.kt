package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import android.content.Context
import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.other.settings.SettingsActivity
import de.tum.`in`.tumcampusapp.component.tumui.calendar.CalendarFragment
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackActivity
import de.tum.`in`.tumcampusapp.component.tumui.grades.GradesFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.fragment.LecturesFragment
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.TuitionFeesFragment
import de.tum.`in`.tumcampusapp.component.ui.barrierfree.BarrierFreeInfoFragment
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.fragment.CafeteriaFragment
import de.tum.`in`.tumcampusapp.component.ui.news.NewsFragment
import de.tum.`in`.tumcampusapp.component.ui.openinghour.OpeningHoursListFragment
import de.tum.`in`.tumcampusapp.component.ui.overview.InformationActivity
import de.tum.`in`.tumcampusapp.component.ui.overview.MainFragment
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment
import de.tum.`in`.tumcampusapp.component.ui.studyroom.StudyRoomsFragment
import de.tum.`in`.tumcampusapp.component.ui.ticket.activity.EventsFragment
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.allItems
import de.tum.`in`.tumcampusapp.utils.plusAssign
import java.util.Locale

class DrawerMenuHelper(
    private val activity: AppCompatActivity,
    private val navigationView: NavigationView
) {
    private val germanContext: Context
        get() {
            val config = Configuration(activity.resources.configuration)
            config.setLocale(Locale.GERMAN)
            return activity.createConfigurationContext(config)
        }

    private val englishContext: Context
        get() {
            val config = Configuration(activity.resources.configuration)
            config.setLocale(Locale.ENGLISH)
            return activity.createConfigurationContext(config)
        }

    private val mapAllItems = mapOf(
        // HOME
        germanContext.getString(HOME.titleRes) to HOME,
        englishContext.getString(HOME.titleRes) to HOME,
        // SEARCH
        germanContext.getString(SEARCH.titleRes) to SEARCH,
        englishContext.getString(SEARCH.titleRes) to SEARCH,
        // MY_TUM
        germanContext.getString(MY_TUM[0].titleRes) to MY_TUM[0],
        englishContext.getString(MY_TUM[0].titleRes) to MY_TUM[0],
        germanContext.getString(MY_TUM[1].titleRes) to MY_TUM[1],
        englishContext.getString(MY_TUM[1].titleRes) to MY_TUM[1],
        germanContext.getString(MY_TUM[2].titleRes) to MY_TUM[2],
        englishContext.getString(MY_TUM[2].titleRes) to MY_TUM[2],
        germanContext.getString(MY_TUM[3].titleRes) to MY_TUM[3],
        englishContext.getString(MY_TUM[3].titleRes) to MY_TUM[3],
        // GENERAL
        germanContext.getString(GENERAL[0].titleRes) to GENERAL[0],
        englishContext.getString(GENERAL[0].titleRes) to GENERAL[0],
        germanContext.getString(GENERAL[1].titleRes) to GENERAL[1],
        englishContext.getString(GENERAL[1].titleRes) to GENERAL[1],
        germanContext.getString(GENERAL[2].titleRes) to GENERAL[2],
        englishContext.getString(GENERAL[2].titleRes) to GENERAL[2],
        germanContext.getString(GENERAL[3].titleRes) to GENERAL[3],
        englishContext.getString(GENERAL[3].titleRes) to GENERAL[3],
        germanContext.getString(GENERAL[4].titleRes) to GENERAL[4],
        englishContext.getString(GENERAL[4].titleRes) to GENERAL[4],
        germanContext.getString(GENERAL[5].titleRes) to GENERAL[5],
        englishContext.getString(GENERAL[5].titleRes) to GENERAL[5],
        // ABOUT
        germanContext.getString(ABOUT[0].titleRes) to ABOUT[0],
        englishContext.getString(ABOUT[0].titleRes) to ABOUT[0],
        germanContext.getString(ABOUT[1].titleRes) to ABOUT[1],
        englishContext.getString(ABOUT[1].titleRes) to ABOUT[1],
        germanContext.getString(ABOUT[2].titleRes) to ABOUT[2],
        englishContext.getString(ABOUT[2].titleRes) to ABOUT[2]
    )

    private val currentFragment: Fragment?
        get() = activity.supportFragmentManager.findFragmentById(R.id.contentFrame)

    private val navigationMenu: Menu
        get() = navigationView.menu

    private val allItems = mutableListOf<NavItem>()

    fun populateMenu() {
        val hasTumOnlineAccess = AccessTokenManager.hasValidAccessToken(activity)
        val isChatEnabled = Utils.getSettingBool(activity, Const.GROUP_CHAT_ENABLED, false)
        val isEmployeeMode = Utils.getSettingBool(activity, Const.EMPLOYEE_MODE, false)

        navigationMenu.clear()
        allItems.clear()

        navigationMenu += HOME
        navigationMenu += SEARCH

        allItems += HOME
        allItems += SEARCH

        val myTumMenu = navigationMenu.addSubMenu(R.string.my_tum)
        if (hasTumOnlineAccess) {
            val candidates = MY_TUM
                .filterNot { !isChatEnabled && it.needsChatAccess }
                .filterNot { isEmployeeMode && it.hideForEmployees }

            for (candidate in candidates) {
                myTumMenu += candidate
                allItems += candidate
            }
        }

        val generalMenu = navigationMenu.addSubMenu(R.string.common_info)
        val generalCandidates = GENERAL.filterNot { it.needsTUMOAccess && !hasTumOnlineAccess }
        for (candidate in generalCandidates) {
            generalMenu += candidate
            allItems += candidate
        }

        val aboutMenu = navigationMenu.addSubMenu(R.string.about)
        for (item in ABOUT) {
            aboutMenu += item
            allItems += item
        }

        highlightCurrentItem()
    }

    fun findNavItem(menuItem: MenuItem): NavItem {
        if (!mapAllItems.containsKey(menuItem.title.toString())) {
            throw IllegalArgumentException("Invalid menu item ${menuItem.title} provided")
        }

        return mapAllItems.getValue(menuItem.title.toString())
    }

    fun updateNavDrawer() {
        highlightCurrentItem()
    }

    private fun highlightCurrentItem() {
        val items = navigationMenu.allItems
        items.forEach { it.isChecked = false }

        val currentIndex = allItems
            .mapNotNull { it as? NavItem.FragmentDestination }
            .indexOfFirst { it.fragment == currentFragment?.javaClass }

        if (currentIndex != -1) {
            items[currentIndex].isCheckable = true
            items[currentIndex].isChecked = true
        }
    }

    private companion object {

        private val HOME = NavItem.FragmentDestination(R.string.home, R.drawable.ic_outline_home_24px, MainFragment::class.java)
        private val SEARCH = NavItem.FragmentDestination(R.string.search, R.drawable.ic_action_search, SearchFragment::class.java, true)

        private val MY_TUM = arrayOf(
            NavItem.FragmentDestination(R.string.calendar, R.drawable.ic_outline_event_24px, CalendarFragment::class.java, true),
            NavItem.FragmentDestination(
                R.string.my_lectures,
                R.drawable.ic_outline_school_24px,
                LecturesFragment::class.java,
                true,
                hideForEmployees = true
            ),
            NavItem.FragmentDestination(
                R.string.my_grades,
                R.drawable.ic_outline_insert_chart_outlined_24px,
                GradesFragment::class.java,
                true,
                hideForEmployees = true
            ),
            NavItem.FragmentDestination(
                R.string.tuition_fees,
                R.drawable.ic_money,
                TuitionFeesFragment::class.java,
                true,
                hideForEmployees = true
            )
        )

        private val GENERAL = arrayOf(
            NavItem.FragmentDestination(R.string.menues, R.drawable.ic_cutlery, CafeteriaFragment::class.java),
            NavItem.FragmentDestination(R.string.study_rooms, R.drawable.ic_outline_group_work_24px, StudyRoomsFragment::class.java),
            NavItem.FragmentDestination(R.string.news, R.drawable.ic_rss, NewsFragment::class.java),
            NavItem.FragmentDestination(R.string.events_tickets, R.drawable.tickets, EventsFragment::class.java),
            NavItem.FragmentDestination(R.string.barrier_free, R.drawable.ic_outline_accessible_24px, BarrierFreeInfoFragment::class.java),
            NavItem.FragmentDestination(
                R.string.opening_hours,
                R.drawable.ic_outline_access_time_24px,
                OpeningHoursListFragment::class.java
            )
        )

        private val ABOUT = arrayOf(
            NavItem.ActivityDestination(R.string.show_feedback, R.drawable.ic_outline_feedback_24px, FeedbackActivity::class.java),
            NavItem.ActivityDestination(R.string.about_tca, R.drawable.ic_action_info, InformationActivity::class.java),
            NavItem.ActivityDestination(R.string.settings, R.drawable.ic_outline_settings_24px, SettingsActivity::class.java)
        )
    }
}
