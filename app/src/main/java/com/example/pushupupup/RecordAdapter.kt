package com.example.pushupupup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 삭제 콜백 인터페이스 정의
interface OnRecordDeleteListener {
    fun onDelete(record: PushupRecord)
}

class RecordAdapter(
    private val records: List<PushupRecord>,
    private val deleteListener: OnRecordDeleteListener
) : RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTimeText: TextView = itemView.findViewById(R.id.dateTimeText)
        private val userInfoText: TextView = itemView.findViewById(R.id.userInfoText)
        private val countText: TextView = itemView.findViewById(R.id.countText)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(record: PushupRecord) {
            dateTimeText.text = record.dateTime
            userInfoText.text = "${record.name} (${record.age}세)"
            countText.text = "푸쉬업 ${record.count}개"
            deleteButton.setOnClickListener {
                deleteListener.onDelete(record)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size
}
