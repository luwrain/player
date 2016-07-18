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

import java.util.*;

import org.luwrain.core.*;
//import org.luwrain.util.RegistryPath;

//import org.luwrain.player.backends.*;

class PlayerImpl implements Player
{
    private final PlayerThread thread = new PlayerThread();
    private Registry registry;

PlayerImpl(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    @Override public void play(Playlist playlist,
			       int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
thread.play(playlist, startingTrackNum, startingPosMsec);
    }

    @Override public void stop()
    {
	thread.stop();
    }

    @Override public void pauseResume()
    {
	thread.pauseResume();
    }

    @Override public void jump(long offsetMsec)
    {
thread.jump(offsetMsec);
    }

    @Override public Playlist getCurrentPlaylist()
    {
	return thread.getCurrentPlaylist();
    }

    @Override public int getCurrentTrackNum()
    {
	return thread.getCurrentTrackNum();
    }

    @Override public Playlist[] loadRegistryPlaylists()
    {
	final String dir = "/org/luwrain/player/playlists";//FIXME:
	final String[] dirs = registry.getDirectories(dir); 
	final LinkedList<Playlist> res = new LinkedList<Playlist>();
	for(String s: dirs)
	{
	    final String path = Registry.join(dir, s);
	    final RegistryPlaylist playlist = new RegistryPlaylist(registry);
	    if (playlist.init(path))
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }

    @Override public void addListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
thread.addListener(listener);
    }

    @Override public void removeListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	thread.removeListener(listener);
    }
}
