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
import java.net.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.util.*;

class Utils
{
    static String getTrackTextAppearanceWithMap(String trackUrl, Map<String, TrackInfo> map)
    {
	final String tagText = getTrackTagText(trackUrl, map);
	if (tagText != null)
	    return tagText;
	String name = "";
	try {
	    final File f = Urls.toFile(new URL(trackUrl));
	    if (f == null)
		name = trackUrl; else
		name = f.getName();
	}
	catch(MalformedURLException e)
	{
	    name = trackUrl;
	}
	return name;
    }

    static private String getTrackTagText(String trackUrl, Map<String, TrackInfo> map)
    {
	if (!map.containsKey(trackUrl))
	    return null;
	final StringBuilder b = new StringBuilder();
	final TrackInfo info = map.get(trackUrl);
	if (info == null)
	    return null;
	if (info.artist.trim().isEmpty() && info.title.trim().isEmpty())
	    return null;
	if (!info.artist.trim().isEmpty())
	    b.append(info.artist.trim());
	if (!info.artist.trim().isEmpty() && !info.title.trim().isEmpty())
	    b.append(" - ");
	if (!info.title.trim().isEmpty())
	    b.append(info.title);
	return new String(b);
    }

    static String getTimeStr(long sec)
    {
	final StringBuilder b = new StringBuilder();
	final long min = sec / 60;
	final long seconds = sec % 60;
	if (min < 10)
	    b.append("0" + min); else
	    b.append("" + min);
	b.append(":");
	if (seconds < 10)
	    b.append("0" + seconds); else
	    b.append("" + seconds);
	return new String(b);
    }

    static boolean isStreamingPlaylist(org.luwrain.player.Playlist playlist)
    {
	/*
	NullCheck.notNull(playlist, "playlist");
	if (!playlist.getProperties().containsKey("streaming"))
	    return false;
	final String value = playlist.getProperties().get("streaming");
	if (value == null)
	    return false;
	return value.toLowerCase().equals("true");
	*/
	return false;
    }
}
