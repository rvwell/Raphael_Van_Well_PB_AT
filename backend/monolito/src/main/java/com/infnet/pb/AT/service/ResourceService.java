package com.infnet.pb.AT.service;

import com.infnet.pb.AT.model.Resource;
import com.infnet.pb.AT.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;

    public List<Resource> findAll() {
        return resourceRepository.findAll();
    }

    public Optional<Resource> findById(UUID id) {
        return resourceRepository.findById(id);
    }

    public Resource save(Resource resource) {
        return resourceRepository.save(resource);
    }

    public void deleteById(UUID id) {
        resourceRepository.deleteById(id);
    }

    public Resource update(UUID id, Resource data) {
        Resource existing = resourceRepository.findById(id).orElseThrow();
        existing.setName(data.getName());
        existing.setDescription(data.getDescription());
        existing.setLocation(data.getLocation());
        existing.setCapacity(data.getCapacity());
        return resourceRepository.save(existing);
    }
}
