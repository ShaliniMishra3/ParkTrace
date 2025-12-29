package com.example.parktrace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parktrace.R
import com.example.parktrace.model.RegistrationModel

class RegistrationAdapter :
    ListAdapter<RegistrationModel, RegistrationAdapter.VH>(_root_ide_package_.com.example.parktrace.RegistrationDiffCallback())
{
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val index: TextView = v.findViewById(R.id.tvIndex)
        val name: TextView = v.findViewById(R.id.tvName)
        val email: TextView = v.findViewById(R.id.tvEmail)
        val mobile: TextView = v.findViewById(R.id.tvMobile)
        val address: TextView = v.findViewById(R.id.tvAddress)
        val password: TextView=v.findViewById(R.id.tvPassword)
    }
    override fun onCreateViewHolder(
        p: ViewGroup,
        vType: Int
    )=  VH(
        LayoutInflater.from(p.context)
        .inflate(R.layout.item_registration, p, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = getItem(position)
        // 🔢 Dynamic index (1,2,3...)
        h.index.text = (position + 1).toString()
        h.name.text = "Name: ${item.ownerName}"
        h.email.text = "Email: ${item.email}"
        h.mobile.text = "Mobile: ${item.mobile}"
        h.address.text = "Address: ${item.address}"
        h.password.text = "Password: ${item.password}"
    }
}