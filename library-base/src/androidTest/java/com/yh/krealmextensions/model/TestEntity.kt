package com.yh.krealmextensions.model

import io.realm.RealmObject

/**
 * Only for test purposes. It's placed here instead of in test folder due to problems with realm plugin and kotlin plugin.
 */
open class TestEntity() : RealmObject() {

    var name: String = ""

    var type: String = ""

    var time: Long = -1

    constructor(name: String) : this() {
        this.name = name
    }

    constructor(name: String, type: String, time: Long = System.currentTimeMillis()) : this() {
        this.name = name
        this.type = type
        this.time = time
    }
}
