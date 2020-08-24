package org.comroid.dux.javacord;

import org.comroid.api.Polyfill;
import org.comroid.dux.adapter.DiscordMessage;
import org.comroid.dux.adapter.DiscordTextChannel;
import org.comroid.dux.adapter.LibraryAdapter;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.comroid.mutatio.ref.Reference;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.concurrent.CompletableFuture;

public final class JavacordDisplayable {
    private static abstract class Base extends DiscordDisplayable<TextChannel, Message> {

        private final JavacordDUX javacordDUX;

        @Override
        public LibraryAdapter<Object, Object, TextChannel, Object, Message> getAdapter() {
            return Polyfill.uncheckedCast(javacordDUX);
        }

        protected Base(JavacordDUX javacordDUX) {
            this.javacordDUX = javacordDUX;
        }
    }

    public static final class OfString extends Base {
        private final Reference<String> stringRef;

        protected OfString(JavacordDUX javacordDUX, Reference<String> stringRef) {
            super(javacordDUX);
            this.stringRef = stringRef;
        }

        @Override
        public CompletableFuture<Message> sendInto(DiscordTextChannel<TextChannel> channel) {
            return stringRef.ifPresentMapOrElseGet(
                    channel::sendMessage,
                    () -> Polyfill.failedFuture(new RuntimeException("Could not find string"))
            );
        }

        @Override
        public CompletableFuture<Message> updateContent(DiscordMessage<Message> oldMessage) {
            return stringRef.process()
                    .map(oldMessage::edit)
                    .map(f -> f.thenCompose(n -> oldMessage.getLatestInstance()))
                    .orElseGet(() -> Polyfill.failedFuture(new RuntimeException("Could not find embed")));
        }
    }

    public static final class OfEmbed extends Base {
        private final Reference<EmbedBuilder> embedRef;

        public OfEmbed(JavacordDUX javacordDUX, Reference<EmbedBuilder> embedRef) {
            super(javacordDUX);
            this.embedRef = embedRef;
        }

        @Override
        public CompletableFuture<Message> sendInto(DiscordTextChannel<TextChannel> channel) {
            return embedRef.ifPresentMapOrElseGet(
                    channel::sendMessage,
                    () -> Polyfill.failedFuture(new RuntimeException("Could not find embed"))
            );
        }

        @Override
        public CompletableFuture<Message> updateContent(DiscordMessage<Message> oldMessage) {
            return embedRef.process()
                    .map(oldMessage::edit)
                    .map(f -> f.thenCompose(n -> oldMessage.getLatestInstance()))
                    .orElseGet(() -> Polyfill.failedFuture(new RuntimeException("Could not find embed")));
        }
    }
}
