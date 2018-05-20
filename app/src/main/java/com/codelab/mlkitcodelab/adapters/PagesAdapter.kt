package com.codelab.mlkitcodelab.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codelab.mlkitcodelab.R
import kotlinx.android.synthetic.main.adapter_pages.view.*

class PagesAdapter(private val mPagesList: ArrayList<String>) :
        RecyclerView.Adapter<PagesAdapter.MyPageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyPageViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_pages, parent, false))

    override fun getItemCount() = mPagesList.size

    override fun onBindViewHolder(holder: MyPageViewHolder, position: Int) {
        holder.pageText.text = mPagesList[position]
    }

    inner class MyPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pageText = itemView.tvPagesText
    }
}