package com.yh.krealmextensions

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.yh.krealmextensions.model.TestEntity
import com.yh.krealmextensions.model.TestEntityAutoPK
import com.yh.krealmextensions.model.TestEntityPK
import com.yh.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import io.realm.Sort
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class KRealmGroupTests {

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

    @Test
    fun testGroupBy() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = groupBy<TestEntity, String>("type")
            assertThat(groupResult).hasSize(2)
            var total = 0
            groupResult.forEach {
                total += it.value.size
            }
            assertThat(total).isEqualTo(100)
            release()
        }
    }

    @Test
    fun testGroupBy2() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = TestEntity().groupBy("type", String::class.java)
            assertThat(groupResult).hasSize(2)
            var total = 0
            groupResult.forEach {
                total += it.value.size
            }
            assertThat(total).isEqualTo(100)
            release()
        }
    }

    @Test
    fun testDistinctGroup() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = distinctGroup<TestEntity, String>("type")
            assertThat(groupResult).hasSize(2)
            release()
        }
    }

    @Test
    fun testDistinctGroup2() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = TestEntity().distinctGroup("type", String::class.java)
            assertThat(groupResult).hasSize(2)
            release()
        }
    }

    @Test
    fun testGroupByWithSort() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = groupBy<TestEntity, String>(
                "type",
                sortFieldName = "time",
                sort = Sort.DESCENDING
            )
            assertThat(groupResult).hasSize(2)

            var total = 0
            groupResult.forEach {
                total += it.value.size
            }
            assertThat(total).isEqualTo(100)

            val trueGroup = groupResult["type_true"]
            assertThat(trueGroup).isNotEmpty()
            val falseGroup = groupResult["type_false"]
            assertThat(falseGroup).isNotEmpty()

            assertThat(trueGroup!!.first().name).isEqualTo("name_99")
            assertThat(falseGroup!!.first().name).isEqualTo("name_98")
            release()
        }
    }

    @Test
    fun testGroupByWithSort2() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = TestEntity().groupBy(
                "type", String::class.java,
                sortFieldName = "time",
                sort = Sort.DESCENDING
            )
            assertThat(groupResult).hasSize(2)

            var total = 0
            groupResult.forEach {
                total += it.value.size
            }
            assertThat(total).isEqualTo(100)

            val trueGroup = groupResult["type_true"]
            assertThat(trueGroup).isNotEmpty()
            val falseGroup = groupResult["type_false"]
            assertThat(falseGroup).isNotEmpty()

            assertThat(trueGroup!!.first().name).isEqualTo("name_99")
            assertThat(falseGroup!!.first().name).isEqualTo("name_98")
            release()
        }
    }

    @Test
    fun testDistinctGroupWithSort() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = distinctGroup<TestEntity, String>(
                "type",
                sortFieldName = "time",
                sort = Sort.DESCENDING
            )
            assertThat(groupResult).hasSize(2)

            val firstTrue = groupResult.find { it.type == "type_true" }
            assertThat(firstTrue).isNotNull()
            val firstFalse = groupResult.find { it.type == "type_false" }
            assertThat(firstFalse).isNotNull()

            assertThat(firstTrue!!.name).isEqualTo("name_99")
            assertThat(firstFalse!!.name).isEqualTo("name_98")
            release()
        }
    }

    @Test
    fun testDistinctGroupWithSort2() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = TestEntity().distinctGroup(
                "type",
                String::class.java,
                sortFieldName = "time",
                sort = Sort.DESCENDING
            )
            assertThat(groupResult).hasSize(2)

            val firstTrue = groupResult.find { it.type == "type_true" }
            assertThat(firstTrue).isNotNull()
            val firstFalse = groupResult.find { it.type == "type_false" }
            assertThat(firstFalse).isNotNull()

            assertThat(firstTrue!!.name).isEqualTo("name_99")
            assertThat(firstFalse!!.name).isEqualTo("name_98")
            release()
        }
    }

    @Test
    fun testGroupByWithQuery() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = groupBy<TestEntity, String>(
                "type",
                sortFieldName = "time",
                sort = Sort.DESCENDING
            ) {
                like("name", "name_1*")
            }
            assertThat(groupResult).hasSize(2)

            var total = 0
            groupResult.forEach {
                total += it.value.size
            }
            assertThat(total).isEqualTo(listOf(1).size + (10..19).toList().size)

            val trueGroup = groupResult["type_true"]
            assertThat(trueGroup).isNotEmpty()
            val falseGroup = groupResult["type_false"]
            assertThat(falseGroup).isNotEmpty()

            assertThat(trueGroup!!.first().name).isEqualTo("name_18")
            assertThat(falseGroup!!.first().name).isEqualTo("name_19")
            release()
        }
    }

    @Test
    fun testGroupByWithQuery2() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = TestEntity().groupBy(
                "type", String::class.java,
                sortFieldName = "time",
                sort = Sort.DESCENDING
            ) {
                like("name", "name_1*")
            }
            assertThat(groupResult).hasSize(2)

            var total = 0
            groupResult.forEach {
                total += it.value.size
            }
            assertThat(total).isEqualTo(listOf(1).size + (10..19).toList().size)

            val trueGroup = groupResult["type_true"]
            assertThat(trueGroup).isNotEmpty()
            val falseGroup = groupResult["type_false"]
            assertThat(falseGroup).isNotEmpty()

            assertThat(trueGroup!!.first().name).isEqualTo("name_18")
            assertThat(falseGroup!!.first().name).isEqualTo("name_19")
            release()
        }
    }

    @Test
    fun testDistinctGroupWithQuery() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = distinctGroup<TestEntity, String>(
                "type",
                sortFieldName = "time",
                sort = Sort.DESCENDING
            ) {
                like("name", "name_1*")
            }
            assertThat(groupResult).hasSize(2)

            val firstTrue = groupResult.find { it.type == "type_true" }
            assertThat(firstTrue).isNotNull()
            val firstFalse = groupResult.find { it.type == "type_false" }
            assertThat(firstFalse).isNotNull()

            assertThat(firstTrue!!.name).isEqualTo("name_18")
            assertThat(firstFalse!!.name).isEqualTo("name_19")
            release()
        }
    }

    @Test
    fun testDistinctGroupWithQuery2() {
        for (pos in 0 until 100) {
            TestEntity("name_$pos", "type_${pos % 3 == 0}").save()
        }
        block {
            val groupResult = TestEntity().distinctGroup(
                "type",
                String::class.java,
                sortFieldName = "time",
                sort = Sort.DESCENDING
            ) {
                like("name", "name_1*")
            }
            assertThat(groupResult).hasSize(2)

            val firstTrue = groupResult.find { it.type == "type_true" }
            assertThat(firstTrue).isNotNull()
            val firstFalse = groupResult.find { it.type == "type_false" }
            assertThat(firstFalse).isNotNull()

            assertThat(firstTrue!!.name).isEqualTo("name_18")
            assertThat(firstFalse!!.name).isEqualTo("name_19")
            release()
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