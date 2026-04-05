package com.aamovies.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddMovieActivity : AppCompatActivity() {

    private lateinit var rgType: RadioGroup
    private lateinit var rbMovie: RadioButton
    private lateinit var rbSeries: RadioButton
    private lateinit var etTitle: EditText
    private lateinit var etYear: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etLanguage: EditText
    private lateinit var etQuality: EditText
    private lateinit var etPoster: EditText
    private lateinit var etDescription: EditText
    private lateinit var etGenre: EditText
    private lateinit var cbTrending: CheckBox
    private lateinit var cbPinned: CheckBox
    private lateinit var llScreenshots: LinearLayout
    private lateinit var btnAddScreenshot: Button
    private lateinit var llDownloadLinks: LinearLayout
    private lateinit var btnAddLink: Button
    private lateinit var btnSave: Button

    private val categorySpinnerList = mutableListOf("Uncategorized")
    private var editMovieId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_movie)

        editMovieId = intent.getStringExtra("movie_id")
        isEditMode = !editMovieId.isNullOrEmpty()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) "Edit Movie" else "Add Movie"
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(0xFF0d0d0d.toInt())
        )

        rgType = findViewById(R.id.rg_type)
        rbMovie = findViewById(R.id.rb_movie)
        rbSeries = findViewById(R.id.rb_series)
        etTitle = findViewById(R.id.et_movie_title)
        etYear = findViewById(R.id.et_movie_year)
        spinnerCategory = findViewById(R.id.spinner_category)
        etLanguage = findViewById(R.id.et_movie_language)
        etQuality = findViewById(R.id.et_movie_quality)
        etPoster = findViewById(R.id.et_movie_poster)
        etDescription = findViewById(R.id.et_movie_description)
        etGenre = findViewById(R.id.et_movie_genre)
        cbTrending = findViewById(R.id.cb_trending)
        cbPinned = findViewById(R.id.cb_pinned)
        llScreenshots = findViewById(R.id.ll_screenshots)
        btnAddScreenshot = findViewById(R.id.btn_add_screenshot)
        llDownloadLinks = findViewById(R.id.ll_download_links)
        btnAddLink = findViewById(R.id.btn_add_download_link)
        btnSave = findViewById(R.id.btn_save_movie)

        loadCategories()

        btnAddScreenshot.setOnClickListener { addScreenshotRow("") }
        btnAddLink.setOnClickListener { addDownloadLinkRow("", "", "") }
        btnSave.setOnClickListener { saveMovie() }

        if (isEditMode) loadExistingMovie(editMovieId!!)
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

    private fun loadExistingMovie(movieId: String) {
        btnSave.isEnabled = false
        FirebaseDatabase.getInstance().getReference("movies/$movieId").get()
            .addOnSuccessListener { snap ->
                btnSave.isEnabled = true
                if (!snap.exists()) { Toast.makeText(this, "Movie not found", Toast.LENGTH_SHORT).show(); finish(); return@addOnSuccessListener }

                etTitle.setText(snap.child("title").getValue(String::class.java) ?: "")
                etYear.setText(snap.child("year").getValue(String::class.java) ?: "")
                etLanguage.setText(snap.child("language").getValue(String::class.java) ?: "")
                etQuality.setText(snap.child("quality").getValue(String::class.java) ?: "")
                etPoster.setText(snap.child("poster").getValue(String::class.java) ?: "")
                etDescription.setText(snap.child("description").getValue(String::class.java) ?: "")
                etGenre.setText(snap.child("genre").getValue(String::class.java) ?: "")

                val type = snap.child("type").getValue(String::class.java) ?: "Movie"
                if (type == "Series") rbSeries.isChecked = true else rbMovie.isChecked = true

                cbTrending.isChecked = snap.child("trending").getValue(Boolean::class.java) ?: false
                cbPinned.isChecked = snap.child("pinned").getValue(Boolean::class.java) ?: false

                val category = snap.child("category").getValue(String::class.java) ?: "Uncategorized"
                val catIdx = categorySpinnerList.indexOf(category)
                if (catIdx >= 0) spinnerCategory.setSelection(catIdx)

                // Load screenshots
                llScreenshots.removeAllViews()
                snap.child("screenshots").children.forEach { sc ->
                    val url = sc.getValue(String::class.java) ?: ""
                    addScreenshotRow(url)
                }

                // Load download links
                llDownloadLinks.removeAllViews()
                snap.child("downloadLinks").children.forEach { dl ->
                    val label = dl.child("label").getValue(String::class.java) ?: ""
                    val url = dl.child("url").getValue(String::class.java) ?: ""
                    val size = dl.child("size").getValue(String::class.java) ?: ""
                    addDownloadLinkRow(label, url, size)
                }
            }
            .addOnFailureListener { btnSave.isEnabled = true }
    }

    private fun addScreenshotRow(existingUrl: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 8.dpToPx()
            layoutParams = params
        }
        val etUrl = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 48.dpToPx(), 1f)
            background = getDrawable(R.drawable.bg_input_field)
            hint = "Screenshot URL"
            setHintTextColor(getColor(R.color.text_hint))
            setTextColor(getColor(R.color.text_primary))
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            setPadding(12.dpToPx(), 0, 12.dpToPx(), 0)
            setText(existingUrl)
        }
        val btnRemove = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                48.dpToPx()
            ).also { it.marginStart = 8.dpToPx() }
            text = "✕"
            setTextColor(getColor(R.color.error_red))
            background = null
            setOnClickListener { llScreenshots.removeView(row) }
        }
        row.addView(etUrl)
        row.addView(btnRemove)
        llScreenshots.addView(row)
    }

    private fun addDownloadLinkRow(existingLabel: String, existingUrl: String, existingSize: String) {
        val inflater = LayoutInflater.from(this)
        val row = inflater.inflate(R.layout.item_download_link_input, llDownloadLinks, false)
        row.findViewById<EditText>(R.id.et_link_label).setText(existingLabel)
        row.findViewById<EditText>(R.id.et_link_url).setText(existingUrl)
        row.findViewById<EditText>(R.id.et_link_size).setText(existingSize)
        row.findViewById<Button>(R.id.btn_remove_link).setOnClickListener { llDownloadLinks.removeView(row) }
        llDownloadLinks.addView(row)
    }

    private fun saveMovie() {
        val title = etTitle.text.toString().trim()
        val year = etYear.text.toString().trim()
        val category = spinnerCategory.selectedItem?.toString() ?: "Uncategorized"
        val type = if (rbSeries.isChecked) "Series" else "Movie"

        if (title.isEmpty()) { Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show(); return }
        if (year.isEmpty()) { Toast.makeText(this, "Year is required", Toast.LENGTH_SHORT).show(); return }

        // Collect screenshots
        val screenshots = mutableMapOf<String, String>()
        for (i in 0 until llScreenshots.childCount) {
            val row = llScreenshots.getChildAt(i) as? LinearLayout ?: continue
            val url = (row.getChildAt(0) as? EditText)?.text?.toString()?.trim() ?: ""
            if (url.isNotEmpty()) screenshots["ss${i + 1}"] = url
        }

        // Collect download links
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

        val movieData = hashMapOf<String, Any>(
            "title" to title,
            "year" to year,
            "category" to category,
            "type" to type,
            "language" to etLanguage.text.toString().trim(),
            "quality" to etQuality.text.toString().trim(),
            "poster" to etPoster.text.toString().trim(),
            "description" to etDescription.text.toString().trim(),
            "genre" to etGenre.text.toString().trim(),
            "trending" to cbTrending.isChecked,
            "pinned" to cbPinned.isChecked,
            "upcoming" to false,
            "featured" to false,
            "screenshots" to screenshots,
            "downloadLinks" to downloadLinks
        )

        btnSave.isEnabled = false
        val db = FirebaseDatabase.getInstance().getReference("movies")

        if (isEditMode && editMovieId != null) {
            // Preserve original createdAt
            db.child(editMovieId!!).child("createdAt").get().addOnSuccessListener { snap ->
                val createdAt = snap.getValue(Long::class.java) ?: System.currentTimeMillis()
                movieData["createdAt"] = createdAt
                db.child(editMovieId!!).setValue(movieData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Movie updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        } else {
            movieData["createdAt"] = System.currentTimeMillis()
            db.push().setValue(movieData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Movie added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSave.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }
}
