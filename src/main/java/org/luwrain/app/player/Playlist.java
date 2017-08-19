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

package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;

class Playlist
{
    enum Flags {HAS_BOOKMARK, STREAMING };

    interface TracksLoader
    {
	String[] loadTracks();
    }

    final String title; 
    final Set<Flags> flags;
    final TracksLoader tracksLoader;
    private String[] loadedTracks = null;

    Playlist(String title, TracksLoader tracksLoader)
    {
	NullCheck.notNull(title, "title");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.flags = EnumSet.noneOf(Flags.class);
	this.title = title;
	this.tracksLoader = tracksLoader;
    }

    Playlist(String title, TracksLoader tracksLoader, Set<Flags> flags)
    {
	NullCheck.notNull(title, "title");
	NullCheck.notNull(flags, "flags");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.flags = flags;
	this.title = title;
	this.tracksLoader = tracksLoader;
    }

    Set<Flags> getFlags()
    {
	return flags;
    }

    org.luwrain.player.Playlist toGeneralPlaylist()
    {
	if (loadedTracks == null)
	    loadedTracks = tracksLoader.loadTracks();
	return new org.luwrain.player.Playlist(loadedTracks);
    }
}
