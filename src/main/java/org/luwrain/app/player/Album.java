/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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

import com.google.gson.annotations.*;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.player.Player;
import org.luwrain.script.*;

final class Album extends EmptyHookObject implements Comparable
{
    enum Type {SECTION, STREAMING, DIR, M3U, UNKNOWN};

    @SerializedName("type")
    private Type type = null;

    @SerializedName("title")
    private String title = "";

    @SerializedName("props")
    private Properties props = new Properties();

    @Override public Object getMember(String name)
    {
	NullCheck.notNull(name, "name");
	switch(name)
	{
	case "title":
	    return getTitle();
	case "type":
	    return getType().toString().toLowerCase();
	case "properties":
	    return new PropertiesHookObject(getProps());
	default:
	    return super.getMember(name);
	}
    }

    String getTitle()
    {
	return title != null?title:"";
    }

    void setTitle(String title)
    {
	NullCheck.notNull(title, "title");
	this.title = title;
    }

    Type getType()
    {
	return this.type;
    }

    void setType(Type type)
    {
	NullCheck.notNull(type, "type");
	this.type = type;
    }

    Properties getProps()
    {
	if (props == null)
	    props = new Properties();
	return props;
    }

    int getVolume()
    {
	final String volumeStr = getProps().getProperty("volume");
	if (volumeStr == null || volumeStr.trim().isEmpty())
	    return Player.MAX_VOLUME;
	try {
	    return Math.min(Math.max(Integer.parseInt(volumeStr.trim()), Player.MIN_VOLUME), Player.MAX_VOLUME);
	}
	    catch(NumberFormatException e)
	    {
		return Player.MAX_VOLUME;
	    }
    }

    boolean isSection()
    {
	return this.type == Type.SECTION;
    }

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
