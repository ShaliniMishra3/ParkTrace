package com.example.parktrace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.parktrace.model.VehicleEntity
import com.example.parktrace.storage.VehicleStorage
import com.example.parktrace.viewmodel.FirebaseVehicleViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddVechicle : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_vechicle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val logoVehicleAdd=findViewById<ImageView>(R.id.logoVehicle)
        logoVehicleAdd.setOnClickListener {
            val intent=Intent(this, MainActivity::class.java)
            Log.d("CHECK_CLICK","Logo was clicked");
            startActivity(intent)
            finish()
        }
        val firebaseViewModel =
            ViewModelProvider(this)[FirebaseVehicleViewModel::class.java]
        //GET ALL INPUT FIELDS
        val inputOwner = findViewById<EditText>(R.id.idInputVehicle)
        val inputYear = findViewById<EditText>(R.id.idInputVehicleModelY)
        val inputNumber = findViewById<EditText>(R.id.idInputVehicleN)
        val inputMobile=findViewById<EditText>(R.id.idInputMobile)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)


        val inputType = findViewById<AutoCompleteTextView>(R.id.idInputVehicleType)

        val typeList = resources.getStringArray(R.array.vehicle_types)
        val adapterType = ArrayAdapter(this, android.R.layout.simple_list_item_1, typeList)
        inputType.setAdapter(adapterType)
        val inputMake = findViewById<AutoCompleteTextView>(R.id.idInputVehicleMake)

        val makeList = resources.getStringArray(R.array.vehicle_make_list)
        val adapterMake = ArrayAdapter(this, android.R.layout.simple_list_item_1, makeList)
        inputMake.setAdapter(adapterMake)
        val inputModel = findViewById<AutoCompleteTextView>(R.id.idInputVehicleModel)

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_LONG).show()
            finish()
        }

        val allModels = resources.getStringArray(R.array.all_vehicle_models)
        val adapterModel = ArrayAdapter(this, android.R.layout.simple_list_item_1, allModels)
        inputModel.setAdapter(adapterModel)
        // ⭐ Change model list based on MAKE selection
        inputMake.setOnItemClickListener { parent, _, position, _ ->
            val selectedMake = parent.getItemAtPosition(position).toString()

            val modelArrayId = when (selectedMake) {
                "Honda" -> R.array.honda_models
                "Hyundai" -> R.array.hyundai_models
                "Toyota" -> R.array.toyota_models
                "Mahindra" -> R.array.mahindra_models
                "Maruti Suzuki" -> R.array.maruti_models
                "Tata" -> R.array.tata_models
                "Kia" -> R.array.kia_models
                "Renault" -> R.array.renault_models
                "Nissan" -> R.array.nissan_models
                "Ford" -> R.array.ford_models
                "Volkswagen" -> R.array.vw_models
                "Skoda" -> R.array.skoda_models
                "BMW" -> R.array.bmw_models
                "Mercedes-Benz" -> R.array.mercedes_models
                "Audi" -> R.array.audi_models
                "MG" -> R.array.mg_models
                "Jeep" -> R.array.jeep_models
                "Volvo" -> R.array.volvo_models
                "Land Rover" -> R.array.landrover_models
                else -> R.array.all_vehicle_models   // fallback
            }
            val modelList = resources.getStringArray(modelArrayId)
            val newModelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, modelList)
            inputModel.setAdapter(newModelAdapter)
        }





        btnSubmit.setOnClickListener {
            val owner = inputOwner.text.toString()
            val type = inputType.text.toString()          // AutoComplete input
            val make = inputMake.text.toString()          // AutoComplete input
            val model = inputModel.text.toString()
            val year = inputYear.text.toString()
            val number = inputNumber.text.toString()
            val mobile = inputMobile.text.toString()
            // Validation
            if (owner.isEmpty() || type.isEmpty() || make.isEmpty() || model.isEmpty() || year.isEmpty() || number.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Owner limit validation
            val existingVehicles = VehicleStorage.loadVehicles(this)
            val ownerCount = existingVehicles.count { it.ownerName.equals(owner, ignoreCase = true) }

            if (ownerCount >= 2) {
                Toast.makeText(this, "Owner already has 2 vehicles registered!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // ✅ CREATE FIREBASE ENTITY
            val vehicleEntity = VehicleEntity(
                ownerName = owner,
                type = type,
                make = make,
                model = model,
                year = year,
                number = number,
                mobile = mobile
            )

            // ✅ ADD TO FIREBASE
            firebaseViewModel.addVehicle(vehicleEntity)
            // Send data
            /*
            val dataIntent = Intent()
            dataIntent.putExtra("owner", owner)
            dataIntent.putExtra("type", type)
            dataIntent.putExtra("make", make)
            dataIntent.putExtra("model", model)
            dataIntent.putExtra("year", year)
            dataIntent.putExtra("number", number)
            dataIntent.putExtra("mobileNo", mobile)
            setResult(RESULT_OK, dataIntent)

            Toast.makeText(this, "Vehicle Added", Toast.LENGTH_SHORT).show()


             */

        }
        firebaseViewModel.success.observe(this) {
            if (it == true) {
                Toast.makeText(this, "Vehicle added to Firebase", Toast.LENGTH_SHORT).show()
                finish() // go back to MainActivity
            }
        }

        firebaseViewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

    }

}