package com.yh.kre.model

import io.realm.RealmObject

open class Item() : RealmObject() {

    var name: String = ""

    constructor(name: String) : this() {
        this.name = name
    }
}