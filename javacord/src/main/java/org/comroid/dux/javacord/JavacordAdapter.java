package org.comroid.dux.javacord;

import org.comroid.api.Polyfill;
import org.comroid.dux.DiscordUX;
import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordServer;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.adapter.DiscordUser;
import org.comroid.dux.ui.input.DiscordInputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.adapter.json.jackson.JacksonJSONAdapter;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class JavacordAdapter implements LibraryAdapter<DiscordEntity, Server, TextChannel, User, Message> {
    public final DiscordApi api;
    private final Map<Long, DiscordServer<Server>> serverCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordTextChannel<TextChannel>> textChannelCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordUser<User>> userCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordMessage<Message>> messageCache = new ConcurrentHashMap<>();

    public JavacordAdapter(DiscordApi api) {
        this.api = api;
    }

    @Override
    public SerializationAdapter<?, ?, ?> getSerializationAdapter() {
        return JacksonJSONAdapter.instance;
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
        return !user.isBot();
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
    public boolean isMostRecentMessage(Message message) {
        return message.getChannel()
                .getMessages(1)
                .thenApply(MessageSet::getNewestMessage)
                .thenApply(opt -> opt.map(message::equals).orElse(false))
                .join();
    }

    @Override
    public CompletableFuture<Void> deleteMessage(Message message) {
        return message.delete();
    }

    @Override
    public DiscordTextChannel<TextChannel> getChannelOfMessage(Message message) {
        return getTextChannelByID(message.getChannel().getId());
    }

    @Override
    public DiscordDisplayable<TextChannel, Message> output(Object display) {
        if (display instanceof EmbedBuilder)
            return new JavacordDisplayable.OfEmbed(this, (EmbedBuilder) display);

        return null; // todo
    }

    @Override
    public <R> DiscordInputSequence<R, TextChannel, User, Message> input(HeldType<R> resultType) {
        if (resultType.equals(ValueType.STRING))
            return Polyfill.uncheckedCast(new JavacordInputSequence.OfString(this));

        return null; // todo
    }
}
