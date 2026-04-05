package com.aamovies.admin.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.admin.AddMovieActivity
import com.aamovies.admin.R
import com.aamovies.admin.adapter.AdminMovieAdapter
import com.aamovies.admin.model.Movie
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MoviesFragment : Fragment() {

    private lateinit var rvMovies: RecyclerView
    private lateinit var tvEmpty: TextView
    private var movieAdapter: AdminMovieAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_admin_movies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvMovies = view.findViewById(R.id.rv_admin_movies)
        tvEmpty = view.findViewById(R.id.tv_admin_movies_empty)
        val btnAdd = view.findViewById<Button>(R.id.btn_add_movie)

        rvMovies.layoutManager = LinearLayoutManager(requireContext())
        movieAdapter = AdminMovieAdapter(
            emptyList(),
            onEditClick = { movie -> editMovie(movie) },
            onDeleteClick = { movie -> confirmDelete(movie) }
        )
        rvMovies.adapter = movieAdapter

        btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddMovieActivity::class.java))
        }

        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val movies = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val m = child.getValue(Movie::class.java) ?: return@forEach
                        m.id = child.key ?: return@forEach
                        movies.add(0, m)
                    }
                    // Pinned movies at top
                    val sorted = movies.sortedWith(
                        compareByDescending<Movie> { it.pinned }
                            .thenByDescending { it.trending }
                    )
                    movieAdapter?.updateMovies(sorted)
                    tvEmpty.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun editMovie(movie: Movie) {
        val intent = Intent(requireContext(), AddMovieActivity::class.java).apply {
            putExtra("movie_id", movie.id)
        }
        startActivity(intent)
    }

    private fun confirmDelete(movie: Movie) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Movie")
            .setMessage("Delete \"${movie.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseDatabase.getInstance().getReference("movies/${movie.id}").removeValue()
                    .addOnSuccessListener { Toast.makeText(requireContext(), "Movie deleted", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { e -> Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
