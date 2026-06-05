# CLAUDE.md — AI Collaboration Guide
## Appium Automation Boilerplate

This file tells Claude AI (or any AI assistant) how this framework works,
so it can generate production-ready test code that matches the project's
exact patterns, structure, and conventions.

**Read this file first before generating any code.**

---

## 1. PROJECT OVERVIEW

- **Language:** Java 11
- **Framework:** TestNG
- **Build Tool:** Maven
- **Mobile Automation:** Appium (UIAutomator2 for Android, XCUITest for iOS)
- **Reporting:** Allure Reports
- **Parallel Execution:** ThreadLocal driver — each thread has its own AppiumDriver
- **Page Objects:** PageFactory with AppiumFieldDecorator (dual-platform locators)

---

## 2. FOLDER STRUCTURE

```
src/main/java/
  base/         → Base.java (all utility methods, lifecycle hooks)
  factory/      → DriverFactory.java (creates Android/iOS driver)
  listeners/    → RetryAnalyzer, RetryListener, TestListener
  utils/        → ReadConfig, ExcelUtils

src/test/java/
  pages/        → Page Objects (one class per screen)
  tests/        → Test classes (extend Base)

ConfigFiles/    → config.properties (device, app, timeout settings)
TestData/       → .xlsx files for data-driven tests
TestSuites/     → testng.xml files (Sanity.xml, Regression.xml)
Documentations/ → Screen documentation, locator references
```

---

## 3. TEST CLASSIFICATION

When asked to write a test, classify it first:

| Type        | Criteria                              | Suite File      | Group Tag    |
|-------------|---------------------------------------|-----------------|--------------|
| **Sanity**  | Critical path, runs daily             | Sanity.xml      | `sanity`     |
| **Regression** | All features, runs on release      | Regression.xml  | `regression` |
| **Smoke**   | Quickest build check (5–10 tests)     | Smoke.xml       | `smoke`      |
| **Bug Fix** | Reproduces a specific bug             | BugFix-XXX.xml  | `bugfix`     |

---

## 4. MANDATORY RULES FOR GENERATED CODE

Always follow these rules — no exceptions:

### 4.1 Test Class Structure
```java
package tests;

import base.Base;
import io.qameta.allure.*;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import pages.YourPage;
import listeners.TestListener;

@Listeners({TestListener.class})
public class YourTest extends Base {

    @Test(priority = 1, groups = "sanity")
    @Feature("Feature Name")
    @Story("Story Name")
    @Description("What this test verifies")
    @Severity(SeverityLevel.CRITICAL)
    public void tc01_ScreenName_whatIsBeingTested() {

        SoftAssert softAssert = new SoftAssert();
        YourPage page = new YourPage(getDriver());

        // Steps here...

        softAssert.assertAll(); // ALWAYS last line
    }
}
```

### 4.2 Test Naming Convention
```
tc{number}_{ScreenName}_{whatIsBeingTested}

Examples:
  tc01_Home_verifyHeaderIsDisplayed
  tc02_Login_loginWithValidCredentials
  tc03_Profile_scrollToSettingsAndTap
```

### 4.3 SoftAssert — ALWAYS use, NEVER hard Assert in mobile tests
```java
SoftAssert softAssert = new SoftAssert();

// Use softAssert.fail(), softAssert.assertTrue(), softAssert.assertEquals()
// NEVER use Assert.assertTrue() — it stops test on first failure

softAssert.assertAll(); // ALWAYS the last line of every @Test method
```

### 4.4 Allure Steps — ALWAYS wrap logic in Allure.step()
```java
Allure.step("Step description", () -> {
    if (isElementPresentIsDisplayed(element, "yes")) {
        Allure.step("Element found ✓", Status.PASSED);
    } else {
        Allure.step("Element NOT found ✗", Status.FAILED);
        getScreenshots("ElementName_Missing");
        softAssert.fail("Element not found: ElementName");
    }
});

// Log data in report (no pass/fail)
Allure.step("Current device: " + getDeviceName());
```

### 4.5 Scroll Pattern — ALWAYS check-first, bounded loop, break on find
```java
// CORRECT PATTERN
if (!isElementPresentIsDisplayed(page.getElement(), "")) {
    for (int i = 0; i < 5; i++) {              // max 5 swipes
        swipeDirection("up");
        if (isElementPresentIsDisplayed(page.getElement(), "")) {
            break;                              // always break when found
        }
    }
}

// WRONG — never do this
while (true) { swipeDirection("up"); }         // infinite loop risk
```

### 4.6 Page Object Structure — ALWAYS private elements, public getters
```java
public class YourPage {

    public YourPage(AppiumDriver driver) {
        PageFactory.initElements(
            new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    @AndroidFindBy(id = "your_element_id")
    @iOSXCUITFindBy(id = "your_element_id")
    private WebElement yourElement;

    public WebElement getYourElement() { return yourElement; }
}
```

---

## 5. LOCATOR STRATEGIES (by priority)

### Android
1. `@AndroidFindBy(id = "resource-id")` — fastest, most stable
2. `@AndroidFindBy(accessibility = "content-desc")` — use for semantic IDs
3. `@AndroidFindBy(xpath = "//android.widget.TextView[@text='Login']")` — flexible
4. `@AndroidFindBy(xpath = "//*[contains(@content-desc,'partial_id')]")` — for dynamic

### iOS
1. `@iOSXCUITFindBy(id = "accessibility-label")` — most stable
2. `@iOSXCUITFindBy(xpath = "//XCUIElementTypeButton[@name='Login']")` — specific
3. `@iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[contains(@name,'partial')]")` — dynamic

### Never use
- Absolute XPath: `/html/body/div[1]/...` — breaks on any UI change
- Index-based XPath: `//button[2]` — fragile

---

## 6. UTILITY METHODS IN Base.java

Use these methods — do NOT call driver methods directly in tests:

| Method | Description |
|--------|-------------|
| `click(element)` | Safe click with wait — returns boolean |
| `clickByText("text")` | Click element by visible text |
| `sendKeys(element, "text")` | Clear and type text |
| `getText(element)` | Get text safely |
| `getDomAttribute(element)` | Gets content-desc (Android) or name (iOS) |
| `isElementPresentIsDisplayed(element, "yes/")` | Check visibility |
| `isElementPresentIsDisplayed("text", "fallback", "yes/")` | Check by text |
| `findElementsByPlatform(androidXp, iosXp)` | Returns List<WebElement> |
| `swipeDirection("up/down/left/right")` | Swipe gesture |
| `swipeDirection("up", startX%, startY%, offset%)` | Swipe with control |
| `swipeWithDimensions(sx%, sy%, ex%, ey%, ms)` | Full coordinate swipe |
| `longPress(element)` | 1500ms press |
| `tapOnScreen(0.5, 0.9)` | Tap at % coords |
| `scrollToElement(element, "down", 5)` | Scroll until found |
| `getScreenshots()` | Screenshot → Allure |
| `getScreenshots("name")` | Named screenshot → Allure |
| `hideKeyboard()` | Hide soft keyboard |
| `isAndroid()` / `isIOS()` | Platform check |
| `getDriver()` | Current thread's AppiumDriver |
| `getDeviceName()` / `getUdid()` | Current device info |

---

## 7. DATA-DRIVEN TESTING

### Option A — Hardcoded @DataProvider (small datasets)
```java
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    return new Object[][] {
        {"user@test.com", "pass123", "success"},
        {"wrong@test.com", "badpass", "failure"},
    };
}

@Test(dataProvider = "loginData")
public void tc01_Login(String email, String pass, String expected) { ... }
```

### Option B — From Excel (recommended for large datasets)
```java
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    return ExcelUtils.getTableArray(
        config.getTestDataPath(), config.getTestDataSheet());
}
```

---

## 8. SCREEN DOCUMENTATION FORMAT

When documenting a new screen, create a markdown file in `Documentations/`:

```markdown
# ScreenName Screen

## Purpose
Brief description of what this screen does.

## Navigation
How to reach this screen: Home → Menu → Settings

## Elements

| Element         | Android Locator                          | iOS Locator                   |
|----------------|------------------------------------------|-------------------------------|
| Header Text    | id: "screen_header"                      | id: "screen_header"           |
| Submit Button  | id: "submit_btn"                         | accessibility: "Submit"       |
| Input Field    | xpath: //android.widget.EditText         | xpath: //XCUIElementTypeTextField |

## Business Rules
- Rule 1: ...
- Rule 2: ...

## Test Scenarios
1. tc01 — Verify header is displayed
2. tc02 — Verify submit button is enabled
```

---

## 9. BUG FIX TEST PATTERN

When automating a specific bug fix:

```java
// 1. Create a dedicated test class
// src/test/java/tests/BugFix_TICKET123_LoginCrash.java

// 2. Create a dedicated XML in TestSuites/
// TestSuites/BugFix-TICKET123.xml

// 3. Add @Issue annotation
@Issue("TICKET-123")
@Description("Reproduces login crash reported in TICKET-123")
@Test
public void tc01_BugFix_TICKET123_loginCrash() { ... }

// 4. After bug is fixed and verified — move test to regression suite
// Add to Regression.xml under appropriate test class
```

---

## 10. PARALLEL EXECUTION RULES

- **Always use `getDriver()`** — never store driver in a local variable and pass around
- **Never use `static` fields for test data** — use ThreadLocal or local variables
- **SoftAssert is NOT thread-safe** — create a new `SoftAssert` instance per test method
- **Page Objects are NOT shared** — create a new page object instance per test: `new YourPage(getDriver())`

---

## 11. WHAT NOT TO DO

```java
// ❌ Never do these:

Thread.sleep(3000);                           // hardcoded wait
static WebDriver driver;                      // shared in parallel
Assert.assertTrue(condition);                 // stops test on first failure
driver.findElement(By.id("...")).click();     // call driver directly in test
while(true) { swipeDirection("up"); }         // unbounded scroll
softAssert.fail("error");                     // without softAssert.assertAll() at end

// ✅ Always do:
fluentWait().until(...);                      // dynamic wait
ThreadLocal driver;                           // isolated per thread
SoftAssert softAssert = new SoftAssert();     // new per test
click(page.getElement());                     // use Base utility
for (int i=0; i<5; i++) { ... break; }       // bounded scroll
softAssert.assertAll();                       // always last line
```

---

## 12. HOW TO ADD A NEW FEATURE TEST

Step-by-step when asked to add tests for a new feature:

1. **Create Page Object** — `src/test/java/pages/FeatureName.java`
   - Add dual-platform locators for all elements
   - Public getters for each element

2. **Create Test Class** — `src/test/java/tests/FeatureNameTest.java`
   - Extend Base
   - @Listeners({TestListener.class})
   - Write tc01_, tc02_... methods
   - SoftAssert per test, assertAll() at end
   - Allure.step() wrapping all logic

3. **Add to Suite XML** — `TestSuites/Sanity.xml` or `Regression.xml`
   - Add `<class name="tests.FeatureNameTest"/>`

4. **Create Screen Docs** — `Documentations/FeatureName.md`
   - Element table, business rules, test scenarios

---

*Last updated: 2026 | Author: Varun R*
