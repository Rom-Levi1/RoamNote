package dev.romle.roamnoteapp.model

data class User(
    val uid: String,
    val mail: String,
    val trips: MutableList<Trip>
) {
    class Builder(
        var uid: String = "",
        var mail: String = "",
        var trips: MutableList<Trip> = mutableListOf(),
    ){
        fun uid(uid: String) = apply { this.uid = uid }
        fun mail(mail: String) = apply { this.mail = mail }
        fun addTrip(trip: Trip) = apply { (this.trips as MutableList).add(trip)}

        fun build(): User = User(uid, mail, trips)

    }

}