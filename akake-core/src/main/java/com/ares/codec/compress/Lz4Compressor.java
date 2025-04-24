package com.ares.codec.compress;

import com.ares.common.enums.CompressType;
import com.ares.common.exception.CompressException;
import net.jpountz.lz4.LZ4Factory;

public class Lz4Compressor implements Compressor {

  @Override
  public byte getCompressorType() {
    return CompressType.LZ4.getCode();
  }

  @Override
  public String getCompressorName() {
    return CompressType.LZ4.getName();
  }

  @Override
  public byte[] compress(byte[] data) throws CompressException {
    return LZ4Factory.fastestInstance().fastCompressor().compress(data);
  }

  @Override
  public byte[] decompress(byte[] data) throws CompressException {
    return LZ4Factory.fastestInstance().fastDecompressor().decompress(data, data.length);
  }
}
