package com.example.parktrace

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRScannerActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrscanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        previewView = findViewById(R.id.cameraPreview)

        checkPermissionAndStart()
    }

    private fun checkPermissionAndStart() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            finish()
        }
    }



   private fun startCamera() {
       val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

       cameraProviderFuture.addListener({
           val cameraProvider = cameraProviderFuture.get()

           val preview = Preview.Builder().build().also {
               it.setSurfaceProvider(previewView.surfaceProvider)
           }

           val scanner = BarcodeScanning.getClient()

           val analysis = ImageAnalysis.Builder()
               .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
               .build()

           analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
               val mediaImage = imageProxy.image
               if (mediaImage != null) {
                   val image = InputImage.fromMediaImage(
                       mediaImage,
                       imageProxy.imageInfo.rotationDegrees
                   )

                   scanner.process(image)
                       .addOnSuccessListener { barcodes ->
                           if (barcodes.isNotEmpty()) {
                               val data = barcodes[0].rawValue ?: ""
                               val intent = Intent()
                               intent.putExtra("qrData", data)
                               setResult(RESULT_OK, intent)
                               finish()
                           }
                       }
                       .addOnCompleteListener {
                           imageProxy.close()
                       }
               } else {
                   imageProxy.close()
               }
           }

           val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
           cameraProvider.unbindAll()
           cameraProvider.bindToLifecycle(
               this, cameraSelector, preview, analysis
           )

       }, ContextCompat.getMainExecutor(this))
   }


}