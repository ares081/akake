package com.ares.common.pool;

public record PoolConfigProperties(int minSize, int maxSize, long maxIdleTime,
        long validationInterval, long maxLifetime) {

}
