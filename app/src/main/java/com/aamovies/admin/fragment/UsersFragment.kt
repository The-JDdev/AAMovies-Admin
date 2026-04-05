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

class UsersFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvUserCount = view.findViewById<TextView>(R.id.tv_user_count)
        val tvUsersList = view.findViewById<TextView>(R.id.tv_users_list)

        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val count = snapshot.childrenCount
                    tvUserCount.text = "Total Users: $count"
                    val sb = StringBuilder()
                    snapshot.children.forEach { child ->
                        val email = child.child("email").getValue(String::class.java) ?: "Anonymous"
                        val name = child.child("displayName").getValue(String::class.java) ?: ""
                        sb.appendLine("• $email${if (name.isNotEmpty()) "  ($name)" else ""}")
                    }
                    tvUsersList.text = if (sb.isEmpty()) "No registered users yet." else sb.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
