Android Dualcache SizeOf
========================
[![API](https://img.shields.io/badge/API-12%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=12)
[![Build Status](https://travis-ci.org/iagocanalejas/dualcache.svg?branch=master)](https://travis-ci.org/iagocanalejas/dualcache)
[![](https://jitpack.io/v/iagocanalejas/dualcache.svg)](https://jitpack.io/#iagocanalejas/dualcache)


# Description
[SizeOf<V>](dualcache/src/main/java/com/iagocanalejas/dualcache/interfaces/SizeOf.java) is the interface you should implement if you want to use the _RAM_ cache.

# Usage
You can handle the Size computing in two ways.

  1. Entries Count

      Your sizeOf count one for each cache entrie so, when you call `useReferenceInRam(RAM_MAX_SIZE, new SizeOfYourClass())` **RAM_MAX_SIZE** is the number of entries your cache can handle in RAM.

      Overridden method for this type looks like:
      ```java
      @Override
      public class SizeOfMyObject implements SizeOf<YOUR_CLASS> {
         public int sizeOf(YOUR_CLASS object) {
             return 1;
         }
      }
      ```

  2. Bytes size

      Your sizeOf count the number of bytes your object take. in this case your **RAM_MAX_SIZE** is the number of bytes your cache can use in RAM.

      Implement this tipe of sizeOf is harder but nomally you can use the basic types size like:
      ```java
      public class MyObject {
          public int id;
          public String name;
      }

      public class SizeOfMyObject implements SizeOf<MyObject> {

          @Override
          public int sizeOf(MyObject object) {
              return (Integer.SIZE + object.name.length() * Char.SIZE) / Byte.SIZE;
          }

      }
      ```