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
    private val tripsRef = FirebaseDatabase.getInstance().getReference("users").child(uid!!).child("trips")
    private val mediaManager = MediaManager()


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

    fun updateTrip(updatedTrip: Trip,oldName: String, oldImage: String?) {
        val safeTripName = updatedTrip.name.replace(".", "_")

        val updates = mapOf(
            "name" to safeTripName,
            "photoUrl" to updatedTrip.photoUrl
        )

        tripsRef.child(oldName).updateChildren(updates)

        if(oldImage != updatedTrip.photoUrl && oldImage != null){
            mediaManager.deleteImage(oldImage)
        }

    }

    fun deleteTrip(tripName: String) {
        val safeTripName = tripName.replace(".", "_")

        tripsRef.child(safeTripName).removeValue()
            .addOnSuccessListener {
                Log.d("TripsRepository", "Trip '$tripName' deleted!")
            }
            .addOnFailureListener {
                Log.e("TripsRepository", "Failed to delete trip '$tripName'", it)
            }
    }

    fun addOrUpdateDailyLog(tripName: String, log: DayLog) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(log.date))

        val dayLogRef = tripsRef.child(tripName).child("dayLogs").child(dateId)
        val tripRef = tripsRef.child(tripName)

        val updates = mapOf(
            "tripName" to log.tripName,
            "date" to log.date,
            "hotel" to log.hotel,
            "logCost" to log.logCost,
            "photoUrl" to log.photoUrl
        )

        dayLogRef.updateChildren(updates)
            .addOnSuccessListener {
                tripsRef.child(tripName).child("dayLogs").get().addOnSuccessListener { snapshot ->
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


    fun addActivity(tripName: String, logDate: Long,activityId: String, activity: ActivityLog) {

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(tripName)
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

                        tripsRef.child(tripName).child("cost").setValue(newCost)
                            .addOnSuccessListener {
                                Log.d("TripsRepo", "Updated trip cost to $newCost")
                            }
                            .addOnFailureListener {
                                Log.e("TripsRepo", "Failed to update trip Cost", it)
                            }

                        dayLogRef.child("logCost").setValue(newCost)
                            .addOnSuccessListener {
                                Log.d("TripsRepo", "Updated logCost to $newCost")
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

    fun deleteActivity(tripName: String, logDate: Long, activityId: String) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)

        val activitiesRef = dayLogRef.child("activities")

        activitiesRef.child(activityId).get()
            .addOnSuccessListener{ snapshot ->
                val activity = snapshot.getValue(ActivityLog::class.java)
                activitiesRef.child(activityId).removeValue()
                    .addOnSuccessListener {
                        Log.d("TripsRepo", "Activity $activityId deleted from $dateId!")

                        dayLogRef.child("logCost").get()
                            .addOnSuccessListener { costSnapshot ->
                                val currentCost = costSnapshot.getValue(Double::class.java) ?: 0.0
                                val newCost = currentCost - (activity?.cost ?: 0.0)

                                tripsRef.child(tripName).child("cost").setValue(newCost)
                                    .addOnSuccessListener {
                                        Log.d("TripsRepo", "Updated trip cost to $newCost")
                                    }
                                    .addOnFailureListener {
                                        Log.e("TripsRepo", "Failed to update trip Cost", it)
                                    }

                                dayLogRef.child("logCost").setValue(newCost)
                                    .addOnSuccessListener {
                                        Log.d("TripsRepo", "Updated logCost to $newCost")
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

    fun addExpense(tripName: String, logDate: Long,expenseId: String,expense: Expense){
        val dateId = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)

        val expenseRef = dayLogRef.child("expenses")

        expenseRef.child(expenseId).setValue(expense)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Expense added to $dateId!")

                dayLogRef.child("logCost").get()
                    .addOnSuccessListener { costSnapshot ->
                        val currentCost = costSnapshot.getValue(Double::class.java) ?: 0.0
                        val newCost = currentCost + expense.cost

                        tripsRef.child(tripName).child("cost").setValue(newCost)
                            .addOnSuccessListener {
                                Log.d("TripsRepo", "Updated trip cost to $newCost")
                            }
                            .addOnFailureListener {
                                Log.e("TripsRepo", "Failed to update trip Cost", it)
                            }

                        dayLogRef.child("logCost").setValue(newCost)
                            .addOnSuccessListener {
                                Log.d("TripsRepo", "Updated logCost to $newCost")
                            }
                            .addOnFailureListener {
                                Log.e("TripsRepo", "Failed to update logCost", it)
                            }
                    }
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to add expense", it)
            }
    }

    fun deleteExpense(tripName: String, logDate: Long, expenseId: String) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val dayLogRef = tripsRef
            .child(tripName)
            .child("dayLogs")
            .child(dateId)

        val expenseRef = dayLogRef.child("expenses")

        expenseRef.child(expenseId).get()
            .addOnSuccessListener { snapshot ->
                val expense = snapshot.getValue(Expense::class.java)

                expenseRef.child(expenseId).removeValue()
                    .addOnSuccessListener {
                        Log.d("TripsRepo", "Expense $expenseId deleted from $dateId!")

                        dayLogRef.child("logCost").get()
                            .addOnSuccessListener { costSnapshot ->
                                val currentCost = costSnapshot.getValue(Double::class.java) ?: 0.0
                                val newCost = currentCost - (expense?.cost ?: 0.0)

                                tripsRef.child(tripName).child("cost").setValue(newCost)
                                    .addOnSuccessListener {
                                        Log.d("TripsRepo", "Updated trip cost to $newCost")
                                    }
                                    .addOnFailureListener {
                                        Log.e("TripsRepo", "Failed to update trip Cost", it)
                                    }

                                dayLogRef.child("logCost").setValue(newCost)
                                    .addOnSuccessListener {
                                        Log.d("TripsRepo", "Updated logCost to $newCost")
                                    }
                                    .addOnFailureListener {
                                        Log.e("TripsRepo", "Failed to update logCost", it)
                                    }
                            }

                    }
                    .addOnFailureListener {
                        Log.e("TripsRepo", "Failed to delete expense", it)
                    }
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

    fun loadDayLog(tripName: String, logDate: Long, onLoaded: (DayLog?) -> Unit) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))
        val safeTripName = tripName.replace(".", "_")

        val dayLogRef = tripsRef
            .child(safeTripName)
            .child("dayLogs")
            .child(dateId)

        dayLogRef.get()
            .addOnSuccessListener { data ->
                val log = data.getValue(DayLog::class.java)
                onLoaded(log)
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to load DayLog for $safeTripName/$dateId", it)
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

