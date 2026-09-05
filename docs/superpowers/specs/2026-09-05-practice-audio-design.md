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

## Phase 2 (2026-09-06): background playback

`audio/PracticeService.kt` — a foreground service (`mediaPlayback` type) now
owns the voice, mood bed, and line loop; audio continues when the screen
closes or the phone locks. A framework `MediaSession` + media-style
notification provide play/pause/end from the shade and lock screen, with
audio-focus handling (pause on loss) and stop on task removal. The screen is
a remote: it mirrors `PracticeService.state` (StateFlow) and sends
start/toggle intents. Entering Practice from a card jumps the running
session to that card's line. Paused sessions detach the notification so a
swipe ends them. No new dependencies — framework media APIs only.

## Groups 1+2 (2026-09-06): session depth + growth

- Sleep timer: top-right chip cycles ∞ → 5′ → 10′ → 15′; wall-clock in the
  service, fades the mood bed and ends after the line being read.
- Breathing hairline under the line: 4s in / 6s out (`AttaMotion.Breath*`),
  still while paused. The only motion on the screen.
- Practice from Saved: `PracticeQueue` builds the queue from `feed` or
  `saved` (user's own lines first, then bookmarks); "Listen" entry on Saved.
- Share as image: `share/ShareCard.kt` renders a 1080×1920 card (theme
  gradient, serif line, ATTA mark) via FileProvider (`cache/share/`).
- Own lines (Plus): editor sheet on Saved, stored in prefs as
  `custom_lines` entries (`CustomLines`), removable, read aloud in practice.
- Quiet check-in: once a day when leaving practice — Calm / Okay / Heavy,
  one `mood_log` entry per date, a fortnight of dots. No streaks.

## Testing

Build, install on the emulator, drive into Practice, screenshot. English TTS
verified on emulator; Thai voice availability differs per device and falls
back to the caption when missing.
