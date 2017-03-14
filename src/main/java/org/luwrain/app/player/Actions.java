
package org.luwrain.app.player;

import org.luwrain.core.*;

class Actions
{

    private final Luwrain luwrain;
    private final Base base;

    Actions(Luwrain luwrain, Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	this.luwrain = luwrain;
	this.base = base;
    }


    void playPlaylist(Playlist playlist, int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
	base.player.play(playlist, startingTrackNum, startingPosMsec);
	base.onNewPlaylist(playlist);
    }

    void pauseResume()
    {
	base.player.pauseResume();
    }

    void stop()
    {
	base.player.stop();
    }

    void jump(long offsetMsec)
    {
	base.player.jump(offsetMsec);
    }


}
