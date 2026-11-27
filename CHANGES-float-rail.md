# Floating keyboard: one card, tools in its foot (design "Final")

- The floating keyboard is one rounded card (22dp, accent edge). Nothing sits outside it any more:
  the top bar and the two outside corner grips are gone.
- The card's foot is the rail. At rest: a thin dim handle (20dp). Touched: 42dp rail with
  [resize corner] [dock] [handle] [opacity] [resize corner]. It collapses 3.5 s after the last touch
  (never while a finger is dragging). A drag that starts on the thin handle moves the keyboard and opens the rail.
- Resize corners are in the rail, so they are always inside the card and have a 44dp touch area.

# Resize mode (docked keyboard)

- No black scrim. Only the part between the edges is drawn (rounded), the rest is the real screen,
  and touches outside the edges go to the app.
- The whole top strip and both side strips (40dp) are draggable, not just the small pill (these were the dead zones).

# Toolbar follows the keyboard size

- Toolbar height and its buttons scale with the key row height (0.7x to 1.35x), in docked and floating mode.
  Panels that reuse the toolbar slot (emoji search, clip naming, brightness) use the same height.

Files
- ime/FloatFrame.kt, FloatChrome.kt, FloatContent.kt, FloatController.kt, DockRoot.kt
- ime/ResizeOverlay.kt, ResizeGrips.kt, ResizeParts.kt
- ime/PanelDeck.kt, ToolbarContent.kt
