/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import org.luwrain.core.*;

class PlaylistItem
{
    final String url;
    final String title;

    PlaylistItem(String url, String title)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(title, "title");
	this.url = url;
	this.title = title;
    }

    @Override public String toString()
    {
	return title;
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof PlaylistItem))
	    return false;
	return url.equals(((PlaylistItem)o).url);
    }
}
