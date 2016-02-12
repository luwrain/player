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

import java.util.*;

class PlaylistBase implements Playlist
{
protected String title;
    protected String url;
    protected boolean streaming = false;
    protected boolean hasBookmark = false;
protected final Vector<String> items = new Vector<String>();

    @Override public String getPlaylistTitle()
    {
	return title;
    }

    @Override public String[] getPlaylistItems()
    {
	return items.toArray(new String[items.size()]);
    }

    @Override public boolean isStreaming()
    {
	return streaming;
    }

    @Override public boolean hasBookmark()
    {
	return hasBookmark;
    }

    @Override public String toString()
    {
	return title;
    }


}
