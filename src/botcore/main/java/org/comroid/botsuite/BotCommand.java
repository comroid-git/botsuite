package org.comroid.botsuite;

import org.comroid.api.Named;
import org.comroid.common.info.Described;
import org.comroid.dux.adapter.DiscordUser;

import java.util.function.Predicate;

public interface BotCommand<USR> extends Named, Described, Predicate<DiscordUser<USR>> {
    String[] aliases();

     

    @Override
    default boolean test(DiscordUser<USR> usrDiscordUser) {
        return true;
    }
}
