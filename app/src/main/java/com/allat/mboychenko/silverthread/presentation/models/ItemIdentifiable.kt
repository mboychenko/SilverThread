package com.allat.mboychenko.silverthread.presentation.models

interface ItemIdentifiable {
    fun id(): String
    override fun equals(other: Any?): Boolean
}