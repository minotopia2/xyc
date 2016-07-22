/*
 * Copyright (c) 2013 - 2015 xxyy (Philipp Nowak; devnull@nowak-at.net). All rights reserved.
 *
 * Any usage, including, but not limited to, compiling, running, redistributing, printing,
 *  copying and reverse-engineering is strictly prohibited without explicit written permission
 *  from the original author and may result in legal steps being taken.
 *
 * See the included LICENSE file (core/src/main/resources) or email xxyy98+xyclicense@gmail.com for details.
 */

package li.l1t.common.games.teams;

import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages instances of {@link Team}.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 05/03/14
 */
public final class TeamRegistry {
    private static final Map<String, Team> TEAM_MAP = new HashMap<>(7, 1F); //There are not many games where you'd need more than 6 teams
    private static final PropertyChangeSupport PROPERTY_CHANGE_SUPPORT =
            new PropertyChangeSupport(new Object());

    private TeamRegistry() {
    }

    /**
     * @return All registered teams.
     */
    public static Collection<Team> getTeams() {
        return TEAM_MAP.values();
    }

    /**
     * Register a team.
     *
     * @param team Team to register
     */
    public static void registerTeam(@Nonnull Team team) {
        TEAM_MAP.put(team.getName(), team);

        PROPERTY_CHANGE_SUPPORT.firePropertyChange("teams", null, team);
    }

    /**
     * Get a team by its name.
     *
     * @param teamName Name of the team to get
     * @return A {@link Team} or null if there's no such team.
     */
    public static Team getTeam(@Nonnull String teamName) {
        return TEAM_MAP.get(teamName);
    }

    @SuppressWarnings("unchecked") //The class is actually checked, so no errors can occur. Sorry, Mr. Compiler
    public static <T> T getTeam(@Nonnull String teamName, Class<T> clazz) {
        Team team = getTeam(teamName);

        if (team == null) {
            return null;
        }

        Validate.isTrue(clazz.isAssignableFrom(team.getClass()), "The team denoted by the name " + teamName + " is of an invalid type!");

        return (T) team;
    }

    /**
     * Unregister a team by its name.
     *
     * @param teamName Name of the team to unregister.
     * @return Whether the team list changed as a result of this call.
     */
    public static boolean unregisterTeam(@Nonnull String teamName) {
        Team oldValue = TEAM_MAP.remove(teamName);

        if (oldValue != null) {
            PROPERTY_CHANGE_SUPPORT.firePropertyChange("teams", oldValue, null);
        }

        return oldValue != null;
    }

    /**
     * Unregister a team.
     *
     * @param team Team to unregister.
     * @return Whether the team list changed as a result of this call.
     */
    public static boolean unregisterTeam(@Nonnull Team team) {
        return unregisterTeam(team.getName());
    }

    /**
     * Add a listener that is called every time the team list changes.
     *
     * @param listener Listener to add
     */
    public static void addTeamListChangeListener(PropertyChangeListener listener) {
        PROPERTY_CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }

    /**
     * Remove a listener that is called every time the team list changes.
     *
     * @param listener Listener to remove
     */
    public static void removeTeamListChangeListener(PropertyChangeListener listener) {
        PROPERTY_CHANGE_SUPPORT.removePropertyChangeListener(listener);
    }
}