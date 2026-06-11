package com.videotogether.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAssistant(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        if (message == null) message = "";
        
        String lowerMsg = message.toLowerCase();
        String reply;
        
        if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
            reply = "Hello there! How can I help you with VideoTogether today?";
        } else if (lowerMsg.contains("create") || lowerMsg.contains("host") || lowerMsg.contains("start")) {
            reply = "To create a room, click the 'Create Room' button. You can then paste a YouTube URL or upload a local video file. You'll get a room name and a security key to share with your friends.";
        } else if (lowerMsg.contains("join") || lowerMsg.contains("guest")) {
            reply = "To join a room, click 'Join Room' on the home page. You will need the exact Room Name and the 8-character Security Key from the host.";
        } else if (lowerMsg.contains("upload") || lowerMsg.contains("file size") || lowerMsg.contains("limit")) {
            reply = "VideoTogether supports uploading large media files (up to 2000MB/2GB). The file is uploaded to the streaming server and streamed directly to your guests without them needing to download it.";
        } else if (lowerMsg.contains("sync") || lowerMsg.contains("drift")) {
            reply = "Playback is synced automatically. If you experience drift, simply click 'Play' or jump to a timestamp on the progress bar to force a re-sync for all guests.";
        } else if (lowerMsg.contains("youtube") || lowerMsg.contains("yt")) {
            reply = "You can play YouTube videos simply by pasting the YouTube URL in the 'Media Source' tab when creating a room. Ensure the video allows embedding.";
        } else if (lowerMsg.contains("frozen") || lowerMsg.contains("banned") || lowerMsg.contains("lock")) {
            reply = "If your account is frozen, you will not be able to join or create rooms. Please contact our support team to appeal this action.";
        } else {
            reply = "I'm a simple assistant. I can answer questions about creating rooms, joining rooms, syncing, file uploads, and YouTube playback. How can I help?";
        }

        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return ResponseEntity.ok(response);
    }
}
