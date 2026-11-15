# Procedure animations (optional)

`ProcedureAnimation` (ui/components) loads one file per indicator from this folder. The file name is the lowercase id:

| File | Indicator | What it must show |
| ----- | ----- | ----- |
| `bp_sys.json` | Systolic blood pressure | Cuff on the upper arm at heart level |
| `heart_rate.json` | Heart rate | Pulse sensor on the finger |
| `glucose.json` | Glucose | Drop from the finger to the glucose meter strip |
| `temp.json` | Temperature | Thermometer under the tongue |
| `sleep.json` | Sleep | Sleep hours (clock or moon) |
| `mood.json` | Behavioral state | Mood and alertness scale |

Rules:

- Format: Lottie JSON (not dotLottie).
- Only free LottieFiles animations (Lottie Simple License) or our own.
- Check that the animation shows the correct procedure before adding it.
- If a file is missing the app does not fail: it simply shows no animation.

The guided measurement already shows a built-in `Canvas` animation per step (`StepAnimation`), so these files are optional.

## Credits

| File | Animation | Author | URL |
| ----- | ----- | ----- | ----- |
