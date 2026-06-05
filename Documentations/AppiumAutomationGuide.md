# Appium Automation Guide
## Complete Framework Reference

This document is the single source of truth for all automation patterns,
utility methods, and code conventions used in this boilerplate.

Read this before writing any test or page object.

---

## 1. FRAMEWORK ARCHITECTURE

```
Test Class (extends Base)
    │
    ├── Page Object (PageFactory + AppiumFieldDecorator)
    │       └── Dual-platform locators (@AndroidFindBy + @iOSXCUITFindBy)
    │
    ├── Base.java
    │       ├── ThreadLocal<AppiumDriver>  → parallel-safe driver
    │       ├── @BeforeTest               → driver init per thread
    │       ├── @BeforeMethod             → screen recording + ADB logcat start
    │       ├── @AfterMethod              → recording stop + logcat attach to Allure
    │       ├── @AfterTest                → driver quit
    │       └── All utility methods
    │
    ├── DriverFactory.java
    │       └── Creates AndroidDriver or IOSDriver from config.properties
    │
    └── Allure Reports
            ├── Allure.step() → nested steps visible in report
            ├── Screenshots   → attached per verify step
            ├── Video MP4     → attached per @Test method
            └── ADB Logcat    → attached per @Test method (Android)
```

---

## 2. DRIVER MANAGEMENT

### ThreadLocal Pattern
```java
// Base.java handles this — you never need to set up ThreadLocal yourself

// In your test, always use:
AppiumDriver driver = getDriver();       // current thread's driver
String device = getDeviceName();         // current thread's device name
String udid   = getUdid();               // current thread's UDID
boolean android = isAndroid();           // true if Android
boolean ios     = isIOS();               // true if iOS

// NEVER do:
static AppiumDriver driver;              // ❌ shared across threads
AppiumDriver d = new AndroidDriver(...); // ❌ bypasses ThreadLocal
```

### Parallel Execution
```xml
<!-- testng.xml — 3 parallel threads, each on its own device -->
<suite parallel="tests" thread-count="3">
  <test name="Thread1"><parameter name="udid" value="emulator-5554"/></test>
  <test name="Thread2"><parameter name="udid" value="emulator-5556"/></test>
  <test name="Thread3"><parameter name="udid" value="emulator-5558"/></test>
</suite>
```

---

## 3. PAGE OBJECTS

### Template
```java
package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import java.time.Duration;

public class LoginPage {

    // Constructor — copy exactly, change only class name
    public LoginPage(AppiumDriver driver) {
        PageFactory.initElements(
            new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    // Element declaration — always private
    @AndroidFindBy(id = "com.example.app:id/email_input")
    @iOSXCUITFindBy(accessibility = "email_input")
    private WebElement emailField;

    @AndroidFindBy(id = "com.example.app:id/password_input")
    @iOSXCUITFindBy(accessibility = "password_input")
    private WebElement passwordField;

    @AndroidFindBy(id = "com.example.app:id/login_button")
    @iOSXCUITFindBy(accessibility = "login_button")
    private WebElement loginButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Invalid')]")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[contains(@name,'Invalid')]")
    private WebElement errorMessage;

    // Getters — always public
    public WebElement getEmailField()    { return emailField; }
    public WebElement getPasswordField() { return passwordField; }
    public WebElement getLoginButton()   { return loginButton; }
    public WebElement getErrorMessage()  { return errorMessage; }
}
```

### Locator Decision Chart
```
Q: Does my element have a resource-id?
   YES → Use @AndroidFindBy(id = "resource-id")    ← PREFERRED
   NO  → Does it have a content-desc / accessibility label?
         YES → Use @AndroidFindBy(accessibility = "label")
         NO  → Use XPath as fallback

Q: Is the element dynamic (text changes at runtime)?
   YES → Use contains(): //*[contains(@content-desc,'partial_text')]
   NO  → Use exact match: //*[@text='Exact Text']
```

---

## 4. CLICK METHODS

```java
// 1. Safe click — waits for clickable, catches all click exceptions
click(page.getLoginButton());              // returns true/false

// 2. Click by visible text (platform-aware)
clickByText("Login");                      // finds text on screen and clicks

// 3. JS click — for elements that aren't interactable normally
jsClick(page.getSubmitButton());

// 4. Check result after click
boolean clicked = click(page.getButton());
if (!clicked) {
    softAssert.fail("Could not click button");
}
```

---

## 5. INPUT METHODS

```java
// Clear field and type
sendKeys(page.getEmailField(), "user@test.com");

// Get visible text
String text = getText(page.getHeaderLabel());

// Get content-desc (Android) or name (iOS)
String desc = getDomAttribute(element);

// Parse multi-line content-desc (common in mobile apps)
String raw   = getDomAttribute(element);
String[] lines = raw.split("\\r?\\n");
// lines[0] = first line (e.g. item name)
// lines[1] = second line (e.g. item value)

// Hide keyboard after typing
sendKeys(page.getSearchField(), "hello");
hideKeyboard();
```

---

## 6. ELEMENT PRESENCE CHECKS

```java
// By WebElement — most common
isElementPresentIsDisplayed(element, "");          // just check
isElementPresentIsDisplayed(element, "yes");       // check + screenshot

// By text — platform-aware xpath built internally
isElementPresentIsDisplayed("Login", "", "");      // find element with text "Login"
isElementPresentIsDisplayed("Login", "Sign In", "yes"); // try both texts

// Returns boolean — safe to use in if/else
if (isElementPresentIsDisplayed(page.getErrorMsg(), "yes")) {
    softAssert.fail("Unexpected error shown: " + getText(page.getErrorMsg()));
}

// Find multiple elements
List<WebElement> items = findElementsByPlatform(
    "//android.widget.ListView/android.widget.TextView",
    "//XCUIElementTypeTable/XCUIElementTypeStaticText"
);
// Iterate and validate
for (WebElement item : items) {
    String name = getText(item);
    Allure.step("Found item: " + name);
}
```

---

## 7. WAIT METHODS

```java
// FluentWait — polls every 100ms (configured in config.properties)
// Used internally by isElementPresentIsDisplayed and click
fluentWait().until(ExpectedConditions.visibilityOf(element));

// Explicit wait — for specific conditions
waitForVisibility(element);              // waits up to timeout.explicit seconds
waitForClickable(element);              // waits for element to be clickable

// Custom explicit wait
explicitWait(20).until(
    ExpectedConditions.textToBePresentInElement(element, "Success"));

// Wait for element to disappear (e.g. loading spinner)
explicitWait(10).until(
    ExpectedConditions.invisibilityOf(loadingSpinner));
```

---

## 8. SCROLL & GESTURE METHODS

### swipeDirection — Main Scroll Method
```java
// Basic — uses default 50%x, 70%y start, 30% distance
swipeDirection("up");                              // scroll up
swipeDirection("down");                            // scroll down
swipeDirection("left");                            // swipe left (carousel)
swipeDirection("right");                           // swipe right

// With control — all values are % of screen size
swipeDirection("up", 50, 80, 40);   // startX=50%, startY=80%, distance=40%
swipeDirection("left", 80, 50, 50); // swipe left from right side of screen
```

### swipeWithDimensions — Absolute Control
```java
// startX%, startY%, endX%, endY%, durationMs
swipeWithDimensions(50, 80, 50, 20, 600);  // scroll up (center of screen)
swipeWithDimensions(80, 50, 20, 50, 400);  // swipe left
swipeWithDimensions(5, 50, 80, 50, 400);   // iOS back gesture (left edge swipe)
```

### longPress
```java
longPress(page.getListItem());  // holds for 1500ms, reveals context menu
```

### tapOnScreen
```java
tapOnScreen(0.5, 0.5);   // tap center of screen
tapOnScreen(0.5, 0.9);   // tap bottom center
tapOnScreen(0.1, 0.5);   // tap left edge (iOS back)
```

### scrollToElement
```java
// Scroll up (max 5 swipes) until element is visible
scrollToElement(page.getSettingsBtn(), "up", 5);
```

---

## 9. SCROLL PATTERNS

### Pattern 1 — Check-First (Standard)
```java
// Always check before scrolling — don't scroll if already visible
if (!isElementPresentIsDisplayed(page.getTargetElement(), "")) {
    for (int i = 0; i < 5; i++) {
        swipeDirection("up");
        if (isElementPresentIsDisplayed(page.getTargetElement(), "")) {
            break;  // found — stop scrolling
        }
    }
}
// Verify after scrolling
isElementPresentIsDisplayed(page.getTargetElement(), "yes");
```

### Pattern 2 — Scroll With Deduplication (Long Lists)
```java
// Use HashSet to avoid processing the same item twice during scroll
Set<String> seen = new HashSet<>();
for (int scroll = 0; scroll <= 10; scroll++) {
    List<WebElement> items = findElementsByPlatform(androidXp, iosXp);
    for (WebElement item : items) {
        String name = getText(item).trim();
        if (name.isEmpty() || seen.contains(name)) continue;
        seen.add(name);
        // process this item
        Allure.step("Verifying: " + name);
    }
    swipeDirection("up");
    try { Thread.sleep(150); } catch (InterruptedException ignored) {}
}
```

### Pattern 3 — Carousel Scroll (Horizontal)
```java
// Swipe left until target element appears
int maxSwipes = 5;
for (int i = 0; i < maxSwipes; i++) {
    if (isElementPresentIsDisplayed(page.getTargetCard(), "")) {
        break;
    }
    swipeDirection("left");
}
```

### Pattern 4 — Scroll Until Text Found
```java
boolean found = false;
for (int i = 0; i < 8; i++) {
    if (isElementPresentIsDisplayed("Target Text", "", "")) {
        found = true;
        break;
    }
    swipeDirection("up");
}
if (!found) {
    softAssert.fail("Target Text not found after scrolling");
}
```

---

## 10. SCREENSHOT & REPORTING

```java
// Screenshot attached to Allure (auto-named)
getScreenshots();

// Screenshot with custom name
getScreenshots("Login_Error_Screenshot");

// Allure step with pass/fail status
Allure.step("Verify header", () -> {
    if (isElementPresentIsDisplayed(page.getHeader(), "yes")) {
        Allure.step("Header visible ✓", Status.PASSED);
    } else {
        Allure.step("Header missing ✗", Status.FAILED);
        getScreenshots("Header_Missing");
        softAssert.fail("Header not found");
    }
});

// Info-only step (just logs data in report)
Allure.step("Device: " + getDeviceName());
Allure.step("Platform: " + getPlatform());
Allure.step("Item count: " + items.size());
```

---

## 11. COMPLETE TEST TEMPLATE

```java
package tests;

import base.Base;
import io.qameta.allure.*;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import pages.YourPage;
import listeners.TestListener;

@Listeners({TestListener.class})
public class YourFeatureTest extends Base {

    @Test(priority = 1, groups = "sanity")
    @Feature("Your Feature")
    @Story("Your Story")
    @Description("Clear description of what is being tested")
    @Severity(SeverityLevel.CRITICAL)
    public void tc01_ScreenName_whatIsBeingVerified() {

        // 1. Always create SoftAssert per test
        SoftAssert softAssert = new SoftAssert();

        // 2. Create page object with current thread's driver
        YourPage page = new YourPage(getDriver());

        // 3. Navigate to the screen (if needed)
        // click(homePage.getYourFeatureBtn());

        // 4. Scroll to element if not visible (bounded, check-first)
        Allure.step("Scroll to target element", () -> {
            if (!isElementPresentIsDisplayed(page.getTargetElement(), "")) {
                for (int i = 0; i < 5; i++) {
                    swipeDirection("up");
                    if (isElementPresentIsDisplayed(page.getTargetElement(), "")) break;
                }
            }
        });

        // 5. Verify element
        Allure.step("Verify target element is displayed", () -> {
            if (isElementPresentIsDisplayed(page.getTargetElement(), "yes")) {
                Allure.step("Target element visible ✓", Status.PASSED);
            } else {
                Allure.step("Target element NOT visible ✗", Status.FAILED);
                getScreenshots("TargetElement_Missing");
                softAssert.fail("Target element not found");
            }
        });

        // 6. Log additional info
        Allure.step("Running on: " + getDeviceName() + " | " + getPlatform());

        // 7. ALWAYS last line
        softAssert.assertAll();
    }
}
```

---

## 12. DATA-DRIVEN TESTING

### Excel File Structure
```
TestData/sample_data.xlsx
Sheet: LoginData

| email              | password  | expectedResult |
|--------------------|-----------|----------------|
| valid@test.com     | pass123   | success        |
| invalid@test.com   | wrongpass | failure        |
| empty@test.com     |           | empty_error    |
```

### @DataProvider from Excel
```java
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    return ExcelUtils.getTableArray(
        config.getTestDataPath(),    // from config.properties
        config.getTestDataSheet()    // from config.properties
    );
}

@Test(dataProvider = "loginData")
public void tc01_Login_withMultipleCredentials(
        String email, String password, String expected) {
    // runs once per row in Excel
}
```

---

## 13. ADDING A NEW SCREEN — STEP BY STEP

1. **Inspect element locators** using Appium Inspector or UIAutomatorViewer
2. **Create Page Object** in `src/test/java/pages/NewScreenPage.java`
3. **Add dual locators** for each element
4. **Create Test Class** in `src/test/java/tests/NewScreenTest.java`
5. **Add to suite XML** under `TestSuites/`
6. **Document the screen** in `Documentations/NewScreen.md`

---

## 14. COMMON ISSUES & FIXES

| Issue | Root Cause | Fix |
|-------|-----------|-----|
| Test passes but no Allure data | Missing `softAssert.assertAll()` | Add as last line always |
| Parallel tests corrupt each other | Static field used for driver/data | Use ThreadLocal or local variables |
| Element not found after scroll | Unbounded scroll / no break | Use bounded loop with break |
| Flaky timing failures | Thread.sleep() or mixed waits | Use FluentWait or WebDriverWait |
| Stale element exception | Element re-rendered after scroll | Re-find element or use PageFactory lazy init |
| App not launching | Wrong appPackage/appActivity | Verify with `adb shell pm list packages` |
| ADB logcat not capturing | Wrong UDID | Verify with `adb devices` |

---

*Author: Varun R | Version: 1.0.0*
