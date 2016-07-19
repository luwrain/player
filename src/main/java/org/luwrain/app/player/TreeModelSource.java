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

package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.NullCheck;
import org.luwrain.controls.*;
import org.luwrain.player.Playlist;

class TreeModelSource implements CachedTreeModelSource
{
    private String root;
    private String playlistsWithoutBookmarks = "Альбомы без закладок";//FIXME:;
    private String playlistsWithBookmarks;
    private String streamingPlaylists;

    private Playlist[] playlists ;

    TreeModelSource(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	root = strings.treeRoot();
	playlistsWithBookmarks = strings.treePlaylistsWithBookmarks();
	playlistsWithoutBookmarks = strings.treePlaylistsWithoutBookmarks();
	streamingPlaylists = strings.treeStreamingPlaylists();
    }

    @Override public Object getRoot()
    {
	return root;
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	if (obj == root)
	    return new Object[]{playlistsWithoutBookmarks, playlistsWithBookmarks, streamingPlaylists};
	final LinkedList res = new LinkedList();
	for(Playlist p: playlists)
	{
	    if (obj == streamingPlaylists && p.isStreaming())
		res.add(p);
	    if (obj == playlistsWithBookmarks && p.hasBookmark() && !p.isStreaming())
		res.add(p);
	    if (obj == playlistsWithoutBookmarks &&! p.hasBookmark() && !p.isStreaming())
		res.add(p);
	}
	final Object[] toSort = res.toArray(new Object[res.size()]);
	Arrays.sort(toSort);
	return toSort;
    }

    void setPlaylists(Playlist[] playlists)
    {
	NullCheck.notNullItems(playlists, "playlists");
	this.playlists = playlists;
    }
}
