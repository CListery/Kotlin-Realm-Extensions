package com.yh.krealmextensions

import io.realm.RealmModel
import io.realm.kotlin.isValid

fun RealmModel?.isValidWithSafe(): Boolean = this?.isValid()
    ?: false