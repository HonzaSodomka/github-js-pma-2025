package com.example.fitnesstracker.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.ExerciseData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExerciseFragment : Fragment() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: ExercisesExpandableAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val exercisesMap = HashMap<String, MutableList<ExerciseItem>>()
    private val categoriesList = ArrayList<String>()

    data class ExerciseItem(val name: String, val isCustom: Boolean, val docId: String? = null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expandableListView = view.findViewById(R.id.elvExercises)
        fabAdd = view.findViewById(R.id.fabAddExercise)
        fabAdd.setOnClickListener { showAddExerciseDialog() }
        loadData()
    }

    private fun loadData() {
        exercisesMap.clear()
        categoriesList.clear()

        for ((category, list) in ExerciseData.categories) {
            categoriesList.add(category)
            val items = ArrayList<ExerciseItem>()
            for (ex in list) {
                items.add(ExerciseItem(ex, isCustom = false))
            }
            exercisesMap[category] = items
        }

        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("custom_exercises")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val category = doc.getString("category") ?: "Ostatní"

                    if (!exercisesMap.containsKey(category)) {
                        if (!categoriesList.contains(category)) categoriesList.add(category)
                        exercisesMap.putIfAbsent(category, ArrayList())
                    }
                    exercisesMap[category]?.add(ExerciseItem(name, isCustom = true, docId = doc.id))
                }

                for (cat in exercisesMap.keys) {
                    exercisesMap[cat]?.sortBy { it.name }
                }

                adapter = ExercisesExpandableAdapter(requireContext(), categoriesList, exercisesMap) { item ->
                    deleteCustomExercise(item)
                }
                expandableListView.setAdapter(adapter)
            }
    }

    private fun showAddExerciseDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(context).apply { hint = "Název cviku" }
        layout.addView(etName)

        val tvLabel = TextView(context).apply {
            text = "Kategorie:"
            setPadding(0, 30, 0, 10)
        }
        layout.addView(tvLabel)

        var selectedCategoryIndex = 0
        val categoriesArray = categoriesList.toTypedArray()

        val btnCategory = android.widget.Button(context).apply {
            text = if (categoriesArray.isNotEmpty()) categoriesArray[0] else "Ostatní"
            setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Vyber kategorii")
                    .setItems(categoriesArray) { _, which ->
                        selectedCategoryIndex = which
                        text = categoriesArray[which]
                    }
                    .show()
            }
        }
        layout.addView(btnCategory)

        AlertDialog.Builder(context)
            .setTitle("Nový cvik")
            .setView(layout)
            .setPositiveButton("Uložit") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty() && categoriesArray.isNotEmpty()) {
                    saveCustomExercise(name, categoriesArray[selectedCategoryIndex])
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun saveCustomExercise(name: String, category: String) {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf("name" to name, "category" to category)

        db.collection("users").document(userId).collection("custom_exercises")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "Cvik přidán", Toast.LENGTH_SHORT).show()
                loadData()
            }
    }

    private fun deleteCustomExercise(item: ExerciseItem) {
        if (item.docId == null) return

        AlertDialog.Builder(requireContext())
            .setTitle("Smazat cvik?")
            .setMessage("Opravdu chceš smazat '${item.name}'?")
            .setPositiveButton("Smazat") { _, _ ->
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                db.collection("users").document(userId)
                    .collection("custom_exercises").document(item.docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Smazáno", Toast.LENGTH_SHORT).show()
                        loadData()
                    }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    inner class ExercisesExpandableAdapter(
        private val context: Context,
        private val headers: List<String>,
        private val data: HashMap<String, MutableList<ExerciseItem>>,
        private val onDeleteClick: (ExerciseItem) -> Unit
    ) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = headers.size
        override fun getChildrenCount(groupPosition: Int): Int = data[headers[groupPosition]]?.size ?: 0
        override fun getGroup(groupPosition: Int): Any = headers[groupPosition]
        override fun getChild(groupPosition: Int, childPosition: Int): Any = data[headers[groupPosition]]!![childPosition]
        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
        override fun hasStableIds(): Boolean = false
        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            val textView = (convertView as? TextView) ?: TextView(context)
            textView.apply {
                text = headers[groupPosition]
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1F2937"))
                setPadding(50, 30, 30, 30)
            }
            return textView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            val item = getChild(groupPosition, childPosition) as ExerciseItem

            val layout = (convertView as? LinearLayout) ?: LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            layout.removeAllViews()
            layout.setPadding(60, 20, 30, 20)

            val tvName = TextView(context).apply {
                text = item.name
                textSize = 16f
                setTextColor(if (item.isCustom) Color.parseColor("#4F46E5") else Color.parseColor("#374151"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            layout.addView(tvName)

            if (item.isCustom) {
                val btnDelete = ImageView(context).apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    setColorFilter(Color.parseColor("#EF4444"))
                    setPadding(20, 20, 20, 20)
                    setOnClickListener { onDeleteClick(item) }
                }
                layout.addView(btnDelete)
            }

            return layout
        }
    }
}
