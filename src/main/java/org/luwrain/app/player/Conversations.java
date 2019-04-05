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

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

final class Conversations
{
    private final Luwrain luwrain;

    Conversations(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    Album.Type newAlbumType()
    {
	final String dir = "Каталог с музыкальными файлами";//FIXME:
	final String m3u = "M3U-файл";//FIXME:
	final String streaming = "Интернет-радио";//FIXME:
	final Object typeRes = Popups.fixedList(luwrain, "Тип нового плейлиста:", new Object[]{dir, m3u, streaming});//FIXME:
	if (typeRes == null)
	    return null;
	if (typeRes == dir)
	    return Album.Type.DIR;
	if (typeRes == m3u)
	    return Album.Type.M3U;
	if (typeRes == streaming)
	    return Album.Type.STREAMING;
	return null;
    }

    boolean confirmAlbumDeleting(String title)
    {
	NullCheck.notNull(title, "title");
	return Popups.confirmDefaultNo(luwrain, "Удаление плейлиста", "Вы действительно хотите удалить плейлист \"" + title + "\"?");
    }
}
