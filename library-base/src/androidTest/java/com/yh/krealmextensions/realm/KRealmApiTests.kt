package com.yh.krealmextensions.realm

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.yh.krealmextensions.*
import com.yh.krealmextensions.model.TestEntity
import com.yh.krealmextensions.model.TestEntityAutoPK
import com.yh.krealmextensions.model.TestEntityPK
import com.yh.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class KRealmApiTests {


    @get:Rule
    var configFactory = TestRealmConfigurationFactory()
    lateinit var realm: Realm
    lateinit var latch: CountDownLatch
    var latchReleased = false

    @Before
    fun setUp() {
        val realmConfig = configFactory.createConfiguration()
        realm = Realm.getInstance(realmConfig)
        latch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        TestEntity().deleteAll()
        TestEntityPK().deleteAll()
        TestEntityAutoPK().deleteAll()
        realm.close()
        latchReleased = false
    }

    /**
     * Distinct TESTS
     */
    @Test
    fun testDistinctEntityEmptyCollectionWhenDBIsEmpty() {
        block {
            queryAsync<TestEntity>({
                distinct("type")
            }, { result ->
                assertThat(result).isEmpty()
                release()
            })
        }
    }

    @Test
    fun testDistinctEntityNotEmpty() {
        block {
            for (pos in 0 until 100) {
                TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
            }
            queryAllAsync<TestEntity>{ result ->
                val groupResults = result.groupBy {
                    it.type
                }
                assertThat(groupResults).isNotEmpty()
                assertThat(listOf("type_true", "type_false")).containsAllIn(groupResults.keys)
                val fullResult = (0 until 100).filter {
                    it % 3 == 0
                }.map {
                    "name_$it"
                }.toList()
                assertThat(fullResult).containsAllIn(groupResults["type_true"]?.map { it.name }!!)
                release()
            }
        }
    }

    private fun blockLatch() {
        if (!latchReleased) {
            latch.await()
        }
    }

    private fun release() {
        latchReleased = true
        latch.countDown()
        latch = CountDownLatch(1)
    }

    fun block(closure: () -> Unit) {
        latchReleased = false
        closure()
        blockLatch()
    }
}