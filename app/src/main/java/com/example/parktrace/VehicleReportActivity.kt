package com.example.parktrace

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parktrace.adapter.VehicleAdapter
import com.example.parktrace.model.VehicleEntity
import com.example.parktrace.viewmodel.FirebaseVehicleViewModel
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.File
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.OutputStreamWriter

class VehicleReportActivity : AppCompatActivity() {
    private lateinit var viewModel: FirebaseVehicleViewModel
    private lateinit var adapter: VehicleAdapter
    private val vehicleList= ArrayList<VehicleEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vehicle_report)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel= ViewModelProvider(this)[FirebaseVehicleViewModel::class.java]
        val etMake=findViewById<AutoCompleteTextView>(R.id.etMake)
        val etModel=findViewById<AutoCompleteTextView>(R.id.etModel)
        val btnFilter=findViewById<Button>(R.id.btnApplyFilter)
        val recyclerView=findViewById<RecyclerView>(R.id.rvVehicles)
        val vtLogin=findViewById<TextView>(R.id.VTLogin)
        vtLogin.setOnClickListener {
            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        // Setup RecyclerView
        adapter = VehicleAdapter(
            this,
            vehicleList,
            onDelete = { position -> /* handle delete */ },
            onEdit = { position -> /* handle edit */ },
            onDownload = { position -> /* handle download */ }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        // 🔹 Load all vehicles initially
        loadAllVehicles()
        // 🔹 Apply filter button
        btnFilter.setOnClickListener {
            applyFilter(etMake.text.toString(), etModel.text.toString())
        }
    }
    private fun loadAllVehicles() {
        viewModel.getAllVehicles { vehicles ->
            vehicleList.clear()
            vehicleList.addAll(vehicles)
            adapter.notifyDataSetChanged()
            // Populate Make and Model dropdown dynamically
            val makes = vehicles.map { it.make }.distinct().sorted()
            val models = vehicles.map { it.model }.distinct().sorted()
            findViewById<AutoCompleteTextView>(R.id.etMake).setAdapter(
                ArrayAdapter(this, android.R.layout.simple_list_item_1, makes)
            )
            findViewById<AutoCompleteTextView>(R.id.etModel).setAdapter(
                ArrayAdapter(this, android.R.layout.simple_list_item_1, models)
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun applyFilter(make: String?, model: String?) {
        viewModel.getAllVehicles { vehicles ->
            val filtered = vehicles.filter { vehicle ->
                (make.isNullOrEmpty() || vehicle.make.equals(make, ignoreCase = true)) &&
                        (model.isNullOrEmpty() || vehicle.model.equals(model, ignoreCase = true))
            }
            vehicleList.clear()
            vehicleList.addAll(filtered)
            adapter.notifyDataSetChanged()
        }
        val spinner = findViewById<Spinner>(R.id.spinnerFormat)
        val formats = listOf("Select format", "PDF", "CSV")
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            formats
        )
        val btnExport = findViewById<Button>(R.id.btnExportVehicle)
        btnExport.setOnClickListener {
            if (vehicleList.isEmpty()) {
                Toast.makeText(this, "No vehicles to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (spinner.selectedItem.toString()) {
                "PDF" -> exportVehiclesToPdf()
                "CSV" -> exportVehiclesToCsv()
                else -> Toast.makeText(this, "Select export format", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportVehiclesToCsv() {

        val fileName = "vehicle_report.csv"

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: return

        resolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write("Owner Name,Mobile No,Make,Year,Model,Number,Type\n")
                vehicleList.forEach {
                    writer.write(
                        "${it.ownerName},${it.mobile},${it.make},${it.year},${it.model},${it.number},${it.type}\n"
                    )
                }
            }
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        Toast.makeText(this, "CSV saved in Downloads", Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportVehiclesToPdf() {

        val fileName = "vehicle_report.pdf"

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: return

        resolver.openOutputStream(uri)?.use { outputStream ->
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            document.add(Paragraph("Vehicle Report").setBold())

            vehicleList.forEach {
                document.add(
                    Paragraph(
                        "${it.ownerName},${it.mobile} | ${it.make} | ${it.year} | ${it.model} | ${it.number} | ${it.type}"
                    )
                )
            }
            document.close()
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show()
    }

}