
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.player.Playlist;

class ListenerEvent extends ThreadSyncEvent
{
    enum Type {NEW_PLAYLIST};

    private Type type;
    private Playlist playlist;

    ListenerEvent(Area area, Playlist playlist)
    {
	super(area);
	this.playlist = playlist;
    }

    Type type()
    {
	return type;
    }

    Playlist playlist()
    {
	return playlist;
    }

}
