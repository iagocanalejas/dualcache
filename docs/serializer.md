Android Dualcache Serializer
========================
[![API](https://img.shields.io/badge/API-12%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=12)
[![Build Status](https://travis-ci.org/iagocanalejas/dualcache.svg?branch=master)](https://travis-ci.org/iagocanalejas/dualcache)
[![](https://jitpack.io/v/iagocanalejas/dualcache.svg)](https://jitpack.io/#iagocanalejas/dualcache)


# Description
[Serializer<V>](dualcache/src/main/java/com/iagocanalejas/dualcache/interfaces/Serializer.java) is the interface you should implement so the cache can candle serialized in RAM or serialized in Disk entries.

# Usage
This interface is required so cache can serialize and deserialize object.

  ```java
  public class MyObject {
      public int id;
      public String name;
  }

  public class MyObjectSerializer implements Serializer<MyObject> {

      @Override
      public MyObject fromString(String data) {
          return your_deserialized_object;
      }

      @Override
      public String toString(MyObject object) {
          return your_serialized_object;
      }

  }
  ```

I have also done some libraries for help with the serialization of basic objects:
  * [With GSON](docs/gson_cache_serializer.md)
  * [With LoganSquare](docs/logan_cache_serializer.md)
  * [With Jackson](docs/jackson_serializer.md)