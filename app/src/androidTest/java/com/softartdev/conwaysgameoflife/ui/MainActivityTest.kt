package com.softartdev.conwaysgameoflife.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.softartdev.conwaysgameoflife.R
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {
        val mainStepsTextView = onView(withId(R.id.main_steps_text_view))
        mainStepsTextView.check(matches(withText("Steps: 0")))

        val mainRandomButton = onView(withId(R.id.main_random_button))
        mainRandomButton.perform(click())

        mainStepsTextView.check(matches(withText("Steps: 1")))

        val mainStepButton = onView(withId(R.id.main_step_button))
        mainStepButton.perform(click())

        mainStepsTextView.check(matches(withText("Steps: 2")))

        val mainStartButton = onView(withId(R.id.main_start_button))
        mainStartButton.perform(click())

        Thread.sleep(1000)
        mainStepsTextView.check(matches(withText(not("Steps: 2"))))
    }

}
