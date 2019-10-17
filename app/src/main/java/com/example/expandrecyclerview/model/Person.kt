package com.example.expandrecyclerview.model

data class Person(
    var name: String,
    var description: String,
    var imageResource: Int,
    var isExpanded: Boolean
) {
    fun reverseExpanded() {
        isExpanded = !isExpanded
    }
}

