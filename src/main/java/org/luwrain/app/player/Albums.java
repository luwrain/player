/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Albums implements ListArea.Model
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;
    	static final Type ALBUM_LIST_TYPE = new TypeToken<List<Album>>(){}.getType();

    private final Gson gson = new Gson();
    private final Luwrain luwrain;
    private final File albumsFile;
    private final List<Album> albums = new Vector();

    Albums(Luwrain luwrain) throws IOException
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
		final File dataDir = luwrain.getAppDataDir(App.DATA_DIR_NAME).toFile();
this.albumsFile = new File(dataDir, "albums.json");
	load();
    }

    private void load() throws IOException
    {
	this.albums.clear();
	if (!albumsFile.exists() || !albumsFile.isFile())
	    return;
	final List<Album> res;
	final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(albumsFile)));
	try {
	 res = gson.fromJson(r, ALBUM_LIST_TYPE);
	}
    finally {
	r.close();
    }
	if (res == null)
	    return;
	for(Album a: res)
	    if (a != null)
		this.albums.add(a);
		        }

        private void save() throws IOException
    {
	final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(albumsFile)));
	try {
	    gson.toJson(albums, w);
	    w.flush();
	}
	finally {
	    w.close();
	}
		        }

    void add(Album album) throws IOException
    {
	NullCheck.notNull(album, "album");
	albums.add(album);
	save();
    }

    void delete(int index) throws IOException
    {
	if (index < 0 || index >= albums.size())
	    throw new IllegalArgumentException("index (" + String.valueOf(index) + ") must be non-negative and less than " + String.valueOf(albums.size()));
	albums.remove(index);
	save();
    }

    @Override public int getItemCount()
    {
	return albums.size();
    }

    @Override public Album getItem(int index)
    {
	return albums.get(index);
    }

    @Override public void refresh()
    {
    }
}
