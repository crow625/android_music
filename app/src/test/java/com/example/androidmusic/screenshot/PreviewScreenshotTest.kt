package com.example.androidmusic.screenshot

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Discovers every `@Preview` in the app and renders it to a committed golden PNG
 * (under `src/test/screenshots`) on the JVM via Robolectric — no emulator.
 *
 *   ./gradlew :app:recordRoborazziDebug   # write/update goldens
 *   ./gradlew :app:verifyRoborazziDebug   # fail on visual regressions
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class PreviewScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    @Test
    fun snapshot() {
        captureRoboImage(filePath = "src/test/screenshots/${preview.fileName()}.png") {
            preview()
        }
    }

    companion object {
        // Previews containing infinite animations (indeterminate progress
        // indicators) never let the Compose clock go idle, so the capture spins
        // for minutes. They remain @Preview for IDE reference but are not goldens.
        private val ANIMATED_PREVIEWS = setOf(
            "LibraryLoadingPreview",
            "SourcesScanningPreview",
        )

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("com.example.androidmusic.preview")
                .getPreviews()
                .filterNot { it.methodName in ANIMATED_PREVIEWS }

        private fun ComposablePreview<AndroidPreviewInfo>.fileName(): String =
            "${declaringClass.substringAfterLast('.')}_$methodName"
    }
}
