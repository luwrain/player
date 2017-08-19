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

class Playlist implements Comparable
{
    enum Flags {HAS_BOOKMARK, STREAMING };

    interface TracksLoader
    {
	String[] loadTracks();
    }

    final Settings.Base sett;
    final Set<Flags> flags;
    final TracksLoader tracksLoader;
    private String[] loadedTracks = null;

    Playlist(Settings.Base sett, TracksLoader tracksLoader)
    {
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.flags = EnumSet.noneOf(Flags.class);
	this.sett = sett;
	this.tracksLoader = tracksLoader;
    }

    Playlist(Settings.Base sett, TracksLoader tracksLoader, Set<Flags> flags)
    {
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(flags, "flags");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.flags = flags;
	this.sett = sett;
	this.tracksLoader = tracksLoader;
    }

    Set<Flags> getFlags()
    {
	return flags;
    }

    String getPlaylistTitle()
    {
	return sett.getTitle("-");
    }

    org.luwrain.player.Playlist toGeneralPlaylist()
    {
	if (loadedTracks == null)
	    loadedTracks = tracksLoader.loadTracks();
	return new org.luwrain.player.Playlist(loadedTracks);
    }

    @Override public int compareTo(Object o)
    {
	if (o == null || !(o instanceof Playlist))
	    return 0;
	return getPlaylistTitle().compareTo(((Playlist)o).getPlaylistTitle());
    }

    @Override public String toString()
    {
	return getPlaylistTitle();
    }
}
