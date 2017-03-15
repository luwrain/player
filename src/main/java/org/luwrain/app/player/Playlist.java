
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

    @Override public String[] getPlaylistItems()
    {
	if (loadedTracks == null)
	    loadedTracks = tracksLoader.loadTracks();
	    return loadedTracks;
    }
}
