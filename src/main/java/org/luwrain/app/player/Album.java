
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.script.*;

final class Album extends EmptyHookObject implements Comparable
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
    final Map<String, String> props;

    private String[] loadedTracks = null;

    Album(String registryPath, Settings.Base sett, TracksLoader tracksLoader)
    {
	NullCheck.notEmpty(registryPath, "registryPath");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(tracksLoader, "tracksLoader");
	this.registryPath = registryPath;
	this.flags = EnumSet.noneOf(Flags.class);
	this.sett = sett;
	this.tracksLoader = tracksLoader;
	this.props = new HashMap();
    }

    Album(String registryPath, Settings.Base sett, Map<String, String> props, Set<Flags> flags)
    {
	NullCheck.notEmpty(registryPath, "registryPath");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(props, "props");
	NullCheck.notNull(flags, "flags");
	this.registryPath = registryPath;
	this.flags = flags;
	this.sett = sett;
	this.tracksLoader = null;
	this.props = props;
    }


    Album(String registryPath, Settings.Base sett,
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
	this.props = new HashMap();
    }

    @Override public Object getMember(String name)
    {
	NullCheck.notNull(name, "name");
	switch(name)
	{
	case "title":
	    return getPlaylistTitle();
	case "streaming":
	    return new Boolean(flags.contains(Flags.STREAMING));
	case "url":
	    if (!props.containsKey("url"))
		return "";
	    return props.get("url") != null?props.get("url"):"";
	default:
	    return super.getMember(name);
	}
    }

    String getPlaylistTitle()
    {
	return sett.getTitle("-");
    }

    org.luwrain.player.Playlist toPlaylist()
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
	if (o == null || !(o instanceof Album))
	    return 0;
	return getPlaylistTitle().compareTo(((Album)o).getPlaylistTitle());
    }

    @Override public String toString()
    {
	return getPlaylistTitle();
    }
}
