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

        final Type type;
    final String registryPath;
    final Properties props;

    Album(Type type, Properties props, String registryPath)
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
	case "props":
	    return new PropertiesHookObject(props);
	default:
	    return super.getMember(name);
	}
    }

    String getPlaylistTitle()
    {
	if (!props.containsKey("title"))
	    return "-";
	final String res = props.getProperty("title");
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
