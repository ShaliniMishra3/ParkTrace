package com.example.parktrace

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import com.example.parktrace.model.VehicleModel
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateVehiclePdf(context: Context, vehicle: VehicleModel){
        val pdf= PdfDocument()
        val pageInfo=PdfDocument.PageInfo.Builder(300,600,1).create()
        val page=pdf.startPage(pageInfo)

        val canvas=page.canvas
        val paint= Paint()
        paint.textSize=16f
        canvas.drawText("Vehicle Details",80f,40f,paint)

        paint.textSize=14f
        canvas.drawText("Owner:${vehicle.ownerName}",20f,80f,paint)
        canvas.drawText("Model:${vehicle.model}",20f,110f,paint)
        canvas.drawText("Make:${vehicle.make}",20f,140f,paint)
        canvas.drawText("Year:${vehicle.year}",20f,170f,paint)
        canvas.drawText("Number:${vehicle.number}",20f,200f,paint)

        pdf.finishPage(page)

        val file= File(context.getExternalFilesDir(null), "${vehicle.number}.pdf")
        pdf.writeTo(FileOutputStream(file))
        pdf.close()

        Toast.makeText(context, "PDF Saved:${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }
}