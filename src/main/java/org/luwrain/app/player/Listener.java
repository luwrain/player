
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.player.Playlist;

class Listener  implements org.luwrain.player.Listener
{
    private Luwrain luwrain;
    private ControlArea area;

    Listener(Luwrain luwrain, ControlArea area)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(area, "area");
	this.luwrain = luwrain;
	this.area = area;
    }

    @Override public void onNewPlaylist(final Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	luwrain.runInMainThread(()->area.onNewPlaylist(playlist));
    }

    @Override public void onNewTrack(Playlist playlist, int trackNum)
    {
	luwrain.runInMainThread(()->area.onNewTrack(trackNum));
    }

    @Override public void onTrackTime(Playlist playlist, int trackNum, long msec)
    {
	luwrain.runInMainThread(()->area.onTrackTime(msec));
    }

    @Override public void onPlayerStop()
    {
	luwrain.runInMainThread(()->area.onStop());
    }
}
