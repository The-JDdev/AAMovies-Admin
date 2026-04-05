package com.aamovies.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddMovieActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etYear: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etLanguage: EditText
    private lateinit var etQuality: EditText
    private lateinit var etPoster: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnAddLink: Button
    private lateinit var btnSave: Button
    private lateinit var llDownloadLinks: LinearLayout

    private var categories: MutableList<String> = mutableListOf()
    private val categorySpinnerList = mutableListOf("Uncategorized")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_movie)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Movie"
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(0xFF0d0d0d.toInt())
        )

        etTitle = findViewById(R.id.et_movie_title)
        etYear = findViewById(R.id.et_movie_year)
        spinnerCategory = findViewById(R.id.spinner_category)
        etLanguage = findViewById(R.id.et_movie_language)
        etQuality = findViewById(R.id.et_movie_quality)
        etPoster = findViewById(R.id.et_movie_poster)
        etDescription = findViewById(R.id.et_movie_description)
        btnAddLink = findViewById(R.id.btn_add_download_link)
        btnSave = findViewById(R.id.btn_save_movie)
        llDownloadLinks = findViewById(R.id.ll_download_links)

        loadCategories()

        btnAddLink.setOnClickListener { addDownloadLinkRow() }
        btnSave.setOnClickListener { saveMovie() }
    }

    private fun loadCategories() {
        FirebaseDatabase.getInstance().getReference("categories").get()
            .addOnSuccessListener { snapshot ->
                categorySpinnerList.clear()
                categorySpinnerList.add("Uncategorized")
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java)
                    if (!name.isNullOrEmpty()) categorySpinnerList.add(name)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorySpinnerList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }
    }

    private fun addDownloadLinkRow() {
        val inflater = LayoutInflater.from(this)
        val row = inflater.inflate(R.layout.item_download_link_input, llDownloadLinks, false)
        val btnRemove = row.findViewById<Button>(R.id.btn_remove_link)
        btnRemove.setOnClickListener { llDownloadLinks.removeView(row) }
        llDownloadLinks.addView(row)
    }

    private fun saveMovie() {
        val title = etTitle.text.toString().trim()
        val year = etYear.text.toString().trim()
        val category = spinnerCategory.selectedItem?.toString() ?: "Uncategorized"

        if (title.isEmpty()) { Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show(); return }
        if (year.isEmpty()) { Toast.makeText(this, "Year is required", Toast.LENGTH_SHORT).show(); return }

        val downloadLinks = mutableMapOf<String, Map<String, String>>()
        for (i in 0 until llDownloadLinks.childCount) {
            val row = llDownloadLinks.getChildAt(i)
            val label = row.findViewById<EditText>(R.id.et_link_label).text.toString().trim()
            val url = row.findViewById<EditText>(R.id.et_link_url).text.toString().trim()
            val size = row.findViewById<EditText>(R.id.et_link_size).text.toString().trim()
            if (url.isNotEmpty()) {
                downloadLinks["link${i + 1}"] = mapOf("label" to label, "url" to url, "size" to size)
            }
        }

        val movieData = hashMapOf(
            "title" to title,
            "year" to year,
            "category" to category,
            "language" to etLanguage.text.toString().trim(),
            "quality" to etQuality.text.toString().trim(),
            "poster" to etPoster.text.toString().trim(),
            "description" to etDescription.text.toString().trim(),
            "createdAt" to System.currentTimeMillis(),
            "trending" to false,
            "upcoming" to false,
            "featured" to false,
            "downloadLinks" to downloadLinks
        )

        btnSave.isEnabled = false
        FirebaseDatabase.getInstance().getReference("movies").push()
            .setValue(movieData)
            .addOnSuccessListener {
                Toast.makeText(this, "Movie added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }
}
