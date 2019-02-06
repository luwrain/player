
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.script.*;

final class Album extends EmptyHookObject implements Comparable
{
    enum Type {STREAMING, DIR, PLAYLIST};

    enum Flags {HAS_BOOKMARK, STREAMING };

    interface TracksLoader
    {
	String[] loadTracks();
    }

    final String registryPath;
    final Settings.Base sett;
    final Type type;
    final Map<String, String> props;

    Album(Type type, String registryPath, Settings.Base sett, Map<String, String> props)
    {
	NullCheck.notNull(type, "type");
	NullCheck.notEmpty(registryPath, "registryPath");
	NullCheck.notNull(sett, "sett");
	NullCheck.notNull(props, "props");
	this.type = type;
	this.registryPath = registryPath;
	this.sett = sett;
	this.props = props;
    }


    @Override public Object getMember(String name)
    {
	NullCheck.notNull(name, "name");
	switch(name)
	{
	case "title":
	    return getPlaylistTitle();
	case "type":
	    return type.toString().toLowerCase();
	case "url":
	    if (!props.containsKey("url"))
		return "";
	    return props.get("url") != null?props.get("url"):"";
	    	case "path":
	    if (!props.containsKey("path"))
		return "";
	    return props.get("path") != null?props.get("path"):"";
	default:
	    return super.getMember(name);
	}
    }

    String getPlaylistTitle()
    {
	if (!props.containsKey("title"))
	    return "-";
	final String res = props.get("title");
	if (res == null || res.isEmpty())
	    return "-";
	return res;
    }

    org.luwrain.player.Playlist toPlaylist()
    {
	return null;
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
