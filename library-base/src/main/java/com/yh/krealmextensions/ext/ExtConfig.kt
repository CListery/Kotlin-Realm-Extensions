package com.yh.krealmextensions.ext

import io.realm.RealmConfiguration

fun RealmConfiguration.printString(prefix: String = ""): String {
    val logSB = StringBuilder()
    logSB.append(prefix)
    logSB.append("\nconfig: ").append(
        (toString().split("\n")
            .plus("support-models: ${realmObjectClasses.map { it.canonicalName }}"))
            .joinToString("") { "\n    $it" }
    )
    return logSB.toString()
}