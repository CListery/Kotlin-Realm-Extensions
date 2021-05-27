package com.yh.krealmextensions

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.yh.krealmextensions.model.TestEntity
import com.yh.krealmextensions.model.TestEntityPK
import com.yh.krealmextensions.util.TestRealmConfigurationFactory
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.Sort
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class KRealmExtensionsRxTests {

    @get:Rule var configFactory = TestRealmConfigurationFactory()
    lateinit var realm: Realm
    lateinit var latch: CountDownLatch
    var latchReleased = false
    var disposable: Disposable? = null

    @Before fun setUp() {
        val realmConfig = configFactory.createConfiguration()
        realm = Realm.getInstance(realmConfig)
        latch = CountDownLatch(1)
    }

    @After fun tearDown() {
        TestEntity().deleteAll()
        TestEntityPK().deleteAll()
        realm.close()
        latchReleased = false
        disposable = null
    }

    /**
     * SINGLE AND FLOWABLE TESTS
     */

    @Test fun testQueryAllAsFlowable() {

        var itemsCount = 5
        var disposable: Disposable? = null

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = TestEntity().queryAllAsFlowable().subscribe({
                assertThat(it).hasSize(itemsCount)
                release()
            }, { it.printStackTrace() })
        }

        block {
            //Add one item more to db
            ++itemsCount
            populateDBWithTestEntity(numItems = 1)
        }

        disposable?.dispose()
    }

    @Test fun testQueryAllAsFlowableManaged() {

        var itemsCount = 5
        var disposable: Disposable? = null

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = TestEntity().queryAllAsFlowable(managed = true).subscribe({
                assertThat(it).hasSize(itemsCount)
                it.forEach { assertThat(it.isManaged).isTrue() }
                release()
            }, { it.printStackTrace() })
        }

        block {
            //Add one item more to db
            ++itemsCount
            populateDBWithTestEntity(numItems = 1)
        }

        disposable?.dispose()
    }

    @Test fun testQueryAsFlowable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().queryAsFlowable { equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test fun testQueryAsFlowableManaged() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().queryAsFlowable(managed = true) { equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                it.forEach { assertThat(it.isManaged).isTrue() }
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test fun testQueryAllAsSingle() {

        var itemsCount = 5

        populateDBWithTestEntity(numItems = itemsCount)

        block {
            disposable = TestEntity().queryAllAsSingle().subscribe({ result ->
                assertThat(result).hasSize(itemsCount)
                assertThat(result[0].isManaged).isFalse()
                release()
            }, { it.printStackTrace() })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test fun testQueryAsSingle() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().queryAsSingle { equalToValue("id", 1) }.subscribe({ it ->
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                release()
            }, { it.printStackTrace() })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test fun testQuerySortedAsFlowable() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsFlowable("id", Sort.DESCENDING).subscribe({
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test fun testQuerySortedAsFlowableManaged() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsFlowable("id", Sort.DESCENDING, managed = true).subscribe({
                assertThat(it).hasSize(5)
                it.forEach { assertThat(it.isManaged).isTrue() }
                assertThat(it[0].id).isEqualTo(4)
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test fun testQuerySortedAsFlowableWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsFlowable("id", Sort.DESCENDING) { equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(1)
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test fun testQuerySortedAsFlowableManagedWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsFlowable("id", Sort.DESCENDING, managed = true) { equalToValue("id", 1) }.subscribe({
                assertThat(it).hasSize(1)
                it.forEach { assertThat(it.isManaged).isTrue() }
                assertThat(it[0].id).isEqualTo(1)
                release()
            }, { it.printStackTrace() })
        }

        disposable?.dispose()
    }

    @Test fun testQuerySortedAsSingle() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsSingle("id", Sort.DESCENDING).subscribe({ it ->
                assertThat(it).hasSize(5)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(4)
                release()
            }, { it.printStackTrace() })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    @Test fun testQuerySortedAsSingleWithQuery() {

        populateDBWithTestEntityPK(numItems = 5)

        block {
            disposable = TestEntityPK().querySortedAsSingle("id", Sort.DESCENDING) { equalToValue("id", 1) }.subscribe({ it ->
                assertThat(it).hasSize(1)
                assertThat(it[0].isManaged).isFalse()
                assertThat(it[0].id).isEqualTo(1)
                release()
            }, { it.printStackTrace() })
        }

        assertThat(disposable?.isDisposed ?: false).isTrue()
    }

    /**
     * UTILITY TEST METHODS
     */
    private fun populateDBWithTestEntity(numItems: Int) {
        (0 until numItems).forEach { TestEntity().save() }
    }

    private fun populateDBWithTestEntityPK(numItems: Int) {
        (0 until numItems).forEach { TestEntityPK(it.toLong()).save() }
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
