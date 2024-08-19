package com.example.pinokkio.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.Set;


@Slf4j
@RequiredArgsConstructor
@Service
public class RedisUtil {

    private final StringRedisTemplate template;

    public String getData(String key) {
        // Redis에서 주어진 키에 대한 값을 가져오는 메소드
        ValueOperations<String, String> valueOperations = template.opsForValue();
        // 키에 해당하는 값을 반환
        return valueOperations.get(key);
    }

    public boolean existData(String key) {
        // 주어진 키가 Redis 에 존재하는지 여부를 확인하는 메소드
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public void setDataExpire(String key, String value, long duration) {
        try {
            // Redis 에 데이터를 저장하고 만료 시간을 설정할 때 로그 출력
            log.info("[setDataExpire] 키: {}, 값: {}", key, value);
            ValueOperations<String, String> valueOperations = template.opsForValue();
            // 지속 시간을 분 단위로 설정
            Duration expireDuration = Duration.ofMinutes(duration);
            // 키와 값, 만료 시간을 설정
            valueOperations.set(key, value, expireDuration);
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 출력
            System.err.println("Redis 에 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteData(String key) {
        // 주어진 키에 해당하는 데이터를 Redis 에서 삭제하는 메소드
        template.delete(key);
    }

    // 주어진 패턴에 맞는 모든 키를 삭제하는 메소드
    public void deleteDataByPattern(String pattern) {
        // 패턴에 맞는 모든 키를 가져옴
        Set<String> keys = template.keys(pattern);
        if (keys != null) {
            // 가져온 키를 삭제
            template.delete(keys);
        }
    }

    // Hash 관련 메소드 추가
    public void hset(String key, String hashKey, String value) {
        try {
            // Redis 해시에 값을 저장할 때 로그 출력
            log.info("[hset] 키: {}, 해시 키: {}, 값: {}", key, hashKey, value);
            HashOperations<String, String, String> hashOperations = template.opsForHash();
            // 지정된 키와 해시 키에 값을 저장
            hashOperations.put(key, hashKey, value);
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 출력
            System.err.println("Redis 해시에 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, String> hgetAll(String key) {
        // 주어진 키에 대한 모든 해시 항목을 가져오는 메소드
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        // 해시의 모든 항목을 Map 형태로 반환
        return hashOperations.entries(key);
    }

    public void expire(String key, long duration) {
        try {
            // 키의 만료 시간을 설정할 때 로그 출력
            log.info("[expire] 키: {}, 지속 시간(초): {}", key, duration);
            // 주어진 키에 대해 만료 시간을 설정
            template.expire(key, Duration.ofSeconds(duration));
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 출력
            System.err.println("키의 만료 시간 설정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}