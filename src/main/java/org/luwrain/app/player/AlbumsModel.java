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
import org.luwrain.controls.*;

final class AlbumsModel implements ListArea.Model 
{
    private Object[] items = new Object[0];

    void setAlbums(Album[] albums)
    {
	NullCheck.notNullItems(albums, "albums");
	items = albums;
    }

    @Override public int getItemCount()
    {
	return items.length;
    }

    @Override public Object getItem(int index)
    {
	if (index < 0 || index >= items.length)
	    return null;
	return items[index];
    }

    @Override public void refresh()
    {
    }
}
