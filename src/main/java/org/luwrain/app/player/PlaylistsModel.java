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
import org.luwrain.controls.*;
import org.luwrain.player.Playlist;

class PlaylistsModel implements ListArea.Model 
{
    private final String playlistsWithoutBookmarks;
    private final String playlistsWithBookmarks;
    private final String streamingPlaylists;

    private Object[] items = new Object[0];
    
    PlaylistsModel(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	playlistsWithBookmarks = strings.treePlaylistsWithBookmarks();
	playlistsWithoutBookmarks = strings.treePlaylistsWithoutBookmarks();
	streamingPlaylists = strings.treeStreamingPlaylists();
    }

    void setPlaylists(Playlist[] playlists)
    {
	NullCheck.notNullItems(playlists, "playlists");
	final LinkedList<Playlist> withBookmarks = new LinkedList<Playlist>();
	final LinkedList<Playlist> withoutBookmarks = new LinkedList<Playlist>();
	final LinkedList<Playlist> streaming = new LinkedList<Playlist>();
	for(Playlist p: playlists)
	{
	    if (p.isStreaming())
		streaming.add(p);
	    if (p.hasBookmark() && !p.isStreaming())
		withBookmarks.add(p);
	    if (!p.hasBookmark() && !p.isStreaming())
		withoutBookmarks.add(p);
	}
	final Playlist[] sortingWithBookmarks = withBookmarks.toArray(new Playlist[withBookmarks.size()]);
	final Playlist[] sortingWithoutBookmarks = withoutBookmarks.toArray(new Playlist[withoutBookmarks.size()]);
	final Playlist[] sortingStreaming = streaming.toArray(new Playlist[streaming.size()]);
	Arrays.sort(sortingWithBookmarks);
	Arrays.sort(sortingWithoutBookmarks);
	Arrays.sort(sortingStreaming);
	final LinkedList res = new LinkedList();
	res.add(playlistsWithoutBookmarks);
	for(Playlist p: sortingWithoutBookmarks)
	    res.add(p);
	res.add(playlistsWithoutBookmarks);
	for(Playlist p: sortingWithBookmarks)
	    res.add(p);
	res.add(streamingPlaylists);
	for(Playlist p: sortingStreaming)
	    res.add(p);
	items = res.toArray(new Object[res.size()]);
    }

    @Override public int getItemCount()
    {
	return items.length;
    }

    @Override public Object getItem(int index)
    {
	if (index < 0 || index >= items.length)
	    return null;
	return items[index];
    }

    @Override public void refresh()
    {
    }

    @Override public boolean toggleMark(int index)
    {
	return false;
    }
}
