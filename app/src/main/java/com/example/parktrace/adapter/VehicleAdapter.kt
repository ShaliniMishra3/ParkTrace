package com.example.parktrace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parktrace.R
import com.example.parktrace.model.VehicleModel

class VehicleAdapter(
    private val context: Context,
    private val list:ArrayList<VehicleModel>,
    private val onDelete:(Int)-> Unit,
    private val onEdit:(Int)-> Unit,
    private val onDownload:(VehicleModel)-> Unit
    ): RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val owner=itemView.findViewById<TextView>(R.id.txtOwner)
        val model=itemView.findViewById<TextView>(R.id.txtModel)
        val number=itemView.findViewById<TextView>(R.id.txtNumber)
        val type=itemView.findViewById<TextView>(R.id.txtType)
        val make=itemView.findViewById<TextView>(R.id.txtMake)
        val year=itemView.findViewById<TextView>(R.id.txtYear)
        val mobileNo=itemView.findViewById<TextView>(R.id.txtMobile)
        val btnDelete=itemView.findViewById<ImageView>(R.id.btnDelete)
        val btnEdit=itemView.findViewById<ImageView>(R.id.btnEdit)
        val btnDownload=itemView.findViewById<ImageView>(R.id.btnDownload)
    }
        override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v= LayoutInflater.from(context)
            .inflate(R.layout.vehicle_card,parent,false)
            return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: VehicleAdapter.ViewHolder, position: Int) {
        val vehicle=list[position]
        holder.owner.text=vehicle.ownerName
        holder.model.text=vehicle.model
        holder.number.text=vehicle.number
        holder.type.text = vehicle.type
        holder.make.text = vehicle.make
        holder.year.text = vehicle.year
        holder.mobileNo.text=vehicle.mobileNo
        holder.btnDelete.setOnClickListener {
            val dialog=androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Delete Vehicle?")
                .setMessage("Are you sure you want to remove this vehicle?.")
                .setCancelable(true)
                .setPositiveButton("Delete",null)
                .setNegativeButton("CANCEL",null)
                .create()

            dialog.setOnShowListener {
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(context.getColor(android.R.color.black))

                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(context.getColor(android.R.color.darker_gray))

                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener {
                        onDelete(position)
                        dialog.dismiss()
                    }
            }
          dialog.show()

        }
        holder.btnDownload.setOnClickListener {
           // PdfGenerator.generateVehiclePdf(context,vehicle)
            onDownload(vehicle)
        }
        holder.btnEdit.setOnClickListener {
            onEdit(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}