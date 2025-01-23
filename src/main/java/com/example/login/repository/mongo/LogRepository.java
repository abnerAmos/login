package com.example.login.repository.mongo;

import com.example.login.model.collection.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<LogEntry, String> {
}
