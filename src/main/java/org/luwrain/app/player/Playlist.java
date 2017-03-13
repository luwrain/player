
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;

class Playlist extends org.luwrain.player.DefaultPlaylist
{
    enum Flags {HAS_BOOKMARK, STREAMING };

    final Set<Flags> flags;

    Playlist(String title, String[] items)
    {
	super(title, items, 0, 0, null);
	this.flags = EnumSet.noneOf(Flags.class);
    }

    Playlist(String title, String[] items, Set<Flags> flags)
    {
	super(title, items, 0, 0, null);
	NullCheck.notNull(flags, "flags");
	this.flags = flags;
    }

    Set<Flags> getFlags()
    {
	return flags;
    }
}
