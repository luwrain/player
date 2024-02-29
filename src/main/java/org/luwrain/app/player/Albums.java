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
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Albums extends ArrayList<Album> implements EditableListArea.Model<Album>
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;
    static final Type ALBUM_LIST_TYPE = new TypeToken<List<Album>>(){}.getType();

    private transient final Gson gson = new Gson();
    private transient final Luwrain luwrain;
    private transient final Settings sett;

    Albums(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.sett = Settings.create(luwrain.getRegistry());
	load();
    }

    private synchronized void load()
    {
	clear();
	final List<Album> res = gson.fromJson(sett.getAlbums(""), ALBUM_LIST_TYPE);
	if (res == null)
	    return;
	addAll(res);
		        }

        synchronized void save()
    {
	final String value = gson.toJson(this);
	sett.setAlbums(value);
    }

    synchronized int addAlbum(int pos, Album album)
    {
	NullCheck.notNull(album, "album");
	if (pos < 0 || pos >= size())
	{
	    add(album);
	    	save();
	    return size() - 1;
	}
	add(pos, album);
	save();
	return pos;
    }

    synchronized void deleteAlbum(int index) throws IOException
    {
	if (index < 0 || index >= size())
	    throw new IllegalArgumentException("index (" + String.valueOf(index) + ") must be non-negative and less than " + String.valueOf(size()));
	remove(index);
	save();
    }

    @Override public synchronized boolean addToModel(int pos, java.util.function.Supplier<Object> supplier)
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
	    addAll(pos, Arrays.asList(Arrays.copyOf(newObjs, newObjs.length, Album[].class)));
	save();
	return true;
    }

    @Override public synchronized boolean removeFromModel(int posFrom, int posTo)
    {
	removeRange(posFrom, posTo);
	save();
	return true;
    }

    @Override public synchronized int getItemCount()
    {
	return size();
    }

    @Override public synchronized Album getItem(int index)
    {
	return get(index);
    }

    @Override public synchronized void refresh()
    {
    }
}
