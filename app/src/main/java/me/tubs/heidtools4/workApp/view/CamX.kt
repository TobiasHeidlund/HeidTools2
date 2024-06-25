package me.heid.heidtools.work.support

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
    private val analysisExecutor = Executor{
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

    @OptIn(ExperimentalGetImage::class)
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

        val imageAnalysis = imageAnalysis()


        if(!(cameraProvider.isBound(imageCapture) && cameraProvider.isBound(preview))){
            try{
                camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, imageAnalysis ,preview)
                //camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture ,preview)
            }catch (e:IllegalArgumentException){
                Log.e("TAG", "bindPhoto: ", e)
                Toast.makeText(view.context,"ERROR WITH CAMERA",Toast.LENGTH_LONG).show()
            }

        }


    }
    var lastTime = System.currentTimeMillis()
    var lastText: Text? = null;
    var lastImageWidth = 1
    var lastImageHeight = 1
    @ExperimentalGetImage
    private fun imageAnalysis():  ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()


        imageAnalysis.setAnalyzer(analysisExecutor, ImageAnalysis.Analyzer { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            // insert your code here.
            val time = System.currentTimeMillis();

            if(imageProxy.image!= null && lastTime+300< time){
               // Log.d("CamX", "imageAnalysis: TRYING HARD")
                lastTime = time
                CheckImage(imageProxy, time= System.currentTimeMillis(), onSuccessListener ={
                    lastText = it
                    lastImageHeight =imageProxy.height
                    lastImageWidth =imageProxy.width
                    imageProxy.close()
                })
            }else{
                imageProxy.close()
            }
            // after done, release the ImageProxy object


        })

        return imageAnalysis
    }

    @ExperimentalGetImage
    fun CheckImage(
        it: ImageProxy,
        time: Long,
        onSuccessListener: OnSuccessListener<Text> = OnSuccessListener { Text ->
            Log.d("CamX", "imageAnalysis: ${System.currentTimeMillis()-time}")
            it.close()
        },
        onFailureListener: OnFailureListener = OnFailureListener { ex ->
            Log.e("CamX", "CheckImage: ",ex )
            it.close()
        }
    ) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val impImage = InputImage.fromMediaImage(it.image!!,it.imageInfo.rotationDegrees)

        recognizer.process(impImage)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener ( onFailureListener)

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

    override fun onDestroy(owner: LifecycleOwner) {
        Log.v("CamX", "onDestroy: ")
        release()
        super.onDestroy(owner)
    }

    @SuppressLint("RestrictedApi")
    fun getClosestText(meteringPoint: MeteringPoint) {
        Log.d("CamX", "CamPreview: Clicked")
        
        lastText?.textBlocks?.forEach{
            Log.d("CamX", "getClosestText: ${it.text},${it.boundingBox?.top},${it.boundingBox?.width()},${it.boundingBox?.height()}")

        }
        Log.d("CamX", "getClosestText: ${meteringPoint.x},${meteringPoint.y},$meteringPoint.,$lastImageHeight")
        
        
        /*lastText?.textBlocks?.forEach{
            if(it.boundingBox?.contains((meteringPoint.x*lastImageWidth).toInt(),(meteringPoint.y*lastImageHeight).toInt()) == true){
                Log.d("CamX", "getClosestText: ${it.text}")
            }
        }*/
    }

}