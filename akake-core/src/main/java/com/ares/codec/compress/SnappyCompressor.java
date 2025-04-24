package com.ares.codec.compress;

import com.ares.common.enums.CompressType;
import com.ares.common.exception.CompressException;
import java.io.IOException;
import org.xerial.snappy.Snappy;

public class SnappyCompressor implements Compressor {

  @Override
  public byte getCompressorType() {
    return CompressType.SNAPPY.getCode();
  }

  @Override
  public String getCompressorName() {
    return CompressType.SNAPPY.getName();
  }

  @Override
  public byte[] compress(byte[] data) throws CompressException {
    try {
      return Snappy.compress(data);
    } catch (IOException e) {
      throw new CompressException("Snappy compression failed", e);
    }
  }

  @Override
  public byte[] decompress(byte[] data) throws CompressException {
    try {
      return Snappy.uncompress(data);
    } catch (IOException e) {
      throw new CompressException("Snappy decompression failed", e);
    }
  }
}
