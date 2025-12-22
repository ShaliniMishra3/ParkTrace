package com.example.parktrace.utils


import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.parktrace.R

import com.example.parktrace.model.VehicleModel

import android.app.AlertDialog
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class QrUtils (private val context: Context){

    fun generateVehicleQR(vehicle: VehicleModel) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.qr_design, null)
        val imgQR = dialogView.findViewById<ImageView>(R.id.imgQR)
        val title = dialogView.findViewById<TextView>(R.id.tvTitle)
        val scan = dialogView.findViewById<TextView>(R.id.tvScan)
        val footer = dialogView.findViewById<TextView>(R.id.tvFooter)
        val qrData = """
            Owner:${vehicle.ownerName}
            Number:${vehicle.number}
            Type:${vehicle.type}
            Make:${vehicle.make}
            Model:${vehicle.model}
            Year:${vehicle.year}
            MobileNo:${vehicle.mobileNo}
        """.trimIndent()

        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val matrix = writer.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 600, 600)
        val bmp = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565)

        for (x in 0 until 600) {
            for (y in 0 until 600) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        imgQR.setImageBitmap(bmp)

        dialogView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        dialogView.layout(0, 0, dialogView.measuredWidth, dialogView.measuredHeight)

        val finalBitmap = createBitmap(dialogView.measuredWidth, dialogView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        dialogView.draw(canvas)

        saveImageToGallery(finalBitmap)

        AlertDialog.Builder(context)
            .setTitle("Success")
            .setMessage("QR saved to gallery")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val fileName = "VehicleQR_${System.currentTimeMillis()}.png"
        val fos: OutputStream

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ParkTracer")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = resolver.openOutputStream(imageUri!!)!!
        } else {
            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val image = File(imageDir, fileName)
            fos = FileOutputStream(image)
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        Log.i("SAVE_IMAGE", "Image saved to gallery")
    }



}