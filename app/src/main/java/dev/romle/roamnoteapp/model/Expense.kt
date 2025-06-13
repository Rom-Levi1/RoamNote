package dev.romle.roamnoteapp.model

data class Expense(
    val name: String,
    val tags: List<String>,
    val cost: Double,
) {
    class Builder(
        var name: String = "",
        var tags: List<String> = listOf(),
        var cost: Double = 0.0
    ){
        fun name(name: String) = apply { this.name = name }
        fun tags(tags: List<String>) = apply { this.tags = tags }
        fun cost(cost: Double) = apply{this.cost = cost}

        fun build() = Expense(
            name, tags, cost
        )
    }
}
