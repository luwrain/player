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
import org.luwrain.core.*;
import org.luwrain.player.*;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
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
		    final Player player = luwrain.getPlayer();
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
		    if (args == null || args.length < 1)
			return new Application[]{new PlayerApp()};
		    final LinkedList<Application> v = new LinkedList<Application>();
		    for(String s: args)
			if (s != null)
			    v.add(new PlayerApp(s));
		    if (v.isEmpty())
			return new Application[]{new PlayerApp()};
		    return v.toArray(new Application[v.size()]);
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
		    luwrain.getPlayer().play(new SingleLocalFilePlaylist("file://" + args[0].replaceAll(" ", "%20")));
		    return null;
		}
	    },


	};
    }
}
