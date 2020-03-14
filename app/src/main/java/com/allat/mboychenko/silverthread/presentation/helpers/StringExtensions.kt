package com.allat.mboychenko.silverthread.presentation.helpers

import android.annotation.SuppressLint

private const val SEPARATOR = " "

private val multipleWhitespacePattern = "\\s+".toRegex()

fun String.trimRedurantWhitespaces() = this.trim().replace(multipleWhitespacePattern, SEPARATOR)

@SuppressLint("DefaultLocale")
fun String.capitalizeEachNewWord() =
    this.splitToSequence(SEPARATOR).map { it.capitalize() }.joinToString(SEPARATOR)

