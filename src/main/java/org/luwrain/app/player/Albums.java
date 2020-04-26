
package org.luwrain.app.player;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Albums implements ListArea.Model
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    private final Registry registry;

    Albums(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    Album[] loadRegistryAlbums()
    {
	final List<Album> res = new LinkedList();
	registry.addDirectory(Settings.ALBUMS_PATH);
	for(String s: registry.getDirectories(Settings.ALBUMS_PATH))
	{
	    final String path = Registry.join(Settings.ALBUMS_PATH, s);
	    final Album album = loadAlbum(path);
	    if (album != null)
		res.add(album);
	}
	return res.toArray(new Album[res.size()]);
    }

    private Album loadAlbum(String path)
    {
	NullCheck.notEmpty(path, "path");
	Log.debug(LOG_COMPONENT, "loading album from " + path);
	final Settings.Album sett = Settings.createAlbum(registry, path);
	final String typeStr = sett.getType("");
	final Album.Type type;
	switch(typeStr)
	{
	case Settings.TYPE_DIR:
	    type = Album.Type.DIR;
	    break;
	case Settings.TYPE_STREAMING:
	    type = Album.Type.STREAMING;
	    break;
	default:
	    Log.warning(LOG_COMPONENT, "the album of the unknown type \'" + typeStr + "\' in " + path);
	    return null;
	}
	return new Album(type, sett.getTitle(""), Settings.decodeProperties(sett.getProperties("")), path);
    }

    //Returns the path of the newly created album or null in the case of any error
    static String addAlbum(Registry registry, Album.Type type, String title)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(type, "type");
	NullCheck.notEmpty(title, "title");
	registry.addDirectory(Settings.ALBUMS_PATH);
	final int num = Registry.nextFreeNum(registry, Settings.ALBUMS_PATH);
	final String path = Registry.join(Settings.ALBUMS_PATH, String.valueOf(num));
	registry.addDirectory(path);
	final Settings.Album sett = Settings.createAlbum(registry, path);
	switch(type)
	{
	case DIR:
	    sett.setType(Settings.TYPE_DIR);
	    break;
	case STREAMING:
	    sett.setType(Settings.TYPE_STREAMING);
	    break;
	default:
	    return null;
	}
	sett.setTitle(title);
	return path;
    }

    static void deleteAlbum(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	registry.deleteDirectory(path);
    }

    @Override public int getItemCount()
    {
	return 0;
    }

    @Override public Object getItem(int index)
    {
	return "123";
    }

    @Override public void refresh()
    {
    }
}
