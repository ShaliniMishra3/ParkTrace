package com.example.parktrace

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parktrace.adapter.VehicleAdapter
import com.example.parktrace.model.VehicleEntity
import com.example.parktrace.model.VehicleModel
import com.google.android.material.snackbar.Snackbar
import com.example.parktrace.utils.QrUtils
import com.example.parktrace.viewmodel.FirebaseVehicleViewModel
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var qrUtils: QrUtils

    lateinit var adapter: VehicleAdapter

    private val firebaseVehicleList = ArrayList<VehicleEntity>()
    private lateinit var firebaseViewModel: FirebaseVehicleViewModel

    private val addVehicleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data
            val vehicleEntity = VehicleEntity(
                data?.getStringExtra("owner") ?: "",
                data?.getStringExtra("type") ?: "",
                data?.getStringExtra("make") ?: "",
                data?.getStringExtra("model") ?: "",
                data?.getStringExtra("year") ?: "",
                data?.getStringExtra("number") ?: "",
                data?.getStringExtra("mobileNo")?:""

            )
            firebaseViewModel.addVehicle(vehicleEntity)

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
        // 1️⃣ Initialize Firebase ViewModel FIRST
        firebaseViewModel = ViewModelProvider(this)[FirebaseVehicleViewModel::class.java]
        qrUtils = QrUtils(this)
        findViewById<Button>(R.id.btnScanQR).setOnClickListener {
            val intent = Intent(this, QRScannerActivity::class.java)
            qrScanLauncher.launch(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvVehicles)
        adapter = VehicleAdapter(
            this, firebaseVehicleList,
            onDelete = {position ->
                if (position < 0 || position >= firebaseVehicleList.size) return@VehicleAdapter
                val vehicleEntity = firebaseVehicleList[position]
                firebaseViewModel.deleteVehicle(vehicleEntity)
                firebaseVehicleList.removeAt(position)
                adapter.notifyItemRemoved(position)

                Snackbar.make(recyclerView, "Vehicle deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        firebaseViewModel.loadVehicles()
                    }.show()


            },
            onEdit = { position ->
                val entity = firebaseVehicleList[position]
                showEditDialog(entity)
            },
            onDownload = { position ->
                val vehicleEntity = firebaseVehicleList[position]
                qrUtils.generateVehicleQR(vehicleEntity)
            }
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
        firebaseViewModel.vehicles.observe(this) { entityList ->
            firebaseVehicleList.clear()
            firebaseVehicleList.addAll(entityList)
            //vehicleList.clear()
            adapter.notifyDataSetChanged()
        }
        //firebaseViewModel.loadVehicles()
        firebaseViewModel.startListening()
        firebaseViewModel.success.observe(this) {
           // firebaseViewModel.loadVehicles()
        }

    }
    private fun showEditDialog(vehicle: VehicleEntity) {
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

        edtOwner.setText(vehicle.ownerName)
        edtYear.setText(vehicle.year)
        edtNumber.setText(vehicle.number)
        edtMobile.setText(vehicle.mobile)
        edtType.setText(vehicle.type, false)
        edtMake.setText(vehicle.make, false)
        edtModel.setText(vehicle.model, false)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Vehicle")
            .setPositiveButton("Save") { _, _ ->
                val updatedVehicle = VehicleEntity(
                    id = vehicle.id,   // 🔥 VERY IMPORTANT
                    ownerName = edtOwner.text.toString(),
                    type = edtType.text.toString(),
                    make = edtMake.text.toString(),
                    model = edtModel.text.toString(),
                    year = edtYear.text.toString(),
                    number = edtNumber.text.toString(),
                    mobile = edtMobile.text.toString()
                )
                firebaseViewModel.updateVehicle(updatedVehicle)
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

                val pos = viewHolder.adapterPosition
                if (pos == RecyclerView.NO_POSITION || pos >= firebaseVehicleList.size) return

                val vehicleEntity = firebaseVehicleList[pos]

                firebaseViewModel.deleteVehicle(vehicleEntity)

                firebaseVehicleList.removeAt(pos)
                adapter.notifyItemRemoved(pos)

                Snackbar.make(recyclerView, "Vehicle deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                       // firebaseViewModel.loadVehicles()
                    }.show()

            }
        }
        ItemTouchHelper(swipeHelper).attachToRecyclerView(recyclerView)
        firebaseViewModel.success.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Vehicle deleted successfully", Toast.LENGTH_SHORT).show()
                //firebaseViewModel.loadVehicles()  // refresh list
            }
        }
        firebaseViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
        }

    }

}





