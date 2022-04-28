package com.filipmajewski;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

public class VoiceChannelJoinEvent implements EventListener<VoiceStateUpdateEvent>{

    private final Container container;
    private final AudioProvider provider;

    public VoiceChannelJoinEvent(Container container, AudioProvider provider) {
        this.container = container;
        this.provider = provider;
    }

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    @Override
    public Mono<?> execute(VoiceStateUpdateEvent event) {

        if(event.isJoinEvent()) {
            VoiceChannel channel = event.getCurrent().getChannel().block();
            Member member = event.getCurrent().getMember().block();
            boolean isMemberConnected = channel.isMemberConnected(container.getTargetUser().getId()).block();

            if(member.getId().equals(container.getTargetUser().getId())) {
                channel.join(spec -> spec.setProvider(provider)).block();
                container.getPlayerManager().loadItem(
                        container.getSoundEffectsMap().get("amongus_role_revel"),
                        container.getScheduler()
                );
                System.out.println(member.getDisplayName() + " joined channel \"" + channel.getName() +
                        "\". Playing amongus_role");
            } else if(!member.isBot()) {
                if(isMemberConnected) {
                    container.getPlayerManager().loadItem(
                            container.getSoundEffectsMap().get("amongus_crewmate_enter"),
                            container.getScheduler()
                    );
                }
            }
        }

        return Mono.empty();
    }
}
