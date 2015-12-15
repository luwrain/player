/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.player;

import org.luwrain.core.NullCheck;
import org.luwrain.controls.*;
import org.luwrain.player.Playlist;

class TreeModelSource implements CachedTreeModelSource
{
    private final String root = "Альбомы и станции";//FIXME:
    private final String playlistsWithoutBookmarks = "Альбомы без закладок";//FIXME:
    private final String playlistsWithBookmarks = "Альбомы с закладками";//FIXME:
    private final String streamingResources = "Интернет-радио";//FIXME:

    private Playlist[] playlists ;

    @Override public Object getRoot()
    {
	return root;
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	if (obj == root)
	    return new Object[]{playlistsWithoutBookmarks, playlistsWithBookmarks, streamingResources};
	if (obj == streamingResources)
	    return playlists;
	return new Object[0];
    }

    void setPlaylists(Playlist[] playlists)
    {
	NullCheck.notNullItems(playlists, "playlists");
	this.playlists = playlists;
    }
}
