package com.flo.musicplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.flo.musicplayer.databinding.ItemLyricsBinding

class LyricsAdapter(private val list: List<MusicLyrics>, private val callback: (Int) -> Unit) : RecyclerView.Adapter<LyricsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position].lyrics, mPos)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    var mPos = 0

    fun setTextColor(currentPos: Int) {
        mPos = currentPos
        notifyDataSetChanged()
//        notifyItemChanged(currentPos)
    }

    class ViewHolder private constructor(val binding: ItemLyricsBinding, val callback: (Int) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                callback.invoke(adapterPosition)
            }
        }

        fun bind(str: String, pos: Int) {
            binding.itemTvLyrics.text = str

            if (pos == adapterPosition) {
                binding.itemTvLyrics.setTextColor(ContextCompat.getColor(binding.itemTvLyrics.context, R.color.colorWhite))
            }else {
                binding.itemTvLyrics.setTextColor(ContextCompat.getColor(binding.itemTvLyrics.context, R.color.colorGray))
            }
        }

        companion object {
            fun from(parent: ViewGroup, callback: (Int) -> Unit): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLyricsBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(
                    binding, callback
                )
            }
        }
    }
}