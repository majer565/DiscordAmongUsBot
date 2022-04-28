package com.filipmajewski;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordBot {

    private static final String TOKEN = "PASTE YOUR BOT TOKEN HERE";
    private static final Snowflake guildToTargetID = Snowflake.of("PASTE GUILD TO TARGET ID HERE");
    private static final Snowflake userToTargetID = Snowflake.of("PASTE USER TO TARGET ID HERE");
    private static final Map<String, Command> commands = new HashMap<>();
    private static final Map<String, String> soundEffects = new HashMap<>();

    static {

        soundEffects.put(JSONSoundsReader("body_report.json").getName(), JSONSoundsReader("body_report.json").getPath());
        soundEffects.put(JSONSoundsReader("crewmate_enter.json").getName(), JSONSoundsReader("crewmate_enter.json").getPath());
        soundEffects.put(JSONSoundsReader("crewmate_win.json").getName(), JSONSoundsReader("crewmate_win.json").getPath());
        soundEffects.put(JSONSoundsReader("eject.json").getName(), JSONSoundsReader("eject.json").getPath());
        soundEffects.put(JSONSoundsReader("emergency_meeting.json").getName(), JSONSoundsReader("emergency_meeting.json").getPath());
        soundEffects.put(JSONSoundsReader("impostor_win.json").getName(), JSONSoundsReader("impostor_win.json").getPath());
        soundEffects.put(JSONSoundsReader("kill.json").getName(), JSONSoundsReader("kill.json").getPath());
        soundEffects.put(JSONSoundsReader("role_revel.json").getName(), JSONSoundsReader("role_revel.json").getPath());
        soundEffects.put(JSONSoundsReader("stab_kill.json").getName(), JSONSoundsReader("stab_kill.json").getPath());
        soundEffects.put(JSONSoundsReader("sus.json").getName(), JSONSoundsReader("sus.json").getPath());
        soundEffects.put(JSONSoundsReader("vent_in.json").getName(), JSONSoundsReader("vent_in.json").getPath());
        soundEffects.put(JSONSoundsReader("vent_out.json").getName(), JSONSoundsReader("vent_out.json").getPath());
        soundEffects.put(JSONSoundsReader("vote_locking.json").getName(), JSONSoundsReader("vote_locking.json").getPath());

    }

    public static void main(String[] args){
        final GatewayDiscordClient client = DiscordClientBuilder.create(TOKEN).build().login().block();
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        final AudioPlayer player = playerManager.createPlayer();
        final TrackScheduler scheduler = new TrackScheduler(player);
        final Guild guildToTarget = client.getGuildById(guildToTargetID).block();
        final User targetUser = client.getUserById(userToTargetID).block();
        final Container container = new Container(playerManager, scheduler, soundEffects, targetUser);
        final Member targetMember = client.getMemberById(guildToTargetID, userToTargetID).block();

        AudioProvider provider = new LavaPlayerAudioProvider(player);
        DiscordBot.audioSetup(playerManager);

        DiscordBot.register(client, new BotConnectedEvent(container, provider, guildToTarget));
        DiscordBot.register(client, new VoiceChannelJoinEvent(container, provider));
        DiscordBot.register(client, new VoiceChannelLeaveEvent(container));
        DiscordBot.register(client, new TypingInChatEvent(container));
        DiscordBot.register(client, new SelfMuteEvent(container));
        DiscordBot.register(client, new VoteKickEvent(container));
        DiscordBot.register(client, new ContainSusMessageEvent(container, targetMember));

        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
            final String content = event.getMessage().getContent();

            for(final Map.Entry<String, Command> entry : commands.entrySet()) {
                if(content.startsWith("!" + entry.getKey())) {
                    entry.getValue().execute(event);
                    break;
                }
            }
        });

        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }});

        commands.put("disconnect", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final Snowflake guildID = member.getGuildId();
                client.getVoiceConnectionRegistry().disconnect(guildID).block();
            }});

        commands.put("play", event -> {
            final String content = event.getMessage().getContent();
            final List<String> command = Arrays.asList(content.split(" "));
            playerManager.loadItem(command.get(1), scheduler);
            System.out.println("Playing " + content);
            System.out.println(command);
        });

        createSoundEffectCommand("amongus_crewwin", "amongus_crewmate_win", playerManager, scheduler);
        createSoundEffectCommand("amongus_reportbody", "amongus_body_report", playerManager, scheduler);
        createSoundEffectCommand("amongus_crewenter", "amongus_crewmate_enter", playerManager, scheduler);
        createSoundEffectCommand("amongus_eject", "amongus_eject", playerManager, scheduler);
        createSoundEffectCommand("amongus_emeet", "amongus_emergency_meeting", playerManager, scheduler);
        createSoundEffectCommand("amongus_impwin", "amongus_impostor_win", playerManager, scheduler);
        createSoundEffectCommand("amongus_kill", "amongus_kill", playerManager, scheduler);
        createSoundEffectCommand("amongus_rolerev", "amongus_role_revel", playerManager, scheduler);
        createSoundEffectCommand("amongus_stabkill", "amongus_stab_kill", playerManager, scheduler);
        createSoundEffectCommand("amongus_sus", "amongus_sus", playerManager, scheduler);
        createSoundEffectCommand("amongus_venti", "amongus_vent_in", playerManager, scheduler);
        createSoundEffectCommand("amongus_vento", "amongus_vent_out", playerManager, scheduler);
        createSoundEffectCommand("amongus_votelock", "amongus_vote_locking", playerManager, scheduler);

        client.onDisconnect().block();
    }

    private static <T extends Event> void register(GatewayDiscordClient gateway, EventListener<T> eventListener) {
        gateway.getEventDispatcher()
                .on(eventListener.getEventType())
                .flatMap(eventListener::execute)
                .subscribe();
    }

    private static void createSoundEffectCommand(String command, String soundEffectKey, AudioPlayerManager playerManager, TrackScheduler scheduler) {
        commands.put(command, event -> {
            playerManager.loadItem(soundEffects.get(soundEffectKey), scheduler);
            System.out.println("Playing " + soundEffectKey);
        });
    }

    private static MP3Holder JSONSoundsReader(String jsonName){
        JSONParser parser = new JSONParser();
        URL url = DiscordBot.class.getClassLoader().getResource("amongus/mp3storage/" + jsonName);

        try(FileReader reader = new FileReader(url.getFile())){
            Object obj = parser.parse(reader);
            JSONObject first = (JSONObject) obj;
            return new MP3Holder((String)first.get("name"),
                    DiscordBot.class.getClassLoader().getResource(first.get("path").toString()).getPath());
        } catch(IOException | ParseException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void audioSetup(AudioPlayerManager playerManager) {
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }
}
