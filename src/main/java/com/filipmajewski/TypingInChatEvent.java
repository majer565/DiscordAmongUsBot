package com.filipmajewski;

import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class TypingInChatEvent implements EventListener<TypingStartEvent> {

    private final Container container;

    public TypingInChatEvent(Container container) {
        this.container = container;
    }

    @Override
    public Class<TypingStartEvent> getEventType() {
        return TypingStartEvent.class;
    }

    @Override
    public Mono<?> execute(TypingStartEvent event) {
        User typingUser = event.getUser().block();

        if(typingUser.equals(container.getTargetUser())) {
            container.getPlayerManager().loadItem(
                    container.getSoundEffectsMap().get("amongus_eject"),
                    container.getScheduler()
            );
        }
        return Mono.empty();
    }
}
