/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

interface Settings
{
    static final String PLAYLISTS_PATH = "/org/luwrain/player/playlists";
    static final String TYPE_VALUE = "type";

    interface Base
    {
	String getTitle(String defValue);
	void setTitle(String value);
    }

    interface StreamingPlaylist extends Base
    {
	String getUrl(String defValue);
	void setUrl(String value);
    }

    interface DirectoryPlaylist extends Base
    {
	String getPath(String defValue);
	void setPath(String  value);
    }

    static DirectoryPlaylist createDirectoryPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
return RegistryProxy.create(registry, path, DirectoryPlaylist.class);
    }

    static StreamingPlaylist createStreamingPlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
return RegistryProxy.create(registry, path, StreamingPlaylist.class);
    }
}
