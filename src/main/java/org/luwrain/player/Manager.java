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

    private final Map<String, MediaResourcePlayerFactory> factories = new HashMap<String, MediaResourcePlayerFactory>();

    Manager()
    {
	registryFactory(new org.luwrain.extensions.plwave.Factory());
	registryFactory(new org.luwrain.extensions.plogg.Factory());
	registryFactory(new org.luwrain.extensions.plmp3.Factory());
    }

    MediaResourcePlayer play(org.luwrain.base.MediaResourcePlayer.Listener listener, Task task)
    {
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(task, "task");
	final MediaResourcePlayerFactory factory = findFactory(task.url.toString());
	if (factory == null)
	{
	    Log.info(LOG_COMPONENT, "unable to find a media resource player factory for " + task.url.toString());
	    return null;
	}
	final MediaResourcePlayer player = factory.newMediaResourcePlayer(listener);
	if (player == null)
	    return null;
	player.play(task.url, task.startPosMsec(), EnumSet.noneOf(MediaResourcePlayer.Flags.class));
	return player;
    }

    private MediaResourcePlayerFactory findFactory(String url)
    {
	NullCheck.notEmpty(url, "url");
	if (url.trim().toLowerCase().endsWith(".wav") && factories.containsKey("wav"))
	    return factories.get("wav");
	if (url.trim().toLowerCase().endsWith(".mp3") && factories.containsKey("mp3"))
	    return factories.get("mp3");
	if (url.trim().toLowerCase().endsWith(".ogg") && factories.containsKey("ogg"))
	    return factories.get("ogg");
	return null;
    }

    private void registryFactory(MediaResourcePlayerFactory factory)
    {
	NullCheck.notNull(factory, "factory");
	final String mimeType = factory.getSupportedMimeType();
	if (mimeType == null || mimeType.isEmpty())
	    return;
	Log.debug(LOG_COMPONENT, "registering the media resource factory  for \'" + mimeType + "\'");
	factories.put(mimeType.toLowerCase().trim(), factory);
    }
}
