package com.filipmajewski;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class BotConnectedEvent implements EventListener<ReadyEvent>{

    private final Container container;
    private final AudioProvider provider;
    private final Guild guild;

    public BotConnectedEvent(Container container, AudioProvider provider, Guild guild) {
        this.container = container;
        this.provider = provider;
        this.guild = guild;
    }

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<?> execute(ReadyEvent event) {
        List<GuildChannel> channelList = guild.getChannels().collectList().block();
        List<VoiceChannel> voiceChannelList = new ArrayList<>();

        if(channelList != null) {
            for(GuildChannel channel : channelList) {
                if(channel.getType() == Channel.Type.GUILD_VOICE) {
                    voiceChannelList.add((VoiceChannel) channel);
                }
            }

            for(VoiceChannel channel : voiceChannelList) {
                if(isTargetConnectedToVoiceChannel(channel)) {
                    channel.join(spec -> spec.setProvider(provider)).block();
                    container.getPlayerManager().loadItem(
                            container.getSoundEffectsMap().get("amongus_role_revel"),
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
