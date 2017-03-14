
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;

class PlaylistComparator implements Comparator
{
    private final Base base;

    PlaylistComparator(Base base)
    {
	NullCheck.notNull(base, "base");
	this.base = base;
    }

    @Override public int compare(Object o1, Object o2)
    {
	NullCheck.notNull(o1, "oo1");
	NullCheck.notNull(o2, "oo2");
	if (!(o1 instanceof String) || !(o2 instanceof String))
	    return o1.toString().compareTo(o2.toString());
	final String title1 = base.getTrackTextAppearance((String)o1);
	final String title2 = base.getTrackTextAppearance((String)o2);
	return title1.compareTo(title2);
    }
}
