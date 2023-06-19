package com.example.tusa_android.modal.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tusa_android.PaginateTusaPersonalChatsRequest
import com.example.tusa_android.R
import com.example.tusa_android.chat.AllChatsRecyclerAdapter
import com.example.tusa_android.chat.ChatRowModel
import com.example.tusa_android.network.Grpc
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch


class PersonChatsBottomModalFragment : BottomSheetDialogFragment() {
    private val itemsPerPage: Int = 10
    private var page: Int = 0
    private var list: ArrayList<ChatRowModel> = arrayListOf()
    private lateinit var adapter: AllChatsRecyclerAdapter

    private lateinit var recycleView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_person_chats_bottom_modal, container, false)

        val scrollView = view.findViewById<ScrollView>(R.id.chatsScrollView)
        recycleView = view.findViewById<RecyclerView>(R.id.chatsRecycleView)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        recycleView.layoutManager = linearLayoutManager

        scrollView.setOnScrollChangeListener(object: View.OnScrollChangeListener {
            override fun onScrollChange(
                v: View?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                val scroll = v as ScrollView
                if(scrollY == scroll.getChildAt(0).measuredHeight - v.measuredHeight) {
                    page++
                    loadData()
                }
            }

        })

        loadData()
        return view
    }

    private fun loadData() {
        println("Load data. Page = $page")
        lifecycleScope.launch {
            val request = PaginateTusaPersonalChatsRequest.newBuilder()
                .setSkip(itemsPerPage * page)
                .setLimit(itemsPerPage)
                .build()
            val reply = Grpc.getInstance().tusaChatsStub.paginateTusaPersonalChats(request)
            val loadedItems = reply.personalChatsList
            val castedItems = loadedItems.map {
                return@map ChatRowModel(
                    it.id,
                    it.withUserId
                )
            }
            list.addAll(castedItems)

            adapter = AllChatsRecyclerAdapter(list, requireActivity())
            recycleView.adapter = adapter
        }
    }

    companion object {
        val TAG: String = "PersonChatsBottomModalFragment"
    }
}