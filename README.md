<p align="center">
  <img width="80%" src ="/krealmextensions.png" />
</p>

**The library is extended in [KRE](https://github.com/vicpinm/Kotlin-Realm-Extensions)**

**This library is Realm's Kotlin syntax extension, which simplifies your code for realm database operations to minimal expressions. Forget all the boilerplate files related to Realm API, use this lightweight library to perform database operations in one line of code. The library has complete unit test coverage.**

## Download for Kotlin 1.5 and Realm 10.5

Grab via Gradle:

```groovy
repositories {
    mavenCentral()
}

implementation("io.github.clistery:appbasic:2.4.1")
implementation("io.github.clistery:appinject:1.7.0")
implementation("io.github.clistery:kotlin-realm-ext:3.2.0")

//For Single and Flowable queries:
implementation("io.reactivex.rxjava2:rxjava:2.2.19")
implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
```

## Features

Forget about:
- Realm instances management
- Transactions
- Threads limitations
- Boilerplate related with Realm API
- From 2.0 version, your database entities can either extend from RealmObject or implement RealmModule interface.

## Usage

All methods below use Realm default configuration. You can use different Realm configurations per model with RealmConfigStore.init(Entity::class.java, myConfiguration). See application class from sample for details. Thanks to @magillus for its PR.

### Store entities

All your entities should extend RealmObject.

#### Before (java)

```java
User user = new User("John");

Realm realm = Realm.getDefaultInstance();
try{
   realm.executeTransaction(realm -> {
      realm.copyToRealmOrUpdate(user);  
   });  
} finally {
   realm.close();
}
```

#### After (Kotlin + extensions)

````kotlin
User("John").save()
````

Save method creates or updates your entity into database. You can also use create() method, which only create a new entity into database. If a previous one exists with the same primary key, it will throw an exception.

#### Save list: Before (java)

```java
List<User> users = new ArrayList<User>(...);

Realm realm = Realm.getDefaultInstance();
try {
    realm.executeTransaction(realm -> {
        realm.copyToRealmOrUpdate(users);  
    });
} finally {
    realm.close();
}
```

#### Save list: After (Kotlin + extensions)

```kotlin
listOf<User>(...).saveAll()
```

If you need to provide your own Realm instance, you can use the saveManaged(Realm) and saveAllManaged(Realm) methods. These methods return managed objects. You should close manually your Realm instance when you finish with them. 

### Query entities

* All query extensions return detached realm objects, using copy() method.
* All query extensions has two versions. One is an extension of RealmModel, and you need to create an instance of that model to perform your query. The other version is a global parametrized funcion (thanks to @PrashamTrivedi). See below examples for details.

#### Get first entity: Before (java)
```java
Realm realm = Realm.getDefaultInstance();
try {
   Event firstEvent = realm.where(Event.class).findFirst();
   firstEvent = realm.copy(event);
} finally {
   realm.close();
}
```

#### Get first entity: After (Kotlin + extensions)
```kotlin
val firstEvent = Event().queryFirst() //Or val first = queryFirst<Event> 
```


You can use lastItem extension too.

#### Get all entities: Before (java)
```java
Realm realm = Realm.getDefaultInstance();
try {
    List<Event> events = realm.where(Event.class).findAll();
    events = realm.copy(event);
} finally {
    realm.close();
}
```

#### Get  all entities: After (Kotlin + extensions)
```kotlin
val events = Event().queryAll() //Or queryAll<Event>
```

#### Get entities with conditions: Before (java)

```java
Realm realm = Realm.getDefaultInstance();
try{
    List<Event> events = realm.where(Event.class).equalTo("id",1).findAll();
    events = realm.copy(event);
} finally {
    realm.close();
}
```

#### Get entities with conditions: After (Kotlin + extensions)
```kotlin
val events = Event().query { equalTo("id",1) } //Or query<Event> { ... }
//NOTE: If you have a compilation problems in equalTo method (overload ambiguity error), you can use equalToValue("id",1) instead
```

If you only need the first or last result, you can also use:

```kotlin
val first = Event().queryFirst { equalTo("id",1) }
val last = Event().queryLast { equalTo("id",1) }
```

#### Get sorted entities
```kotlin
val sortedEvents = Event().querySorted("name",Sort.DESCENDING) 
```

```kotlin
val sortedEvents = Event().querySorted("name",Sort.DESCENDING) { equalTo("id",1) }
```


### Delete entities

#### Delete all: Before (java)
```kotlin
Realm realm = Realm.getDefaultInstance();
try{
    List<Event> events = realm.where(Event.class).findAll();
    realm.executeTransaction(realm -> {
        events.deleteAllFromRealm();
    });
} finally {
    realm.close();
}
```

#### Delete all: After (Kotlin + extensions)
```kotlin
Event().deleteAll() //Or deleteAll<Event>
```

#### Delete with condition: Before (java)

```kotlin
Realm realm = Realm.getDefaultInstance();
try{
    List<Event> events = realm.where(Event.class).equalTo("id",1).findAll().deleteAllFromRealm();
    events = realm.copy(event);
} finally {
    realm.close();
}
```

#### Delete with condition: After (Kotlin + extensions)
```kotlin
Event().delete { equalTo("id", 1) }
```


### Observe data changes

#### Before (java)

```java
Realm realm = Realm.getDefaultInstance();
Flowable<List<Event>> obs =  realm.where(Event.class).findAllAsync()
.asFlowable()
.filter(RealmResults::isLoaded)
.map(realm::copy)
.doOnUnsubscribe(() -> realm.close());
```

#### After (Kotlin + extensions)

```kotlin
val obs = Event().queryAllAsFlowable() //Or queryAllAsFlowable<Event>
```

#### Observe query with condition: Before (java)

```java
Realm realm = Realm.getDefaultInstance();
Flowable<List<Event>> obs =  realm.where(Event.class).equalTo("id",1).findAllAsync()
.asFlowable()
.filter(RealmResults::isLoaded)
.map(realm::copy)
.doOnUnsubscribe(() -> realm.close());
```

#### Observe query with condition: After (Kotlin + extensions)

```kotlin
val obs = Event().queryAsFlowable { equalTo("id",1) }
```

These kind of observable queries have to be performed on a thread with a looper attached to it. If you perform an observable query on the main thread, it will run on this thread. If you perform the query on a background thread, a new thread with a looper attached will be created for you to perform the query. This thread will be listen for data changes and it will terminate when you call unsubscribe() on your subscription. 

#### RxJava 2 Single support (thanks to @SergiyKorotun)

```kotlin
val single = Event().queryAllAsSingle()
val single = Event().queryAsSingle { equalTo("id", 1) }

```

### Transactions
If you need to perform several operations in one transaction, you can do:

```kotlin
executeTransaction {
   User().deleteAll() //Or deleteAll<User>()
   newUsers.saveAll()
}
```

### Range
If you want to query the specified range (pagination) results, you can do:

```kotlin
User().range(0, 10) //Or range<User>(0, 10)
```

### Distinct Group
If you want to query the unique group-by set in a given range, you can do:

```kotlin
User().distinctGroup("name", String::class.java) //Or distinctGroup<User, String>("name")
```

### Group By
if you want query specify field to group and take out the mapping set in the given range, you can do:

```kotlin
User().groupBy("name", String::class.java) //Or groupBy<User, String>("name")
```

### Threads management
Realm needs to perform observable queries and async queries in a thread with a looper attached to it. This library has that into account, and when you perform queries like queryAsFlowable, queryAsSingle, queryAsync and all other variants, a new thread with a looper attached to it will be created for you if the thread from where you execute the query does not have a looper attached. This thread created by the library will be finished automatically when the subscription is finished, or when the async query return its result.

### Proguard

You need to add these rules if you use proguard, for rxjava and realm:


```bash
-keep class com.yh.krealmextensions.** 
-keepnames public class * extends io.realm.RealmObject
-keepnames public class * extends io.realm.RealmModel
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn io.realm.**
```
