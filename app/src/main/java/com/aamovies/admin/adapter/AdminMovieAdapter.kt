package com.aamovies.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.admin.R
import com.aamovies.admin.model.Movie
import com.bumptech.glide.Glide

class AdminMovieAdapter(
    private var movies: List<Movie>,
    private val onDeleteClick: (Movie) -> Unit
) : RecyclerView.Adapter<AdminMovieAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.img_admin_movie_poster)
        val title: TextView = itemView.findViewById(R.id.tv_admin_movie_title)
        val meta: TextView = itemView.findViewById(R.id.tv_admin_movie_meta)
        val btnDelete: TextView = itemView.findViewById(R.id.btn_admin_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.title.text = movie.title
        holder.meta.text = buildString {
            if (movie.year.isNotEmpty()) append(movie.year)
            if (movie.category.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append(movie.category)
            }
            if (movie.language.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append(movie.language)
            }
        }
        if (movie.poster.isNotEmpty()) {
            Glide.with(holder.poster.context)
                .load(movie.poster)
                .centerCrop()
                .placeholder(R.drawable.placeholder_admin)
                .error(R.drawable.placeholder_admin)
                .into(holder.poster)
        } else {
            holder.poster.setImageResource(R.drawable.placeholder_admin)
        }
        holder.btnDelete.setOnClickListener { onDeleteClick(movie) }
    }

    override fun getItemCount() = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
