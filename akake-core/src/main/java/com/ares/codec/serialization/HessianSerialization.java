package com.ares.codec.serialization;

import com.ares.common.enums.SerializationType;
import com.ares.common.exception.SerializationException;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class HessianSerialization implements Serialization {

  @Override
  public byte[] serialize(Object obj) throws SerializationException {
    if (obj == null) {
      return new byte[0];
    }

    if (obj instanceof byte[]) {
      return (byte[]) obj;
    }
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      HessianOutput output = new HessianOutput(baos);
      output.writeObject(obj);
      output.flush();
      return baos.toByteArray();
    } catch (Exception e) {
      throw new SerializationException("Failed to serialize object of type: " + obj.getClass(), e);
    }
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializationException {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    try (ByteArrayInputStream baos = new ByteArrayInputStream(bytes)) {
      HessianInput input = new HessianInput(baos);
      Object obj = input.readObject();
      return clazz.cast(obj);
    } catch (Exception e) {
      throw new SerializationException("Failed to deserialize object of type: " + clazz, e);
    }
  }

  @Override
  public byte getSerializationCode() {
    return SerializationType.HESSIAN.getCode();
  }

  @Override
  public String getSerializationName() {
    return SerializationType.HESSIAN.getName();
  }
}
