/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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
    enum Type {STREAMING, DIR, M3U};

        final Type type;
    final String title;
        final Properties props;
    final String registryPath;

    Album(Type type, String title, Properties props, String registryPath)
    {
	NullCheck.notNull(type, "type");
	NullCheck.notNull(title, "title");
	NullCheck.notNull(props, "props");
			NullCheck.notEmpty(registryPath, "registryPath");
	this.type = type;
	this.title = title;
	this.props = props;
	this.registryPath = registryPath;
    }

    @Override public Object getMember(String name)
    {
	NullCheck.notNull(name, "name");
	switch(name)
	{
	case "title":
	    return getTitle();
	case "type":
	    return type.toString().toLowerCase();
	case "properties":
	    return new PropertiesHookObject(props);
	default:
	    return super.getMember(name);
	}
    }

    String getTitle()
    {
	return title;
    }

    /*
    org.luwrain.player.Playlist toPlaylist()
    {
	return null;
    }
    */

    @Override public int compareTo(Object o)
    {
	if (o == null || !(o instanceof Album))
	    return 0;
	return getTitle().compareTo(((Album)o).getTitle());
    }

    @Override public String toString()
    {
	return getTitle();
    }
}
