/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

public final class Album implements Comparable
{
    public enum Type {SECTION, STREAMING, DIR, M3U, UNKNOWN};

    private String
	title = null,
	url = null,
	path = null;

    private Integer
	volume = null,
	trackNum = null;

    private Type type = null;
    private Boolean savePos = null;
    private Long posMsec = null;

    public Type getType()
    {
	return this.type != null?type:Type.UNKNOWN;
    }

    public void setType(Type type)
    {
	this.type = type;
    }

    public String getTitle()
    {
	return title != null?title.trim():"";
    }

    public void setTitle(String title)
    {
	this.title = title;
    }

    public String getUrl()
    {
	return url != null?url.trim():"";
    }

    public void setUrl(String url)
    {
	this.url = url;
    }

    public String getPath()
    {
	return path != null?path:"";
    }

    public void setPath(String path)
    {
	this.path = path;
    }

    public int getVolume()
    {
	if (volume == null)
	    return Player.MAX_VOLUME;
	return Math.min(Math.max(volume.intValue(), Player.MIN_VOLUME), Player.MAX_VOLUME);
    }

    public void setVolume(int volume)
    {
	this.volume = Integer.valueOf(volume);
    }

    public boolean isSavePosition()
    {
	return savePos != null?savePos.booleanValue():false;
    }

    public void setSavePosition(boolean value)
    {
	this.savePos = Boolean.valueOf(value);
    }

    public int getTrackNum()
    {
	return trackNum != null?trackNum.intValue():0;
    }

    public void setTrackNum(int value)
    {
	this.trackNum = Integer.valueOf(value);
    }

    public long getPosMsec()
    {
	return posMsec != null?posMsec.longValue():0;
    }

    public void setPosMsec(long value)
    {
	this.posMsec = Long.valueOf(value);
    }

    public boolean isSection()
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
