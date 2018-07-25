package com.king.learn.collection.mycollection.bloomfilter.demo12.redis.helper;

import com.king.learn.collection.mycollection.bloomfilter.demo12.FilterBuilder;
import com.king.learn.collection.mycollection.bloomfilter.demo12.HashProvider;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the Redis keys for the Redis Bloom Filters
 */
public class RedisKeys {

    //Redis key constants
    public static final String N_KEY = "counter";
    public static final String M_KEY = "bitSetSize";
    public static final String K_KEY = "funtions";
    public static final String C_KEY = "c";
    public static final String P_KEY = "p";
    public static final String HASH_METHOD_KEY = "hashmethod";
    public final String BITS_KEY;
    public final String COUNTS_KEY;
    public final String TTL_KEY;
    public final String EXPIRATION_QUEUE_KEY;

    public RedisKeys(String instanceName) {
        this.BITS_KEY = instanceName + ":bits";
        this.COUNTS_KEY = instanceName + ":counts";
        this.TTL_KEY = instanceName + ":ttl";
        this.EXPIRATION_QUEUE_KEY = instanceName + ":queue";
    }


    public FilterBuilder persistConfig(RedisPool pool, FilterBuilder builder) {
        return pool.safelyReturn(jedis -> {
            FilterBuilder newConfig = null;
            //Retry on concurrent changes
            while (newConfig == null) {
                if (!builder.overwriteIfExists() && jedis.exists(builder.name())) {
                    newConfig = this.parseConfigHash(jedis.hgetAll(builder.name()), builder.name(), pool);
                } else {
                    Map<String, String> hash = this.buildConfigHash(builder);
                    jedis.watch(builder.name());
                    Transaction t = jedis.multi();
                    hash.forEach((k, v) -> t.hset(builder.name(), k, v));
                    if (t.exec() != null) {
                        newConfig = builder;
                    }
                }
            }
            return newConfig;
        });
    }

    public Map<String, String> buildConfigHash(FilterBuilder config) {
        Map<String, String> map = new HashMap<>();
        map.put(P_KEY, String.valueOf(config.falsePositiveProbability()));
        map.put(M_KEY, String.valueOf(config.size()));
        map.put(K_KEY, String.valueOf(config.hashes()));
        map.put(N_KEY, String.valueOf(config.expectedElements()));
        map.put(C_KEY, String.valueOf(config.countingBits()));
        map.put(HASH_METHOD_KEY, config.hashMethod().name());
        return map;
    }

    public FilterBuilder parseConfigHash(Map<String, String> map, String name, RedisPool pool) {
        FilterBuilder config = new FilterBuilder();
        config.name(name);
        config.pool(pool);
        config.falsePositiveProbability(Double.valueOf(map.get(P_KEY)));
        config.size(Integer.valueOf(map.get(M_KEY)));
        config.hashes(Integer.valueOf(map.get(K_KEY)));
        config.expectedElements(Integer.valueOf(map.get(N_KEY)));
        config.countingBits(Integer.valueOf(map.get(C_KEY)));
        config.hashFunction(HashProvider.HashMethod.valueOf(map.get(HASH_METHOD_KEY)));
        config.complete();
        return config;
    }

}
