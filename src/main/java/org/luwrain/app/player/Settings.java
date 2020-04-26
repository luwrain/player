
package org.luwrain.app.player;

import java.io.*;
import java.util.*;

import org.luwrain.core.*;

interface Settings
{
    static final String LOG_COMPONENT = App.LOG_COMPONENT;
    
        static final String ALBUMS_PATH = "/org/luwrain/player/playlists";
    static final String PLAYER_PATH = "/org/luwrain/player";
    static final String TYPE_VALUE = "type";

    static final String TYPE_DIR = "dir";
        static final String TYPE_STREAMING = "streaming";
    static final String TYPE_M3U = "m3u";


    interface Album
    {
	String getType(String defValue);
	void setType(String value);
	String getProperties(String defValue);
	void setProperties(String value);
	String getTitle(String defValue);
	void setTitle(String value);
    }

    int getVolume(int defValue);
    void setVolume(int value);

    static Settings create(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
		return RegistryProxy.create(registry, PLAYER_PATH, Settings.class);
    }

    static Album createAlbum(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	return RegistryProxy.create(registry, path, Album.class);
    }

    static Properties decodeProperties(String value)
    {
	NullCheck.notNull(value, "value");
	final StringReader r = new StringReader(value);
	final Properties props = new Properties();
	try {
	    props.load(r);
	    return props;
	}
	catch(IOException e)
	{
	    Log.warning(LOG_COMPONENT, "unable to decode albums properties:" + e.getClass().getName() + ":" + e.getMessage());
	    return new Properties();
	}
    }

    static String encodeProperties(Properties props)
    {
	NullCheck.notNull(props, "props");
	final StringWriter w = new StringWriter();
	try {
	    try {
		props.store(w, "");
	    }
	    finally {
		w.flush();
	    }
	    return w.toString();
	}
	catch(IOException e)
	{
	    Log.warning(LOG_COMPONENT, "unable to save the properties:" + e.getClass().getName() + ":" + e.getMessage());
	    return "";
	}
    }
}
