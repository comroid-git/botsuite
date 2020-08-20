package org.comroid.dux;

import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.model.DiscordMessage;
import org.comroid.dux.model.DiscordServer;
import org.comroid.dux.model.DiscordTextChannel;
import org.comroid.dux.model.DiscordUser;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class JavacordAdapter implements LibraryAdapter<DiscordEntity, Server, TextChannel, User, Message> {
    private final DiscordApi api;
    private final Map<Long, DiscordServer<Server>> serverCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordTextChannel<TextChannel>> textChannelCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordUser<User>> userCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordMessage<Message>> messageCache = new ConcurrentHashMap<>();

    public JavacordAdapter(DiscordApi api) {
        this.api = api;

        DiscordUX.create(new JavacordAdapter(null));
    }

    @Override
    public long getID(DiscordEntity of) {
        return of.getId();
    }

    @Override
    public DiscordServer<Server> getServerByID(long id) {
        return serverCache.computeIfAbsent(id, k -> new DiscordServer<>(this, id,
                x -> api.getServerById(x).orElse(null)));
    }

    @Override
    public DiscordTextChannel<TextChannel> getTextChannelByID(long id) {
        return textChannelCache.computeIfAbsent(id, k -> new DiscordTextChannel<>(this, id,
                x -> api.getTextChannelById(id).orElse(null)));
    }

    @Override
    public DiscordUser<User> getUserByID(long id) {
        return userCache.computeIfAbsent(id, k -> new DiscordUser<>(this, id,
                x -> api.getUserById(id).join()));
    }

    @Override
    public boolean isHumanUser(User user) {
        return !user.isBot() && !(user instanceof Webhook);
    }

    @Override
    public DiscordMessage<Message> getMessageByID(final long chlID, long msgID) {
        return messageCache.computeIfAbsent(msgID,
                k -> new DiscordMessage<>(this, msgID, x -> api
                        .getChannelById(chlID)
                        .flatMap(Channel::asTextChannel)
                        .map(chl -> chl.getMessageById(x))
                        .map(CompletableFuture::join)
                        .orElse(null)));
    }

    @Override
    public String getMessageContent(Message message) {
        return message.getReadableContent();
    }

    @Override
    public DiscordTextChannel<TextChannel> getChannelOfMessage(Message message) {
        return getTextChannelByID(message.getChannel().getId());
    }
}
