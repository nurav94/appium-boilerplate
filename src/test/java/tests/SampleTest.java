package tests;

import base.Base;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Status;
import io.qameta.allure.Story;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SamplePage;
import listeners.TestListener;
import utils.ExcelUtils;

/**
 * SampleTest — Full working example showing the boilerplate test pattern
 *
 * Demonstrates:
 *  - Page Object usage
 *  - SoftAssert (collects all failures before reporting)
 *  - Allure.step() with lambda (nested steps in report)
 *  - Scroll pattern (check-first, bounded loop, break on find)
 *  - Screenshot on verify
 *  - DataProvider from Excel
 *
 * ─────────────────────────────────────────────────────────────────────
 * NAMING CONVENTION:
 *   tc{number}_{ScreenName}_{whatIsBeingTested}
 *   tc01_Home_verifyHeaderIsDisplayed
 *   tc02_Home_scrollToViewsAndClick
 * ─────────────────────────────────────────────────────────────────────
 */
@Listeners({TestListener.class})
public class SampleTest extends Base {

    // ─────────────────────────────────────────────────────────────────
    // TEST 1 — Verify home screen header is visible
    // ─────────────────────────────────────────────────────────────────

    @Test(priority = 1, groups = "sanity")
    @Feature("Home Screen")
    @Story("App Launch")
    @Description("Verifies the home screen loads with the correct header after app launch")
    @Severity(SeverityLevel.CRITICAL)
    public void tc01_Home_verifyHeaderIsDisplayed() {

        SoftAssert softAssert = new SoftAssert();
        SamplePage page = new SamplePage(getDriver());

        Allure.step("Verify home screen header", () -> {
            if (isElementPresentIsDisplayed(page.getHomeHeader(), "yes")) {
                Allure.step("Header is displayed ✓", Status.PASSED);
            } else {
                Allure.step("Header NOT displayed ✗", Status.FAILED);
                getScreenshots("Header_Missing");
                softAssert.fail("Home screen header is not displayed");
            }
        });

        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // TEST 2 — Scroll down to find menu item and click it
    // ─────────────────────────────────────────────────────────────────

    @Test(priority = 2, groups = "sanity")
    @Feature("Home Screen")
    @Story("Navigation")
    @Description("Scrolls to the Views menu item and taps it")
    @Severity(SeverityLevel.NORMAL)
    public void tc02_Home_scrollToViewsAndClick() {

        SoftAssert softAssert = new SoftAssert();
        SamplePage page = new SamplePage(getDriver());

        // Step 1 — Scroll to element (check-first, bounded loop)
        Allure.step("Scroll to Views menu item", () -> {
            if (!isElementPresentIsDisplayed(page.getViewsMenuItem(), "")) {
                for (int i = 0; i < 5; i++) {
                    swipeDirection("up");
                    if (isElementPresentIsDisplayed(page.getViewsMenuItem(), "")) {
                        Allure.step("Found Views after " + (i + 1) + " swipe(s)");
                        break;
                    }
                }
            }
        });

        // Step 2 — Verify element is visible
        Allure.step("Verify Views menu item is visible", () -> {
            if (isElementPresentIsDisplayed(page.getViewsMenuItem(), "yes")) {
                Allure.step("Views menu item visible ✓", Status.PASSED);
            } else {
                Allure.step("Views menu item NOT visible ✗", Status.FAILED);
                getScreenshots("Views_Missing");
                softAssert.fail("Views menu item not found after scrolling");
            }
        });

        // Step 3 — Click it
        Allure.step("Tap on Views menu item", () -> {
            boolean clicked = click(page.getViewsMenuItem());
            if (clicked) {
                Allure.step("Tap successful ✓", Status.PASSED);
            } else {
                Allure.step("Tap failed ✗", Status.FAILED);
                softAssert.fail("Could not tap on Views menu item");
            }
        });

        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // TEST 3 — Verify multiple items exist on screen
    // ─────────────────────────────────────────────────────────────────

    @Test(priority = 3, groups = "regression")
    @Feature("Home Screen")
    @Story("Content Verification")
    @Description("Verifies multiple menu items are present on the home screen")
    @Severity(SeverityLevel.NORMAL)
    public void tc03_Home_verifyMultipleMenuItems() {

        SoftAssert softAssert = new SoftAssert();
        SamplePage page = new SamplePage(getDriver());

        // Verify multiple items — SoftAssert collects all failures
        Allure.step("Verify Animation menu item", () -> {
            if (isElementPresentIsDisplayed(page.getAnimationMenuItem(), "yes")) {
                Allure.step("Animation ✓", Status.PASSED);
            } else {
                getScreenshots();
                softAssert.fail("Animation menu item not found");
            }
        });

        Allure.step("Scroll down and verify Graphics menu item", () -> {
            swipeDirection("up");
            if (isElementPresentIsDisplayed(page.getGraphicsMenuItem(), "yes")) {
                Allure.step("Graphics ✓", Status.PASSED);
            } else {
                getScreenshots();
                softAssert.fail("Graphics menu item not found");
            }
        });

        // Log test info in Allure report
        Allure.step("Running on device: " + getDeviceName());
        Allure.step("Platform: " + getPlatform());

        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // TEST 4 — Data-driven test with @DataProvider
    // ─────────────────────────────────────────────────────────────────

    @DataProvider(name = "menuItems")
    public Object[][] getMenuItems() {
        // Option A — Hardcoded (good for small datasets)
        return new Object[][] {
            {"Animation"},
            {"Views"},
            {"Graphics"},
        };

        // Option B — From Excel (recommended for large datasets)
        // return ExcelUtils.getTableArray(config.getTestDataPath(), "MenuItems");
    }

    @Test(priority = 4, groups = "regression", dataProvider = "menuItems")
    @Feature("Home Screen")
    @Story("Data Driven")
    @Description("Verifies each menu item is present on the home screen")
    @Severity(SeverityLevel.MINOR)
    public void tc04_Home_verifyMenuItemByText(String itemText) {

        SoftAssert softAssert = new SoftAssert();

        Allure.step("Verifying menu item: " + itemText, () -> {
            // Scroll to find it
            boolean found = isElementPresentIsDisplayed(itemText, "", "");
            if (!found) {
                for (int i = 0; i < 5; i++) {
                    swipeDirection("up");
                    if (isElementPresentIsDisplayed(itemText, "", "yes")) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                Allure.step(itemText + " found ✓", Status.PASSED);
            } else {
                Allure.step(itemText + " NOT found ✗", Status.FAILED);
                getScreenshots(itemText + "_missing");
                softAssert.fail(itemText + " menu item not found");
            }
        });

        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // TEST 5 — Long press example
    // ─────────────────────────────────────────────────────────────────

    @Test(priority = 5, groups = "regression")
    @Feature("Home Screen")
    @Story("Gestures")
    @Description("Demonstrates long press on a menu item")
    @Severity(SeverityLevel.MINOR)
    public void tc05_Home_longPressOnMenuItem() {

        SoftAssert softAssert = new SoftAssert();
        SamplePage page = new SamplePage(getDriver());

        Allure.step("Long press on Animation menu item", () -> {
            if (isElementPresentIsDisplayed(page.getAnimationMenuItem(), "")) {
                longPress(page.getAnimationMenuItem());
                Allure.step("Long press executed ✓", Status.PASSED);
                getScreenshots("After_LongPress");
            } else {
                softAssert.fail("Animation item not found for long press");
            }
        });

        softAssert.assertAll();
    }
}
