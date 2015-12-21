
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.player.Playlist;

class Listener  implements org.luwrain.player.Listener
{
    private Luwrain luwrain;
    private PlayerArea area;

    Listener(Luwrain luwrain, PlayerArea area)
    {
	this.luwrain = luwrain;
	this.area = area;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(area, "area");
    }

    @Override public void onNewPlaylist(final Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	luwrain.runInMainThread(()->area.onNewPlaylist(playlist));
    }

    @Override public void onNewTrack(int trackNum)
    {
	luwrain.runInMainThread(()->area.onNewTrack(trackNum));
    }

    @Override public void onTrackTime(int sec)
    {
	luwrain.runInMainThread(()->area.onTrackTime(sec));
    }

    @Override public void onPlayerStop()
    {
	luwrain.runInMainThread(()->area.onStop());
    }
}
