package com.likeki.alquran.data

data class ValidationResult(
    val totalExpected: Int = 115,
    val totalFound: Int = 0,
    val missingNumbers: List<Int> = emptyList(),
    val duplicateNumbers: List<Int> = emptyList(),
    val duplicateFilenames: List<String> = emptyList(),
    val emptyFilenames: List<Int> = emptyList()
) {
    val isValid: Boolean
        get() = totalFound == totalExpected && missingNumbers.isEmpty() &&
                duplicateNumbers.isEmpty() && duplicateFilenames.isEmpty() &&
                emptyFilenames.isEmpty()
}

object PlaylistValidator {
    fun validate(playlist: Playlist): ValidationResult {
        val tracks = playlist.tracks
        val nums = tracks.map { it.number }
        val dupNums = nums.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.toList()
        val missing = (1..115).filter { it !in nums }
        val fnameCounts = tracks.map { it.filename.trim() }.filter { it.isNotEmpty() }
            .groupingBy { it }.eachCount().filter { it.value > 1 }.keys.toList()
        val empty = tracks.filter { it.filename.trim().isEmpty() }.map { it.number }
        return ValidationResult(
            totalFound = tracks.size,
            missingNumbers = missing,
            duplicateNumbers = dupNums,
            duplicateFilenames = fnameCounts,
            emptyFilenames = empty
        )
    }
}
