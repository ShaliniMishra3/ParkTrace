package com.example.parktrace

import androidx.recyclerview.widget.DiffUtil
import com.example.parktrace.model.RegistrationModel

class RegistrationDiffCallback: DiffUtil.ItemCallback<RegistrationModel>() {
    override fun areItemsTheSame(
        oldItem: RegistrationModel,
        newItem: RegistrationModel
    ): Boolean {
        return oldItem.mobile == newItem.mobile
    }

    override fun areContentsTheSame(
        oldItem: RegistrationModel,
        newItem: RegistrationModel
    ): Boolean {
        return oldItem.id== newItem.id   }
}