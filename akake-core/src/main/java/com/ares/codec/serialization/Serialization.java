package com.ares.codec.serialization;

import com.ares.common.exception.SerializationException;

public interface Serialization {

  byte getSerializationCode();

  String getSerializationName();

  byte[] serialize(Object obj) throws SerializationException;

  <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException;
}
