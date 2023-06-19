package com.example.tusa_android.chat.messages

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tusa_android.R
import com.google.android.material.card.MaterialCardView

class MessagesChatAdapter(val list: ArrayList<ChatMessageModel>) : RecyclerView.Adapter<MessagesChatAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var messageTextView: TextView
        var messageCardView: MaterialCardView

        init {
            this.messageTextView = view.findViewById(R.id.messageText)
            this.messageCardView = view.findViewById(R.id.messageCardView)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.sample_message_row_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.messageTextView.text = item.message
        if(!item.myMessage) {
            (holder.messageCardView.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}