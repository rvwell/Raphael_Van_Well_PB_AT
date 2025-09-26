package com.infnet.pb.AT.controller;

import com.infnet.pb.AT.model.Resource;
import com.infnet.pb.AT.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.infnet.pb.AT.DTO.CreateResourceRequest;
import com.infnet.pb.AT.DTO.UpdateResourceRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getById(@PathVariable String id) {
        return resourceService.findById(UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> createResource(@RequestBody CreateResourceRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Resource resource = Resource.builder()
                .name(req.getName())
                .description(req.getDescription())
                .location(req.getLocation())
                .capacity(req.getCapacity())
                .build();
        Resource saved = resourceService.save(resource);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> updateResource(@PathVariable String id, @RequestBody UpdateResourceRequest req) {
        return resourceService.findById(UUID.fromString(id))
                .map(existing -> {
                    Resource data = Resource.builder()
                            .name(req.getName() != null ? req.getName() : existing.getName())
                            .description(req.getDescription() != null ? req.getDescription() : existing.getDescription())
                            .location(req.getLocation() != null ? req.getLocation() : existing.getLocation())
                            .capacity(req.getCapacity() != null ? req.getCapacity() : existing.getCapacity())
                            .build();
                    Resource updated = resourceService.update(existing.getId(), data);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable String id) {
        var uuid = UUID.fromString(id);
        if (resourceService.findById(uuid).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        resourceService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

}
