/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

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
import java.io.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.app.player.PlayerApp;
import org.luwrain.app.player.Strings;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    private Player player;

    @Override public String init(Luwrain luwrain)
    {
	player = new PlayerImpl();
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

	    new Command(){
		@Override public String getName()
		{
		    return "player-pause";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.pauseResume();
		}
	    },

	    	    new Command(){
		@Override public String getName()
		{
		    return "player-jump-forward";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.jump(5000);
		}
	    },

	    	    	    new Command(){
		@Override public String getName()
		{
		    return "player-jump-backward";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.jump(-5000);
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
			return new Application[]{new PlayerApp(args)};
		}
	    },

	    new Shortcut() {
		@Override public String getName()
		{
		    return "player-single-local";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    if (args.length != 1 || args[0].isEmpty())
			return null;
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
		    {
			final File f = new File(args[0]).getAbsoluteFile();
			final URL url = Urls.toUrl(f);
			if (url == null)
			    return null;//FIXME:message
			player.play(new org.luwrain.player.Playlist(url.toString()), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS);
		    }
		    return null;
		}
	    },

	};
    }

@Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
{
    return new ExtensionObject[]{

	new SharedObject(){
	    @Override public String getExtObjName()
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
