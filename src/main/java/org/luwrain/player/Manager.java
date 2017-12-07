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

import java.util.*;

import org.luwrain.base.*;
import org.luwrain.core.*;

class Manager
{
	static private final String LOG_COMPONENT = PlayerImpl.LOG_COMPONENT;

    private final Map<String, MediaResourcePlayer> players = new HashMap();

    Manager()
    {
	registryPlayer(new org.luwrain.extensions.plwave.Factory());
	registryPlayer(new org.luwrain.extensions.plogg.Factory());
	registryPlayer(new org.luwrain.extensions.plmp3.Factory());
    }

    MediaResourcePlayer.Instance play(org.luwrain.base.MediaResourcePlayer.Listener listener, Task task)
    {
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(task, "task");
	final MediaResourcePlayer player = findPlayer(task.url.toString());
	if (player == null)
	{
	    Log.info(LOG_COMPONENT, "unable to find a media resource player for " + task.url.toString());
	    return null;
	}
	final MediaResourcePlayer.Instance instance = player.newMediaResourcePlayer(listener);
	if (instance == null)
	    return null;
	instance.play(task.url, task.startPosMsec(), EnumSet.noneOf(MediaResourcePlayer.Flags.class));
	return instance;
    }

    private MediaResourcePlayer findPlayer(String url)
    {
	NullCheck.notEmpty(url, "url");
	if (url.trim().toLowerCase().endsWith(".wav") && players.containsKey("wav"))
	    return players.get("wav");
	if (url.trim().toLowerCase().endsWith(".mp3") && players.containsKey("mp3"))
	    return players.get("mp3");
	if (url.trim().toLowerCase().endsWith(".ogg") && players.containsKey("ogg"))
	    return players.get("ogg");
	//mp3 as a default
	if (players.containsKey("mp3"))
	    return players.get("mp3");
	return null;
    }

    private void registryPlayer(MediaResourcePlayer player)
    {
	NullCheck.notNull(player, "player");
	final String mimeType = player.getSupportedMimeType();
	if (mimeType == null || mimeType.isEmpty())
	    return;
	Log.debug(LOG_COMPONENT, "registering the media resource player for \'" + mimeType + "\'");
	players.put(mimeType.toLowerCase().trim(), player);
    }
}
