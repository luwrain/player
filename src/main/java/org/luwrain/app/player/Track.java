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

import java.util.*;

import org.luwrain.core.*;

final class Track
{
    final String url;
    final Map<String, TrackInfo> trackInfoMap;

    Track(String url, Map<String, TrackInfo> trackInfoMap)
    {
	NullCheck.notEmpty(url, "url");
	NullCheck.notNull(trackInfoMap, "trackInfoMap");
	this.url = url;
	this.trackInfoMap = trackInfoMap;
    }

    String getTitle()
    {
	final TrackInfo info = trackInfoMap.get(url);
	if (info == null)
	    return url;
	final StringBuilder b = new StringBuilder();
	b.append(info.artist).append(" - ").append(info.title);
	return new String(b);
    }

    @Override public String toString()
    {
	return getTitle();
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof Track))
	    return false;
	return url.equals(((Track)o).url);
    }
}
