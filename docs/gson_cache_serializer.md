[![Build Status](https://travis-ci.org/google/gson.svg?branch=master)](https://travis-ci.org/google/gson)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.google.code.gson/gson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.google.code.gson/gson)

# Serializer example using [Gson](https://github.com/google/gson)

```ruby
dependencies {
  compile "com.google.code.gson:gson:<VERSION>"
}
```

```java
public class GsonSerializer<T> implements CacheSerializer<T> {

    private static Gson sGson;
    private final Class<T> mClazz;

    static {
        sGson = new Gson();
    }

    public GsonSerializer(Class<T> clazz){
        this.mClazz = clazz;
    }

    @Override
    public T fromString(String data) {
        return sGson.fromJson(data, mClazz);
    }

    @Override
    public String toString(T object) {
        return sGson.toJson(object, mClazz);
    }

}
```
