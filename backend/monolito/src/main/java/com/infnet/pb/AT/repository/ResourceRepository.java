package com.infnet.pb.AT.repository;

import com.infnet.pb.AT.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
}
