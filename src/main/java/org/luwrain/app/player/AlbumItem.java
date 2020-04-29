
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;

final class AlbumItem
{
    final String url;
    final Map<String, TrackInfo> trackInfoMap;

    AlbumItem(String url, Map<String, TrackInfo> trackInfoMap)
    {
	NullCheck.notEmpty(url, "url");
	NullCheck.notNull(trackInfoMap, "trackInfoMap");
	this.url = url;
	this.trackInfoMap = trackInfoMap;
    }

    String getTitle()
    {
	final TrackInfo info = trackInfoMap.get(url);
	if (info == null)
	    return url;
	final StringBuilder b = new StringBuilder();
	b.append(info.artist).append(" - ").append(info.title);
	return new String(b);
    }

    @Override public String toString()
    {
	return getTitle();
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof AlbumItem))
	    return false;
	return url.equals(((AlbumItem)o).url);
    }
}
