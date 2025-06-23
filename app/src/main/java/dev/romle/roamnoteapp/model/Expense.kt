package dev.romle.roamnoteapp.model

import java.io.Serializable

data class Expense (
    val name: String,
    val tags: List<ExpenseTag>,
    val cost: Double,
): Serializable {
    class Builder(
        var name: String = "",
        var tags: List<ExpenseTag> = listOf(),
        var cost: Double = 0.0
    ){
        fun name(name: String) = apply { this.name = name }
        fun tags(tags: List<ExpenseTag>) = apply { this.tags = tags }
        fun cost(cost: Double) = apply{this.cost = cost}

        fun build() = Expense(
            name, tags, cost
        )
    }

    constructor() : this(
        name = "",
        tags = listOf(),
        cost = 0.0
    )
}
