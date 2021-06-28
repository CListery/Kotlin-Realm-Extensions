package com.yh.kre.model

import com.yh.krealmextensions.AutoIncrementPK
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.RealmModule

/**
 * Created by magillus on 8/14/2017.
 */
@RealmClass
@AutoIncrementPK
open class User(
    var name: String? = null,
    var address: Address? = Address()
) : RealmModel {

    @PrimaryKey var userId: Long = Long.MIN_VALUE

    override fun toString(): String {
        return "User(userId=$userId, name=$name, address=$address)"
    }
}

open class Address(var street: String? = null, var city: String? = null, var zip: String? = null) : RealmObject() {

    override fun toString(): String {
        return "Address(street=$street, city=$city, zip=$zip)"
    }
}

@RealmModule(classes = [(User::class), (Address::class)])
class UserModule