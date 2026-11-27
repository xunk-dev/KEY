# Autofix understands neighbouring keys (any layout)

A finger slip lands on a key next to the intended one. The engine now knows which keys are next to each other from the real key positions on screen, so this holds for QWERTY, AZERTY, Arabic and layouts the user rearranged.

- feature-autofix: new KeyNeighbors / KeyMap / KeyPoint. adjacent(pressed, intended), toward(pressed, intended) (the 3x3 zone facing the intended key) and aligned(pressed, intended, zone). Unknown keys are never blocked, and KeyNeighbors.Unknown keeps the old behaviour
- Learner: a changed letter is learned as a letter rule and as a touch rule only when the two keys are neighbours. A far change (spelling, or the user simply changed their mind) is still learned as a whole word, never as a letter rule. The touch rule is learned only when the touch leaned toward the intended key
- AutoFixEngine.neighbors and AutoFixController.keys(...) take the current map
- ime: KeyNeighborsBuilder turns the Placement into a map (pair keys split in two); KeyboardView has onNeighbors, called whenever the placement is recomputed, and SpaceInputService passes it to autofix
- Tests: NeighborsTest (map, direction, two layouts, learning rules)
- Not compiled or run here (no Gradle in the sandbox): run :feature-autofix:testDebugUnitTest and :ime:compileDebugKotlin
