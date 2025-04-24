package com.ares.codec.compress;

import com.ares.common.exception.CompressException;

public interface Compressor {

  byte getCompressorType();

  String getCompressorName();

  byte[] compress(byte[] data) throws CompressException;

  byte[] decompress(byte[] data) throws CompressException;
}
