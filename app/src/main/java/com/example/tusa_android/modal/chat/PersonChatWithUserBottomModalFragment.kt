package com.example.tusa_android.modal.chat

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tusa_android.PaginateTusaMessagesRequest
import com.example.tusa_android.R
import com.example.tusa_android.SendTusaMessageRequest
import com.example.tusa_android.chat.ChatRowModel
import com.example.tusa_android.chat.messages.ChatMessageModel
import com.example.tusa_android.chat.messages.MessagesChatAdapter
import com.example.tusa_android.my_profile.MyProfile
import com.example.tusa_android.network.Authentication
import com.example.tusa_android.network.Grpc
import com.example.tusa_android.screen.ScreenMetricsUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.google.protobuf.ByteString
import kotlinx.coroutines.launch

class PersonChatWithUserBottomModalFragment(private val chatRowModel: ChatRowModel) : BottomSheetDialogFragment() {
    private val itemsPerPage: Int = 10
    private var page: Int = 0
    private var pagingState: ByteString = ByteString.fromHex("")
    private var list: ArrayList<ChatMessageModel> = arrayListOf()
    private lateinit var adapter: MessagesChatAdapter
    private lateinit var recycleView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_person_chat_with_user_bottom_modal,
            container,
            false
        )

        val frameLayout = view.findViewById<FrameLayout>(R.id.chatWithUserFrameLayout)
        ScreenMetricsUtils.rememberMetrics(requireActivity())
        val height = ScreenMetricsUtils.getScreenHeigth()
        frameLayout.layoutParams.height = height

        dialog!!.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.behavior.skipCollapsed = true
            bottomSheetDialog.behavior.isDraggable = false
        }

        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        recycleView = view.findViewById<RecyclerView>(R.id.recycleViewMessages)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.stackFromEnd = true
        recycleView.layoutManager = linearLayoutManager

        val messageTextInputLayout = view.findViewById<TextInputLayout>(R.id.messageInputTextLayout)
        val messageEditText = messageTextInputLayout.editText!!
        messageTextInputLayout.setEndIconOnClickListener {
            val text = messageEditText.text
            if(text.isNullOrEmpty()) {
                return@setEndIconOnClickListener
            }

            val messageText = text.toString()

            // add sent message
            addToList(arrayListOf(
                ChatMessageModel(messageText, true)
            ))
            text.clear()

            lifecycleScope.launch {
                val request = SendTusaMessageRequest.newBuilder()
                    .setChatId(chatRowModel.chatId)
                    .setToUserId(chatRowModel.withUserId)
                    .setMessage(messageText)
                    .build()
                val reply = Grpc.getInstance().tusaCommunicationsBlockingStub.sendTusaMessage(request)
            }
        }

        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val scroll = v as ScrollView
            if (scrollY == scroll.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                loadData()
            }
        }
        loadData()

        return view
    }

    private fun addToList(messages: List<ChatMessageModel>) {
        list.addAll(messages)

        adapter = MessagesChatAdapter(list)
        recycleView.adapter = adapter
    }

    private fun loadData() {
        println("Load data. Page = $page")
        lifecycleScope.launch {
            try {
                val request = PaginateTusaMessagesRequest.newBuilder()
                    .setChatId(chatRowModel.chatId)
                    .setLimit(itemsPerPage)
                    .setPagingState(pagingState)
                    .build()
                val reply = Grpc.getInstance().tusaCommunicationsBlockingStub.paginateTusaMessages(request)
                val loadedItems = reply.messagesList
                pagingState = reply.pagingState
                val castedItems = loadedItems.map {
                    return@map ChatMessageModel(
                        it.content,
                        it.fromUserId == MyProfile.instance().username
                    )
                }
                println("loaded count = ${castedItems.size}")
                addToList(castedItems)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    companion object {
        var TAG: String = "PersonChatWithUserBottomModalFragment"
    }
}