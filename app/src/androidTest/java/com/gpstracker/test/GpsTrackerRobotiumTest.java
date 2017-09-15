/*package com.gpstracker.unicornsystems.eu.gpstracker.test;

import SplashScreen;
import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


public class GpsTrackerRobotiumTest extends ActivityInstrumentationTestCase2<SplashScreen> {
  	private Solo solo;

  	public GpsTrackerRobotiumTest() {
		super(SplashScreen.class);
  	}

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}

   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}

	public void testRun() {
        //Take screenshot
        solo.takeScreenshot();
        //Wait for activity: 'SplashScreen'
		solo.waitForActivity(SplashScreen.class, 2000);
        //Wait for activity: 'LoginActivity'
		assertTrue("LoginActivity is not found!", solo.waitForActivity(LoginActivity.class));
        //Set default small timeout to 1560361 milliseconds
		Timeout.setSmallTimeout(1560361);
        //Click on Prihlásiť sa
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.buttonLogin));
        //Wait for activity: 'DashboardActivity'
		assertTrue("DashboardActivity is not found!", solo.waitForActivity(DashboardActivity.class));
        //Click on Všetko
		solo.clickOnText(java.util.regex.Pattern.quote("Všetko"));
        //Click on Minulý týždeň
		solo.clickOnText(java.util.regex.Pattern.quote("Minulý týždeň"));
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on New Run
		solo.clickOnText(java.util.regex.Pattern.quote("New Run"));
        //Wait for activity: 'MapsActivity'
		assertTrue("MapsActivity is not found!", solo.waitForActivity(MapsActivity.class));
        //Click on Stop a Uložiť
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.button_stop_tracking));
        //Click on Späť
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.button_go_back));
        //Wait for activity: 'DashboardActivity'
		assertTrue("DashboardActivity is not found!", solo.waitForActivity(DashboardActivity.class));
        //Click on Delete 04-10-2016 0,000 00:00:09
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.swipeSingleRun));
        //Wait for activity: 'DetailOfRunActivity'
		assertTrue("DetailOfRunActivity is not found!", solo.waitForActivity(DetailOfRunActivity.class));
        //Click on android.view.TextureView
		solo.clickOnView(solo.getView(android.view.TextureView.class, 0));
        //Click on Späť
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.button_go_back, 1));
        //Wait for activity: 'DashboardActivity'
		assertTrue("DashboardActivity is not found!", solo.waitForActivity(DashboardActivity.class));
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on GpsTracker ActionMenuView Všetko Tento týždeň Minulý týždeň PieChart Dátum
		solo.clickOnText(java.util.regex.Pattern.quote("GpsTracker"), 3);
        //Assert that: 'View' is shown
		assertTrue("'View' is not shown!", solo.waitForView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.ingoredView, 2)));
        //Assert that: 'View' is shown
		assertTrue("'View' is not shown!", solo.waitForView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.ingoredView, 2)));
        //Assert that: 'View' is shown
		assertTrue("'View' is not shown!", solo.waitForView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.ingoredView, 2)));
        //Assert that: 'View' is shown
		assertTrue("'View' is not shown!", solo.waitForView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.ingoredView, 2)));
        //Assert that: 'View' is shown
		assertTrue("'View' is not shown!", solo.waitForView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.ingoredView, 2)));
        //Click on ImageView
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.fab, 2));
        //Wait for activity: 'MapsActivity'
		assertTrue("MapsActivity is not found!", solo.waitForActivity(MapsActivity.class));
        //Click on Späť
		solo.clickOnView(solo.getView(com.gpstracker.unicornsystems.eu.gpstracker.R.id.button_go_back, 2));
        //Wait for activity: 'DashboardActivity'
		assertTrue("DashboardActivity is not found!", solo.waitForActivity(DashboardActivity.class));
	}
}*/