/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.player;

import java.net.*;
import java.util.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.app.player.PlayerApp;
import org.luwrain.app.player.Strings;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    private Player player;

    @Override public String init(Luwrain luwrain)
    {
	player = new PlayerImpl(luwrain.getRegistry());
	Log.debug("player", "player is initialized");
	return null;
    }

    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{

	    new Command(){
		@Override public String getName()
		{
		    return "player";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("player");
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "player-stop";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.stop();
		}
	    },

	};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{

	    new Shortcut() {
		@Override public String getName()
		{
		    return "player";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (args.length == 1 && FileTypes.getExtension(args[0]).toLowerCase().equals("m3u"))
		    {
			final Path path = Paths.get(args[0]);
			if (path == null)
			{
			    luwrain.message(strings.badPlaylistPath(args[0]), Luwrain.MESSAGE_ERROR);
			    return null;
			}
			final M3uPlaylist playlist = new M3uPlaylist();
			if (!playlist.load(path))
			{
			    luwrain.message(strings.errorLoadingPlaylist(path.toString()), Luwrain.MESSAGE_ERROR);
			    return null;
			}
			return new Application[]{new PlayerApp(playlist)};
		    }

		    return new Application[]{new PlayerApp()};
		}
	    },

	    new Shortcut() {
		@Override public String getName()
		{
		    return "player-single-local";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length != 1)
			return null;
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.play(new SingleLocalFilePlaylist("file://" + args[0].replaceAll(" ", "%20")), 0, 0);
		    return null;
		}
	    },

	};
    }

    @Override public SharedObject[] getSharedObjects(Luwrain luwrain)
    {
	return new SharedObject[]{

	    new SharedObject(){
		@Override public String getName()
		{
		    return Player.SHARED_OBJECT_NAME;
		}
		@Override public Object getSharedObject()
		{
		    return player;
		}
	    },

	};
    }
}
