# Screen Name — Documentation Template

> Copy this file for each new screen you automate.
> Save in: Documentations/YourScreenName.md

---

## Overview
Brief description of what this screen does and its purpose in the app.

## How to Navigate Here
Describe the steps to reach this screen:
- App launch → Home → Menu → This Screen

## Screenshot
*(Add a screenshot of the screen here for reference)*

---

## Element Reference Table

| Element Name       | Android Locator                                        | iOS Locator                                         | Notes           |
|--------------------|--------------------------------------------------------|-----------------------------------------------------|-----------------|
| Screen Header      | `id: "screen_header_text"`                             | `accessibility: "screen_header_text"`               | Always visible  |
| Primary Button     | `id: "primary_action_btn"`                             | `accessibility: "primary_action_btn"`               |                 |
| Input Field        | `id: "input_field"`                                    | `accessibility: "input_field"`                      |                 |
| Error Message      | `xpath: //TextView[contains(@text,'Error')]`           | `xpath: //XCUIElementTypeStaticText[contains(@name,'Error')]` | Conditional |
| List Item (repeat) | `xpath: //*[contains(@content-desc,'list_item_')]`     | `xpath: //XCUIElementTypeButton[contains(@name,'list_item_')]` | Dynamic |

---

## Business Rules
List the rules that govern this screen's behaviour:

1. **Rule 1:** What happens when user does X
2. **Rule 2:** What is the expected state when Y condition is met
3. **Rule 3:** Which elements are conditional/dynamic

---

## Test Scenarios

| TC#  | Test Name                                        | Group      | Priority |
|------|--------------------------------------------------|------------|----------|
| tc01 | Verify screen header is displayed                | sanity     | 1        |
| tc02 | Verify primary button is enabled and tappable    | sanity     | 2        |
| tc03 | Verify input field accepts text                  | regression | 3        |
| tc04 | Verify error message on invalid input            | regression | 4        |
| tc05 | Verify list items load and are scrollable        | regression | 5        |

---

## Page Object Class
```
src/test/java/pages/YourScreenNamePage.java
```

## Test Class
```
src/test/java/tests/YourScreenNameTest.java
```

---

## Notes / Known Issues
- Any known limitations, timing issues, or special handling needed
- List any platform-specific differences between Android and iOS behaviour
