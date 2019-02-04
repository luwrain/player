
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;

final class Playlist implements Comparable
{
    enum Flags {HAS_BOOKMARK, STREAMING };

    interface TracksLoader
    {
	String[] loadTracks();
    }

    final String registryPath;
    final Settings.Base sett;
    final TracksLoader tracksLoader;
    final Set<Flags> flags;

    private String[] loadedTracks = null;

    Playlist(String registryPath, Settings.Base sett, TracksLoader tracksLoader)
    {
	NullCheck.notEmpty(registryPath, "registryPath");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.registryPath = registryPath;
	this.flags = EnumSet.noneOf(Flags.class);
	this.sett = sett;
	this.tracksLoader = tracksLoader;
    }

    Playlist(String registryPath, Settings.Base sett,
	     TracksLoader tracksLoader, Set<Flags> flags)
    {
	NullCheck.notNull(registryPath, "registryPath");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(flags, "flags");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.registryPath = registryPath;
	this.sett = sett;
	this.tracksLoader = tracksLoader;
	this.flags = flags;
    }

    String getPlaylistTitle()
    {
	return sett.getTitle("-");
    }

    org.luwrain.player.Playlist toGeneralPlaylist()
    {
	if (loadedTracks == null)
	    loadedTracks = tracksLoader.loadTracks();
	final Map<String, String> props = new HashMap<String, String>();
	props.put("streaming", flags.contains(Flags.STREAMING)?"yes":"no");
	props.put("title", getPlaylistTitle());
	return new org.luwrain.player.Playlist(loadedTracks, props);
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
