
package org.luwrain.app.player;

import org.luwrain.core.*;

final class AlbumItem
{
    final String url;
    final String title;

    AlbumItem(String url, String title)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(title, "title");
	this.url = url;
	this.title = title;
    }

    @Override public String toString()
    {
	return title;
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof AlbumItem))
	    return false;
	return url.equals(((AlbumItem)o).url);
    }
}
