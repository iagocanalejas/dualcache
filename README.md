Android Dualcache
=================
[![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)
[![Build Status](https://travis-ci.org/vincentbrison/dualcache.svg?branch=master)](https://travis-ci.org/vincentbrison/dualcache)
[![](https://jitpack.io/v/iagocanalejas/dualcache.svg)](https://jitpack.io/#iagocanalejas/dualcache)

**LIBRARY BASED IN [dualcache](https://github.com/vincentbrison/dualcache)**

# Added 
 - The option to set a lifetime to entries

# Description
This android library provide a cache with 2 layers, one in RAM in top of one on local storage.
Caches can be configured to have a persistence time
This library is highly configurable :


| Configurations | Disk : `Specific serializer` | Disk : `disable` |
| -------------- | -------------------------- | ---------------- |
| Ram : `Volatil option` | YES | YES |
| Ram : `Specific serializer` | YES | YES |
| Ram : `References` | YES | YES |
| Ram : `disable` | YES | NO |

 - `Volatile option` : the object have maximun lifetime
 - `Specific serializer` : the object stored in cache will be serialized through a serializer provided by yourself.
 - `References` : the objects stored in Ram are cached through there references (no serialization is done).
 - `Disable` : the corresponding layer (Ram or disk) is disable.

If you work with `specific serializer` or `references` you will have to provide (through an interface) the
way of compute the size of cached objects, to be able to correctly execute the [LRU policy] (http://en.wikipedia.org/wiki/Cache_algorithms).

If you do not want to write your own serializer and a json serializer is enough for you, you can use
`vincentbrison/dualcache-jsonserializer` which will serialize object using [Jackson](https://github.com/FasterXML/jackson-databind)

```ruby
 compile 'com.vincentbrison.openlibraries.android:dualcache-jsonserializer:3.1.1'
```

For a better performance, I recommend that you use larger size for the disk layer than for the Ram layer. When you try to get an object from the cache which is already in the Ram layer, the disk wont be use to keep the best performance from the Ram. If you try to get an object from the cache which is on disk and not on Ram, the object will be loaded into RAM, to ensure better further access time.

To see a deply description for base dualcache work flow visit [dualcache](https://github.com/vincentbrison/dualcache).

The Philosophy behind this library
==================================
When you want to use a [cache] (http://en.wikipedia.org/wiki/Cache_\(computing\)) on Android today, you have two possibilities. You whether use :
 - The [LruCache] (http://developer.android.com/reference/android/util/LruCache.html) included into the Android SDK.
 - The [DiskLruCache] (https://github.com/JakeWharton/DiskLruCache) of Jake Wharton.

The thing is the first one only works in RAM, and the second one only on disk (internal memory of the phone). So you need to choose
whether if you will use the [LruCache] (http://developer.android.com/reference/android/util/LruCache.html) (RAM) :
 - Very fast access to your cache.
 - High resources constraints, since the RAM allocated to your application is used for caching.
 - Not persistent among different execution of your app.

Or you will use the [DiskLruCache] (https://github.com/JakeWharton/DiskLruCache) (Disk) :
 - Slower access time than the LruCache.
 - Almost no resources constraints, since the size used on the disk (internal memory), will not impact your application.
 - Persistent among different execution of your app.

The purpose of this library is to provide both features of these two caches, by making them working together. You do not need
to ask yourself anymore "Should I use this one or this one ? But this one is persistent, but the other one is faster...".
With this library you only use one cache, with two layers, one in RAM, and one in Disk and you configure how they have to work
to provide exactly what you need in term of caching for you application.

Setup
=====

- Ensure you can pull artifacts from Maven Central :
```gradle
repositories {
    mavenCentral()
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
    compile 'com.vincentbrison.openlibraries.android:dualcache:3.1.1'

    //compile 'com.vincentbrison.openlibraries.android:dualcache-jsonserializer:3.1.1' // If you
    // want a ready to use json serializer
}
```

All the configuration of the cache is done when you are building the cache through its `Builder` class.

Basic examples
==============

Build your cache
---------------
 First of all, you need to build you cache, through the `Builder` class.
 1. A cache with a serializer for RAM and disk disable :

```Java
cache = new Builder<>(CACHE_NAME, TEST_APP_VERSION, AbstractVehicule.class)
    .enableLog()
    .useSerializerInRam(RAM_MAX_SIZE, new SerializerForTesting())
    .noDisk()
    .build();
```

 2. A cache with references in RAM and a default serializer on disk :

```Java
cache = new Builder<>(CACHE_NAME, TEST_APP_VERSION, AbstractVehicule.class)
    .enableLog()
    .useReferenceInRam(RAM_MAX_SIZE, new SizeOfVehiculeForTesting())
    .useSerializerInDisk(DISK_MAX_SIZE, true, new DualCacheTest.SerializerForTesting(), getContext())
    .build();
```
You can note that when you build the cache, you need to provide an `app version` number. When the cache
is loaded, if data exist with a inferior number, it will be invalidate. It can be extremely useful when
you update your app, and change your model, to avoid crashes. This feature is possible because the DiskLruCache of Jake Wharton
implemented this feature.

Put
---
To put an object into your cache, simply call `put` :

```Java
DummyClass object = new DummyClass();
object = cache.put("mykey", object);
```

Get
---
To get an object from your cache, simply call `get` :

```Java
DummyClass object = null;
object = cache.get("mykey");
```

Use cases
=========
 - Using default serialization on RAM and on disk can be very useful for caching network exchange of data.
 - Using references in RAM and serialization on disk can be very useful to cache bitmaps.

Javadoc
=======
The javadoc provided with this library is fully written and released on Maven.

Testing
=======
All the configurations of the cache are (almost) fully tested through automated tests. If you fork
this repo, you can launch them with the gradle command `connectedAndroidTest`.
You need to have a device connected since the tests will be run on every device connected to your computer.
An emulator or a [GenyMotion] instance is enough.
A report will be available at : `/{location of your fork}/lib/build/outputs/reports/androidTests/connected/index.html`

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
