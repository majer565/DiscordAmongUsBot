package com.filipmajewski;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.core.object.entity.User;

import java.util.Map;

public class Container {
    private final AudioPlayerManager playerManager;
    private final TrackScheduler scheduler;
    private final Map<String, String> soundEffects;
    private final User targetUser;
    public static int kickCount = 0;


    public Container(AudioPlayerManager playerManager, TrackScheduler scheduler, Map<String, String> soundEffects, User targetUser) {
        this.playerManager = playerManager;
        this.scheduler = scheduler;
        this.soundEffects = soundEffects;
        this.targetUser = targetUser;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public Map<String, String> getSoundEffectsMap() {
        return soundEffects;
    }

    public User getTargetUser() {
        return targetUser;
    }
}
