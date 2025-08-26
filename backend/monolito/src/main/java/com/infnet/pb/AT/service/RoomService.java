package com.infnet.pb.AT.service;

import com.infnet.pb.AT.model.Room;
import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.repository.RoomRepository;
import com.infnet.pb.AT.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public Room createRoom(String name) {
        Room room = new Room();
        room.setName(name);
        return roomRepository.save(room);
    }

    public List<Room> listRooms() {
        return roomRepository.findAll();
    }

    public Room joinRoom(UUID roomId, UUID userId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        room.getParticipants().add(user);
        return roomRepository.save(room);
    }
}
