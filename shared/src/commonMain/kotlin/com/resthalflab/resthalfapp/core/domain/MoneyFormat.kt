package com.resthalflab.resthalfapp.core.domain

/** Groups an integer amount with dot separators, e.g. 250000 -> "250.000". */
fun formatThousands(value: Int): String =
    value.toString().reversed().chunked(3).joinToString(".").reversed()

fun currencySymbol(code: String): String = if (code == "IDR") "Rp" else code

/** e.g. (250000, "IDR") -> "Rp 250.000". */
fun formatMoney(amount: Int, currency: String): String =
    "${currencySymbol(currency)} ${formatThousands(amount)}"

/** Compact review count, e.g. 1240 -> "1.2K", 856 -> "856". */
fun formatCompactCount(count: Int): String =
    if (count >= 1000) "${count / 100 / 10.0}K" else count.toString()
