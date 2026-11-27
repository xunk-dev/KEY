# Keyboard background: photo or video with transparency

Where: Customize > Style tab, under the theme grid ("Background" card). The live keyboard at the bottom shows the result while you drag the sliders.

- Pick a photo or a video from the gallery (system photo picker, no storage permission). The file is copied into private app storage; nothing depends on the gallery afterwards.
- Photos are rotated by EXIF, scaled to 1280 px and saved as JPEG. Videos are limited to 40 MB and 1920 px; the first frame is saved as a still.
- Transparency slider (0-90%) blends the picture over the theme background. Darken slider (0-80%) multiplies the picture so keys stay readable on bright photos.
- Video plays muted and looping, only while the keyboard window is visible. It fades in over the still once the first frame renders. In battery saver the still is shown instead. A failing player falls back to the still.
- Wallpaper sits under the keys and also fills the navigation-bar inset area. Float mode, one hand and split modes are unaffected.
- Remove background: X on the preview or the button below the sliders.

Code
- core/theme: Wallpaper.kt (WallpaperChoice, WallpaperMath), WallpaperStore.kt (prefs `space_wallpaper`, files in filesDir/wallpaper), WallpaperImport.kt. Test: WallpaperTest.
- ime/theme: ThemeSnapshot.wallpaper, ThemeController wallpaper functions, WallpaperPresenter.kt (still ImageView + TextureView/MediaPlayer, pinned to the frame), ThemedFrame hosts it and forwards visibility.
- app: WallpaperSection.kt, hooked into StyleTab. Strings wallpaper_* in en and ar.
- The wallpaper is not part of the settings backup (files are not included).
- Not compiled here (no Android SDK). scripts/check-lines, check-comments (for the new files), check-function-lines and check-strings pass.

Trim long videos and live photos
- A video longer than 15 s opens a bottom sheet instead of being rejected: a filmstrip of 10 frames, two handles, a preview of the frame under the finger, and the chosen range as m:ss. Drag a handle to move one edge, drag the middle to slide the whole part. Minimum 1 s, maximum 15 s (TrimMath.MIN_CLIP_MS / MAX_CLIP_MS). Shorter videos import directly as before.
- The cut is a remux (MediaExtractor + MediaMuxer): no re-encoding, no new dependency, fast, no quality loss. The start snaps back to the previous keyframe, which is exactly the frame the start preview shows. Audio is dropped (the video is muted anyway). The 40 MB and 1080p limits now apply to the trimmed clip, so a long phone video can be used.
- Live photos: Android Motion Photos (a JPEG with the clip appended, XMP Container or MicroVideoOffset) are detected on import. The sharp photo becomes the still, the embedded clip loops behind the keys, the preview shows a Live badge, and a switch "Play the motion" turns the motion off while keeping the still. If the clip cannot be read or is too big, the photo is imported as a plain photo.
- iPhone Live Photos are a HEIC plus a separate MOV, so the picker gives only one half. Pick the MOV from the Video button to get the motion (it goes through the same trim sheet).

Code
- core/theme: WallpaperTrim.kt (TrimRange, TrimMath), VideoClipper.kt (VideoInfo, probe and remux), WallpaperFrames.kt (frames for the filmstrip and preview), MotionPhoto.kt (pure byte parser). WallpaperImport gained probe() and import(uri, trim). WallpaperChoice has live and animate; WallpaperStore keeps them.
- ime/theme: ThemeController.probeWallpaper, importWallpaper(uri, trim), setWallpaperAnimate. WallpaperPresenter plays the clip only when animate is on.
- app: WallpaperTrimSheet.kt (Dialog pinned to the bottom, timeline forced left-to-right in Arabic), WallpaperPreview.kt (preview moved out of WallpaperSection), strings wallpaper_trim_* and wallpaper_live* in en and ar.
- Tests: TrimMathTest, MotionPhotoTest, new cases in WallpaperTest. Not compiled or run here (no Android SDK, no network).

