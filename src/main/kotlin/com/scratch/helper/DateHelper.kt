package com.scratch.helper

class DateHelper {
    companion object {
        fun getDaysInMiliseconds(days: Long): Long {
            return days * 24 * 60 * 60 * 1000
        }
    }
}