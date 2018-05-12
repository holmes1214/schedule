package com.evtape.schedule.service;

import com.evtape.schedule.exception.BaseException;
import com.evtape.schedule.util.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by holmes1214 on 28/11/2017.
 */
@Service
public class RedisService {
    @Autowired
    private JedisPool pool;

    private static Logger logger = LoggerFactory.getLogger(RedisService.class);

    public Jedis getConn() {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            logger.error("获取缓存连接出现异常", e);
            return null;
        }
        return jedis;
    }

    public void closeConn(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.close();
            } catch (Exception e) {
                logger.error("获取缓存连接出现异常", e);
            }
        }
    }

    public boolean exists(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.exists(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public Long sadd(String key, String... fields) {
        Jedis j = null;
        try {
            j = getConn();
            return j.sadd(key, fields);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public Long scard(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.scard(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public Long srem(String key, String field) {
        Jedis j = null;
        try {
            j = getConn();
            return j.srem(key, field);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public Set<String> smembers(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.smembers(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public boolean sismember(String key, String field) {
        Jedis j = null;
        try {
            j = getConn();
            return j.sismember(key, field);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public Long expire(String key, int seconds) {
        Jedis j = null;
        try {
            j = getConn();
            return j.expire(key, seconds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public void set(String key, String value) {
        Jedis j = null;
        try {
            j = getConn();
            j.set(key, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
    }

    public void setex(String key, String value, int seconds) {
        Jedis j = null;
        try {
            j = getConn();
            j.setex(key, seconds, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
    }

    public void hset(String key, String field, String value) {
        Jedis j = null;
        try {
            j = getConn();
            j.hset(key, field, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public void hset(String key, String field, String value, int seconds) {
        Jedis j = null;
        try {
            j = getConn();
            j.hset(key, field, value);
            j.expire(key, seconds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public String hget(String key, String field) {
        Jedis j = null;
        try {
            j = getConn();
            return j.hget(key, field);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public long hdel(String key, String... fields) {
        Jedis j = null;
        try {
            j = getConn();
            return j.hdel(key, fields);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public Map<String, String> hgetAll(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.hgetAll(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public boolean hexists(String key, String field) {
        Jedis j = null;
        try {
            j = getConn();
            return j.hexists(key, field);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public String get(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.get(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BaseException(ErrorCode.RedisError.value);
        } finally {
            closeConn(j);
        }
    }

    public List<String> get(String... key) {
        Jedis j = null;
        try {
            j = getConn();
            List<String> list = new ArrayList<String>();
            for (String k : key) {
                String v = j.get(k);
                if (StringUtils.isNotEmpty(v))
                    list.add(v);
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public void del(String key) {
        Jedis j = null;
        try {
            j = getConn();
            j.del(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
    }

    public void lpush(String key, String... values) {
        Jedis j = null;
        try {
            j = getConn();
            j.lpush(key, values);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
    }
    public void lrem(String key, String value) {
        Jedis j = null;
        try {
            j = getConn();
            j.lrem(key,0, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
    }

    public void rpush(String key,  String... values) {
        Jedis j = null;
        try {
            j = getConn();
            j.rpush(key, values);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
    }

    public List<String> lrange(String key, long start, long end) {
        Jedis j = null;
        try {
            j = getConn();
            List<String> list = j.lrange(key, start, end);
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public long llen(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.llen(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return -1;
    }

    public List<String> lpop(String key, int size) {
        Jedis j = null;
        List<String> list = new ArrayList<>(size);
        try {
            j = getConn();
            for (int i = 0; i < size; i++) {
                String str = j.lpop(key);
                if (str == null)
                    return list;
                list.add(str);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return list;
    }

    public List<String> rpop(String key, int size) {
        Jedis j = null;
        List<String> list = new ArrayList<>(size);
        try {
            j = getConn();
            for (int i = 0; i < size; i++) {
                String str = j.rpop(key);
                if (str==null){
                    return list;
                }
                list.add(str);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return list;
    }

    public Long counterIncr(String key, int expire) {
        Jedis j = null;
        try {
            Long count = null;
            j = getConn();
            count = j.incr(key);
            j.expire(key, expire);
            return count;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Long counterIncrBy(String key, Long value, int expire) {
        Jedis j = null;
        try {
            Long count = null;
            j = getConn();
            count = j.incrBy(key, value);
            if (count == 1) {
                j.expire(key, expire);
            }
            return count;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Long getTTL(String key) {
        Jedis j = null;
        try {
            Long expireSeconds = null;
            j = getConn();
            expireSeconds = j.ttl(key);
            return expireSeconds;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Long zadd(String key, Double score, String member) {
        Jedis j = null;
        try {
            j = getConn();
            Long zadd = j.zadd(key, score, member);
            return zadd;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Long zrank(String key, String member) {
        Jedis j = null;
        try {
            j = getConn();
            Long zrank = j.zrank(key, member);
            return zrank == null ? -1l : zrank;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return -1l;
    }

    public Set<Tuple> zrange(String key, Integer start, Integer stop) {
        Jedis j = null;
        try {
            j = getConn();
            Set<Tuple> value = j.zrangeWithScores(key, start, stop);
            return value;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }
    public Set<String> zRangeByScore(String key, double start, double stop) {
        Jedis j = null;
        try {
            j = getConn();
            Set<String> value = j.zrangeByScore(key, start, stop);
            return value;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public ScanResult<Tuple> zscan(String key, String cursor) {
        Jedis j = null;
        try {
            j = getConn();
            ScanResult<Tuple> zscan = j.zscan(key, cursor);
            return zscan;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Long zrem(String key, String... members) {
        Jedis j = null;
        try {
            j = getConn();
            Long rem = j.zrem(key, members);
            return rem;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Double zscore(String key, String member) {
        Jedis j = null;
        try {
            j = getConn();
            Double rem = j.zscore(key, member);
            return rem;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Double zincreby(String key, String member, double value) {
        Jedis j = null;
        try {
            j = getConn();
            Double rem = j.zincrby(key,value,member);
            return rem;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }

    public Long zcard(String key) {
        Jedis j = null;
        try {
            j = getConn();
            return j.zcard(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            closeConn(j);
        }
        return null;
    }
}
