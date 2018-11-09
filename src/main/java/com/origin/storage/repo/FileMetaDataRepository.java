package com.origin.storage.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.origin.storage.model.FileMetadata;

public interface FileMetaDataRepository extends MongoRepository<FileMetadata, String> {

	public FileMetadata findByUserId(String userId);
	
	public FileMetadata findByName(String name);
	
	public Optional<FileMetadata> findById(String id);
	
}
