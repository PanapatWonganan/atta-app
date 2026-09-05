# Practice audio mode — design

Date: 2026-09-05. Approved in chat.

## What

A "Practice" mode: the app reads affirmations aloud (on-device TTS), one line
at a time with quiet gaps, over an optional ambient mood loop. Entry is a pill
on the home card. Playback is screen-scoped — leaving the screen stops audio
(no foreground service in v1).

## Decisions locked with the user

- Voice: Android `TextToSpeech` (on-device, free, offline). No backend.
- Scope: full Practice screen (queue from the home feed, mood, pace,
  play/pause). No timer, no manual queue editing in v1.
- Mood sounds: synthesized in-repo (`tools/generate_moods.py`) into
  `app/src/main/res/raw/` as seamless loops. Moods: Calm, Rain, Waves + None.
- Monetization: playback free for everyone; picking a mood other than None on
  the free tier opens the existing paywall (same pattern as widget themes).

## Architecture

- `audio/PracticeVoice.kt` — wraps `TextToSpeech`: async init, locale from the
  app language (`th-TH`/`en-US`), speech rate, `speak()` with an on-done
  callback (utterance listener), `shutdown()`. Reports `unavailable` when the
  engine or language is missing; the screen shows a quiet caption instead of
  crashing.
- `audio/MoodPlayer.kt` — a looping `MediaPlayer` over a `res/raw` file at low
  volume (~0.3). `play(mood)` / `stop()` / `release()`.
- `ui/screens/PracticeScreen.kt` — route `practice/{index}`; rebuilds the same
  feed as Home (same inputs → same list) and starts at the card the user was
  on. Theme background + dark scrim, line centered with a slow crossfade,
  chevron-down to close, mood chip (bottom sheet picker), pace toggle
  (Slow/Normal), one large play/pause circle.
- Loop: speak line → 4 s silence → fade to next line → repeat, until pause or
  close. `DisposableEffect` releases voice + mood player.
- Prefs: `practiceMood` (default `calm`; forced to None while on the free
  tier at playback time), `practicePace` (default `slow`).
- Home: `HomeCard` gains an optional Practice pill (centered above the action
  row); `MainActivity` gains the route.

## Testing

Build, install on the emulator, drive into Practice, screenshot. English TTS
verified on emulator; Thai voice availability differs per device and falls
back to the caption when missing.
