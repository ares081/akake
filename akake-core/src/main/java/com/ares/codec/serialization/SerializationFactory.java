package com.ares.codec.serialization;

import com.ares.common.exception.SerializationException;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationFactory {

  private static final ConcurrentHashMap<Byte, Serialization> SERIALIZE_MAP = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Serialization> SERIALIZE_NAME_MAP = new ConcurrentHashMap<>();

  static {
    Serialization hessian = new HessianSerialization();
    Serialization protobuf = new ProtoBufSerialization();

    SERIALIZE_MAP.put(hessian.getSerializationCode(), hessian);
    SERIALIZE_MAP.put(protobuf.getSerializationCode(), protobuf);

    SERIALIZE_NAME_MAP.put(hessian.getSerializationName(), hessian);
    SERIALIZE_NAME_MAP.put(protobuf.getSerializationName(), protobuf);
  }

  public static Serialization getSerialization(byte type) throws SerializationException {
    Serialization codec = SERIALIZE_MAP.get(type);
    if (codec == null) {
      throw new SerializationException("Serializer not found: " + type);
    }
    return codec;
  }

  public static Serialization getSerializationByName(String name) throws SerializationException {
    Serialization codec = SERIALIZE_NAME_MAP.get(name);
    if (codec == null) {
      throw new SerializationException("Serializer not found: " + name);
    }
    return codec;
  }

  public static void registerSerializer(Serialization serialization) {
    SERIALIZE_MAP.put(serialization.getSerializationCode(), serialization);
    SERIALIZE_NAME_MAP.put(serialization.getSerializationName(), serialization);
  }
}
