# Test data guide — SoftieCare

Values to test and demo the app. The range for each level is in the Blueprint, section 4.3 (table "Reading the cutoffs"); this guide only lists what to enter and which result to expect.

Internal levels and what the app shows: `NORMAL` = "STABLE" (green), `WARNING` = "NOT OPTIMAL" (amber), `CRITICAL` = "CRITICAL" (red). Exception: for `MOOD`, `CRITICAL` is shown as "NOT OPTIMAL" (Blueprint 1.1).

The data is simulated and the cutoffs are heuristic (Blueprint 1.1).

## 1. Manual entry per indicator

Each row is a value to type in the guided measurement (`AssessmentScreen`) and the level the app must show.

### BP_SYS — Systolic blood pressure (mmHg)

| Case | Value | Expected result |
| ----- | ----- | ----- |
| Stable | 120 | `NORMAL` |
| Low caution | 92 | `WARNING` |
| High caution | 135 | `WARNING` |
| Low bad | 85 | `CRITICAL` |
| High bad (Phase 3 demo) | 145 | `CRITICAL` |
| Boundaries | 94.99 / 95 / 129.99 / 130 / 139.99 / 140 | `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rejected | 49, 251, -10, "abc" | Error message, not saved |

### HEART_RATE — Heart rate (bpm)

| Case | Value | Expected result |
| ----- | ----- | ----- |
| Stable | 72 | `NORMAL` |
| Low caution | 55 | `WARNING` |
| High caution | 100 | `WARNING` |
| Low bad | 45 | `CRITICAL` |
| High bad | 110 | `CRITICAL` |
| Boundaries | 49.99 / 50 / 59.99 / 60 / 90 / 90.01 / 105 / 105.01 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rejected | 29, 221, "abc" | Error message, not saved |

### GLUCOSE — Fasting glucose (mg/dL)

| Case | Value | Expected result |
| ----- | ----- | ----- |
| Stable | 90 | `NORMAL` |
| Low caution | 65 | `WARNING` |
| High caution | 110 | `WARNING` |
| Low bad | 55 | `CRITICAL` |
| High bad | 130 | `CRITICAL` |
| Boundaries | 59.99 / 60 / 69.99 / 70 / 99.99 / 100 / 125 / 125.01 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rejected | 29, 401, "abc" | Error message, not saved |

### TEMP — Body temperature (°C)

| Case | Value | Expected result |
| ----- | ----- | ----- |
| Stable | 36.6 | `NORMAL` |
| Low caution | 35.8 | `WARNING` |
| High caution | 37.6 | `WARNING` |
| Low bad | 35.0 | `CRITICAL` |
| High bad | 38.4 | `CRITICAL` |
| Boundaries | 35.49 / 35.5 / 36.09 / 36.1 / 37.29 / 37.3 / 37.99 / 38.0 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rejected | 31.9, 43.1, 50, "abc" | Error message, not saved (outside the 32–43 °C capture range) |

### SLEEP — Sleep hours (hrs)

| Case | Value | Expected result |
| ----- | ----- | ----- |
| Stable | 7.5 | `NORMAL` |
| Low caution | 6.0 | `WARNING` |
| High caution | 9.5 | `WARNING` |
| Low bad | 4.5 | `CRITICAL` |
| High bad | 11 | `CRITICAL` |
| Boundaries | 5.49 / 5.5 / 6.99 / 7.0 / 9.0 / 9.01 / 10.0 / 10.01 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rejected | -1, 24.5, "abc" | Error message, not saved |

### MOOD — Behavioral state (pts, 1 to 5)

| Case | Value | Expected result |
| ----- | ----- | ----- |
| Stable | 4.5 | `NORMAL` |
| Caution | 3.0 | `WARNING` |
| Bad | 2.0 | `CRITICAL` |
| Boundaries | 2.49 / 2.5 / 3.99 / 4.0 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` |
| Rejected | 0.5, 5.5, "abc" | Error message, not saved |

Each valid entry is appended to the end of the 7-reading chart and the oldest reading is dropped.

## 2. PVT (affects MOOD only)

Starting from `MOOD` = 4.5:

| Reaction time | Expected result |
| ----- | ----- |
| 300 ms or 380 ms | No change |
| 381 ms or 500 ms | `MOOD` = 3.0 (`WARNING`) |
| 501 ms or more | `MOOD` = 2.0 (`CRITICAL`, shown as "NOT OPTIMAL") |
| 450 ms with `MOOD` already at 2.0 | No change and no new reading on the chart |
| Tap before the green stimulus | "False start — retry" message, nothing recorded |

## 3. Simulator scenarios

| Scenario | Expected result |
| ----- | ----- |
| Nominal | All 6 indicators `NORMAL` and charts with their 7 initial readings |
| Radiation spike | Only `TEMP` = 38.4 (`CRITICAL`); listed under "Needs attention" on the Today tab |
| Isolation fatigue | Only `SLEEP` = 4.5 (`CRITICAL`) and `MOOD` = 2.0 (shown as "NOT OPTIMAL"); both listed under "Needs attention" |

## 4. Countermeasure

With an indicator in `WARNING` or `CRITICAL`, "Start countermeasure" opens the plan. After checking all steps, the wait starts (accelerated to about 15 seconds) and a "Time to measure again" notification appears when it ends (Android 13+ asks for permission first). "Measure again", or tapping the notification, opens the guided measurement; a value from the "Stable" row in section 1 brings the indicator back to green and is added to the chart. The full demo flow is in the Blueprint, section 8, Phase 3.
