package com.spacekeyboard.keyboard.ime.theme.backdrop

import com.spacekeyboard.keyboard.core.theme.BackdropKind

object BackdropFactory {
    fun create(kind: BackdropKind, density: Float): BackdropLayer = LayerStack(layersFor(kind, density))

    private fun stars(density: Float, spec: StarSpec) = StarLayer(spec, density, SEED)

    private fun blobs(roles: List<ColorRole>, radius: Float, alpha: Float, speed: Float) =
        BlobLayer(BlobSpec(roles, radius, alpha, speed), SEED)

    private fun layersFor(kind: BackdropKind, d: Float): List<BackdropLayer> = when (kind) {
        BackdropKind.NEBULA -> listOf(
            blobs(listOf(ColorRole.GLOW, ColorRole.BORDER, ColorRole.MIX), 0.75f, 0.38f, 0.12f),
            stars(d, StarSpec(60, 0.5f, 1.3f, StarMotion.TWINKLE, 0.3f, 0.9f)),
        )
        BackdropKind.GALAXY -> listOf(
            blobs(listOf(ColorRole.GLOW), 0.9f, 0.3f, 0.03f),
            stars(d, StarSpec(90, 0.5f, 1.4f, StarMotion.ORBIT, 0.35f, 0.9f)),
        )
        BackdropKind.BLACK_HOLE -> listOf(
            stars(d, StarSpec(40, 0.5f, 1.2f, StarMotion.DRIFT, 0.1f, 0.8f, dx = -0.02f)),
            DiscLayer(DiscStyle.BLACK_HOLE, d),
        )
        BackdropKind.AURORA -> listOf(
            stars(d, StarSpec(35, 0.5f, 1.2f, StarMotion.TWINKLE, 0.2f, 0.8f)),
            BandLayer(),
        )
        BackdropKind.ECLIPSE -> listOf(
            stars(d, StarSpec(30, 0.5f, 1.2f, StarMotion.TWINKLE, 0.2f, 0.8f)),
            DiscLayer(DiscStyle.ECLIPSE, d),
        )
        BackdropKind.NEON_GRID -> listOf(GridLayer(d))
        BackdropKind.FOG -> listOf(
            blobs(listOf(ColorRole.GLOW, ColorRole.BORDER, ColorRole.GLOW, ColorRole.MIX), 0.6f, 0.22f, 0.08f),
        )
        BackdropKind.BLOOD_MOON -> listOf(
            stars(d, StarSpec(45, 0.5f, 1.2f, StarMotion.TWINKLE, 0.15f, 0.8f)),
            DiscLayer(DiscStyle.MOON, d),
        )
        BackdropKind.ICE -> listOf(
            blobs(listOf(ColorRole.GLOW), 0.8f, 0.2f, 0.04f),
            stars(d, StarSpec(50, 0.8f, 1.8f, StarMotion.DRIFT, 0f, 0.85f, dy = 0.06f)),
        )
        BackdropKind.GOLD_DUST -> listOf(
            blobs(listOf(ColorRole.GLOW, ColorRole.BORDER), 0.6f, 0.25f, 0.06f),
            stars(d, StarSpec(55, 0.6f, 1.5f, StarMotion.DRIFT, 1f, 0.9f, dy = -0.03f)),
        )
        BackdropKind.NONE -> emptyList()
    }

    private const val SEED = 7
}
