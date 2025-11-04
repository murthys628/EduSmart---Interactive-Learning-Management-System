package com.edusmart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CacheMonitorService
{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ✅ Count all cached entries
    public long getCacheEntryCount()
    {
        Set<String> keys = redisTemplate.keys("*");
        return keys != null ? keys.size() : 0;
    }

    // ✅ Check if cache is active
    public boolean isCacheAvailable()
    {
        try
        {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        }
        
        catch (Exception e)
        {
            return false;
        }
    }
}