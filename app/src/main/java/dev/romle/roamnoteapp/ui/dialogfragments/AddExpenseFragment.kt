package dev.romle.roamnoteapp.ui.dialogfragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import dev.romle.roamnoteapp.LocationRequestManager
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentAddExpenseBinding
import dev.romle.roamnoteapp.model.Expense
import dev.romle.roamnoteapp.model.ExpenseTag
import dev.romle.roamnoteapp.model.Hotel
import dev.romle.roamnoteapp.model.Location


class AddExpenseFragment : DialogFragment() {

    private var _binding: FragmentAddExpenseBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddExpenseBinding.inflate(layoutInflater)
        val root: View = binding.root

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

    }

    private fun initViews() {
        val chipGroup = binding.addExpenseTagChipGroup

        ExpenseTag.entries.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag.name.lowercase().replaceFirstChar { it.uppercase() }
                isCheckable = true
                isClickable = true
                this.tag = tag
            }
            chipGroup.addView(chip)
        }

        binding.addExpenseBTNSave.setOnClickListener{
            val name = binding.addExpenseTXTName.text.toString().trim()
            val cost = binding.addExpenseTXTCost.text.toString()
            val selectedTags = mutableListOf<ExpenseTag>()

            for (i in 0 until binding.addExpenseTagChipGroup.childCount) {
                val chip = binding.addExpenseTagChipGroup.getChildAt(i) as? Chip
                if (chip != null && chip.isChecked) {
                    val tag = chip.tag as? ExpenseTag
                    tag?.let { selectedTags.add(it) }
                }
            }

            Log.d("SelectedTags", selectedTags.toString())

            if (name.isEmpty()) {
                binding.addExpenseTXTName.error = "Please expense  name"
                return@setOnClickListener
            }

            if (cost.isEmpty()) {
                binding.addExpenseTXTCost.error = "Please enter cost in USD"
                return@setOnClickListener
            }

            if (selectedTags.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one tag", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                name = name,
                tags = selectedTags,
                cost = cost.toDouble()
            )

            val bundle = Bundle().apply {
                putSerializable("EXPENSE", expense)
            }
            parentFragmentManager.setFragmentResult("EXPENSE_RESULT", bundle)
            dismiss()

        }
    }
}