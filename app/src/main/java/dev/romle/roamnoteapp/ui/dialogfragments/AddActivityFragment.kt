package dev.romle.roamnoteapp.ui.dialogfragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import dev.romle.roamnoteapp.LocationRequestManager
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentAddActivityBinding
import dev.romle.roamnoteapp.model.ActivityLog
import dev.romle.roamnoteapp.model.ActivityTag
import dev.romle.roamnoteapp.model.ExpenseTag
import dev.romle.roamnoteapp.model.Location


class AddActivityFragment : DialogFragment() {

    private var _binding: FragmentAddActivityBinding? = null

    private val binding get() = _binding!!

    private lateinit var locationManager: LocationRequestManager

    private var activityLat: Double = 0.0
    private var activityLon: Double = 0.0

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            locationManager.handlePermissionsResult(permissions)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
                _binding = FragmentAddActivityBinding.inflate(layoutInflater)
        val root: View = binding.root

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroup = binding.addActivityTagChipGroup

        ActivityTag.entries.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag.name.lowercase().replaceFirstChar { it.uppercase() }
                isCheckable = true
                isClickable = true
                this.tag = tag
            }
            chipGroup.addView(chip)
        }

        locationManager = LocationRequestManager(requireContext(),permissionLauncher,null){
                lat, lon ->
            activityLat = lat
            activityLon = lon
        }

        binding.logIMGLocation.setOnClickListener {
            locationManager.requestLocationPermissions()
        }

        binding.addActivityTXTNote.addTextChangedListener(object : android.text.TextWatcher {

            private var lastValidText: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val words = it.trim().split("\\s+".toRegex()).filter { word -> word.isNotBlank() }

                    if (words.size <= 30) {
                        lastValidText = it.toString()
                    } else {
                        // Revert to last valid text
                        binding.addActivityTXTNote.removeTextChangedListener(this)
                        binding.addActivityTXTNote.setText(lastValidText)
                        binding.addActivityTXTNote.setSelection(lastValidText.length)
                        binding.addActivityTXTNote.addTextChangedListener(this)

                        Toast.makeText(requireContext(), "Limit is 30 words", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        binding.addActivityBTNSave.setOnClickListener {
            val name = binding.addActivityTXTName.text.toString().trim()
            val cost = binding.addActivityTXTCost.text.toString()
            val rating = binding.addActivityRatingBar.rating
            val note = binding.addActivityTXTNote.text.toString().let {
                if (it.isNotBlank()) it else null
            }
            val selectedTags = mutableListOf<ActivityTag>()

            for (i in 0 until binding.addActivityTagChipGroup.childCount) {
                val chip = binding.addActivityTagChipGroup.getChildAt(i) as? Chip
                if (chip != null && chip.isChecked) {
                    val tag = chip.tag as? ActivityTag
                    tag?.let { selectedTags.add(it) }
                }
            }
            Log.d("SelectedTags", selectedTags.toString())

            val activityLocation = Location.Builder()
                .latitude(activityLat)
                .longitude(activityLon)
                .build()

            if (name.isEmpty()) {
                binding.addActivityTXTName.error = "Please expense  name"
                return@setOnClickListener
            }

            if (cost.isEmpty()) {
                binding.addActivityTXTCost.error = "Please enter cost in USD"
                return@setOnClickListener
            }

            val parsedCost = cost.toDoubleOrNull()
            if (parsedCost == null) {
                binding.addActivityTXTCost.error = "Enter a valid number"
                return@setOnClickListener
            }

            if (selectedTags.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one tag", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val newActivity = ActivityLog(
                name = name,
                location = activityLocation,
                cost = cost.toDouble(),
                tags = selectedTags,
                satisfaction = rating.toInt(),
                note = note
            )

            val bundle = Bundle().apply {
                putSerializable("ACTIVITY", newActivity)
            }
            parentFragmentManager.setFragmentResult("ACTIVITY_RESULT", bundle)
            dismiss()
        }
    }

}