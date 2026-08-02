package com.videotogether.controller;

import com.videotogether.model.Room;
import com.videotogether.repository.RoomRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Map<String, String> payload, Authentication authentication) {
        String name = payload.get("name");
        String securityKey = payload.get("securityKey");
        String mediaSource = payload.get("mediaSource");
        String ytId = payload.get("ytId");

        String hostUsername = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            hostUsername = authentication.getName();
        }

        Room room = new Room(name, securityKey, mediaSource, ytId, hostUsername);
        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.ok(savedRoom);
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms(Authentication authentication) {
        // Only allow ADMIN to fetch all rooms
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(roomRepository.findAllByOrderByCreatedAtDesc());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        roomRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
