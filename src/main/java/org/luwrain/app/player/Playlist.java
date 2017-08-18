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

class Playlist extends org.luwrain.player.DefaultPlaylist
{
    enum Flags {HAS_BOOKMARK, STREAMING };

    interface TracksLoader
    {
	String[] loadTracks();
    }

    final Set<Flags> flags;
    final TracksLoader tracksLoader;
    private String[] loadedTracks = null;

    Playlist(String title, TracksLoader tracksLoader)
    {
	super(title, new String[0], null);
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.flags = EnumSet.noneOf(Flags.class);
	this.tracksLoader = tracksLoader;
    }

    Playlist(String title, TracksLoader tracksLoader, Set<Flags> flags)
    {
	super(title, new String[0], null);
	NullCheck.notNull(flags, "flags");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.flags = flags;
	this.tracksLoader = tracksLoader;
    }

    Set<Flags> getFlags()
    {
	return flags;
    }

    @Override public String[] getPlaylistUrls()
    {
	if (loadedTracks == null)
	    loadedTracks = tracksLoader.loadTracks();
	    return loadedTracks;
    }
}
