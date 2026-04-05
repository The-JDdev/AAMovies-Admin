package com.aamovies.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aamovies.admin.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvTotalMovies = view.findViewById<TextView>(R.id.tv_total_movies)
        val tvTotalUsers = view.findViewById<TextView>(R.id.tv_total_users)
        val tvTotalViews = view.findViewById<TextView>(R.id.tv_total_views)

        val db = FirebaseDatabase.getInstance()

        db.getReference("movies").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                tvTotalMovies.text = snapshot.childrenCount.toString()
                var totalViews = 0L
                snapshot.children.forEach { totalViews += it.child("views").getValue(Long::class.java) ?: 0L }
                tvTotalViews.text = totalViews.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        db.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                tvTotalUsers.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
