package com.yh.kre.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.RealmModule

/**
 * Created by magillus on 8/14/2017.
 */
@RealmClass
open class User(@PrimaryKey var name: String? = null, var address: Address? = Address()) : RealmModel{
    
    override fun toString(): String {
        return "User(name=$name, address=$address)"
    }
}

open class Address(var street: String? = null, var city: String? = null, var zip: String? = null) : RealmObject(){
    
    override fun toString(): String {
        return "Address(street=$street, city=$city, zip=$zip)"
    }
}

@RealmModule(classes = [(User::class), (Address::class)])
class UserModule