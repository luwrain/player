
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

class ControlArea extends NavigationArea
{
    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;

    private String opPlayPause, opStop, opPrevTrack, opNextTrack;

ControlArea(Luwrain luwrain, Actions actions,
	       Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strigns");
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	opPlayPause = strings.opPlayPause();
	opStop = strings.opStop();
	opPrevTrack = strings.opPrevTrack();
	opNextTrack = strings.opNextTrack();
    }

    @Override public int getLineCount()
    {
	return 7;
    }

    @Override public String getLine(int index)
    {
	switch(index)
	{
	case 0:
	    return "";
	case 1:
	    return "";
	case 2:
	    return "";
	case 3:
	    return opPlayPause;
	case 4:
	    return opStop;
	case 5:
	    return opPrevTrack;
	case 6:
	    return opNextTrack;
	default:
	    return "";
	}
    }

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    actions.goToTree();
			    return true;
			case BACKSPACE:
			    actions.goToTree();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	return strings.controlAreaName();
    }

    @Override public void announceLine(int index, String line)
    {
	if (line == opPlayPause || line == opStop ||
line == opPrevTrack || line == opNextTrack)
	    luwrain.playSound(Sounds.LIST_ITEM);
	if (line.isEmpty())
	    luwrain.hint(Hints.EMPTY_LINE); else
	    luwrain.say(line);
    }
}
