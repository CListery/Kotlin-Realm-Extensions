package com.yh.krealmextensions

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.yh.krealmextensions.model.TestEntity
import com.yh.krealmextensions.model.TestEntityAutoPK
import com.yh.krealmextensions.model.TestEntityPK
import com.yh.krealmextensions.util.TestRealmConfigurationFactory
import io.realm.Realm
import io.realm.Sort
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class KRealmExtensionsTests {

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
     * PERSISTENCE TESTS
     */
    @Test
    fun testPersistEntityWithCreate() {
        TestEntity().create() //No exception expected
    }

    @Test
    fun testPersistEntityWithCreateManaged() {
        val result = TestEntity().createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test
    fun testPersistPKEntityWithCreate() {
        TestEntityPK(1).create() //No exception expected
    }

    @Test
    fun testPersistPKEntityWithCreateManaged() {
        val result = TestEntityPK(1).createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPersistEntityWithCreateOrUpdateMethod() {
        TestEntity().createOrUpdate() //Exception expected due to TestEntity has no primary key
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPersistEntityWithCreateOrUpdateMethodManaged() {
        TestEntity().createOrUpdateManaged(realm) //Exception expected due to TestEntity has no primary key
    }

    fun testPersistPKEntityWithCreateOrUpdateMethod() {
        TestEntityPK(1).createOrUpdate() //No exception expected
    }

    fun testPersistPKEntityWithCreateOrUpdateMethodManaged() {
        val result = TestEntityPK(1).createOrUpdateManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
    }

    @Test
    fun testPersistEntityWithSaveMethod() {
        TestEntity().save() //No exception expected
    }

    @Test
    fun testPersistEntityWithSaveMethodManaged() {
        val result = TestEntity().saveManaged(realm) //No exception expected
        assertThat(result.isManaged)
        assertThat(TestEntity().count(realm)).isEqualTo(1)
    }

    @Test
    fun testPersistPKEntityWithSaveMethod() {
        TestEntityPK(1).save() //No exception expected
    }

    @Test
    fun testPersistPKEntityWithSaveMethodManaged() {
        val result = TestEntityPK(1).saveManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
        assertThat(TestEntityPK().count(realm)).isEqualTo(1)
    }

    @Test(expected = RealmPrimaryKeyConstraintException::class)
    fun testPersistPKEntityWithCreateMethodAndSamePrimaryKey() {
        TestEntityPK(1).create() //No exception expected
        TestEntityPK(1).create() //Exception expected
    }

    @Test(expected = RealmPrimaryKeyConstraintException::class)
    fun testPersistPKEntityWithCreateMethodAndSamePrimaryKeyManaged() {
        val result = TestEntityPK(1).createManaged(realm) //No exception expected
        assertThat(result.isManaged).isTrue()
        TestEntityPK(1).createManaged(realm) //Exception expected
    }

    @Test
    fun testPersistPKEntityListWithSaveMethod() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
    }

    @Test
    fun testPersistPKEntityListWithSaveMethodManaged() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        val results = list.saveAllManaged(realm)
        results.forEach { assertThat(it.isManaged).isTrue() }
    }

    @Test
    fun testCountPKEntity() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()
        assertThat(TestEntityPK().count()).isEqualTo(3)
    }

    @Test
    fun testCountDuplicatePKEntity() {
        val list = listOf(TestEntityPK(1), TestEntityPK(1), TestEntityPK(1))
        list.saveAll()
        assertThat(TestEntityPK().count()).isEqualTo(1)
    }

    @Test
    fun testCountEntity() {
        val list = listOf(TestEntity(), TestEntity(), TestEntity())
        list.saveAll()
        assertThat(TestEntity().count()).isEqualTo(3)
    }

    /**
     *  PERSISTENCE TEST WITH AUTO PRIMARY KEY
     */
    @Test
    fun testPersistAutoPKEntityWithSaveMethod() {
        TestEntityAutoPK().save() //No exception expected
    }

    @Test
    fun testPersistAutoPKEntityWithSaveMethodShouldHavePK() {
        TestEntityAutoPK().save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(1)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(1)
        TestEntityAutoPK().save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(2)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(2)
        TestEntityAutoPK().save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(3)
    }

    @Test
    fun testPersistAutoPkEntityWithPkShouldNotBeOverrided() {
        TestEntityAutoPK(4, "").save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(1)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(4)
        TestEntityAutoPK(10, "").save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(2)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(10)
        TestEntityAutoPK(12, "").save()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(12)
    }

    @Test
    fun testPersistAutoPKEntityWithSaveManagedMethod() {
        val result = TestEntityAutoPK().saveManaged(realm)
        assertThat(result.isManaged)
        assertThat(TestEntityAutoPK().count(realm)).isEqualTo(1)
    }

    @Test
    fun testPersistAutoPKEntityListWithSaveMethod() {
        val list = listOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAll()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(TestEntityAutoPK().queryFirst()?.id).isEqualTo(1)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(3)
    }

    @Test
    fun testPersistAutoPKEntityArrayWithSaveMethod() {
        val list = arrayOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAll()
        assertThat(TestEntityAutoPK().count()).isEqualTo(3)
        assertThat(TestEntityAutoPK().queryFirst()?.id).isEqualTo(1)
        assertThat(TestEntityAutoPK().queryLast()?.id).isEqualTo(3)
    }

    @Test
    fun testPersistAutoPKEntityListWithSaveManagedMethod() {
        val list = listOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAllManaged(realm)
        assertThat(TestEntityAutoPK().count(realm)).isEqualTo(3)
    }

    @Test
    fun testPersistAutoPKEntityArrayWithSavemanagedMethod() {
        val list = arrayOf(TestEntityAutoPK(), TestEntityAutoPK(), TestEntityAutoPK())
        list.saveAllManaged(realm)
        assertThat(TestEntityAutoPK().count(realm)).isEqualTo(3)
    }

    @Test
    fun testUpdateEntity() {
        TestEntity("test").save()
        TestEntity().queryAndUpdate({ equalTo("name", "test") }) {
            it.name = "updated"
        }

        val result = TestEntity().queryFirst { equalTo("name", "updated") }
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("updated")
    }

    @Test
    fun testUpdatePKEntity() {
        TestEntityPK(1, "test").save()
        TestEntityPK().queryAndUpdate({ equalTo("name", "test") }) {
            it.name = "updated"
        }

        val result = TestEntityPK().queryFirst { equalTo("name", "updated") }
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("updated")
    }

    /**
     * QUERY TESTS WITH EMPTY DB
     */
    @Test
    fun testQueryFirstObjectWithEmptyDBShouldReturnNull() {
        assertThat(TestEntity().queryFirst()).isNull()
    }

    @Test
    fun testAsyncQueryFirstObjectWithEmptyDBShouldReturnNull() {
        block {
            TestEntity().queryFirstAsync { assertThat(it).isNull(); release() }
        }
    }

    @Test
    fun testQueryLastObjectWithEmptyDBShouldReturnNull() {
        assertThat(TestEntity().queryLast()).isNull()
    }

    @Test
    fun testQueryLastObjectWithConditionAndEmptyDBShouldReturnNull() {
        assertThat(TestEntity().queryLast { equalTo("name", "test") }).isNull()
    }

    @Test
    fun testAsyncQueryLastObjectWithEmptyDBShouldReturnNull() {
        block {
            TestEntity().queryLastAsync { assertThat(it).isNull(); release() }
        }
    }

    @Test
    fun testAllItemsShouldReturnEmptyCollectionWhenDBIsEmpty() {
        assertThat(TestEntity().queryAll()).hasSize(0)
    }

    @Test
    fun testAllItemsAsyncShouldReturnEmptyCollectionWhenDBIsEmpty() {
        block {
            TestEntity().queryAllAsync { assertThat(it).hasSize(0); release() }
        }
    }

    @Test
    fun testQueryConditionalWhenDBIsEmpty() {
        val result = TestEntity().query { equalTo("name", "test") }
        assertThat(result).hasSize(0)
    }

    @Test
    fun testQueryFirstItemWhenDBIsEmpty() {
        val result = TestEntity().queryFirst { equalTo("name", "test") }
        assertThat(result).isNull()
    }

    @Test
    fun testQuerySortedWhenDBIsEmpty() {
        val result = TestEntity().querySorted("name", Sort.ASCENDING) { equalTo("name", "test") }
        assertThat(result).hasSize(0)
    }

    /**
     * QUERY TESTS WITH POPULATED DB
     */
    @Test
    fun testQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().queryFirst()).isNotNull()
        assertThat(TestEntityPK().queryFirst()?.id).isEqualTo(0)
    }

    @Test
    fun testAsyncQueryFirstItemShouldReturnFirstItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryFirstAsync {
                assertThat(it).isNotNull()
                assertThat(it?.id).isEqualTo(0)
                release()
            }
        }
    }

    @Test
    fun testQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().queryLast()?.id).isEqualTo(4)
    }

    @Test
    fun testQueryLastItemWithConditionShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)
        assertThat(TestEntityPK().queryLast { equalToValue("id", 3) }?.id).isEqualTo(3)
    }

    @Test
    fun testAsyncQueryLastItemShouldReturnLastItemWhenDBIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryLastAsync {
                assertThat(it).isNotNull()
                release()
            }
        }
    }

    @Test
    fun testQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)
        assertThat(TestEntity().queryAll()).hasSize(5)
    }

    @Test
    fun testAsyncQueryAllItemsShouldReturnAllItemsWhenDBIsNotEmpty() {
        populateDBWithTestEntity(numItems = 5)

        block {
            TestEntity().queryAllAsync { assertThat(it).hasSize(5); release() }
        }
    }

    @Test
    fun testQueryAllItemsAfterSaveCollection() {
        val list = listOf(TestEntityPK(1), TestEntityPK(2), TestEntityPK(3))
        list.saveAll()

        assertThat(TestEntityPK().queryAll()).hasSize(3)
    }

    /**
     * QUERY TESTS WITH WHERE STATEMENT
     */
    @Test
    fun testWhereQueryShouldReturnExpectedItems() {
        populateDBWithTestEntityPK(numItems = 5)
        val results = TestEntityPK().query { equalToValue("id", 1) }

        assertThat(results).hasSize(1)
        assertThat(results.first().id).isEqualTo(1)
    }

    @Test
    fun testAsyncWhereQueryShouldReturnExpectedItems() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryAsync({ equalToValue("id", 1) }) { results ->
                assertThat(results).hasSize(1)
                assertThat(results.first().id).isEqualTo(1)
                release()
            }
        }
    }

    @Test
    fun testWhereQueryShouldNotReturnAnyItem() {
        populateDBWithTestEntityPK(numItems = 5)
        val results = TestEntityPK().query { equalToValue("id", 6) }

        assertThat(results).hasSize(0)
    }

    @Test
    fun testAsyncWhereQueryShouldNotReturnAnyItem() {
        populateDBWithTestEntityPK(numItems = 5)

        block {
            TestEntityPK().queryAsync({ equalToValue("id", 6) }) { results ->
                assertThat(results).hasSize(0)
                release()
            }
        }
    }

    @Test
    fun testFirstItemWhenDbIsNotEmpty() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().queryFirst { equalToValue("id", 2) }

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(2)
    }

    @Test
    fun testQueryAscendingShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().querySorted("id", Sort.ASCENDING)

        assertThat(result).hasSize(5)
        assertThat(result.first().id).isEqualTo(0)
        assertThat(result.last().id).isEqualTo(4)
    }

    @Test
    fun testQueryDescendingShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().querySorted("id", Sort.DESCENDING)

        assertThat(result).hasSize(5)
        assertThat(result.first().id).isEqualTo(4)
        assertThat(result.last().id).isEqualTo(0)
    }

    @Test
    fun testQueryDescendingWithFilterShouldReturnOrderedResults() {
        populateDBWithTestEntityPK(numItems = 5)

        val result = TestEntityPK().querySorted("id", Sort.DESCENDING) {
            lessThan("id", 3).greaterThan("id", 0)
        }

        assertThat(result).hasSize(2)
        assertThat(result.first().id).isEqualTo(2)
        assertThat(result.last().id).isEqualTo(1)
    }

    /**
     * DELETION TESTS
     */
    @Test
    fun testDeleteEntities() {
        populateDBWithTestEntity(numItems = 5)

        TestEntity().deleteAll()

        assertThat(TestEntity().queryAll()).hasSize(0)
    }

    @Test
    fun testDeleteEntitiesWithPK() {
        populateDBWithTestEntityPK(numItems = 5)

        TestEntityPK().deleteAll()

        assertThat(TestEntityPK().queryAll()).hasSize(0)
    }

    @Test
    fun testDeleteEntitiesWithStatement() {
        populateDBWithTestEntityPK(numItems = 5)

        TestEntityPK().delete { equalToValue("id", 1) }

        assertThat(TestEntityPK().queryAll()).hasSize(4)
    }

    /**
     * UTILITY TEST METHODS
     */
    private fun populateDBWithTestEntity(numItems: Int) {
        (0..numItems - 1).forEach { TestEntity().save() }
    }

    private fun populateDBWithTestEntityPK(numItems: Int) {
        (0..numItems - 1).forEach { TestEntityPK(it.toLong()).save() }
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
