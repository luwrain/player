/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

final class AlbumsModel implements ListArea.Model 
{
    private final String playlistsWithoutBookmarks;
    private final String playlistsWithBookmarks;
    private final String streamingPlaylists;

    private Object[] items = new Object[0];

    AlbumsModel(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	playlistsWithBookmarks = strings.treePlaylistsWithBookmarks();
	playlistsWithoutBookmarks = strings.treePlaylistsWithoutBookmarks();
	streamingPlaylists = strings.treeStreamingPlaylists();
    }

    void setPlaylists(Album[] playlists)
    {
	NullCheck.notNullItems(playlists, "playlists");
	final List<Album> withBookmarks = new LinkedList();
	final List<Album> withoutBookmarks = new LinkedList();
	final List<Album> streaming = new LinkedList();
	for(Album p: playlists)
	{
	    if (p.type == Album.Type.STREAMING)
		streaming.add(p);
	    if (p.type != Album.Type.STREAMING)
		withBookmarks.add(p);
	    if (true)
		withoutBookmarks.add(p);
	}
	final Album[] sortingWithBookmarks = withBookmarks.toArray(new Album[withBookmarks.size()]);
	final Album[] sortingWithoutBookmarks = withoutBookmarks.toArray(new Album[withoutBookmarks.size()]);
	final Album[] sortingStreaming = streaming.toArray(new Album[streaming.size()]);
	Arrays.sort(sortingWithBookmarks);
	Arrays.sort(sortingWithoutBookmarks);
	Arrays.sort(sortingStreaming);
	final List res = new LinkedList();
	res.add(playlistsWithoutBookmarks);
	for(Album p: sortingWithoutBookmarks)
	    res.add(p);
	res.add(playlistsWithBookmarks);
	for(Album p: sortingWithBookmarks)
	    res.add(p);
	res.add(streamingPlaylists);
	for(Album p: sortingStreaming)
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
}
