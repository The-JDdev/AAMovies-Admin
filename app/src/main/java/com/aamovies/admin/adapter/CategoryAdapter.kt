package com.aamovies.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.admin.R
import com.aamovies.admin.model.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_category_name)
        val count: TextView = itemView.findViewById(R.id.tv_category_count)
        val btnDelete: TextView = itemView.findViewById(R.id.btn_category_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = categories[position]
        holder.name.text = cat.name
        holder.count.text = "${cat.movieCount} movies"
        holder.btnDelete.setOnClickListener { onDeleteClick(cat) }
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCats: List<Category>) {
        categories = newCats
        notifyDataSetChanged()
    }
}
