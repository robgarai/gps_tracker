package com.gpstracker.activities;


import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.gpstracker.activities.SplashScreen;
import com.gpstracker.unicornsystems.eu.gpstracker.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class gps139unittest1 {

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);

    @Test
    public void gps139unittest1() {

        SystemClock.sleep(10000);
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.loginNameInput), isDisplayed()));
        appCompatEditText.perform(replaceText("aaa"), closeSoftKeyboard());

        SystemClock.sleep(2000);
        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.passwordInput), isDisplayed()));
        appCompatEditText2.perform(replaceText("a"), closeSoftKeyboard());

        SystemClock.sleep(2000);
        ViewInteraction appCompatButtonLogin = onView(
                allOf(withId(R.id.buttonLogin), isDisplayed()));
        appCompatButtonLogin.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction appCompatImageButton =
                onView(withId(R.id.my_toolbar));
        appCompatImageButton.perform(swipeRight());

        SystemClock.sleep(1000);
        ViewInteraction resideMenuItem =
                onView(withText(R.string.settings));
        resideMenuItem.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction editText = onView(
                allOf(withId(R.id.editText_set_goal_numberOfKm), isDisplayed()));
        editText.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction editText2 = onView(
                allOf(withId(R.id.editText_set_goal_numberOfKm), isDisplayed()));
        editText2.perform(replaceText("1"), closeSoftKeyboard());

        SystemClock.sleep(1000);
        ViewInteraction button = onView(
                allOf(withId(R.id.button_setkm), isDisplayed()));
        button.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction button2 = onView(
                allOf(withId(R.id.go_back), isDisplayed()));
        button2.perform(click());

        SystemClock.sleep(1000);
        appCompatImageButton.perform(swipeRight());

        SystemClock.sleep(1000);
        ViewInteraction resideMenuItem2 =
                onView(withText(R.string.logout));
        resideMenuItem2.perform(click());

        SystemClock.sleep(1000);
        appCompatButtonLogin.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction textView = onView(
                allOf(withText(R.string.week_all), isDisplayed()));
        textView.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction textView2 = onView(
                allOf(withText(R.string.week_past), isDisplayed()));
        textView2.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction textView3 = onView(
                allOf(withText(R.string.week_this), isDisplayed()));
        textView3.perform(click());

        SystemClock.sleep(1000);
        onView(withId(R.id.root_dashboard_layout)).perform(swipeRight());
        SystemClock.sleep(1000);
        onView(withId(R.id.root_dashboard_layout)).perform(swipeLeft());
        SystemClock.sleep(1000);
        onView(withId(R.id.root_dashboard_layout)).perform(swipeLeft());
        SystemClock.sleep(1000);
        onView(withId(R.id.root_dashboard_layout)).perform(swipeRight());

        SystemClock.sleep(1000);
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab),
                        withParent(allOf(withId(R.id.root_dashboard_layout),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        floatingActionButton.perform(click());

        SystemClock.sleep(6000);
        ViewInteraction appCompatButtonStopAndSave = onView(
                allOf(withId(R.id.button_stop_tracking), isDisplayed()));
        appCompatButtonStopAndSave.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction swipeLayout = onView(
                allOf(withId(R.id.swipeSingleRun),
                        childAtPosition(
                                allOf(withId(R.id.runsListView),
                                        withParent(withId(R.id.linlaywholefontchange))),
                                1),
                        isDisplayed()));
        swipeLayout.perform(click());

        SystemClock.sleep(1000);
        onView(withText(R.string.distance)).check(matches(isDisplayed()));

        SystemClock.sleep(1000);
        onView(withText(R.string.tracked_runtime)).check(matches(isDisplayed()));

        SystemClock.sleep(1000);
        onView(withText(R.string.date_of_run)).check(matches(isDisplayed()));

        SystemClock.sleep(1000);
        ViewInteraction button3 = onView(
                allOf(withId(R.id.button_go_back), isDisplayed()));
        button3.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction swipeLayout5 = onView(
                allOf(withId(R.id.swipeSingleRun),
                        childAtPosition(
                                allOf(withId(R.id.runsListView),
                                        withParent(withId(R.id.linlaywholefontchange))),
                                1),
                        isDisplayed()));
        swipeLayout5.perform(longClick());

        SystemClock.sleep(1000);
        ViewInteraction mDButton = onView(
                allOf(withId(R.id.md_buttonDefaultPositive), isDisplayed()));
        mDButton.perform(click());

        SystemClock.sleep(1000);
        floatingActionButton.perform(click());

        SystemClock.sleep(8000);
        appCompatButtonStopAndSave.perform(click());

        SystemClock.sleep(1000);
        ViewInteraction swipeLayoutDelete = onView(
                allOf(withId(R.id.swipeSingleRun),
                        childAtPosition(
                                allOf(withId(R.id.runsListView),
                                        withParent(withId(R.id.linlaywholefontchange))),
                                1),
                        isDisplayed()));
        swipeLayoutDelete.perform(swipeRight());

//        SystemClock.sleep(1000);
//        ViewInteraction appCompatTextView = onView(
//                allOf(withId(R.id.delete_option_text_view),
//                        withParent(allOf(withId(R.id.bottom_wrapper),
//                                withParent(withId(R.id.swipeSingleRun)))),
//                        isDisplayed()));
//        appCompatTextView.perform(click());

        SystemClock.sleep(1000);
        Log.i("TestingResult", "Test finished successfully");
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}