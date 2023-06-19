package com.example.tusa_android.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.tusa_android.R
import com.example.tusa_android.image.TusaImageView
import com.example.tusa_android.modal.chat.PersonChatWithUserBottomModalFragment
import com.example.tusa_android.profile.AnyProfile
import com.google.android.material.card.MaterialCardView

class AllChatsRecyclerAdapter(val list: ArrayList<ChatRowModel>, val fragmentActivity: FragmentActivity) : RecyclerView.Adapter<AllChatsRecyclerAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView
        var avatarView: TusaImageView
        var materialCardView: MaterialCardView

        init {
            this.textView = view.findViewById(R.id.titleChatRow)
            this.avatarView = view.findViewById(R.id.chatRowAvatar)
            this.materialCardView = view.findViewById(R.id.chatRowCard)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.sample_chat_row_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.textView.text = item.withUserId
        holder.avatarView.setupImageUseTryUseMemoryCache(AnyProfile.makePathToAvatar(item.withUserId))
        holder.materialCardView.setOnClickListener {
            val modal = PersonChatWithUserBottomModalFragment(item)
            modal.show(fragmentActivity.supportFragmentManager, PersonChatWithUserBottomModalFragment.TAG)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}