# Bottom row keys: same size on every page and language

Cause: the screen-edge safety inset (12 dp) was cut from the first and last key of the bottom row only. The first key (symbols) lost 36 px of width, and because symbols and 123 are drawn as squares (side = min(width, height)) the first square came out 60 px and the second 96 px.

- Layouts: symbols and 123 weights are 35% larger in all eight letter layouts (taken from the space bar, row totals unchanged), so the two keys are about 133 px wide on a 1080 px screen.
- core/KeyboardGeometry: the bottom row is now laid out inside the inset band, so every key keeps its weight share and symbols and 123 get identical boxes. The touch area of the first and last key still reaches the screen edge.
- ime/KeyPainter: symbols, 123 and a round Enter are drawn with one shared side (the smallest of them), centred in their cells. A key twice as wide as a letter key (ABC on the symbols page) is drawn as a normal key and not as a square.
- Test: KeyboardGeometryTest.edgeInsetDoesNotMakeTheFirstBottomKeyNarrowerThanItsShare.
- Not compiled here (no Gradle or Android SDK). Sizes were checked with a Python port of the geometry: before 60 x 138 and 96 x 138, after 89 x 138 for both (ar, ar_ten, en, az, fa).

# Numbers page: symbol rail drag

- KeyboardView.down: a symbol of the rail is no longer typed on touch-down. It is typed on release, only if the finger did not scroll the rail. Before, dragging from a symbol typed it first and then scrolled.
- KeyboardView.onDraw: while the rail scrolls, the symbol under the finger is not drawn as pressed.
- SymbolRail.finish: a tap that stops a running settle now snaps to the nearest row (no half-cut rows); a finger that rested about 90 ms before lifting no longer flings.
- KeyboardPlacement: on the numbers page the edge inset is 0, so the bottom row stays in line with the columns above (14 / 24 / 24 / 24 / 14), with no step at the left and right edges. Other pages keep the inset.
- Not compiled here.
