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

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

final class Conversations
{
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    Album.Type newAlbumType()
    {
	final String dir = strings.albumTypeDir();
	final String m3u = strings.albumTypeM3u();
	final String streaming = strings.albumTypeStreaming();
	final Object typeRes = Popups.fixedList(luwrain, strings.newAlbumTypePopupName(), new Object[]{dir, m3u, streaming});
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

    boolean confirmAlbumDeleting(Album album)
    {
	NullCheck.notNull(album, "album");
	return Popups.confirmDefaultNo(luwrain, strings.albumDeletingPopupName(), strings.albumDeletingPopupText(album.getTitle()));
    }

    String newAlbumTitle()
    {
	return Popups.textNotEmpty(luwrain, "Новы альбом", "Имя нового альбома:", "");
    }

    String newStreamingAlbumUrl()
    {
	return Popups.textNotEmpty(luwrain, "Новый альбом", "Адрес потока радиостанции:", "http://");
    }

    File newDirAlbumPath()
    {
	return Popups.existingDir(luwrain, "Новый альбом", "Каталог с файлами для нового альбома:");
    }
}
