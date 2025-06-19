package dev.romle.roamnoteapp.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import dev.romle.roamnoteapp.model.ActivityLog
import dev.romle.roamnoteapp.model.DayLog
import dev.romle.roamnoteapp.model.Expense
import dev.romle.roamnoteapp.model.SessionManager
import dev.romle.roamnoteapp.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripsRepository {

    private val authRepo = AuthRepository()
    private val uid = authRepo.getUid()
    private val tripsRef = FirebaseDatabase.getInstance().getReference("users").child(uid!!).child("trips")


    fun addTrip(trip: Trip) {

        val safeTripName = trip.name.replace(".", "_")

        tripsRef.child(safeTripName).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d("TripsRepository", "Trip '${trip.name}' already exists! ")
                }

                else {
                    tripsRef.child(safeTripName).setValue(trip)
                        .addOnSuccessListener {
                            Log.d("TripsRepository", "Trip '${trip.name}' saved!")
                        }
                        .addOnFailureListener {
                            Log.e("TripsRepository", "Failed to save trip", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("TripsRepository", "Failed to check if trip exists", it)
            }
    }

    fun addOrUpdateDailyLog(tripName: String, log: DayLog) {

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(log.date))

        val dayLogsRef = tripsRef.child(tripName).child("dayLogs")
        val tripRef = tripsRef.child(tripName)

        dayLogsRef.child(dateId).setValue(log)
            .addOnSuccessListener {
            dayLogsRef.get().addOnSuccessListener { snapshot ->
                var total = 0.0
                var minDate = Long.MAX_VALUE
                var maxDate = Long.MIN_VALUE

                for (logSnap in snapshot.children) {
                    val dayLog = logSnap.getValue(DayLog::class.java)
                    if (dayLog != null){
                        total += dayLog.logCost
                        if (dayLog.date < minDate) minDate = dayLog.date
                        if (dayLog.date > maxDate) maxDate = dayLog.date
                    }
                }
                val updates = mapOf(
                    "cost" to total,
                    "startDate" to minDate,
                    "lastDate" to maxDate
                )

                tripRef.updateChildren(updates)
            }
        }
    }

    fun addActivity(tripName: String, logDate: Long, activity: ActivityLog) {

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))
        val activityId = UUID.randomUUID().toString()

        val activitiesRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)
            .child("activities")


        activitiesRef.child(activityId).setValue(activity)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Activity added to $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to add activity", it)
            }

    }

    fun deleteActivity(tripName: String, logDate: Long, activityId: String) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val activityRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)
            .child("activities")
            .child(activityId)

        activityRef.removeValue()
            .addOnSuccessListener {
                Log.d("TripsRepo", "Activity $activityId deleted from $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", " Failed to delete activity", it)
            }
    }

    fun addExpense(tripName: String, logDate: Long,expense: Expense){
        val dateId = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date(logDate))
        val expenseId = UUID.randomUUID().toString()

        val expenseRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)
            .child("expenses")

        expenseRef.child(expenseId).setValue(expense)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Expense added to $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to add expense", it)
            }
    }

    fun deleteExpense(tripName: String, logDate: Long, expenseId: String) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val expenseRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)
            .child("expenses")
            .child(expenseId)

        expenseRef.removeValue()
            .addOnSuccessListener {
                Log.d("TripsRepo", "Expense $expenseId deleted from $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to delete expense", it)
            }
    }

    fun loadTrips(callback: (List<Trip>) -> Unit){
        tripsRef.get()
            .addOnSuccessListener { snapshot ->
                val trips = mutableListOf<Trip>()
                for (tripSnap in snapshot.children) {
                    val trip = tripSnap.getValue(Trip::class.java)
                    if (trip != null) {
                        trips.add(trip)
                    }
                }
                SessionManager.currentUser = SessionManager.currentUser?.copy(trips = trips.toMutableList())

                callback(trips)
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepository", "Failed to load trips", error)
                callback(emptyList()) // or handle error differently
            }
    }
}

