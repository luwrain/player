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
import org.luwrain.script.*;

final class Album extends EmptyHookObject implements Comparable
{
    enum Type {STREAMING, DIR, PLAYLIST};

    final String registryPath;
    final Type type;
    final Map<String, String> props;

    Album(Type type, Map<String, String> props, String registryPath)
    {
	NullCheck.notNull(type, "type");
	NullCheck.notNull(props, "props");
	NullCheck.notEmpty(registryPath, "registryPath");
	this.type = type;
	this.props = props;
	this.registryPath = registryPath;
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
