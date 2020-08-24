package org.comroid.dux.javacord;

import org.comroid.api.Polyfill;
import org.comroid.dux.adapter.*;
import org.comroid.dux.ui.input.InputSequence;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import org.comroid.mutatio.pipe.BiPipe;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.adapter.json.jackson.JacksonJSONAdapter;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;

public final class JavacordDUX implements LibraryAdapter<DiscordEntity, Server, TextChannel, User, Message> {
    public final DiscordApi api;
    private final Map<Long, DiscordServer<Server>> serverCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordTextChannel<TextChannel>> textChannelCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordUser<User>> userCache = new ConcurrentHashMap<>();
    private final Map<Long, DiscordMessage<Message>> messageCache = new ConcurrentHashMap<>();

    @Override
    public SerializationAdapter<?, ?, ?> getSerializationAdapter() {
        return JacksonJSONAdapter.instance;
    }

    public JavacordDUX(DiscordApi api) {
        this.api = api;
    }

    @Contract("_ -> new")
    public JavacordDisplayable.OfString stringDisplayable(String content) {
        return new JavacordDisplayable.OfString(this, Reference.constant(content));
    }

    @Contract("_ -> new")
    public JavacordDisplayable.OfEmbed embedDisplayable(final Consumer<EmbedBuilder> embedConfigurator) {
        return new JavacordDisplayable.OfEmbed(this, Reference.provided(() -> {
            EmbedBuilder embed = DefaultEmbedFactory.create();
            embedConfigurator.accept(embed);
            return embed;
        }));
    }

    @Contract("_ -> new")
    public JavacordDisplayable.OfEmbed embedDisplayable(EmbedBuilder embed) {
        return new JavacordDisplayable.OfEmbed(this, Reference.constant(embed));
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
    public CompletableFuture<Message> send(TextChannel channel, String message) {
        return channel.sendMessage(message);
    }

    @Override
    public Runnable listenForMessages(TextChannel channel, BiConsumer<Long, String> handler) {
        return channel.addMessageCreateListener(
                event -> handler.accept(
                        event.getMessageAuthor().getId(),
                        event.getReadableMessageContent()
                ))::remove;
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
    public CompletableFuture<?> addReactionsToMessage(Message message, String... emojis) {
        if (message.isPrivateMessage())
            return message.addReactions(emojis);

        // prepare computation for emoji strings
        final ReferenceIndex<String> base = ReferenceIndex.of(Arrays.asList(emojis));
        BiPipe<String, Matcher, String, Matcher> matcherBase = base.pipe()
                .bi(DiscordRegexPattern.CUSTOM_EMOJI::matcher);
        // custom emojis accessor
        Span<Emoji> customEmojis = matcherBase
                .filterSecond(Matcher::matches)
                .merge((str, mtc) -> mtc.group("id"))
                .map(Long::parseLong)
                .map(message.getServer().orElseThrow(AssertionError::new)::getCustomEmojiById)
                .flatMap(Reference::optional)
                .map(Emoji.class::cast)
                .span();
        // unicode emojis accessor
        Span<Emoji> unicodeEmojis = matcherBase
                .filterSecond(mtc -> !mtc.matches())
                .drop()
                .map(UnicodeEmojiImpl::fromString)
                .map(Emoji.class::cast)
                .span();

        // compute and merge
        ArrayList<Emoji> yield = new ArrayList<>();
        for (int i = 0; i < emojis.length; i++) {
            final int c = i;
            unicodeEmojis.process(i)
                    .or(() -> customEmojis.get(c))
                    .ifPresent(yield::add);
        }

        return message.addReactions(yield.toArray(new Emoji[0]));
    }

    @Override
    public Runnable listenForReactions(Message message, BiConsumer<Long, String> handler) {
        return message.addReactionAddListener(
                event -> handler.accept(
                        event.getUser().getId(),
                        event.getEmoji().getMentionTag()
                ))::remove;
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
            return embedDisplayable((EmbedBuilder) display);
        if (display instanceof String)
            return stringDisplayable(String.valueOf(display));

        return null; // todo
    }


    @Override
    public <R> InputSequence<R, User, Message> input(HeldType<R> resultType) {
        if (resultType.equals(ValueType.STRING))
            return Polyfill.uncheckedCast(new JavacordInputSequence.OfString(this));
        if (resultType.equals(ValueType.BOOLEAN))
            return Polyfill.uncheckedCast(new JavacordInputSequence.OfBoolean(this));

        throw new UnsupportedOperationException("Unsupported result type: " + resultType.getName());
    }
}
