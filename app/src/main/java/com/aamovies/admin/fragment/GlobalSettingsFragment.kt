package com.aamovies.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aamovies.admin.R
import com.aamovies.admin.model.GlobalSettings
import com.google.firebase.database.FirebaseDatabase

class GlobalSettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_global_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTelegram = view.findViewById<EditText>(R.id.et_telegram_link)
        val etFacebook = view.findViewById<EditText>(R.id.et_facebook_link)
        val etFcmKey = view.findViewById<EditText>(R.id.et_fcm_server_key)
        val btnSave = view.findViewById<Button>(R.id.btn_save_settings)
        val tvStatus = view.findViewById<TextView>(R.id.tv_settings_status)

        val ref = FirebaseDatabase.getInstance().getReference("settings/global")

        btnSave.isEnabled = false
        ref.get().addOnSuccessListener { snap ->
            if (!isAdded) return@addOnSuccessListener
            btnSave.isEnabled = true
            val settings = snap.getValue(GlobalSettings::class.java) ?: GlobalSettings()
            etTelegram.setText(settings.telegramLink)
            etFacebook.setText(settings.facebookLink)
            etFcmKey.setText(settings.fcmServerKey)
        }.addOnFailureListener {
            btnSave.isEnabled = true
        }

        btnSave.setOnClickListener {
            val settings = GlobalSettings(
                telegramLink = etTelegram.text.toString().trim(),
                facebookLink = etFacebook.text.toString().trim(),
                fcmServerKey = etFcmKey.text.toString().trim()
            )
            btnSave.isEnabled = false
            ref.setValue(settings)
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    btnSave.isEnabled = true
                    tvStatus.text = "✓ Settings saved successfully"
                    tvStatus.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    if (!isAdded) return@addOnFailureListener
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
