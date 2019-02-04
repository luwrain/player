/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.util.*;
import java.net.*;

import org.luwrain.core.*;

class TracksLoaders
{
    static private final String LOG_COMPONENT = Base.LOG_COMPONENT;


    static Album.TracksLoader newDirectoryLoader(Base base, String dirPath)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(dirPath, "dirPath");
	return ()->{
		final List<String> filesList = new LinkedList<String>();
		loadFilesList(new File(dirPath), new String[0], filesList);
		final String[] items = filesList.toArray(new String[filesList.size()]);
		/*
		final Map<String, TrackInfo> trackInfoMap = new HashMap<String, TrackInfo>();
		for(String s: items)
		    try {
			trackInfoMap.put(s, new TrackInfo(new URL(s)));
		    }
		    catch(IOException e)
		    {
			Log.warning(LOG_COMPONENT, "unable to read tags for " + s + ":" + e.getClass().getName() + ":" + e.getMessage());
		    }
		Arrays.sort(items, new PlaylistComparator(base, trackInfoMap));
		*/
		return items;
	};
    }

    static void loadFilesList(File dir, String[] endings, List<String> res)
    {
	NullCheck.notNull(dir, "dir");
	NullCheck.notNullItems(endings, "endings");
	NullCheck.notNull(res, "res");
	final File[] files = dir.listFiles();
	if (files == null)
	    return;
	for(File f: files)
	{
	    if (f.isDirectory())
	    {
		loadFilesList(f, endings, res);
		continue;
	    }
	    try {
		res.add(f.getAbsoluteFile().toURI().toURL().toString());
	    }
	    catch(java.net.MalformedURLException e)
	    {
		Log.warning(LOG_COMPONENT, "unable to get URL of " + f.getAbsolutePath() + ":" + e.getMessage());
	    }
	}
    }

    static private class PlaylistComparator implements Comparator
    {
	private final Base base;
	private final Map<String, TrackInfo> trackInfoMap;

	PlaylistComparator(Base base, Map<String, TrackInfo> trackInfoMap)
	{
	    NullCheck.notNull(base, "base");
	    NullCheck.notNull(trackInfoMap, "trackInfoMap");
	    this.base = base;
	    this.trackInfoMap = trackInfoMap;
	}

	@Override public int compare(Object o1, Object o2)
	{
	    NullCheck.notNull(o1, "oo1");
	    NullCheck.notNull(o2, "oo2");
	    if (!(o1 instanceof String) || !(o2 instanceof String))
		return o1.toString().compareTo(o2.toString());
	    final String title1 = Utils.getTrackTextAppearanceWithMap((String)o1, trackInfoMap);
	    final String title2 = Utils.getTrackTextAppearanceWithMap((String)o2, trackInfoMap);
	    return title1.compareTo(title2);
	}
    }


}
