package com.example.ukol17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukol17.databinding.FragmentWishlistBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var adapter: WishAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())

        setupRecyclerView()
        observeData()

        // FAB - přidat nové přání
        binding.fabAdd.setOnClickListener {
            showAddWishDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = WishAdapter(
            onWishClick = { wish ->
                // Přepnout prioritu
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreManager.togglePriority(wish)
                    loadData()  // Znovu načti
                }
            },
            onWishLongClick = { wish ->
                // Smazat přání
                showDeleteDialog(wish)
            },
            onCheckboxClick = { wish, isChecked ->
                // Přepnout splnění
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreManager.toggleCompleted(wish)
                    loadData()  // Znovu načti
                }
            }
        )

        binding.rvWishes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWishes.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreManager.wishes.collect { wishes ->
                loadData()
            }
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val wishes = dataStoreManager.wishes.first()
            val priority = dataStoreManager.priorityWishes.first()
            val completed = dataStoreManager.completedWishes.first()

            val sorted = wishes.sortedWith(
                compareByDescending<String> { priority.contains(it) }
                    .thenByDescending { !completed.contains(it) }
            )

            adapter.updateData(sorted.toList(), priority, completed)

            // Zobraz/skryj prázdný stav
            if (wishes.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvWishes.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvWishes.visibility = View.VISIBLE
            }
        }
    }

    private fun showAddWishDialog() {
        val input = EditText(requireContext())
        input.hint = "Např. iPhone 15 Pro"
        input.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(requireContext())
            .setTitle("Nové přání")
            .setView(input)
            .setPositiveButton("Přidat") { _, _ ->
                val wish = input.text.toString().trim()
                if (wish.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        dataStoreManager.addWish(wish)
                        Toast.makeText(requireContext(), "Přání přidáno!", Toast.LENGTH_SHORT).show()
                        loadData()  // Znovu načti
                    }
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun showDeleteDialog(wish: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Smazat přání?")
            .setMessage("Opravdu chceš smazat \"$wish\"?")
            .setPositiveButton("Smazat") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreManager.removeWish(wish)
                    Toast.makeText(requireContext(), "Přání smazáno", Toast.LENGTH_SHORT).show()
                    loadData()  // Znovu načti
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}