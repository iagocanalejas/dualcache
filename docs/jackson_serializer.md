[![Build Status](https://travis-ci.org/FasterXML/jackson-databind.svg?branch=master)](https://travis-ci.org/FasterXML/jackson-databind) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fasterxml.jackson.core/jackson-databind/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.fasterxml.jackson.core/jackson-databind)

# Serializer example using [Jackson](https://github.com/FasterXML/jackson-databind)

```ruby
dependencies {
  com.fasterxml.jackson.core:jackson-databind:<VERSION>
}
```

```java
public class JsonSerializer<T> implements CacheSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T> clazz;

    public JsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public T fromString(String data) {
        try {
            return mapper.readValue(data, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }
}
```
