package org.comroid.dux.javacord;

import org.comroid.api.Polyfill;
import org.comroid.dux.abstr.LibraryAdapter;
import org.comroid.dux.ui.output.DiscordDisplayable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.concurrent.CompletableFuture;

public final class JavacordDisplayable {
    public static final class OfEmbed extends DiscordDisplayable<TextChannel, Message> {
        private final JavacordAdapter javacordAdapter;
        private final EmbedBuilder embedBuilder;

        @Override
        public LibraryAdapter<Object, Object, TextChannel, Object, Message> getAdapter() {
            return Polyfill.uncheckedCast(javacordAdapter);
        }

        public OfEmbed(JavacordAdapter javacordAdapter, EmbedBuilder embedBuilder) {
            this.javacordAdapter = javacordAdapter;
            this.embedBuilder = embedBuilder;
        }

        @Override
        protected CompletableFuture<Message> sendInto(TextChannel channel) {
            return channel.sendMessage(embedBuilder);
        }

        @Override
        protected CompletableFuture<Message> updateContent(Message oldMessage) {
            return oldMessage.edit(embedBuilder).thenApply(nil -> oldMessage);
        }
    }
}
