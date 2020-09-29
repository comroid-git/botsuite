package org.comroid.botsuite.javacord;

import org.comroid.botsuite.BotBase;
import org.comroid.common.io.FileHandle;
import org.comroid.dux.javacord.JavacordDUX;
import org.comroid.uniform.adapter.json.jackson.JacksonJSONAdapter;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public abstract class JavacordBotBase extends BotBase<DiscordEntity, Server, TextChannel, User, Message> {
    protected JavacordBotBase(FileHandle dataDir, DiscordApi api) {
        super(JacksonJSONAdapter.instance, new JavacordDUX(api), dataDir);
    }
}
