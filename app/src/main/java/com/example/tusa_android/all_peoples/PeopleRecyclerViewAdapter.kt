package com.example.tusa_android.all_peoples

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tusa_android.R


class PeopleRecyclerViewAdapter : RecyclerView.Adapter<PeopleRecyclerViewAdapter.ViewHolder> {

    private var mData: List<String>
    private var mInflater: LayoutInflater

    constructor(context: Context, data: List<String>) {
        this.mInflater = LayoutInflater.from(context)
        this.mData = data
    }

    public class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
       lateinit var textView: TextView

        override fun onClick(view: View?) {
        }

        init {
            textView = itemView.findViewById(R.id.peoplesButton)
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(
            R.layout.sample_people_row_view,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = mData!![position]
        holder.textView.text = animal
    }

    override fun getItemCount(): Int {
        return mData!!.size
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}