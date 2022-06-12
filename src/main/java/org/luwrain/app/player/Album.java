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

import org.luwrain.player.Player;

final class Album implements Comparable
{
    enum Type {SECTION, STREAMING, DIR, M3U, UNKNOWN};

    private String
	title = null,
	url = null,
	path = null;

    private Type type = null;
    private Integer volume = null;

    Type getType()
    {
	return this.type != null?type:Type.UNKNOWN;
    }

    void setType(Type type)
    {
	this.type = type;
    }

    String getTitle()
    {
	return title != null?title:"";
    }

    void setTitle(String title)
    {
	this.title = title;
    }

    String getUrl()
    {
	return url;
    }

    void setUrl(String url)
    {
	this.url = url;
    }

    String getPath()
    {
	return path;
    }

    void setPath(String path)
    {
	this.path = path;
    }

    int getVolume()
    {
	if (volume == null)
	    return Player.MAX_VOLUME;
	return Math.min(Math.max(volume.intValue(), Player.MIN_VOLUME), Player.MAX_VOLUME);
    }

    void setVolume(int volume)
    {
	this.volume = new Integer(volume);
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
