
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

    void setPlaylists(Playlist[] playlists)
    {
	NullCheck.notNullItems(playlists, "playlists");
	final List<Playlist> withBookmarks = new LinkedList<Playlist>();
	final List<Playlist> withoutBookmarks = new LinkedList<Playlist>();
	final List<Playlist> streaming = new LinkedList<Playlist>();
	for(Playlist p: playlists)
	{
	    if (p.flags.contains(Playlist.Flags.STREAMING))
		streaming.add(p);
	    if (p.flags.contains(Playlist.Flags.HAS_BOOKMARK) && !p.flags.contains(Playlist.Flags.STREAMING))
		withBookmarks.add(p);
	    if (!p.flags.contains(Playlist.Flags.HAS_BOOKMARK) && !p.flags.contains(Playlist.Flags.STREAMING))
		withoutBookmarks.add(p);
	}
	final Playlist[] sortingWithBookmarks = withBookmarks.toArray(new Playlist[withBookmarks.size()]);
	final Playlist[] sortingWithoutBookmarks = withoutBookmarks.toArray(new Playlist[withoutBookmarks.size()]);
	final Playlist[] sortingStreaming = streaming.toArray(new Playlist[streaming.size()]);
	Arrays.sort(sortingWithBookmarks);
	Arrays.sort(sortingWithoutBookmarks);
	Arrays.sort(sortingStreaming);
	final List res = new LinkedList();
	res.add(playlistsWithoutBookmarks);
	for(Playlist p: sortingWithoutBookmarks)
	    res.add(p);
	res.add(playlistsWithBookmarks);
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
}
