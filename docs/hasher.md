Android Dualcache Hasher
========================
[![API](https://img.shields.io/badge/API-12%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=12)
[![Build Status](https://travis-ci.org/iagocanalejas/dualcache.svg?branch=master)](https://travis-ci.org/iagocanalejas/dualcache)
[![](https://jitpack.io/v/iagocanalejas/dualcache.svg)](https://jitpack.io/#iagocanalejas/dualcache)


# Description
[Hasher<K>](dualcache/src/main/java/com/iagocanalejas/dualcache/interfaces/Hasher.java) is the interface you should implement if you want to use a custom object as cache key, instead of String.

**It's mandatory set a Hasher with `useHashedKey` method if your _KEY_ is not a String**


# Usage
Just needed to get a _unique_ String from your key object. You can do it as you wish.

  Some normal methods are:
  - Use an object unique field, if you have one.
  - Serialize your object.
  - Get a hashed string from your object.

	```java
	@Override
	public String hash(KEY_TYPE key) {
	    return string_key;
	}
	```

The library includes some hashing utilities in [Hashing](dualcache/src/main/java/com/iagocanalejas/dualcache/hashing/Hashing.java) class.
