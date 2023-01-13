package com.flydog.connectanya.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flydog.connectanya.R

class ClipboardDataAdapter(private val clipboardDatas: List<String>) : RecyclerView.Adapter<ClipboardDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val handleDate: TextView
        init {
//            handleDate = view.findViewById(R.id.)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount() = clipboardDatas.size
}