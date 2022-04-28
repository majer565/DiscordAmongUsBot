package com.filipmajewski;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public class ContainSusMessageEvent implements EventListener<MessageCreateEvent>{

    private final Container container;
    private final Member targetMember;

    public ContainSusMessageEvent(Container container, Member targetMember) {
        this.container = container;
        this.targetMember = targetMember;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        String lastMessage = event.getMessage().getContent();
        char[] charList = lastMessage.toCharArray();
        Member eventMember = event.getMember().get();

        if(eventMember.equals(targetMember)) {
            if(charList.length > 2) {
                char firstChar, secondChar, thirdChar;
                for(int i = 2; i < charList.length; i++) {
                    firstChar = charList[i-2];
                    secondChar = charList[i-1];
                    thirdChar = charList[i];
                    if(firstChar == 's' && secondChar == 'u' && thirdChar == 's') {
                        container.getPlayerManager().loadItem(
                                container.getSoundEffectsMap().get("amongus_sus"),
                                container.getScheduler()
                        );
                    }
                }
            }
        }

        return Mono.empty();
    }
}