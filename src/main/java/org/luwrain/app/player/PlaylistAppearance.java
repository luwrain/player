
package org.luwrain.app.player;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class PlaylistAppearance implements ListArea.Appearance
{
    private final Luwrain luwrain;
    private final Base base;
    //    private final Strings strings;

    PlaylistAppearance(Luwrain luwrain, Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	//	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.base = base;
	//	this.strings = strings;
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (!(item instanceof String))
	    return;
	final String url = (String)item;
	final String value = base.getTrackTextAppearance(url);
	if (value == null || value.isEmpty())
	{
	    luwrain.hint(Hints.EMPTY_LINE);
	    return;
	}
	luwrain.silence();
	luwrain.playSound(Sounds.LIST_ITEM);
	luwrain.say(value);
    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (!(item instanceof String))
	    return "";
	final String url = (String)item;
	final String value = base.getTrackTextAppearance(url);
	if (value == null)
	    return "";
	return value;
    }

    @Override public int getObservableLeftBound(Object item)
    {
	    return 0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	NullCheck.notNull(item, "item");
	return getScreenAppearance(item, EnumSet.noneOf(Flags.class)).length();
    }
}
