package com.yh.kre

import androidx.multidex.MultiDexApplication
import com.yh.kre.model.UserModule
import com.yh.krealmextensions.RealmConfigManager
import io.realm.Realm
import io.realm.RealmConfiguration

class Application : MultiDexApplication() {
    
    override fun onCreate() {
        super.onCreate()
        
        Realm.init(this)
        val userAddressConfig = RealmConfiguration.Builder()
            .name("user-db")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
        // clear previous data for fresh start
        Realm.deleteRealm(Realm.getDefaultConfiguration()!!)
        Realm.deleteRealm(userAddressConfig.build())
        
        //Optional: if you want to specify your own realm configuration, you have two ways:
        
        RealmConfigManager.isEnableUiThreadOption = true
        //1. If you want to specify a configuration for a specific module, you can use:
        RealmConfigManager.initModule(UserModule::class.java, userAddressConfig)
        
        //2. You can specify any configuration per model with:
        //RealmConfigStore.init(User::class.java, userAddressConfig)
        //RealmConfigStore.init(Address::class.java, userAddressConfig)
    }
}
