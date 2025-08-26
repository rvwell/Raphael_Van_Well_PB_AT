package com.infnet.pb.AT.service;

import com.infnet.pb.AT.model.Resource;
import com.infnet.pb.AT.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;


    public List<Resource> findAll() {
        return resourceRepository.findAll();
    }


    public Resource save(Resource resource) {
        return resourceRepository.save(resource);
    }
}
