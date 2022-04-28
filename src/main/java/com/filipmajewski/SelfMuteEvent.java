package com.filipmajewski;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;

public class SelfMuteEvent implements EventListener<VoiceStateUpdateEvent> {

    private final Container container;

    public SelfMuteEvent(Container container) {
        this.container = container;
    }

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    @Override
    public Mono<?> execute(VoiceStateUpdateEvent event) {

        VoiceChannel channel = event.getCurrent().getChannel().block();
        Member member = event.getCurrent().getMember().block();
        VoiceState oldState;
        VoiceState newState = event.getCurrent();

        if(event.getOld().isPresent()) {
            oldState = event.getOld().get();

            if(isTargetConnectedToVoiceChannel(channel) && member.getId().equals(container.getTargetUser().getId())) {
                if(!oldState.isSelfDeaf() && newState.isSelfDeaf()) {
                    System.out.println("Playing vent in");
                    container.getPlayerManager().loadItem(
                            container.getSoundEffectsMap().get("amongus_vent_in"),
                            container.getScheduler()
                    );
                } else if(oldState.isSelfDeaf() && !newState.isSelfDeaf()) {
                    System.out.println("Playing vent out");
                    container.getPlayerManager().loadItem(
                            container.getSoundEffectsMap().get("amongus_vent_out"),
                            container.getScheduler()
                    );
                }
            }
        } else Mono.error(new Throwable());

        return Mono.empty();
    }

    private boolean isTargetConnectedToVoiceChannel(VoiceChannel channel) {
        return channel.isMemberConnected(container.getTargetUser().getId()).block();
    }
}
