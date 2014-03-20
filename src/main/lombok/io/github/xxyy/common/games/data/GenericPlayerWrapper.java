/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.xxyy.common.games.data;

import io.github.xxyy.common.sql.SafeSql;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Example implementation of {@link PlayerWrapper}. Allows for access to the API, even if no own data needs to be saved.
 *
 * @author xxyy
 */
public class GenericPlayerWrapper extends PlayerWrapper<GenericPlayerWrapper> {

    protected GenericPlayerWrapper(final CommandSender sender, final SafeSql ssql) {
        super(sender, ssql);
    }

    protected GenericPlayerWrapper(Player plr, SafeSql ssql) {
        super(plr, ssql);
    }

    protected GenericPlayerWrapper(String plrName, SafeSql ssql) {
        super(plrName, ssql);
    }

    @Override
    protected void impFetch() {
        //nothing to do here
    }

    @Override
    protected void impFlush() {
        //nothing to do here
    }
}