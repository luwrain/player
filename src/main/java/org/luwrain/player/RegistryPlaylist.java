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
    private interface Settings
    {
	String getTitle(String defValue);
	void setTitle(String value);
	String getUrl(String defValue);
	boolean getStreaming(boolean defValue);
	void setStreaming(boolean value);
	boolean getHasBookmark(boolean defValue);
	void setHasBookmark(boolean value);
    }

    private Registry registry;
    private Settings settings;

    RegistryPlaylist(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    boolean init(String path)
    {
	NullCheck.notNull(path, "path");
	settings = RegistryProxy.create(registry, path, Settings.class);
	    title = settings.getTitle("");
	    url = settings.getUrl("");
	    streaming = settings.getStreaming(false);
	    hasBookmark = settings.getHasBookmark(false);
	    if (url != null && !url.toLowerCase().endsWith(".m3u"))
	    {
		items.add(url);
		url = "";
	    }
	    return title != null;
    }


}
