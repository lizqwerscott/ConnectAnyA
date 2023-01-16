package com.flydog.connectanya.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flydog.connectanya.R
import com.flydog.connectanya.datalayer.data.ClipboardData
import com.flydog.connectanya.datalayer.model.ReturnClipboardData

class ClipboardDataAdapter(private val clipboardDataList: List<ClipboardData>) : RecyclerView.Adapter<ClipboardDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView
        val clipboardData: TextView
        val info: TextView
        init {
            date = view.findViewById(R.id.clipboardDate)
            clipboardData = view.findViewById(R.id.clipboardData)
            info = view.findViewById(R.id.clipboardDeviceInfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.clipboard_data_row, parent, false)
        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.date.text = clipboardDataList[position].clipboardData.date
        holder.clipboardData.text = clipboardDataList[position].clipboardData.data
        val device = clipboardDataList[position].device
        holder.info.text = "${device.name} (${device.type})"
    }

    override fun getItemCount() = clipboardDataList.size
}