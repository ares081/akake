package com.ares.codec.compress;

import com.ares.common.enums.CompressType;
import java.util.concurrent.ConcurrentHashMap;

public class CompressorFactory {

  private static final ConcurrentHashMap<Byte, Compressor> COMPRESSORS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Compressor> COMPRESSORS_NAME = new ConcurrentHashMap<>();

  static {
    Compressor lz4Compressor = new Lz4Compressor();
    Compressor snnapyCompressor = new SnappyCompressor();
    COMPRESSORS.put(CompressType.LZ4.getCode(), lz4Compressor);
    COMPRESSORS.put(CompressType.SNAPPY.getCode(), snnapyCompressor);
    COMPRESSORS_NAME.put(CompressType.LZ4.getName(), lz4Compressor);
    COMPRESSORS_NAME.put(CompressType.SNAPPY.getName(), snnapyCompressor);
  }

  public static Compressor getCompressor(Byte type) {
    return COMPRESSORS.get(type);
  }

  public static Compressor getCompressorByName(String name) {
    return COMPRESSORS_NAME.get(name);
  }

  public static void registerCompressor(Compressor compressor) {
    COMPRESSORS.put(compressor.getCompressorType(), compressor);
    COMPRESSORS_NAME.put(compressor.getCompressorName(), compressor);
  }
}
