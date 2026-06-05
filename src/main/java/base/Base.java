package base;

import factory.DriverFactory;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.ReadConfig;

import java.io.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Base.java — Foundation class for all test classes
 *
 * Provides:
 *  - ThreadLocal AppiumDriver (parallel-safe)
 *  - Driver init/teardown lifecycle (@BeforeTest / @AfterTest)
 *  - Screen recording per test (@BeforeMethod / @AfterMethod)
 *  - ADB logcat capture per test (optional)
 *  - All utility methods: click, swipe, scroll, wait, screenshot, etc.
 *
 * All test classes extend Base:
 *   public class LoginTest extends Base { ... }
 */
public class Base {

    // ===== THREAD-LOCAL DRIVER (one driver per parallel thread) =====
    private static final ThreadLocal<AppiumDriver> threadLocalDriver = new ThreadLocal<>();
    private static final ThreadLocal<String>       tlDeviceName      = new ThreadLocal<>();
    private static final ThreadLocal<String>       tlUdid            = new ThreadLocal<>();
    private static final ThreadLocal<String>       tlPlatform        = new ThreadLocal<>();

    // ===== SCREEN RECORDING =====
    private final ThreadLocal<String>       tlRecordingMethod = new ThreadLocal<>();

    // ===== ADB LOGCAT =====
    private final ThreadLocal<StringBuilder> tlLogBuilder     = new ThreadLocal<>();
    private final ThreadLocal<Process>       tlLogcatProcess  = new ThreadLocal<>();
    private final ThreadLocal<Thread>        tlLogReader       = new ThreadLocal<>();

    protected static final ReadConfig config = new ReadConfig();

    // =====================================================================
    // LIFECYCLE — TestNG hooks
    // =====================================================================

    /**
     * Runs before each <test> tag in testng.xml
     * Receives device params from XML and initialises the driver
     */
    @BeforeTest(alwaysRun = true)
    @Parameters({"platform", "deviceName", "version", "udid"})
    public void initDriver(
            @Optional("Android") String platform,
            @Optional("Pixel_5_API_33") String deviceName,
            @Optional("13") String version,
            @Optional("emulator-5554") String udid) {

        System.out.println("▶ Initialising driver | Platform: " + platform
                + " | Device: " + deviceName + " | UDID: " + udid);

        tlDeviceName.set(deviceName);
        tlUdid.set(udid);
        tlPlatform.set(platform);

        AppiumDriver driver = DriverFactory.createDriver(udid, deviceName, version);
        threadLocalDriver.set(driver);

        System.out.println("✓ Driver initialised for: " + deviceName);
    }

    /**
     * Runs before EACH @Test method
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeEachTest(java.lang.reflect.Method method) {
        tlRecordingMethod.set(method.getName());
        startScreenRecording();
        if (config.isAdbLogcatEnabled()) {
            startAdbLogCapture();
        }
    }

    /**
     * Runs after EACH @Test method
     */
    @AfterMethod(alwaysRun = true)
    public void afterEachTest(ITestResult result) {
        String methodName = result.getMethod().getMethodName();

        if (config.isAdbLogcatEnabled()) {
            stopAdbLogCaptureAndAttach(methodName);
        }
        stopScreenRecording(methodName);
    }

    /**
     * Runs after each <test> tag in testng.xml — quits driver
     */
    @AfterTest(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            threadLocalDriver.remove();
            System.out.println("✓ Driver quit for: " + tlDeviceName.get());
        }
    }

    // =====================================================================
    // DRIVER ACCESSORS
    // =====================================================================

    /** Returns the AppiumDriver for the current thread */
    public AppiumDriver getDriver() {
        return threadLocalDriver.get();
    }

    /** Returns the current device name */
    public String getDeviceName() { return tlDeviceName.get(); }

    /** Returns the current UDID */
    public String getUdid() { return tlUdid.get(); }

    /** Returns the current platform */
    public String getPlatform() { return tlPlatform.get(); }

    /** Returns true if running on Android */
    public boolean isAndroid() {
        return getPlatform() != null && getPlatform().equalsIgnoreCase("Android");
    }

    /** Returns true if running on iOS */
    public boolean isIOS() {
        return getPlatform() != null && getPlatform().equalsIgnoreCase("iOS");
    }

    // =====================================================================
    // CLICK UTILITIES
    // =====================================================================

    /**
     * Safe click — catches common exceptions, returns false instead of throwing
     */
    public boolean click(WebElement element) {
        try {
            fluentWait().until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            return true;
        } catch (NoSuchElementException | StaleElementReferenceException
                 | TimeoutException | ElementNotInteractableException e) {
            System.out.println("⚠ click() failed: " + e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Click element found by text (builds xpath dynamically)
     */
    public boolean clickByText(String text) {
        try {
            String xpath = isAndroid()
                ? "//*[@text='" + text + "' or @content-desc='" + text + "']"
                : "//*[@name='" + text + "' or @label='" + text + "']";
            WebElement el = getDriver().findElement(By.xpath(xpath));
            return click(el);
        } catch (Exception e) {
            System.out.println("⚠ clickByText() failed for text: " + text);
            return false;
        }
    }

    /**
     * Click using JavascriptExecutor (useful when element is not interactable)
     */
    public void jsClick(WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
    }

    // =====================================================================
    // INPUT UTILITIES
    // =====================================================================

    /**
     * Clear field and type text
     */
    public void sendKeys(WebElement element, String text) {
        try {
            fluentWait().until(ExpectedConditions.visibilityOf(element));
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            System.out.println("⚠ sendKeys() failed: " + e.getMessage());
        }
    }

    /**
     * Get text from element safely
     */
    public String getText(WebElement element) {
        try {
            return element.getText().trim();
        } catch (Exception e) {
            System.out.println("⚠ getText() failed: " + e.getMessage());
            return "";
        }
    }

    /**
     * Get content-desc (Android) or name (iOS) attribute
     */
    public String getDomAttribute(WebElement element) {
        try {
            String attr = isAndroid() ? "content-desc" : "name";
            String value = element.getDomAttribute(attr);
            return value != null ? value : element.getText();
        } catch (Exception e) {
            return "";
        }
    }

    // =====================================================================
    // ELEMENT PRESENCE UTILITIES
    // =====================================================================

    /**
     * Check if element is visible — uses FluentWait, returns false instead of throwing
     *
     * @param element        WebElement to check
     * @param takeScreenshot Pass "yes" to capture screenshot when found
     * @return true if element is present and visible
     */
    public boolean isElementPresentIsDisplayed(WebElement element, String takeScreenshot) {
        try {
            fluentWait().until(ExpectedConditions.visibilityOf(element));
            if ("yes".equalsIgnoreCase(takeScreenshot)) {
                getScreenshots();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if element with specific text is visible
     *
     * @param primaryText    Primary text to find
     * @param fallbackText   Secondary text (leave "" if not needed)
     * @param takeScreenshot Pass "yes" to capture screenshot when found
     */
    public boolean isElementPresentIsDisplayed(String primaryText, String fallbackText, String takeScreenshot) {
        try {
            String xpath;
            if (isAndroid()) {
                xpath = fallbackText.isEmpty()
                    ? "//*[@text='" + primaryText + "' or @content-desc='" + primaryText + "']"
                    : "//*[@text='" + primaryText + "' or @text='" + fallbackText
                        + "' or @content-desc='" + primaryText + "']";
            } else {
                xpath = fallbackText.isEmpty()
                    ? "//*[@name='" + primaryText + "' or @label='" + primaryText + "']"
                    : "//*[@name='" + primaryText + "' or @name='" + fallbackText
                        + "' or @label='" + primaryText + "']";
            }

            WebElement el = fluentWait().until(d -> d.findElement(By.xpath(xpath)));
            if ("yes".equalsIgnoreCase(takeScreenshot)) {
                getScreenshots();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find elements using platform-specific XPath
     */
    public List<WebElement> findElementsByPlatform(String androidXpath, String iosXpath) {
        String xpath = isAndroid() ? androidXpath : iosXpath;
        try {
            return getDriver().findElements(By.xpath(xpath));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find element with text (platform-aware)
     */
    public WebElement findElementWithText(String primaryText, String fallbackText) {
        String xpath;
        if (isAndroid()) {
            xpath = fallbackText.isEmpty()
                ? "//*[@text='" + primaryText + "' or @content-desc='" + primaryText + "']"
                : "//*[@text='" + primaryText + "' or @text='" + fallbackText + "']";
        } else {
            xpath = fallbackText.isEmpty()
                ? "//*[@name='" + primaryText + "' or @label='" + primaryText + "']"
                : "//*[@name='" + primaryText + "' or @name='" + fallbackText + "']";
        }
        return getDriver().findElement(By.xpath(xpath));
    }

    // =====================================================================
    // WAIT UTILITIES
    // =====================================================================

    /**
     * FluentWait — polls every N ms, ignores common exceptions
     * Timeout configured in config.properties (timeout.fluent)
     */
    public Wait<AppiumDriver> fluentWait() {
        return new FluentWait<>(getDriver())
            .withTimeout(Duration.ofSeconds(config.getFluentTimeout()))
            .pollingEvery(Duration.ofMillis(config.getFluentPollMs()))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .ignoring(ElementNotInteractableException.class);
    }

    /**
     * WebDriverWait — explicit wait for a specific condition
     *
     * @param seconds How long to wait
     */
    public WebDriverWait explicitWait(int seconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(seconds));
    }

    /**
     * Convenience: wait for element to be visible
     */
    public WebElement waitForVisibility(WebElement element) {
        return explicitWait(config.getExplicitTimeout())
            .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Convenience: wait for element to be clickable
     */
    public WebElement waitForClickable(WebElement element) {
        return explicitWait(config.getExplicitTimeout())
            .until(ExpectedConditions.elementToBeClickable(element));
    }

    // =====================================================================
    // GESTURE UTILITIES
    // =====================================================================

    /**
     * Swipe in a direction using percentage-based coordinates
     * Works on all screen sizes — no hardcoded pixel values
     *
     * @param direction "up" | "down" | "left" | "right"
     * @param startXPct Starting X as % of screen (0–100). Default: 50
     * @param startYPct Starting Y as % of screen (0–100). Default: 70
     * @param offsetPct Swipe distance as % of screen. Default: 30
     */
    public void swipeDirection(String direction, Integer startXPct, Integer startYPct, Integer offsetPct) {
        Dimension size = getDriver().manage().window().getSize();
        int screenWidth  = size.getWidth();
        int screenHeight = size.getHeight();

        int sX   = (int) (screenWidth  * ((startXPct  != null ? startXPct  : 50) / 100.0));
        int sY   = (int) (screenHeight * ((startYPct  != null ? startYPct  : 70) / 100.0));
        int dist = (int) (screenHeight * ((offsetPct  != null ? offsetPct  : 30) / 100.0));

        int eX = sX;
        int eY = sY;

        switch (direction.toLowerCase()) {
            case "up":    eY = sY - dist; break;
            case "down":  eY = sY + dist; break;
            case "left":  eX = sX - dist; break;
            case "right": eX = sX + dist; break;
            default: throw new IllegalArgumentException("Unknown direction: " + direction);
        }

        performSwipe(sX, sY, eX, eY, 600);
    }

    /**
     * Shorthand swipeDirection with defaults
     */
    public void swipeDirection(String direction) {
        swipeDirection(direction, null, null, null);
    }

    /**
     * Swipe from one % coordinate to another — full control
     *
     * @param startXPct Start X as % of screen width
     * @param startYPct Start Y as % of screen height
     * @param endXPct   End X as % of screen width
     * @param endYPct   End Y as % of screen height
     * @param durationMs Swipe duration in milliseconds
     */
    public void swipeWithDimensions(int startXPct, int startYPct, int endXPct, int endYPct, int durationMs) {
        Dimension size = getDriver().manage().window().getSize();
        int sX = (int) (size.getWidth()  * (startXPct / 100.0));
        int sY = (int) (size.getHeight() * (startYPct / 100.0));
        int eX = (int) (size.getWidth()  * (endXPct   / 100.0));
        int eY = (int) (size.getHeight() * (endYPct   / 100.0));
        performSwipe(sX, sY, eX, eY, durationMs);
    }

    /**
     * Low-level swipe using W3C PointerInput actions
     */
    private void performSwipe(int startX, int startY, int endX, int endY, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(Arrays.asList(swipe));
    }

    /**
     * Long press on an element
     */
    public void longPress(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int x = location.getX() + size.getWidth()  / 2;
        int y = location.getY() + size.getHeight() / 2;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence press = new Sequence(finger, 1);
        press.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        press.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        press.addAction(finger.createPointerMove(Duration.ofMillis(1500), PointerInput.Origin.viewport(), x, y));
        press.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(Arrays.asList(press));
    }

    /**
     * Tap at percentage coordinates of the screen
     *
     * @param xPct X position as fraction (0.0 to 1.0)
     * @param yPct Y position as fraction (0.0 to 1.0)
     */
    public void tapOnScreen(double xPct, double yPct) {
        Dimension size = getDriver().manage().window().getSize();
        int x = (int) (size.getWidth()  * xPct);
        int y = (int) (size.getHeight() * yPct);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(Arrays.asList(tap));
    }

    /**
     * Scroll until element is visible — bounded scroll
     *
     * @param element   Element to scroll to
     * @param direction "up" | "down"
     * @param maxSwipes Maximum scroll attempts (prevents infinite loop)
     */
    public void scrollToElement(WebElement element, String direction, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            if (isElementPresentIsDisplayed(element, "")) break;
            swipeDirection(direction);
        }
    }

    /**
     * Swipe back to home screen (Android: back button | iOS: swipe from left edge)
     */
    public void swipeBackToHome() {
        try {
            if (isAndroid()) {
                ((AndroidDriver) getDriver()).pressKey(
                    new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.HOME));
            } else {
                // iOS: swipe from left edge to navigate back
                swipeWithDimensions(2, 50, 50, 50, 400);
            }
        } catch (Exception e) {
            System.out.println("⚠ swipeBackToHome() failed: " + e.getMessage());
        }
    }

    // =====================================================================
    // SCREENSHOT UTILITIES
    // =====================================================================

    /**
     * Capture screenshot and attach to Allure report
     */
    public void getScreenshots() {
        try {
            byte[] screenshot = ((TakesScreenshot) getDriver())
                .getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment(
                "Screenshot", "image/png", "png", screenshot);
        } catch (Exception e) {
            System.out.println("⚠ Screenshot failed: " + e.getMessage());
        }
    }

    /**
     * Capture screenshot with a custom name
     */
    public void getScreenshots(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) getDriver())
                .getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment(
                name, "image/png", "png", screenshot);
        } catch (Exception e) {
            System.out.println("⚠ Screenshot failed: " + e.getMessage());
        }
    }

    // =====================================================================
    // SCREEN RECORDING
    // =====================================================================

    /**
     * Start screen recording before each test
     */
    private void startScreenRecording() {
        try {
            if (isAndroid()) {
                ((AndroidDriver) getDriver()).startRecordingScreen();
            } else {
                ((IOSDriver) getDriver()).startRecordingScreen();
            }
        } catch (Exception e) {
            System.out.println("⚠ Screen recording start failed: " + e.getMessage());
        }
    }

    /**
     * Stop recording and attach video to Allure
     */
    private void stopScreenRecording(String methodName) {
        try {
            String base64Video;
            if (isAndroid()) {
                base64Video = ((AndroidDriver) getDriver()).stopRecordingScreen();
            } else {
                base64Video = ((IOSDriver) getDriver()).stopRecordingScreen();
            }

            byte[] videoBytes = java.util.Base64.getDecoder().decode(base64Video);
            Allure.getLifecycle().addAttachment(
                "Recording_" + methodName, "video/mp4", "mp4", videoBytes);
        } catch (Exception e) {
            System.out.println("⚠ Screen recording stop failed: " + e.getMessage());
        }
    }

    // =====================================================================
    // ADB LOGCAT CAPTURE (Android only, optional)
    // =====================================================================

    /**
     * Start ADB logcat capture — runs in background thread
     * Each test gets its own logcat, attached to Allure after test
     */
    private void startAdbLogCapture() {
        if (!isAndroid()) return;

        tlLogBuilder.set(new StringBuilder());

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "adb", "-s", getUdid(), "logcat", "-v", "time", "-c");
            pb.start().waitFor(); // clear buffer first

            ProcessBuilder pb2 = new ProcessBuilder(
                "adb", "-s", getUdid(), "logcat", "-v", "time");
            pb2.redirectErrorStream(true);
            Process process = pb2.start();
            tlLogcatProcess.set(process);

            Thread reader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        tlLogBuilder.get().append(line).append("\n");
                    }
                } catch (IOException ignored) {}
            });
            reader.setDaemon(true);
            reader.start();
            tlLogReader.set(reader);

        } catch (Exception e) {
            System.out.println("⚠ ADB logcat start failed: " + e.getMessage());
        }
    }

    /**
     * Stop logcat capture and attach to Allure report
     */
    private void stopAdbLogCaptureAndAttach(String methodName) {
        if (!isAndroid()) return;

        try {
            Process process = tlLogcatProcess.get();
            if (process != null) {
                process.destroy();
            }
            Thread reader = tlLogReader.get();
            if (reader != null) {
                reader.join(2000);
            }

            StringBuilder log = tlLogBuilder.get();
            if (log != null && log.length() > 0) {
                Allure.getLifecycle().addAttachment(
                    "ADB_Logcat_" + methodName,
                    "text/plain", "txt",
                    log.toString().getBytes());
            }
        } catch (Exception e) {
            System.out.println("⚠ ADB logcat stop failed: " + e.getMessage());
        } finally {
            tlLogBuilder.remove();
            tlLogcatProcess.remove();
            tlLogReader.remove();
        }
    }

    // =====================================================================
    // APP MANAGEMENT
    // =====================================================================

    /**
     * Restart the app (without reinstalling)
     */
    public void restartApp() {
        try {
            if (isAndroid()) {
                AndroidDriver androidDriver = (AndroidDriver) getDriver();
                androidDriver.terminateApp(config.getAndroidAppPackage());
                androidDriver.activateApp(config.getAndroidAppPackage());
            }
        } catch (Exception e) {
            System.out.println("⚠ restartApp() failed: " + e.getMessage());
        }
    }

    /**
     * Hide keyboard if visible
     */
    public void hideKeyboard() {
        try {
            getDriver().hideKeyboard();
        } catch (Exception ignored) {}
    }

    /**
     * Accept an alert / system popup
     */
    public void acceptAlert() {
        try {
            explicitWait(5).until(ExpectedConditions.alertIsPresent());
            getDriver().switchTo().alert().accept();
        } catch (Exception ignored) {}
    }
}
