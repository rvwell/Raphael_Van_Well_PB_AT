package com.infnet.pb.AT.controller;

import com.infnet.pb.AT.model.Room;
import com.infnet.pb.AT.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public Room createRoom(@RequestParam String name) {
        return roomService.createRoom(name);
    }

    @GetMapping
    public List<Room> listRooms() {
        return roomService.listRooms();
    }

    @PostMapping("/{roomId}/join/{userId}")
    public Room joinRoom(@PathVariable UUID roomId, @PathVariable UUID userId) {
        return roomService.joinRoom(roomId, userId);
    }
}
