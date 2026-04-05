package com.aamovies.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aamovies.admin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SendNotificationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_send_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etTitle = view.findViewById<EditText>(R.id.et_notif_title)
        val etBody = view.findViewById<EditText>(R.id.et_notif_body)
        val btnSend = view.findViewById<Button>(R.id.btn_send_notification)
        val progressBar = view.findViewById<ProgressBar>(R.id.notif_progress)

        btnSend.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val body = etBody.text.toString().trim()
            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter title and message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnSend.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val notifData = mapOf(
                "title" to title,
                "body" to body,
                "sentAt" to System.currentTimeMillis(),
                "sentBy" to (FirebaseAuth.getInstance().currentUser?.email ?: "admin")
            )

            FirebaseDatabase.getInstance().getReference("notifications").push()
                .setValue(notifData)
                .addOnCompleteListener {
                    if (isAdded) {
                        progressBar.visibility = View.GONE
                        btnSend.isEnabled = true
                        Toast.makeText(requireContext(), "Notification queued! FCM will be dispatched via server.", Toast.LENGTH_LONG).show()
                        etTitle.setText("")
                        etBody.setText("")
                    }
                }
        }
    }
}
