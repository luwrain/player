
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.player.Playlist;

class Listener  implements org.luwrain.player.Listener
{
    private final Luwrain luwrain;
    private final PlayerApp app;
    private final ControlArea area;

    Listener(Luwrain luwrain, PlayerApp app, ControlArea area)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(area, "area");
	this.luwrain = luwrain;
	this.app = app;
	this.area = area;
    }

    @Override public void onNewPlaylist(final Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	luwrain.runInMainThread(()->app.onNewPlaylist(playlist));
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
