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

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

class Conversations
{
    private final Luwrain luwrain;

    Conversations(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

static class NewPlaylistParams
{
    enum Type {DIRECTORY, STREAMING, WITH_BOOKMARK};

    final Type type;
    final String name;
    final String path;

    NewPlaylistParams(Type type, String name, String path)
    {
	NullCheck.notNull(type, "type");
	NullCheck.notEmpty(name, "name");
	this.type = type;
	this.name = name;
	this.path = path;
    }
}

    NewPlaylistParams addPlaylist()
    {
	final String directory = "Каталог с музыкальными файлами";
	final String withBookmark = "Говорящая книга";
	final String streaming = "Интернет-радио";
	final Object type = Popups.fixedList(luwrain, "Тип нового плейлиста:", new Object[]{directory, withBookmark, streaming});
	if (type == null)
	    return null;
	final String path;
	if (type == directory)
	{
	    final File file = Popups.directory(luwrain, "Добавление плейлиста", "Укажите каталог с музыкальными записями:", null);
	    if (file == null)
		return null;
	}
	return null;
}
}
