package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

/**
 * SamplePage — Example Page Object for the ApiDemos app
 *
 * Demonstrates dual-platform locators:
 *   @AndroidFindBy → locator used when platform = Android
 *   @iOSXCUITFindBy → locator used when platform = iOS
 *
 * AppiumFieldDecorator automatically picks the right locator at runtime.
 *
 * ─────────────────────────────────────────────────────────────────────
 * HOW TO ADD YOUR OWN PAGE:
 *
 * 1. Create a new class in src/test/java/pages/
 * 2. Add @AndroidFindBy and @iOSXCUITFindBy on each WebElement
 * 3. Copy the constructor below (just change the class name)
 * 4. Add public getters for each element
 * 5. Instantiate in your test: new YourPage(getDriver())
 * ─────────────────────────────────────────────────────────────────────
 */
public class SamplePage {

    // ===== CONSTRUCTOR — Required for PageFactory =====
    public SamplePage(AppiumDriver driver) {
        PageFactory.initElements(
            new AppiumFieldDecorator(driver, Duration.ofSeconds(10)),
            this
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // ELEMENTS — ApiDemos App (Android demo app from Google)
    // Replace these locators with your own app's elements
    // ─────────────────────────────────────────────────────────────────

    /**
     * Header text visible on the home screen
     * Android: finds by accessibility id
     * iOS: finds by accessibility id (same id if your app supports it)
     */
    @AndroidFindBy(accessibility = "API Demos")
    @iOSXCUITFindBy(accessibility = "API Demos")
    private WebElement homeHeader;

    /**
     * "Views" menu item on the home list
     */
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Views']")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[@name='Views']")
    private WebElement viewsMenuItem;

    /**
     * "Animation" menu item on the home list
     */
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Animation']")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[@name='Animation']")
    private WebElement animationMenuItem;

    /**
     * "Graphics" menu item on the home list
     */
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Graphics']")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[@name='Graphics']")
    private WebElement graphicsMenuItem;

    /**
     * Search bar (if present)
     */
    @AndroidFindBy(id = "android:id/search_src_text")
    @iOSXCUITFindBy(accessibility = "Search")
    private WebElement searchField;

    // ─────────────────────────────────────────────────────────────────
    // GETTERS — Always private elements, public getters
    // ─────────────────────────────────────────────────────────────────

    public WebElement getHomeHeader()       { return homeHeader; }
    public WebElement getViewsMenuItem()    { return viewsMenuItem; }
    public WebElement getAnimationMenuItem(){ return animationMenuItem; }
    public WebElement getGraphicsMenuItem() { return graphicsMenuItem; }
    public WebElement getSearchField()      { return searchField; }
}
