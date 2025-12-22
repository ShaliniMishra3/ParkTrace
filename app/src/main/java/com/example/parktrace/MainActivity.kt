package com.example.parktrace

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parktrace.adapter.VehicleAdapter
import com.example.parktrace.model.VehicleModel
import com.google.android.material.snackbar.Snackbar
import com.example.parktrace.utils.QrUtils
import com.example.parktrace.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var qrUtils: QrUtils
    val vehicleList = ArrayList<VehicleModel>()
    lateinit var adapter: VehicleAdapter

    private val addVehicleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data
            val vehicle = VehicleModel(
                data?.getStringExtra("owner") ?: "",
                data?.getStringExtra("type") ?: "",
                data?.getStringExtra("make") ?: "",
                data?.getStringExtra("model") ?: "",
                data?.getStringExtra("year") ?: "",
                data?.getStringExtra("number") ?: "",
                data?.getStringExtra("mobileNo")?:""

            )

            viewModel.addVehicle(vehicle)
        }
    }

    private val qrScanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val qrText = result.data?.getStringExtra("qrData") ?: return@registerForActivityResult
                val intent = Intent(this, VehicleDetailActivity::class.java)
                intent.putExtra("qrText", qrText)
                startActivity(intent)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        qrUtils = QrUtils(this)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.filteredVehicles.observe(this) { list ->
            vehicleList.clear()
            vehicleList.addAll(list)
            adapter.notifyDataSetChanged()
        }
        viewModel.loadVehicles()

        findViewById<Button>(R.id.btnScanQR).setOnClickListener {
            val intent = Intent(this, QRScannerActivity::class.java)
            qrScanLauncher.launch(intent)
        }
        viewModel.editVehicleRequest.observe(this) { vehicle ->
            vehicle?.let { showEditDialog(it) }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvVehicles)
        adapter = VehicleAdapter(
            this, vehicleList,
            onDelete = { position ->
                viewModel.deleteVehicle(position)

            },
            onEdit = { position -> viewModel.requestEditVehicle(position)},
            onDownload = {vehicle->
                qrUtils.generateVehicleQR(vehicle)
                AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("QR saved to gallery")
                    .setPositiveButton("OK", null)
                    .show()}
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        enableSwipeToDelete(recyclerView)
        findViewById<Button>(R.id.btnAddVehicle).setOnClickListener {
            val intent = Intent(this, AddVechicle::class.java)
            addVehicleLauncher.launch(intent)
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this, LoginActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun showEditDialog(vehicle: VehicleModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_vehicle, null)
        val edtOwner = dialogView.findViewById<EditText>(R.id.edtOwner)
        val edtYear = dialogView.findViewById<EditText>(R.id.edtYear)
        val edtMobile = dialogView.findViewById<EditText>(R.id.edtMobile)
        val edtNumber = dialogView.findViewById<EditText>(R.id.edtNumber)
        val edtType = dialogView.findViewById<AutoCompleteTextView>(R.id.edtType)
        val edtMake = dialogView.findViewById<AutoCompleteTextView>(R.id.edtMake)
        val edtModel = dialogView.findViewById<AutoCompleteTextView>(R.id.edtModel)

        val typeList = resources.getStringArray(R.array.vehicle_types)
        val makeList = resources.getStringArray(R.array.vehicle_make_list)
        val modelList = resources.getStringArray(R.array.all_vehicle_models)

        edtType.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, typeList))
        edtMake.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, makeList))
        edtModel.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, modelList))

        // Set current values
        edtOwner.setText(vehicle.ownerName)
        edtYear.setText(vehicle.year)
        edtNumber.setText(vehicle.number)
        edtMobile.setText(vehicle.mobileNo)
        edtType.setText(vehicle.type, false)
        edtMake.setText(vehicle.make, false)
        edtModel.setText(vehicle.model, false)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Vehicle")
            .setPositiveButton("Save") { _, _ ->
                val updated = VehicleModel(
                    edtOwner.text.toString(),
                    edtType.text.toString(),
                    edtMake.text.toString(),
                    edtModel.text.toString(),
                    edtYear.text.toString(),
                    edtNumber.text.toString(),
                    edtMobile.text.toString()
                )
                viewModel.updateVehicleData(vehicle, updated) // MVVM update
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun enableSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
             //new one
                val pos = viewHolder.adapterPosition

                // ✅ Tell ViewModel to delete
                viewModel.deleteVehicle(pos)

                Snackbar.make(recyclerView, "Vehicle deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.undoDelete()
                    }.show()

            }
        }
        ItemTouchHelper(swipeHelper).attachToRecyclerView(recyclerView)
    }





}





