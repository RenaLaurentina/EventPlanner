package com.example.evplan.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.evplan.databinding.ItemEventBinding
import com.example.evplan.entity.Event

class EventAdapter(
    private val dataset: MutableList<Event>,
    private val eventItemEvents: EventItemEvents
) : RecyclerView.Adapter<EventAdapter.CustomViewHolder>() {

    interface EventItemEvents {
        fun onEventItemEdit(event: Event)
        fun onEventItemDelete(event: Event)
    }

    inner class CustomViewHolder(
        private val view: ItemEventBinding
    ) : RecyclerView.ViewHolder(view.root) {

        fun bindData(data: Event) {
            view.tvTitle.text = data.title
            view.tvDescription.text = data.description

            view.root.setOnClickListener {
                eventItemEvents.onEventItemEdit(data)
            }

            view.root.setOnLongClickListener {
                eventItemEvents.onEventItemDelete(data)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bindData(dataset[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(newData: List<Event>) {
        dataset.clear()
        dataset.addAll(newData)
        notifyDataSetChanged()
    }
}