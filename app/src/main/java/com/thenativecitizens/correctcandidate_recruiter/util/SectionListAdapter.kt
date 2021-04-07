package com.thenativecitizens.correctcandidate_recruiter.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thenativecitizens.correctcandidate_recruiter.databinding.ListSectionViewBinding

class SectionListAdapter(private val listener: SectionListListener): RecyclerView.Adapter<SectionListAdapter.ViewHolder>(){

    var data = listOf<String>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val ctx = holder.itemView.context
        val section = data[position]
        holder.bind(section, listener, position)
    }


    class ViewHolder private constructor(private val binding: ListSectionViewBinding)
        : RecyclerView.ViewHolder(binding.root){

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListSectionViewBinding.inflate(inflater, parent, false)

                return ViewHolder(binding)
            }
        }

        fun bind(section: String, listener: SectionListListener, position: Int) {
            binding.position = position
            binding.section = section
            binding.clickListener = listener
            binding.executePendingBindings()
        }
    }
}


//OnClickListener for the RecyclerView
class SectionListListener(val clickListener: (itemPosition: Int) -> Unit){
    fun onClick(itemPosition: Int) = clickListener(itemPosition)
}