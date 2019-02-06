
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class PlaylistsModel implements ListArea.Model 
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
