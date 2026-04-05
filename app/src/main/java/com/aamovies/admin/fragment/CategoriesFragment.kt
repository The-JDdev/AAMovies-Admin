package com.aamovies.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.admin.R
import com.aamovies.admin.adapter.CategoryAdapter
import com.aamovies.admin.model.Category
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriesFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var tvEmpty: TextView
    private var catAdapter: CategoryAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvCategories = view.findViewById(R.id.rv_categories)
        tvEmpty = view.findViewById(R.id.tv_categories_empty)
        val etCategoryName = view.findViewById<EditText>(R.id.et_category_name)
        val btnAdd = view.findViewById<Button>(R.id.btn_add_category)

        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        catAdapter = CategoryAdapter(emptyList()) { cat -> confirmDeleteCategory(cat) }
        rvCategories.adapter = catAdapter

        btnAdd.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            if (name.isEmpty()) { Toast.makeText(requireContext(), "Enter a category name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val catData = mapOf("name" to name, "movieCount" to 0, "createdAt" to System.currentTimeMillis())
            FirebaseDatabase.getInstance().getReference("categories").push().setValue(catData)
                .addOnSuccessListener { etCategoryName.setText(""); Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { e -> Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
        }

        FirebaseDatabase.getInstance().getReference("categories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val cats = mutableListOf<Category>()
                    snapshot.children.forEach { child ->
                        val c = child.getValue(Category::class.java) ?: return@forEach
                        c.id = child.key ?: return@forEach
                        cats.add(c)
                    }
                    catAdapter?.updateCategories(cats)
                    tvEmpty.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun confirmDeleteCategory(cat: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Delete \"${cat.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseDatabase.getInstance().getReference("categories/${cat.id}").removeValue()
                    .addOnSuccessListener { Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null).show()
    }
}
