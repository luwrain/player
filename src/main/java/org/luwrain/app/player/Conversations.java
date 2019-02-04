
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
	enum Type {DIRECTORY, M3U, STREAMING, WITH_BOOKMARK_DIR, WITH_BOOKMARK_M3U};

	final Type type;
	final String name;
	final String arg;

	NewPlaylistParams(Type type, String name, String arg)
	{
	    NullCheck.notNull(type, "type");
	    NullCheck.notEmpty(name, "name");
	    NullCheck.notNull(arg, "arg");
	    this.type = type;
	    this.name = name;
	    this.arg = arg;
	}
    }

    NewPlaylistParams addPlaylist()
    {
	final String directory = "Каталог с музыкальными файлами";//FIXME:
	final String m3u = "M3U-плейлист";//FIXME:
	final String withBookmarkDir = "Говорящая книга";//FIXME:
	final String withBookmarkM3u = "Говорящая книга с M3U-плейлистом";//FIXME:
	final String streaming = "Интернет-радио";//FIXME:
	final Object typeRes = Popups.fixedList(luwrain, "Тип нового плейлиста:", new Object[]{directory, m3u, withBookmarkDir, withBookmarkM3u, streaming});//FIXME:
	if (typeRes == null)
	    return null;
	final NewPlaylistParams.Type type;
	final String arg;
	if (typeRes == directory)
	{
	    final File file = Popups.directory(luwrain, "Добавление плейлиста", "Каталог с музыкальными записями:", null);
	    if (file == null)
		return null;
	    type = NewPlaylistParams.Type.DIRECTORY;
	    arg = file.getAbsolutePath();
	} else
	    if (typeRes == m3u)
	    {
		final File file = Popups.path(luwrain, "Добавление плейлиста", "Путь к M3U-файлу:", (fileToCheck, announce)->{
			if (fileToCheck.isDirectory())
			{
			    if (announce)
				luwrain.message("" + fileToCheck + " является каталогом", Luwrain.MessageType.ERROR);//FIXME:
			    return false;
			}
			return true;
		    });
		if (file == null)
		    return null;
		type = NewPlaylistParams.Type.M3U;
		arg = file.getAbsolutePath();
	    } else
		if (typeRes == withBookmarkDir)
		{
		    final File file = Popups.directory(luwrain, "Добавление плейлиста", "Каталог с говорящей книгой:", null);
		    if (file == null)
			return null;
		    type = NewPlaylistParams.Type.WITH_BOOKMARK_DIR;
		    arg = file.getAbsolutePath();
		} else
		    if (typeRes == withBookmarkM3u)
		    {
			final File file = Popups.path(luwrain, "Добавление плейлиста", "Путь к M3U-файлу говорящей книги:", (fileToCheck, announce)->{
				if (fileToCheck.isDirectory())
				{
				    if (announce)
					luwrain.message("" + fileToCheck + " является каталогом", Luwrain.MessageType.ERROR);//FIXME:
				    return false;
				}
				return true;
			    });
			if (file == null)
			    return null;
			type = NewPlaylistParams.Type.WITH_BOOKMARK_M3U;
			arg = file.getAbsolutePath();
		    } else
			if (typeRes == streaming)
			{
			    arg = Popups.simple(luwrain, "Добавление интернет-радио", "URL потокового вещания интернет-радио:", "http://");
			    if (arg == null || arg.trim().isEmpty())
				return null;
			    type = NewPlaylistParams.Type.STREAMING;
			} else
			    return null;
	final String title = Popups.simple(luwrain, "Добавление нового плейлиста", "Название нового плейлиста:", "");
	if (title == null || title.trim().isEmpty())
	    return null;
	return new NewPlaylistParams(type, title, arg);
    }

    boolean confirmPlaylistDeleting(String title)
    {
	NullCheck.notNull(title, "title");
	return Popups.confirmDefaultNo(luwrain, "Удаление плейлиста", "Вы действительно хотите удалить плейлист \"" + title + "\"?");
    }
}
