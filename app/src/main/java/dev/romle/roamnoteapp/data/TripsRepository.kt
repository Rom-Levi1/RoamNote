package dev.romle.roamnoteapp.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
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
    private val userRef =
        FirebaseDatabase.getInstance().getReference("users").child(uid!!)
    private val usersRef =
        FirebaseDatabase.getInstance().getReference("users")
    private val tripsRef =
        FirebaseDatabase.getInstance().getReference("users").child(uid!!).child("trips")

    fun addUsername(username: String){
        userRef.child("username").setValue(username)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Username saved successfully")
            }
            .addOnFailureListener{ error ->
                Log.e("TripsRepo", "Failed to save username", error)
            }
    }

    fun loadUsername() {
        userRef.child("username").get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java)
                Log.d("TripsRepo", "Username loaded: $username")
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepo", "Failed to load username", error)
            }
    }


    fun isUsernameAvailable(username: String, onResult: (Boolean) -> Unit) {
        usersRef.get()
            .addOnSuccessListener { snapshot ->
                var isTaken = false

                for (userSnap in snapshot.children) {
                    val existingUsername = userSnap.child("username").getValue(String::class.java)
                    if (existingUsername.equals(username, ignoreCase = true)) {
                        isTaken = true
                        break
                    }
                }

                onResult(!isTaken)
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepo", "Username check failed", error)
                onResult(false)
            }
    }

    fun addTrip(trip: Trip) {


        tripsRef.child(trip.id).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d("TripsRepository", "Trip '${trip.name}' already exists! ")
                } else {
                    tripsRef.child(trip.id).setValue(trip)
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

    fun updateTrip(updatedTrip: Trip) {

        val updates = mapOf(
            "name" to updatedTrip.name,
            "photoUrl" to updatedTrip.photoUrl
        )

        tripsRef.child(updatedTrip.id).updateChildren(updates)

    }

    fun deleteTrip(trip: Trip) {

        tripsRef.child(trip.id).removeValue()
            .addOnSuccessListener {
                Log.d("TripsRepository", "Trip '${trip.name}' deleted!")
            }
            .addOnFailureListener {
                Log.e("TripsRepository", "Failed to delete trip '${trip.name}'", it)
            }
    }

    fun addOrUpdateDailyLog(trip: Trip, log: DayLog) {
        val dateId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(log.date))

        val dayLogRef = tripsRef.child(trip.id).child("dayLogs").child(dateId)
        val tripRef = tripsRef.child(trip.id)

        val updates = mapOf(
            "tripName" to log.tripName,
            "date" to log.date,
            "hotel" to log.hotel,
            "logCost" to log.logCost,
            "photoUrl" to log.photoUrl
        )

        dayLogRef.updateChildren(updates)
            .addOnSuccessListener {
                tripsRef.child(trip.id).child("dayLogs").get().addOnSuccessListener { snapshot ->
                    val total = calculateTotalTripCost(snapshot.children)
                    var minDate = Long.MAX_VALUE
                    var maxDate = Long.MIN_VALUE

                    for (logSnap in snapshot.children) {
                        val dayLog = logSnap.getValue(DayLog::class.java)
                        if (dayLog != null) {
                            if (dayLog.date < minDate) minDate = dayLog.date
                            if (dayLog.date > maxDate) maxDate = dayLog.date
                        }
                    }

                    val tripUpdates = mapOf(
                        "cost" to total,
                        "startDate" to minDate,
                        "lastDate" to maxDate
                    )
                    tripRef.updateChildren(tripUpdates)
                }
            }
    }


    fun addActivity(trip: Trip, logDate: Long, activityId: String, activity: ActivityLog) {

        val dateId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(trip.id)
            .child("dayLogs")
            .child(dateId)

        val activitiesRef = dayLogRef.child("activities")


        activitiesRef.child(activityId).setValue(activity)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Activity added to $dateId!")

                dayLogRef.child("logCost").get()
                    .addOnSuccessListener { costSnapshot ->
                        val currentCost = costSnapshot.getValue(Double::class.java) ?: 0.0
                        val newCost = currentCost + activity.cost

                        dayLogRef.child("logCost").setValue(newCost)
                            .addOnSuccessListener {
                                Log.d("TripsRepo", "Updated logCost to $newCost")

                                tripsRef.child(trip.id).child("cost").get()
                                    .addOnSuccessListener { costSnapshot ->
                                        val currentCost =
                                            costSnapshot.getValue(Double::class.java) ?: 0.0
                                        val newCost = currentCost + activity.cost
                                        tripsRef.child(trip.id).child("cost").setValue(newCost)
                                            .addOnSuccessListener {
                                                Log.d("TripsRepo", "Updated trip cost to $newCost")
                                            }
                                            .addOnFailureListener {
                                                Log.e("TripsRepo", "Failed to update trip Cost", it)
                                            }

                                    }
                                    .addOnFailureListener {
                                        Log.e("TripsRepo", "Failed to update logCost", it)
                                    }
                            }
                    }
                    .addOnFailureListener {
                        Log.e("TripsRepo", "Failed to add activity", it)
                    }

            }
    }

    fun deleteActivity(trip: Trip, logDate: Long, activityId: String) {
        val dateId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(trip.id)
            .child("dayLogs")
            .child(dateId)

        val activitiesRef = dayLogRef.child("activities")

        activitiesRef.child(activityId).get()
            .addOnSuccessListener { snapshot ->
                val activity = snapshot.getValue(ActivityLog::class.java)
                activitiesRef.child(activityId).removeValue()
                    .addOnSuccessListener {
                        Log.d("TripsRepo", "Activity $activityId deleted from $dateId!")

                        dayLogRef.child("logCost").get()
                            .addOnSuccessListener { costSnapshot ->
                                val currentCost =
                                    costSnapshot.getValue(Double::class.java) ?: 0.0
                                val newCost = currentCost - (activity?.cost ?: 0.0)

                                dayLogRef.child("logCost").setValue(newCost)
                                    .addOnSuccessListener {
                                        Log.d("TripsRepo", "Updated logCost to $newCost")

                                        tripsRef.child(trip.id).child("cost").get()
                                            .addOnSuccessListener { costSnapshot ->
                                                val currentCost =
                                                    costSnapshot.getValue(Double::class.java)
                                                        ?: 0.0
                                                val newCost =
                                                    currentCost - (activity?.cost ?: 0.0)

                                                tripsRef.child(trip.id).child("cost")
                                                    .setValue(newCost)
                                                    .addOnSuccessListener {
                                                        Log.d(
                                                            "TripsRepo",
                                                            "Updated trip cost to $newCost"
                                                        )
                                                    }
                                                    .addOnFailureListener {
                                                        Log.e(
                                                            "TripsRepo",
                                                            "Failed to update trip Cost",
                                                            it
                                                        )
                                                    }

                                            }
                                    }
                                    .addOnFailureListener {
                                        Log.e("TripsRepo", "Failed to update logCost", it)
                                    }

                            }
                    }
                    .addOnFailureListener {
                        Log.e("TripsRepo", " Failed to delete activity", it)
                    }
            }

    }

    fun addExpense(trip: Trip, logDate: Long, expenseId: String, expense: Expense) {
        val dateId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(logDate))
        val dayLogRef = tripsRef.child(trip.id).child("dayLogs").child(dateId)
        val expenseRef = dayLogRef.child("expenses")

        expenseRef.child(expenseId).setValue(expense)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Expense added to $dateId!")

                // Update day log cost
                dayLogRef.child("logCost").get().addOnSuccessListener { logCostSnap ->
                    val currentLogCost = logCostSnap.getValue(Double::class.java) ?: 0.0
                    val newLogCost = currentLogCost + expense.cost
                    dayLogRef.child("logCost").setValue(newLogCost)
                }

                // Update trip cost
                tripsRef.child(trip.id).child("cost").get().addOnSuccessListener { tripCostSnap ->
                    val currentTripCost = tripCostSnap.getValue(Double::class.java) ?: 0.0
                    val newTripCost = currentTripCost + expense.cost
                    tripsRef.child(trip.id).child("cost").setValue(newTripCost)
                }
            }
    }

    fun deleteExpense(trip: Trip, logDate: Long, expenseId: String) {
        val dateId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(logDate))
        val dayLogRef = tripsRef.child(trip.id).child("dayLogs").child(dateId)
        val expenseRef = dayLogRef.child("expenses")

        expenseRef.child(expenseId).get().addOnSuccessListener { snapshot ->
            val expense = snapshot.getValue(Expense::class.java)
            if (expense == null) return@addOnSuccessListener

            expenseRef.child(expenseId).removeValue().addOnSuccessListener {
                Log.d("TripsRepo", "Expense $expenseId deleted from $dateId!")

                // Update day log cost
                dayLogRef.child("logCost").get().addOnSuccessListener { logCostSnap ->
                    val currentLogCost = logCostSnap.getValue(Double::class.java) ?: 0.0
                    val newLogCost = currentLogCost - expense.cost
                    dayLogRef.child("logCost").setValue(newLogCost)
                }

                // Update trip cost
                tripsRef.child(trip.id).child("cost").get().addOnSuccessListener { tripCostSnap ->
                    val currentTripCost = tripCostSnap.getValue(Double::class.java) ?: 0.0
                    val newTripCost = currentTripCost - expense.cost
                    tripsRef.child(trip.id).child("cost").setValue(newTripCost)
                }
            }
        }
    }

    fun loadTrips(callback: (List<Trip>) -> Unit) {
        tripsRef.get()
            .addOnSuccessListener { snapshot ->
                val trips = mutableListOf<Trip>()
                for (tripSnap in snapshot.children) {
                    val trip = tripSnap.getValue(Trip::class.java)
                    if (trip != null) {
                        trips.add(trip)
                    }
                }
                SessionManager.currentUser =
                    SessionManager.currentUser?.copy(trips = trips.toMutableList())

                callback(trips)
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepository", "Failed to load trips", error)
                callback(emptyList()) // or handle error differently
            }
    }

    fun loadDayLog(trip: Trip, logDate: Long, onLoaded: (DayLog?) -> Unit) {
        val dateId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(trip.id)
            .child("dayLogs")
            .child(dateId)

        dayLogRef.get()
            .addOnSuccessListener { data ->
                val log = data.getValue(DayLog::class.java)
                onLoaded(log)
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to load DayLog for ${trip.name}/$dateId", it)
                onLoaded(null)
            }
    }

    fun calculateTotalTripCost(logSnapshots: Iterable<DataSnapshot>): Double {
        var total = 0.0
        for (logSnap in logSnapshots) {
            val dayLog = logSnap.getValue(DayLog::class.java)
            if (dayLog != null) {
                total += dayLog.logCost
            }
        }
        return total
    }


}


