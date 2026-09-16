package com.mnmyounus.yala.presentation.intruder

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.mnmyounus.yala.data.repository.IntruderStorage
import com.mnmyounus.yala.domain.model.IntruderCapture
import com.mnmyounus.yala.domain.model.UnlockOutcome
import com.mnmyounus.yala.domain.repository.IntruderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Binds the front camera just long enough to take one still photo, then
 * immediately unbinds. No preview is ever shown to the person attempting the
 * unlock. The resulting JPEG is written only to this app's private
 * `filesDir/intruder_captures/` directory - there is no upload path, no
 * MediaStore insertion, and no share/broadcast of the file unless the device
 * owner explicitly opens it from YALA's own gallery screens.
 */
@Singleton
class IntruderCameraCapture @Inject constructor(
    private val intruderRepository: IntruderRepository
) {
    fun captureSilently(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        packageNameAttempted: String,
        outcome: UnlockOutcome,
        scope: CoroutineScope
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture)
            } catch (e: Exception) {
                return@addListener // no front camera / bind failure - fail silently, never crash the unlock flow
            }

            val dir = IntruderStorage.captureDir(context.filesDir)
            val fileName = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date()) + ".jpg"
            val outputFile = File(dir, fileName)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        provider.unbindAll()
                        scope.launch {
                            intruderRepository.saveCapture(
                                IntruderCapture(
                                    filePath = outputFile.absolutePath,
                                    packageNameAttempted = packageNameAttempted,
                                    outcome = outcome,
                                    timestampEpochMillis = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        provider.unbindAll() // fail silently - a missed capture must never block unlocking
                    }
                }
            )
        }, ContextCompat.getMainExecutor(context))
    }
}
