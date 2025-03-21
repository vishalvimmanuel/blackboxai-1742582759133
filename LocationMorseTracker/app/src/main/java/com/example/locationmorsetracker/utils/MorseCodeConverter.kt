package com.example.locationmorsetracker.utils

object MorseCodeConverter {
    private val morseCodeMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..",
        'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....",
        'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.",
        'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..", '1' to ".----", '2' to "..---",
        '3' to "...--", '4' to "....-", '5' to ".....", '6' to "-....",
        '7' to "--...", '8' to "---..", '9' to "----.", '0' to "-----",
        ' ' to "/", '.' to ".-.-.-", ',' to "--..--", ':' to "---..."
    )

    fun convertToMorse(text: String): String {
        return text.uppercase().map { char ->
            morseCodeMap[char] ?: char.toString()
        }.joinToString(" ")
    }

    fun convertCoordinatesToMorse(latitude: Double, longitude: Double): String {
        val coordinatesText = String.format(
            "LAT %.6f LON %.6f",
            latitude,
            longitude
        )
        return convertToMorse(coordinatesText)
    }

    fun convertGeofenceStatusToMorse(location: String, status: String): String {
        val message = "$status $location"
        return convertToMorse(message)
    }
}