/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.player;

import org.luwrain.core.NullCheck;

public class SingleLocalFilePlaylist implements Playlist
{
    private String uri ;

    public SingleLocalFilePlaylist(String uri)
    {
	NullCheck.notNull(uri, "uri");
	this.uri = uri;
    }

    @Override public String getPlaylistTitle()
    {
	return "-";
    }

    @Override public String[] getPlaylistItems()
    {
	return new String[]{uri};
    }

    @Override public boolean isStreaming()
    {
	return false;
    }

    @Override public boolean hasBookmark()
    {
	return false;
    }
}
