# ATTA

Daily-affirmation Android app where the **home-screen widget is the product**.
Implemented from the Claude Design handoff (`handoff/untitled/project/`):
Phase A tokens/brandbook + Phase B·C screens, in Jetpack Compose + Glance.

## Build & run

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Unit tests: `./gradlew :app:testDebugUnitTest`

## Map

| Area | Where |
|---|---|
| Design tokens (colour/type/spacing/radius/motion) | `app/src/main/java/com/atta/app/ui/theme/` |
| 8 widget themes, gradient specs | `data/WidgetTheme.kt` |
| Affirmations (15 EN+TH, authored line breaks) | `data/Affirmation.kt` |
| Deterministic line-of-day + feed | `data/AffirmationRepository.kt` |
| Prefs (DataStore) | `data/Prefs.kt` |
| Onboarding: welcome → 5 questions → processing → result | `ui/screens/Onboarding.kt` |
| Paywall (trial timeline, 3 plans) | `ui/screens/PaywallScreen.kt` |
| Home feed (vertical pager, drift gradient), nav/theme sheets, viewer | `ui/screens/HomeScreen.kt` |
| Widget gallery / Focus / Saved / Settings | `ui/screens/SecondaryScreens.kt` |
| Widget bitmap renderer (Canvas + StaticLayout, ≤1.5 MB) | `widget/WidgetRenderer.kt` |
| Glance widget + receiver | `widget/AttaWidget.kt` |
| Daily notification + WorkManager scheduling | `notify/DailyLine.kt` |

## Design rules encoded

- One accent per screen; champagne is matte, never gradiented.
- Thai line height ≥ 1.6× on serif sizes (tone-mark clearance); affirmations are
  hand-broken and stored with their breaks — never re-wrapped.
- Widget is a single pre-rendered bitmap (Glance/RemoteViews has no gradients or
  custom fonts). Text positions per Phase A §03.
- Motion is breath-paced (480/560 ms ease-in-out, 4 s in / 6 s out ring, 10 s
  gradient drift); no springs.

## Known placeholders (per the handoff's "Still open")

- Affirmation copy, tagline, and the 10 category names are the designer's
  placeholders — swap verbatim when final copy arrives.
- Paywall is UI + local state only; no Play Billing integration yet.
- Free tier: Linen theme only; widget/theme picks route to the paywall.
- App icon is a minimal brand-rule mark (Phase D deliverable pending).
