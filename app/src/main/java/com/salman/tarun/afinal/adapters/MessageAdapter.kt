package com.salman.tarun.afinal.adapters

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.salman.tarun.afinal.R


class MessageAdapter(private val context: Context, private val messageList: MutableList<String>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageItemLayout: ConstraintLayout = itemView.findViewById(R.id.messageItemLayout)
        // text view of the message
        var textMessage: TextView = itemView.findViewById(R.id.messageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val messageItemRow = inflater.inflate(R.layout.recycler_item_message, parent, false)
        return ViewHolder(messageItemRow)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        val chosenColor = if (position % 2 == 0) R.color.alternatingItemColor else R.color.white
        holder.messageItemLayout.setBackgroundColor(context.resources.getColor(chosenColor, context.theme))
        // sets the text of the text view to the message
        holder.textMessage.text = message
    }

    override fun getItemCount() = messageList.size

    private fun update() {
        notifyDataSetChanged()
    }

    fun addMessage(message: String) {
        messageList.add(message)
        update()
    }

    fun getMessageList(): List<String> {
        return messageList.toList()
    }
}
