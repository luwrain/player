/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

final class Albums implements EditableListArea.Model
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;
    static final Type ALBUM_LIST_TYPE = new TypeToken<List<Album>>(){}.getType();

    private final Gson gson = new Gson();
    private final Luwrain luwrain;
    private final Settings sett;
    private final List<Album> albums = new ArrayList();

    Albums(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.sett = Settings.create(luwrain.getRegistry());
	load();
    }

    private void load()
    {
	this.albums.clear();
	final List<Album> res = gson.fromJson(sett.getAlbums(""), ALBUM_LIST_TYPE);
	if (res == null)
	    return;
	this.albums.addAll(res);
		        }

        private void save()
    {
	final String value = gson.toJson(albums);
	sett.setAlbums(value);
		        }

    int add(int pos, Album album)
    {
	NullCheck.notNull(album, "album");
	if (pos < 0 || pos >= albums.size())
	{
	    albums.add(album);
	    	save();
	    return albums.size() - 1;
	}
	albums.add(pos, album);
	save();
	return pos;
    }

    void delete(int index) throws IOException
    {
	if (index < 0 || index >= albums.size())
	    throw new IllegalArgumentException("index (" + String.valueOf(index) + ") must be non-negative and less than " + String.valueOf(albums.size()));
	albums.remove(index);
	save();
    }

    @Override public boolean clearModel()
    {
	this.albums.clear();
	save();
	return true;
    }

    @Override public boolean addToModel(int pos, java.util.function.Supplier supplier)
    {
	NullCheck.notNull(supplier, "supplier");
	final Object supplied = supplier.get();
	if (supplied == null)
	    return false;
	final Object[] newObjs;
	if (supplied instanceof Object[])
	    newObjs = (Object[])supplied; else
	    newObjs = new Object[]{supplied};
	if (newObjs.length == 0)
	    return false;
	for(Object o: newObjs)
	    if (!(o instanceof Album))
	    {
				Log.error(LOG_COMPONENT, "illegal class of album object: " + o.getClass().getName());

		return false;
	    }
	    this.albums.addAll(pos, Arrays.asList(Arrays.copyOf(newObjs, newObjs.length, Album[].class)));
	save();
	return true;
    }

    @Override public boolean removeFromModel(int pos)
    {
	this.albums.remove(pos);
	save();
	return true;
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
