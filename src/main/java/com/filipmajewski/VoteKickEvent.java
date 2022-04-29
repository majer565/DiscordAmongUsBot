package com.filipmajewski;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class VoteKickEvent implements EventListener<MessageCreateEvent>{

    private final Container container;
    private final EmbedCreateSpec errorEmbed;

    public VoteKickEvent(Container container) {
        this.container = container;
        this.errorEmbed = EmbedCreateSpec.builder()
                .color(Color.of(255, 13, 13))
                .title("Error!")
                .author("DiscordAmongUsBot", "", "")
                .description("There is less than 5 people in the voice channel to make that action.")
                .build();
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        if(event.getMessage().getContent().equals("!kick_impostor")) {
            Guild guild = event.getGuild().block();
            List<GuildChannel> channels = guild.getChannels().collectList().block();
            List<VoiceChannel> voiceChannelList = new ArrayList<>();
            Member targerMember = event.getMember().get();

            for(GuildChannel channel : channels) {
                if(channel.getType() == Channel.Type.GUILD_VOICE) {
                    voiceChannelList.add((VoiceChannel) channel);
                }
            }

            for(VoiceChannel channel : voiceChannelList) {
                List<VoiceState> members = channel.getVoiceStates().collectList().block();

                if(isTargetConnectedToVoiceChannel(channel)) {
                    if(members.size() >= 5) {
                        container.getPlayerManager().loadItem(
                                container.getSoundEffectsMap().get("amongus_vote_locking"),
                                container.getScheduler()
                        );
                        Container.kickCount++;

                        if(Container.kickCount == members.size() - 2) {
                            System.out.println("Kicking target " + targetMember.getDisplayName());
                            Container.kickCount = 0;
                            targetMember.edit().withNewVoiceChannelOrNull(null).block();
                        }
                    } else {
                        event.getMessage().getChannel().flatMap(textChannel -> textChannel.createMessage(errorEmbed)).block();
                    }
                }
            }
        }

        return Mono.empty();
    }

    private boolean isTargetConnectedToVoiceChannel(VoiceChannel channel) {
        return channel.isMemberConnected(container.getTargetUser().getId()).block();
    }
}
