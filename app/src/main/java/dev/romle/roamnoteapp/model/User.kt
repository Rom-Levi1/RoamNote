package dev.romle.roamnoteapp.model

data class User(
    val username: String,
    val uid: String,
    val mail: String,
    val trips: MutableList<Trip>
) {
    class Builder(
        var username: String = "",
        var uid: String = "",
        var mail: String = "",
        var trips: MutableList<Trip> = mutableListOf(),
    ){
        fun username(username: String) = apply { this.username = username }
        fun uid(uid: String) = apply { this.uid = uid }
        fun mail(mail: String) = apply { this.mail = mail }
        fun addTrip(trip: Trip) = apply { (this.trips as MutableList).add(trip)}

        fun build(): User = User(username,uid, mail, trips)

    }

}