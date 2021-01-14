/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.player;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.util.*;

public final class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    static private final int VOLUME_STEP = 3;

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
	};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "player";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    return new Application[]{new App(args)};
		}
	    },

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "player-single-local";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    if (args.length != 1 || args[0].isEmpty())
			return null;
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
		    {
			final File f = new File(args[0]).getAbsoluteFile();
			final URL url = Urls.toUrl(f);
			if (url == null)
			    return null;//FIXME:message
			player.play(new org.luwrain.player.Playlist(url.toString()), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, null);
		    }
		    return new Application[0];
		}
	    },

	};
    }
}
