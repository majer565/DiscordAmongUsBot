package com.filipmajewski;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

public class VoiceChannelLeaveEvent implements EventListener<VoiceStateUpdateEvent>{

    private final Container container;

    public VoiceChannelLeaveEvent(Container container) {
        this.container = container;
    }

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    @Override
    public Mono<?> execute(VoiceStateUpdateEvent event) {

        if(event.isLeaveEvent()) {
            Member member = event.getCurrent().getMember().block();
            VoiceChannel oldChannel = event.getOld().get().getChannel().block();
            List<VoiceState> memberList = oldChannel.getVoiceStates().collectList().block();

            if(member.getId().equals(container.getTargetUser().getId())) {
                container.getPlayerManager().loadItem(
                        container.getSoundEffectsMap().get("amongus_crewmate_win"),
                        container.getScheduler()
                );
            } else if(!member.isBot()) {
                System.out.println("????");
                if(isTargetConnectedToVoiceChannel(oldChannel) && memberList.size() >= 3) {
                    Random randomNumber = new Random();
                    int killSound = randomNumber.nextInt(2);

                    if(killSound == 0) {
                        container.getPlayerManager().loadItem(
                                container.getSoundEffectsMap().get("amongus_kill"),
                                container.getScheduler()
                        );
                    } else {
                        container.getPlayerManager().loadItem(
                                container.getSoundEffectsMap().get("amongus_stab_kill"),
                                    container.getScheduler()
                        );
                    }
                } else {
                    container.getPlayerManager().loadItem(
                            container.getSoundEffectsMap().get("amongus_impostor_win"),
                            container.getScheduler()
                    );
                }
            }
        }

        return Mono.empty();
    }

    private boolean isTargetConnectedToVoiceChannel(VoiceChannel channel) {
        return channel.isMemberConnected(container.getTargetUser().getId()).block();
    }
}
