package com.infnet.pb.AT.repository;

import com.infnet.pb.AT.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

}
