# Floating glass dock

## Compact glass dock (v37)

Ported from the approved HTML simulation. Conversion rule: 1 CSS px = 1 dp on a 393 dp phone. The simulation draws the capsule with `box-sizing: border-box`, a 1 px border and 4 px padding. Compose draws borders inside the bounds without changing layout, so the inner inset is 4 + 1 = 5 dp, which gives the same 48 dp tab height and 66 dp tab width.

| Item | Before | Now |
| --- | --- | --- |
| Dock height | 68 dp | 58 dp |
| Dock width | fills the row | 3 x 66 + 2 x 5 = 208 dp |
| Tab height | 56 dp | 48 dp |
| Keyboard button | 64 dp | 58 dp, same height as the dock |
| Gap between button and dock | 12 dp | 10 dp |
| Side and bottom margin | 16 dp / 12 dp | 16 dp / 12 dp |
| Content reserve | 88 dp | 12 + 58 + 10 = 80 dp |
| Tab icon | 28 dp square, big empty margin | 32.14 x 30 dp, cropped to the artwork |
| Try icon | 38 dp | 40 dp, cropped |
| Selected icon scale | 1.2 | 1.14 |
| Tab label | bold, theme label size | 11 sp, medium, 14 sp line, 2 dp above |
| Beam | 40 x 4 dp, glow circle 48 dp | 34 x 3 dp, glow ellipse 37 x 26 dp, starts below the 1 dp border |
| Blur | 24 dp | 30 dp, plus saturation 1.9 and brightness 1.06 (Android 12+) |
| Container alpha (dark) | 0.58 | 0.36 with a white sheen from 0.13 to 0.02 |

Icon frames. Each icon is cropped on the 128 grid so it fills its slot and is centred on its artwork: Home `4 4 120 112`, Library `4 2 120 112`, Settings `4 14 120 112`, Try `6 8 116 116`. The width is `30 x 120 / 112 = 32.14 dp`.

New details: a glass pill behind the selected tab (scale 0.8 to 1 on the same spring), an outer soft shadow drawn only outside the capsule, top and bottom inner highlights, a relief gradient on the keyboard button, and a solid fallback when reduce transparency is on.

Layout. The group is anchored to the start side (right in Arabic) instead of spanning the width. The tab row still uses `weight(1f)`, so three tabs share the 208 dp evenly.

Not ported: the 0.95 press scale on tabs. The tab uses `hapticClickable` without an interaction source, and a second pointer handler could swallow taps.

## Original design

## What it is

A floating capsule dock over every top-level screen, plus a round keyboard-try button beside it. Chosen design: top beam indicator, icon pop motion, gradient button with a light ring. Only the selected tab shows its label.

## Files (all in `app/src/main/java/com/spacekeyboard/keyboard/dock/`)

- `DockHost.kt`: wraps the screens, floats the dock, hides it while the system keyboard is shown, reserves bottom space.
- `DockBar.kt`, `DockTabItem.kt`, `DockBeam.kt`: capsule, tabs (Role.Tab, selected state, 48dp+), beam with spring motion.
- `DockIcons.kt`: Home, Customize (Library), Settings and Try as full-colour glass `ImageVector`s with gradient brushes, all built on one keyboard silhouette. Try is a text field with a caret above a keyboard.
- Tab icons are no longer tinted. Idle tabs are desaturated and faded; the selected tab animates to full colour. The Try button shows its own icon at 38dp on the gradient circle.
- `DockPalette.kt`: every colour derived from `SpaceTheme.colors` (`accentA/B/C`, `surface`, `onSurfaceVariant`).
- `Backdrop.kt`: real backdrop blur on Android 12+ through a graphics layer. Older versions get a translucent container without blur.
- `KeyboardFab.kt`, `TrySheet.kt`: the 64dp button and the bottom sheet with a large text field that raises the keyboard.
- `DockInset.kt`, `DockMetrics.kt`, `DockTab.kt`, `DockPreviews.kt`: inset local, sizes, tab enum, three previews.

## Changed files

- `MainActivity.kt`: wraps `Content()` in `DockHost`. The selected tab is derived from `screen`, so `ScreenSave` already restores it after rotation.
- `DockRoutes.kt` (new, app): Home = HOME, Library = THEMES, Settings = CUSTOMIZE. Dock is hidden on SETUP and EDITOR.
- `ScreenFrame.kt`, `HomeScreen.kt`, `DecorateScreen.kt`, `ImagesScreen.kt`: bottom padding from `LocalDockInset` so the last item clears the dock. Content scrolls under the glass.
- `strings.xml` (en, ar): 8 new strings.
- `app/build.gradle.kts`, `libs.versions.toml`: `ui-tooling-preview` (BOM managed) for the previews.

## Notes

- The dock follows `AppTheme`, which is fixed to `SpaceLook.Eclipse`, so it shows the Eclipse accent. With `SpaceLook.Galaxy` it turns violet.
- Tab order follows the layout direction: right to left in Arabic.
- Not compiled in the authoring environment (no Gradle dependencies available). Build and run `scripts/check-all.sh` before merging.
