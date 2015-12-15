
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.player.Playlist;

class Listener  implements org.luwrain.player.Listener
{
    private Luwrain luwrain;
    private Area area;

    Listener(Luwrain luwrain, Area area)
    {
	this.luwrain = luwrain;
	this.area = area;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(area, "area");
    }

    @Override public void onNewPlaylist(Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	luwrain.enqueueEvent(new ListenerEvent(area, playlist));
    }
}
