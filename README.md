Android Dualcache
=================
[![API](https://img.shields.io/badge/API-12%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=12)
[![Build Status](https://travis-ci.org/iagocanalejas/dualcache.svg?branch=master)](https://travis-ci.org/iagocanalejas/dualcache)
[![](https://jitpack.io/v/iagocanalejas/dualcache.svg)](https://jitpack.io/#iagocanalejas/dualcache)

**LIBRARY BASED IN [dualcache](https://github.com/vincentbrison/dualcache)**

# Description
This android library provide a cache with 2 layers, one in RAM in top of one on local storage.
Caches can be configured to have a persistence time
This library is highly configurable :


| Configurations | Disk : `Specific serializer` | Disk: `References` | Disk : `Volatil` | Disk: `disable` |
| -------------- | ---------------------------- | ------------------ | ---------------- | --------------- |
| Ram : `Specific serializer` | YES | NO | YES | YES |
| Ram : `References` | YES | NO | YES | YES |
| Ram : `Volatil` | YES | NO | YES | YES |
| Ram : `disable` | YES | NO | YES | NO |

 - `Volatile` : the object have maximun lifetime
 - `Specific serializer` : the object stored in cache will be serialized through a serializer provided by yourself.
 - `References` : the objects stored in Ram are cached through there references (no serialization is done).
 - `Disable` : the corresponding layer (Ram or disk) is disable.

If you work with `specific serializer` or `references` you will have to provide (through an interface) the
way of compute the size of cached objects, to be able to correctly execute the [LRU policy](http://en.wikipedia.org/wiki/Cache_algorithms).

For a better performance, I recommend that you use larger size for the disk layer than for the Ram layer. When you try to get an object from the cache which is already in the Ram layer, the disk wont be use to keep the best performance from the Ram. If you try to get an object from the cache which is on disk and not on Ram, the object will be loaded into RAM, to ensure better further access time.

To see a deply description for base dualcache work flow visit [dualcache](https://github.com/vincentbrison/dualcache).

# Explanation

When you want to use a [cache](http://en.wikipedia.org/wiki/Cache_\(computing\)) on Android today, you have two possibilities. You whether use :
 - The [LruCache](http://developer.android.com/reference/android/util/LruCache.html) included into the Android SDK.
  - Works on RAM
  - Very fast access to your cache.
  - High resources constraints, since the RAM allocated to your application is used for caching.
  - Not persistent among different execution of your app.
 - The [DiskLruCache](https://github.com/JakeWharton/DiskLruCache) of Jake Wharton.
  - Works on Disk
  - Slower access time than the LruCache.
  - Almost no resources constraints, since the size used on the disk (internal memory), will not impact your application.
  - Persistent among different execution of your app.

The purpose of this library is to provide both features of these two caches, by making them working together. You do not need
to ask yourself anymore "Should I use this one or this one ? But this one is persistent, but the other one is faster...".
With this library you only use one cache, with two layers, one in RAM, and one in Disk and you configure how they have to work
to provide exactly what you need in term of caching for you application.

# Setup

- Ensure you can pull artifacts from JitPack :
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
- And add to your module gradle file :
```gradle
android {
    packagingOptions {
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/NOTICE'
    }
}

dependencies {
    compile 'com.github.iagocanalejas.dualcache:dualcache:<VERSION>'
}
```

All the configuration of the cache is done when you are building the cache through its `Builder` class.

**It's mandatory to use the `useHashedKey` method if your KEY_TYPE is not String**

# Basic Cache Example

 First of all, you need to build you cache, through the `Builder` class.
 Basic cache with references in RAM and a default serializer on disk :

```Java
cache = new Builder<KEY_TYPE, VALUE_TYPE>(CACHE_NAME, TEST_APP_VERSION)
    .enableLog()
    .useReferenceInRam(RAM_MAX_SIZE, new SizeOfYourClass())
    .useSerializerInDisk(DISK_MAX_SIZE, true, new SerializerForYourClass(), getContext())
    .build();
```
You can note that when you build the cache, you need to provide an `app version` number. When the cache
is loaded, if data exist with a inferior number, it will be invalidate. It can be extremely useful when
you update your app, and change your model, to avoid crashes. This feature is possible because the DiskLruCache of Jake Wharton
implemented this feature.

# Basic Volatile Cache Example

 With this type of cache your entries will only live for the PERSIST_TIME you set.

```Java
cache = new Builder<KEY_TYPE, VALUE_TYPE>(CACHE_NAME, TEST_APP_VERSION)
    .enableLog()
    .useReferenceInRam(RAM_MAX_SIZE, new SizeOfYourClass())
    .useSerializerInDisk(DISK_MAX_SIZE, true, new SerializerForYourClass(), getContext())
    .useVolatileCache(PERSISTENCE_TIME)
    .build();
```

# Cache configuration

  - Key Configuration **You _Must_ use a [Hasher](docs/hasher.md) if your Key is not _String_**
  	  - Hashed Key:
  	  	 ```Java
	     builder = builder.useHashedKey(new MyHasher());
	     ```
	     
	  **[Hasher Details](docs/hasher.md)**

  - Ram Configuration
	  - Serializer in RAM:
	     ```Java
	     builder = builder.useSerializerInRam(MAX_SIZE, new YourClassSerializer());
	     ```

	  - Reference in RAM:
	     ```Java
	     builder = builder.useReferenceInRam(MAX_SIZE, new SizeOfYourClass());
	     ```

	  - No RAM:
	     ```Java
	     builder = builder.noRam();
	     ```
	     
	  **[SizeOf Details](docs/sizeof.md)**, **[Serializer Details](docs/serializer.md)**

  - Disk Configuration
	  - Serializer in Disk:
	     ```Java
	     builder = builder.useSerializerInDisk(MAX_SIZE, usePrivateFiles, new YourClassSerializer(), context);
	     builder = builder.useSerializerInDisk(MAX_SIZE, cacheFile, new YourClassSerializer());
	     ```

	  - No Disk:
	     ```Java
	     builder = builder.noDisk();
	     ```
	     
	  **[Serializer Details](docs/serializer.md)**

  - Volatile Configuration
	  - Volatile Entries:
	     ```Java
	     builder = builder.useVolatileCache(PERSISTENCE_TIME);
	     ```

  - Other
  	  - Logs:
  	     ```Java
	     builder = builder.enableLog();
	     ```

# Available operations
----------------------

 - Put
    To put an object into your cache, simply call `put` :

    ```Java
    cache.put("mykey", object); //Can be used in both cache types
    cache.put("mykey", object, time); //Just for VOLATILE caches
    ```

 - Get
    To get an object from your cache, simply call `get` :

    ```Java
    object = cache.get("mykey");
    ```

 - Contains
    You can check if cache has an entry with `contains` :

    ```Java
    boolean = cache.contains("mykey")
    ```

 - Delete
    To delete an object in cache call `delete` :

    ```Java
    cache.remove("mykey");
    ```

 - Invalidate
    To remove all cache data you can call on of the `ìnvalidate` methods :

    ```Java
    cache.clear();
    cache.clearRam();
    cache.clearDisk();
    ```

 - Configuration
    You can check how is your cache configured with `mode` methods :

    ```Java
    cache.getKeyMode();
    cache.getRamMode();
    cache.getDiskMode();
    cache.getPersistenceMode();
    ```
 
# CacheSerializer
 * [With GSON](docs/gson_cache_serializer.md)
 * [With LoganSquare](docs/logan_cache_serializer.md)
 * [With Jackson](docs/jackson_serializer.md)

# Pull Requests
I welcome and encourage all pull requests. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (on Mac) or Ctrl+Alt+L on Windows to reformat code with Android Studio defaults.
  2. Pull Request must pass all tests, you can check the style with `gradlew check` and run tests with `gradlew module:connectedAndroidTest` (Dualcache tests must be ran first)
  2. If its a feature, bugfix, or anything please only change code to what you specify.
  3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge.
  4. Pull requests _must_ be made against `develop` branch. Any other branch (unless specified by the maintainers) will get rejected.
  5. Have fun!

A test report will be available on your project subfolder once you execute the tests.

License
=======

    Copyright 2016 IagoCanalejas.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
