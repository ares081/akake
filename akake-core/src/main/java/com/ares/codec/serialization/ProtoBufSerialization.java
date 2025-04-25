package com.ares.codec.serialization;

import com.ares.common.enums.SerializationType;
import com.ares.common.exception.SerializationException;
import com.google.protobuf.MessageLite;
import java.lang.reflect.Method;

public class ProtoBufSerialization implements Serialization {

  @Override
  public byte[] serialize(Object obj) throws SerializationException {
    if (obj == null) {
      throw new NullPointerException();
    }
    try {
      if (!(obj instanceof MessageLite)) {
        throw new SerializationException("Object to serialize must be MessageLite");
      }
      return ((MessageLite) obj).toByteArray();
    } catch (Exception e) {
      throw new SerializationException("Protobuf serialize error", e);
    }
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
    if (bytes == null) {
      throw new NullPointerException();
    }
    try {
      Method method = clazz.getMethod("parseFrom", byte[].class);
      return clazz.cast(method.invoke(null, bytes));
    } catch (Exception e) {
      throw new SerializationException("Protobuf deserialize error", e);
    }
  }

  @Override
  public byte getSerializationCode() {
    return SerializationType.PROTOBUF.getCode();
  }

  @Override
  public String getSerializationName() {
    return SerializationType.PROTOBUF.getName();
  }

}
