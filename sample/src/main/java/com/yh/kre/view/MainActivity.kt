package com.yh.kre.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yh.kre.databinding.ActivityMainBinding
import com.yh.kre.extensions.isMainThread
import com.yh.kre.extensions.wait
import com.yh.kre.model.Address
import com.yh.kre.model.Item
import com.yh.kre.model.User
import com.yh.krealmextensions.*
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.Sort
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    
    companion object {
        
        private const val TAG = "MainActivity"
    }
    
    val dbSize = 100
    val userSize = 5
    
    private val mMainActBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(mMainActBinding.root)

        performTest("main thread") {
            thread {
                performTest("background thread items") {
                    // User perform Test
                    performUserTest("main thread users") {
                        thread { performUserTest("background thread users") }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        deleteAll<User>()
        deleteAll<Item>()
    }
    
    private fun performUserTest(threadName: String, finishCallback: (() -> Unit)? = null) {
        
        addMessage("Starting test on $threadName with User realm configuration", important = true)
        
        deleteAll<User>()
        addTimingMessageBlock("DB populated with $userSize users") { populateUserDb(userSize) }

        addTimingMessageBlock(mapOf("User count: %s" to { count<User>(getRealmInstance<User>()) }))

        addMessage("DB populated with $userSize users")
        
        addMessage("Querying users on $threadName...")
        
        addTimingMessageBlock(mapOf("Result: %s items" to { User().queryAll().size }))
        addTimingMessageBlock(mapOf("Result: %s items" to { queryAll<User>().size }))
        
        addMessage("Deleting users on $threadName...")
        
        addTimingMessageBlock(
            mapOf("Result: %s items" to { User().queryAll().size },
                "\n  -> all: %s " to {
                    User().queryAll()
                        .joinToString("\n         ") { "${it.name} - ${it.address?.street}" }
                })
        )
        
        addTimingMessageBlock("Deleting user") { delete<User> { equalTo("name", "name_2") } }
        
        addTimingMessageBlock(
            mapOf("Result: %s items" to { User().queryAll().size },
                "\n  -> all: %s " to {
                    User().queryAll()
                        .joinToString("\n         ") { "${it.name} - ${it.address?.street}" }
                })
        )
        
        addTimingMessageBlock("Deleting all users") { deleteAll<User>() }
        
        addMessage("Querying users on $threadName...")
        
        addTimingMessageBlock(mapOf("Result: %s users" to { User().queryAll().size }))
        
        addMessage("Observing table changes...")
        
        val subscription = User().queryAllAsFlowable().subscribe {
            addMessage("Flowable Changes received on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, total items: " + it.size)
            // addMessage("   name_2: ${it.find { u -> u.name == "name_2" }}")
        }
        
        val changeSetSubscription = queryAllByChangeSetAsFlowable<User>().subscribe {
            fun List<User>.print(): String {
                return map { u -> u.name }.joinToString(",")
            }
            addMessage("|ChangeSet - [${it.setState} - ${it.isOk}]")
            when(it.setState) {
                OrderedCollectionChangeSet.State.UPDATE -> {
                    addMessage(
                        """
                        |   insert: ${it.insert.print()}
                        |   change: ${it.change.print()}
                        |   delete: ${it.delete.print()}
                        """.trimIndent()
                    )
                }
                OrderedCollectionChangeSet.State.ERROR  -> {
                    addMessage("|   error: ${it.error}")
                }
                else                                    -> {
                }
            }
        }
        
        val singleSubscription = User().queryAllAsSingle().subscribe { it ->
            addMessage("Single received on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, total items: " + it.size)
        }

        querySortedAsync<User>({
            addMessage("querySortedAsync received on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, total items: " + it.size)
        }, "userId", Sort.DESCENDING)

        User().queryAllAsync {
            addMessage("queryAllAsync1 received on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, total items: " + it.size)
        }
        
        queryAllAsync<User> {
            addMessage("queryAllAsync2 received on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, total items: " + it.size)
        }
        
        wait(1) {
            queryAsync<User>({ equalTo("name", "name_2") }, {
                addMessage("queryAsync1 name_2 on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, item: " + it.firstOrNull())
            })
            populateUserDb(10)
            queryAsync<User>({ equalTo("name", "name_2") }, {
                addMessage("queryAsync2 name_2 on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, item: " + it.firstOrNull())
            })
        }
        
        wait(if(isMainThread()) 2 else 1) {
            queryAsync<User>({ equalTo("name", "name_2") }, {
                addMessage("queryAsync name_2 on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, items: " + it)
                val user2 = it.first()
                user2.address = Address("sdlfjsdfljsdf")
                user2.save()
            })
        }
        
        wait(if(isMainThread()) 3 else 1) {
            populateUserDb(10, 10)
        }
        
        wait(if(isMainThread()) 4 else 1) {
            populateUserDb(10, 20)
        }
        
        wait(if(isMainThread()) 5 else 1) {
            subscription.dispose()
            changeSetSubscription.dispose()
            singleSubscription.dispose()
            addMessage("Subscription finished")
            val defaultCount = Realm.getDefaultInstance().where(User::class.java).count()
            val userCount = User().getRealmInstance().where(User::class.java).count()
            
            addMessage("All users from default configuration : $defaultCount")
            addMessage("All users from user configuration : $userCount")
            finishCallback?.invoke()
        }
    }
    
    private fun performTest(threadName: String, finishCallback: (() -> Unit)? = null) {
        
        addMessage("Starting test on $threadName...", important = true)
        
        deleteAll<Item>()
        addTimingMessageBlock("DB populated with $dbSize items") { populateDB(numItems = dbSize) }
        
        addMessage("Querying items on $threadName...")
        
        addTimingMessageBlock(mapOf("Result: %s items" to { Item().queryAll().size }))
        addTimingMessageBlock(mapOf("Result: %s items" to { queryAll<Item>().size }))
        
        addMessage("Deleting items on $threadName...")
        
        addTimingMessageBlock("Deleting all items") { Item().deleteAll() }
        
        addMessage("Querying items on $threadName...")
        
        addTimingMessageBlock(mapOf("Result: %s items" to { Item().queryAll().size }))
        
        addTimingMessageBlock(mapOf("Result: %s items " to { Item().queryAll().size }))
        
        addMessage("Observing table changes...")
        
        val subscription = Item().queryAllAsFlowable().subscribe {
            addMessage("Changes received on ${if(Looper.myLooper() == Looper.getMainLooper()) "main thread" else "background thread"}, total items: " + it.size)
        }
        wait(1) {
            populateDB(numItems = 10)
        }
        
        wait(if(isMainThread()) 2 else 1) {
            populateDB(numItems = 10)
        }
        
        wait(if(isMainThread()) 3 else 1) {
            populateDB(numItems = 10)
        }
        
        wait(if(isMainThread()) 4 else 1) {
            subscription.dispose()
            addMessage("Subscription finished")
            finishCallback?.invoke()
        }
    }
    
    private fun addTimingMessageBlock(msg: String, block: () -> Unit) {
        addMessage("$msg - ${callBlockTiming(block)}ms")
    }
    
    private fun addTimingMessageBlock(blockMap: Map<String, () -> Any>) {
        val msg = StringBuilder()
        blockMap.forEach {
            val timing = callBlockTiming {
                val result = it.value()
                msg.append(it.key.format(result.toString()))
            }
            msg.append(" - ${timing}ms")
        }
        addMessage(msg.toString())
    }
    
    private inline fun callBlockTiming(block: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block.invoke()
        val endTime = System.currentTimeMillis()
        return endTime - startTime
    }
    
    private fun populateUserDb(numUsers: Int, startPos: Int = 0) {
        addMessage("Create users...")
        Array(numUsers) { User("name_%d".format(it.plus(startPos)), Address("street_%d".format(it.plus(startPos)))) }.saveAll()
        addMessage("Create users done size: $numUsers")
    }
    
    private fun populateDB(numItems: Int) {
        Array(numItems) { Item() }.saveAll()
    }
    
    private fun addMessage(message: String, important: Boolean = false) {
        Handler(Looper.getMainLooper()).post {
            val view = TextView(this)
            if(important) view.typeface = Typeface.DEFAULT_BOLD
            view.text = message
            mMainActBinding.mainContainer.addView(view)
            mMainActBinding.scroll.smoothScrollBy(0, 1000)
        }
    }
}
