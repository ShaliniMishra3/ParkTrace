package com.example.parktrace

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parktrace.adapter.RegistrationAdapter
import com.example.parktrace.databinding.ActivityRegistrationReportBinding
import com.example.parktrace.model.RegistrationModel
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument

import com.itextpdf.layout.Document

import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistrationReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationReportBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val adapter = RegistrationAdapter()

    private var fromTimestamp = 0L
    private var fetchedList: List<RegistrationModel> = emptyList()

    private var toTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrationReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecycler()
        setupDatePickers()
        binding.RtoLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnFetch.setOnClickListener {
            if (fromTimestamp == 0L || toTimestamp == 0L) {
                Toast.makeText(this, "Please select date range", Toast.LENGTH_SHORT).show()
            } else {
                fetchRegistrations()
            }
        }
        val formats = listOf("Select format", "PDF", "CSV")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            formats
        )
        binding.spinnerFormat.adapter = spinnerAdapter

    }

    private fun setupRecycler() {
        binding.rvRegistrations.layoutManager =
            LinearLayoutManager(this)
        binding.rvRegistrations.adapter = adapter
    }

    //--------DATE PICKER-------//

    private fun setupDatePickers() {
        binding.etFromDate.setOnClickListener {
            openDatePicker { timestamp, formatted ->
                fromTimestamp = timestamp
                binding.etFromDate.setText(formatted)
            }
        }
        binding.etToDate.setOnClickListener {
            openDatePicker { timestamp, formatted ->
                // Add end-of-day time (23:59:59)
                toTimestamp = timestamp + 86399999
                binding.etToDate.setText(formatted)
            }
        }
    }

    private fun openDatePicker(onDateSelected: (Long, String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, day, 0, 0, 0)
                val timestamp = selectedCal.timeInMillis
                val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(selectedCal.time)

                onDateSelected(timestamp, formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchRegistrations() {
        firestore.collection("users")
            .whereEqualTo("role", "user")
            .orderBy("createdAt")
            .whereGreaterThanOrEqualTo("createdAt", fromTimestamp)
            .whereLessThanOrEqualTo("createdAt", toTimestamp)
            .get()
            .addOnSuccessListener { snapshot ->

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RegistrationModel::class.java)?.apply {
                        id = doc.id
                    }
                }
                fetchedList = list
                adapter.submitList(list)

                Toast.makeText(
                    this,
                    "Records found: ${list.size}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
        binding.btnExport.setOnClickListener {

            if (fetchedList.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (binding.spinnerFormat.selectedItem.toString()) {
                "PDF" -> exportToPdf()
                "CSV" -> exportToCsv()

                else -> Toast.makeText(this, "Select export format", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportToCsv() {

        val fileName = "registration_report.csv"

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
                writer.write("Name,Email,Mobile,Address,Password\n")
                fetchedList.forEach {
                    writer.write(
                        "${it.ownerName},${it.email},${it.mobile},${it.address},${it.password}\n"
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
    private fun exportToPdf() {

        val fileName = "registration_report.pdf"

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

            document.add(Paragraph("Registration Report").setBold())

            fetchedList.forEach {
                document.add(
                    Paragraph(
                        "${it.ownerName} | ${it.email} | ${it.mobile} | ${it.address}"
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

