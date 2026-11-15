# Bundled animations

`emoji/<codepoint>.json` are Lottie files from **Noto Emoji Animation** by Google, used unmodified.

- Source: https://googlefonts.github.io/noto-emoji-animation/ (copied from https://github.com/rbozan/animated-emojis-rs)
- License: CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/). Credit is shown on the About screen.

`StepAnimation` (ui/components) maps each measurement step to one of these files. To change an animation, replace the codepoint in `emojiFor`.
