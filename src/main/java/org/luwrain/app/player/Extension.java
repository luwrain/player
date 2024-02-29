/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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


import org.luwrain.util.*;
import org.luwrain.core.*;
import static org.luwrain.core.NullCheck.*;

public final class Extension extends EmptyExtension
{
    static private final int VOLUME_STEP = 3;

    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new SimpleShortcutCommand("player"),
	    new Command(){
		@Override public String getName() { return "player-pause"; }
		@Override public void onCommand(Luwrain luwrain)
		{
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
			player.pauseResume(); else
			luwrain.playSound(Sounds.BLOCKED);
		}},
	};
    }

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new Shortcut[]{

	    new Shortcut() {
		@Override public String getExtObjName() { return "player"; }
		@Override public Application[] prepareApp(String[] args)
		{
		    notNullItems(args, "args");
		    return new Application[]{new App(args)};
		}
	    },

	    new Shortcut() {
		@Override public String getExtObjName() { return "player-single-local"; }
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
			player.play(new org.luwrain.player.FixedPlaylist(url.toString()), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS);
		    }
		    return new Application[0];
		}
	    },

	};
    }
}
