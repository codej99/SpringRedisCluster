package com.redis.cluster.repo.redis;

import com.redis.cluster.entity.redis.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRedisRepo extends CrudRepository<Student, Long> {
}

