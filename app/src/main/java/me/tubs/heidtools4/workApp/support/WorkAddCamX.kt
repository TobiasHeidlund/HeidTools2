package me.tubs.heidtools4.workApp.support

import android.Manifest
import android.content.pm.PackageManager
import android.view.Surface
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.Executor

class WorkAddCamX(val fragment: Fragment){
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var camera : Camera

    private val cameraExecutor = Executor{
        it.run()
    }
    val imageCapture = ImageCapture.Builder()
        .setTargetRotation(Surface.ROTATION_0)
        .build()
    fun onCreate() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(fragment.requireContext())
    }

    fun onViewCreated(view: PreviewView){

        val requestPermissionLauncher =
            fragment.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }



        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }else -> {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA

            )
        }
        }



        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPhoto(cameraProvider,view)
        }, ContextCompat.getMainExecutor(fragment.requireContext()))

    }

    private fun bindPhoto(cameraProvider: ProcessCameraProvider, view: PreviewView) {

        val preview : Preview = Preview.Builder()
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(view.surfaceProvider)


        val imageAnalysis = null
        camera = cameraProvider.bindToLifecycle(fragment, cameraSelector, imageCapture,
            preview)
    }

    fun takePhoto(path:String,lmbd: (result: ImageCapture.OutputFileResults, path:String) -> Unit){
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(path)).build()
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException)
                {
                    error.printStackTrace()
                    Toast.makeText(fragment.requireContext(),"Picture Failed", Toast.LENGTH_LONG).show()
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    lmbd(outputFileResults,path)
                }
            })

    }


}