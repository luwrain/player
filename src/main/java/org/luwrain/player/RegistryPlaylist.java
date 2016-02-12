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

import org.luwrain.core.*;

class RegistryPlaylist extends PlaylistBase
{
    private interface Params
    {
	String getTitle(String defValue);
	String getUrl(String defValue);
	boolean getStreaming(boolean defValue);
	boolean getHasBookmark(boolean defValue);
    }

    private Registry registry;

    RegistryPlaylist(Registry registry)
    {
	this.registry = registry;
	NullCheck.notNull(registry, "registry");
    }

    boolean init(String path)
    {
	NullCheck.notNull(path, "path");
	final Params params = RegistryProxy.create(registry, path, Params.class);
	try {
	    title = params.getTitle("");
	    url = params.getUrl("");
	    streaming = params.getStreaming(false);
	    hasBookmark = params.getHasBookmark(false);

	    if (url != null && !url.toLowerCase().endsWith(".m3u"))
	    {
		items.add(url);
		url = "";
	    }
	    return title != null;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
    }
}
