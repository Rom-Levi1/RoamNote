package dev.romle.roamnoteapp.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Typeface
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import dev.romle.roamnoteapp.ImageLoader
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.data.MediaManager
import dev.romle.roamnoteapp.data.TripsRepository
import dev.romle.roamnoteapp.databinding.FragmentDailyLogBinding
import dev.romle.roamnoteapp.model.ActivityLog
import dev.romle.roamnoteapp.model.DayLog
import dev.romle.roamnoteapp.model.Expense
import dev.romle.roamnoteapp.model.Hotel
import dev.romle.roamnoteapp.model.SessionManager
import dev.romle.roamnoteapp.model.Trip
import dev.romle.roamnoteapp.ui.dialogfragments.AddActivityFragment
import dev.romle.roamnoteapp.ui.dialogfragments.AddExpenseFragment
import dev.romle.roamnoteapp.ui.dialogfragments.AddHotelFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DailyLogFragment : Fragment() {

    private var _binding: FragmentDailyLogBinding? = null


    private val binding get() = _binding!!

    private var selectedTrip: Trip? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private val dayLogBuilder = DayLog.Builder()
    private val repo = TripsRepository()

    private var selectedImageUri: Uri? = null
    private val imagePicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri

            binding.logIMGPhoto.setImageResource(R.drawable.check_24px)
            binding.logTXTPhoto.text = "Change photo"

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentDailyLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {



        handleSpinner()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = Date(selectedDate)
        binding.dateSelectorDate.text = dateFormat.format(today)

        binding.logIMGCalendar.setOnClickListener {
            hotelDateHandle { day, month, year ->
                val displayDate  = "$day/$month/$year"
                binding.dateSelectorDate.text = displayDate

                val calendar = Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }

                selectedDate = calendar.timeInMillis
                dayLogBuilder.date(calendar.time)

                selectedTrip?.let {
                    loadDayLogIfExists(it, selectedDate)
                }
            }
        }

        hotelDialogHandle { hotel ->
            binding.hotelSelectorName.text = hotel.name
            dayLogBuilder.hotel(hotel)

        }

        expenseDialogHandle { expense ->
            val expenseId = UUID.randomUUID().toString()
            val expenseWithId = expense.copy(id = expenseId)

            dayLogBuilder.addExpense(expenseId, expenseWithId)
            repo.addExpense(selectedTrip!!, selectedDate, expenseId, expenseWithId)
            addExpenseRow(selectedTrip!!, selectedDate,expenseWithId)

            val dateKey = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(selectedDate))
            val updatedLogs = selectedTrip!!.dayLogs.toMutableMap()
            val existingLog = updatedLogs[dateKey] ?: DayLog()

            val updatedLog = existingLog.copy(
                expenses = existingLog.expenses.toMutableMap().apply {
                    put(expense.id, expense)
                }
            )

            updatedLogs[dateKey] = updatedLog
            selectedTrip = selectedTrip!!.copy(dayLogs = updatedLogs)
            SessionManager.updateTrip(selectedTrip!!)
        }

        activityDialogHandle { activity ->

            val activityId = UUID.randomUUID().toString()
            val activityWithId = activity.copy(id = activityId)

            dayLogBuilder.addActivity(activityId, activityWithId)
            repo.addActivity(selectedTrip!!, selectedDate, activityId, activityWithId)
            addActivityRow(selectedTrip!!, selectedDate,  activityWithId)

            val dateKey = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(selectedDate))
            val updatedLogs = selectedTrip!!.dayLogs.toMutableMap()
            val existingLog = updatedLogs[dateKey]?.copy() ?: DayLog.Builder().tripName(selectedTrip!!.name).date(Date(selectedDate)).build()

            val newActivity = existingLog.activities.toMutableMap().apply {
                put(activityId, activity)
            }
            val updatedLog = existingLog.copy(activities = newActivity)

            updatedLogs[dateKey] = updatedLog
            selectedTrip = selectedTrip!!.copy(dayLogs = updatedLogs)
            SessionManager.updateTrip(selectedTrip!!)

        }

        binding.logIMGPhoto.setOnClickListener {

            Toast.makeText(requireContext(), "Don't forget to save the log to upload the photo!", Toast.LENGTH_SHORT).show()

            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.logBTNSave.setOnClickListener {
            if (selectedTrip == null) {
                Toast.makeText(requireContext(), "Please select a trip", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                binding.logBTNSave.isEnabled = false
                MediaManager().uploadImage(requireContext(), selectedImageUri!!,
                    onSuccess = { imageUrl ->
                        binding.logBTNSave.isEnabled = true
                        dayLogBuilder.photoUrl(imageUrl)
                        saveDayLogToFirebase()
                    },
                    onFailure = {
                        binding.logBTNSave.isEnabled = true
                        Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                saveDayLogToFirebase()
            }
        }
    }

    private fun saveDayLogToFirebase() {
        val finalLog = dayLogBuilder.build()

        val dateKey = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(finalLog.date))
        val updatedLogs = selectedTrip!!.dayLogs.toMutableMap()
        updatedLogs[dateKey] = finalLog

        selectedTrip = selectedTrip!!.copy(dayLogs = updatedLogs)

        SessionManager.updateTrip(selectedTrip!!)

        repo.addOrUpdateDailyLog(selectedTrip!!, finalLog)

        Toast.makeText(requireContext(), "Day log saved successfully", Toast.LENGTH_SHORT).show()
    }


    fun handleSpinner(){
        val trips = SessionManager.currentUser?.trips.orEmpty()

        if (trips.isEmpty()) {
            Toast.makeText(requireContext(), "No trips available", Toast.LENGTH_SHORT).show()
            return
        }

        val tripNames = trips.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tripNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tripSelectorSpinner.adapter = adapter

        binding.tripSelectorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTrip = trips[position]
                dayLogBuilder.reset()
                dayLogBuilder.tripName(selectedTrip!!.name)
                Log.d("TripSpinner", "Selected trip: ${selectedTrip!!.name}")

                loadDayLogIfExists(selectedTrip!!, selectedDate)

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    fun hotelDateHandle(onDatePicked: (day: Int, month: Int, year: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selYear, selMonth, selDay ->
            onDatePicked(selDay, selMonth + 1, selYear)
        }, year, month, day)

        datePicker.show()
    }

    fun hotelDialogHandle(onHotelPicked: (Hotel) -> Unit) {
        parentFragmentManager.setFragmentResultListener("HOTEL_RESULT", viewLifecycleOwner) { _, result ->
            val hotel = result.getSerializable("HOTEL") as Hotel
            Log.d("HotelReceived", hotel.toString())
            onHotelPicked(hotel)
        }

        binding.logAddHotelSelectorText.setOnClickListener {
            val dialog = AddHotelFragment()
            dialog.show(parentFragmentManager, "AddHotelDialog")
        }
    }

    fun expenseDialogHandle(onExpensePicked: (Expense) -> Unit){
        parentFragmentManager.setFragmentResultListener("EXPENSE_RESULT",viewLifecycleOwner){_,result ->
            val expense = result.getSerializable("EXPENSE") as Expense
            Log.d("ExpenseReceived", expense.toString())
            onExpensePicked(expense)
        }

        binding.logExpenseBTNAdd.setOnClickListener{
            val dialog = AddExpenseFragment()
            dialog.show(parentFragmentManager,"AddExpenseDialog")
        }
    }

    fun activityDialogHandle(onActivityPicked: (ActivityLog) -> Unit){
        parentFragmentManager.setFragmentResultListener("ACTIVITY_RESULT",viewLifecycleOwner){_,result ->
            val activity = result.getSerializable("ACTIVITY") as ActivityLog
            Log.d("ExpenseReceived", activity.toString())
            onActivityPicked(activity)
        }

        binding.logTXTActivityAdd.setOnClickListener{
            val dialog = AddActivityFragment()
            dialog.show(parentFragmentManager,"AddActivityDialog")
        }

    }

    private fun loadDayLogIfExists(trip: Trip, dateMillis: Long) {

        resetLocalData()

        repo.loadDayLog(trip, dateMillis) { log ->
            if (log == null) {
                resetLocalData()
                Log.d("DailyLog", "No DayLog found")
                return@loadDayLog
            }

            Log.d("DailyLog", "Loaded DayLog: $log")

            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    val activityPairs = log.activities.map { UUID.randomUUID().toString() to it.value }
                    val expensePairs = log.expenses.map { UUID.randomUUID().toString() to it.value }

                    log.hotel?.let { dayLogBuilder.hotel(it) }
                    log.photoUrl?.let { dayLogBuilder.photoUrl(it) }
                    activityPairs.forEach { (id, act) -> dayLogBuilder.addActivity(id, act) }
                    expensePairs.forEach { (id, exp) -> dayLogBuilder.addExpense(id, exp) }

                    withContext(Dispatchers.Main) {
                        binding.expenseListContainer.removeAllViews()
                        binding.activitiesListContainer.removeAllViews()

                        log.hotel?.let {
                            binding.hotelSelectorName.text = it.name
                        }

                        log.photoUrl?.let {
                            binding.logIMGPhoto.setImageResource(R.drawable.check_24px)
                            binding.logTXTPhoto.text = "Change photo"
                            selectedImageUri = null
                        }

                        activityPairs.forEach { (_, act) -> addActivityRow(trip, dateMillis,act) }
                        expensePairs.forEach { (_, exp) -> addExpenseRow(trip, dateMillis,exp) }
                    }
                }
            }
        }
    }

    private fun addActivityRow(trip: Trip, logDate: Long, activity: ActivityLog) {
        val activityRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val activityText = MaterialTextView(requireContext()).apply {
            text = activity.name
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.log_button_size))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.delete_24px)
            setPadding(16, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                val parent = it.parent as? LinearLayout
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete \"${activity.name}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        dayLogBuilder.removeActivity(activity.id)
                        Log.d("TripsRepo", "Expense ${activity.id} deleted from ${activity.id}!")
                        repo.deleteActivity(trip,logDate,activity.id)
                        binding.activitiesListContainer.removeView(parent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        activityRow.addView(activityText)
        activityRow.addView(deleteIcon)
        binding.activitiesListContainer.addView(activityRow)
    }



    private fun addExpenseRow(trip: Trip, logDate: Long, expense: Expense) {
        val expenseRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val expenseText = MaterialTextView(requireContext()).apply {
            text = "${expense.name}: ${expense.cost}$"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.log_button_size))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.delete_24px)
            setPadding(16, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete \"${expense.name}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        dayLogBuilder.removeExpense(expense.id)
                        repo.deleteExpense(trip,logDate,expense.id)
                        binding.expenseListContainer.removeView(expenseRow)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        expenseRow.addView(expenseText)
        expenseRow.addView(deleteIcon)
        binding.expenseListContainer.addView(expenseRow)
    }

    private fun resetLocalData() {

        val b = _binding ?: return

        dayLogBuilder.reset()
        // Clear UI

        b.hotelSelectorName.text = ""
        b.hotelSelectorName.hint = "Hotel name"
        b.logImageContainer.removeAllViews()
        b.logIMGPhoto.setImageResource(R.drawable.outline_add_a_photo_24)
        b.logTXTPhoto.text = "Add photo"
        b.expenseListContainer.removeAllViews()
        b.activitiesListContainer.removeAllViews()

        selectedImageUri = null

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}