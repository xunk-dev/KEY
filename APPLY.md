# Keyboard background (photo / video)

Copy the contents of this folder over the project folder and replace the existing files. Then run:
./gradlew :core:test :ime:test :app:assembleDebug
then scripts/check-all.sh (the comment and line-count failures in KeyPainter, KeyboardView, KeyboardPlacement, SymbolRail and KeyboardGeometry were already there before this package).

Test by hand:
1. Customize > Style, scroll to Background. Tap Photo, pick an image: the preview and the keyboard at the bottom show it.
2. Drag Transparency up and Darken: the picture fades into the theme and darkens; keys stay readable.
3. Tap Video, pick a clip under 40 MB: a Video badge appears, the keyboard plays it muted. Pick a 4K clip: an error asks for 1080p or less.
4. Open the keyboard in any app: the same background shows, also behind the bottom navigation area. Hide the keyboard: the video pauses.
5. Turn on battery saver: the video becomes a still picture and a note appears in Customize.
6. Switch theme, change colors, use float and one-hand modes: the background stays.
7. X on the preview (or Remove background): back to the plain theme.

Trim and live photo, by hand:
8. Background > Video, pick a clip longer than 15 s: the trim sheet opens with frames. Drag each handle, then the middle; the preview follows the finger. Tap Use this part: the keyboard loops only that part.
9. Pick a clip under 15 s: no sheet, it imports directly. Cancel the sheet or tap outside it: nothing changes.
10. Try a clip of a few minutes (over 40 MB): the 15 s cut imports, the whole file would not.
11. Background > Photo, pick a Motion Photo from the Pixel or Samsung camera: a Live badge shows and the clip loops. Turn off Play the motion: only the sharp photo stays.
12. Switch the phone to Arabic: the timeline still runs left to right, texts are Arabic.

