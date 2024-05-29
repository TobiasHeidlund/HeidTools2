package me.heid.heidtools.work.support

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.Executor

class CamX():DefaultLifecycleObserver{
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private var cameraProvider: ProcessCameraProvider? = null;
    private lateinit var cameraSelector: CameraSelector
    private lateinit var camera : Camera
    private var state = 0;

    init {
        Log.e("CamX", "STARTING")
    }

    private val cameraExecutor = Executor{
        it.run()
    }
    val imageCapture = ImageCapture.Builder()
        .setTargetRotation(Surface.ROTATION_0)
        .build()

    fun onViewCreated(view: PreviewView,host:ComponentActivity,lifecycleOwner: LifecycleOwner){
        state.inc()


        cameraProviderFuture = ProcessCameraProvider.getInstance(view.context)

        if (ContextCompat.checkSelfPermission(host.baseContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast(host.baseContext).apply {
                setText("Camera Access is required for the app to function, Please Enter Settings to fix")
                show()
            }
        } else {
            bindCamera(host,view)
        }




    }
    private fun bindCamera( lifecycleOwner: LifecycleOwner, view: PreviewView){
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindPhoto(cameraProvider!!,lifecycleOwner,view)
            Log.e("onViewCreated", "Binding Compleated" )
        }, ContextCompat.getMainExecutor(view.context))
    }
    private fun bindPhoto(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        view: PreviewView,
    ) {

        val preview : Preview = Preview.Builder()
            .build().apply { setSurfaceProvider(view.surfaceProvider) }
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        if(!(cameraProvider.isBound(imageCapture) && cameraProvider.isBound(preview))){
            try{
                camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture,preview)
            }catch (e:IllegalArgumentException){
                Log.e("TAG", "bindPhoto: ", e)
                Toast.makeText(view.context,"ERROR WITH CAMERA",Toast.LENGTH_LONG).show()
            }

        }


    }

    fun takePhoto(context:Context,path:String,lmbd: (result: ImageCapture.OutputFileResults, path:String) -> Unit){


        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(path)).build()
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException)
                {
                    error.printStackTrace()
                    Toast.makeText(context,"Picture Failed", Toast.LENGTH_LONG).show()
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    lmbd(outputFileResults,path)
                }
            })

    }

    fun release() {
        if (cameraProvider != null) cameraProvider!!.unbindAll()
        Log.v("CamX", "release: ")
    }


    override fun onPause(owner: LifecycleOwner) {
        Log.v("CamX", "onPause: ")
        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.v("CamX", "onResume: ")
        super.onResume(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.v("CamX", "onStart: ")
        super.onStart(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.v("CamX", "onStop: ")
        super.onStop(owner)

    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.v("CamX", "onDestroy: ")
        release()
        super.onDestroy(owner)
    }

}