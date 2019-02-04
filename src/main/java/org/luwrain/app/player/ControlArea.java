
package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

class ControlArea extends NavigationArea
{
    enum Mode {STOPPED, PAUSED, PLAYING, PLAYING_STREAMING};

interface Callback
{
}
    
    private final Luwrain luwrain;
    private final Callback callback;
    private final Strings strings;

    private final String pauseResume;
    private final String stop;

    private Mode mode = Mode.STOPPED;
    private String playlistTitle = "";
    private String trackTitle = "";
    private long timeSec = 0;

    ControlArea(Luwrain luwrain, Callback callback, Strings strings,
		 String pauseResume, String stop)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(callback, "callback");
	NullCheck.notNull(strings, "strings");
	NullCheck.notEmpty(pauseResume, "pauseResume");
	NullCheck.notEmpty(stop, "stop");
	this.luwrain = luwrain;
	this.callback = callback;
	this.strings = strings;
	this.pauseResume = pauseResume;
	this.stop = stop;
    }

    void setMode(Mode mode)
    {
	NullCheck.notNull(mode, "mode");
	this.mode = mode;
	luwrain.onAreaNewContent(this);
    }

        void setPlaylistTitle(String value)
    {
	NullCheck.notNull(value, "value");
	this.playlistTitle = value;
	luwrain.onAreaNewContent(this);
    }

    void setTrackTitle(String value)
    {
	NullCheck.notNull(value, "value");
	this.trackTitle = value;
	luwrain.onAreaNewContent(this);
    }

    void setTrackTime(long msec)
    {
	if (msec < 0)
	    throw new IllegalArgumentException("msec (" + msec + ") may not be negative");
	final long sec = msec / 1000;
	if (sec != timeSec)
	{
	    timeSec = sec;
	    luwrain.onAreaNewContent(this);
	}
    }

    @Override public int getLineCount()
    {
	switch(mode)
	{
	case STOPPED:
	    return 1;
	case PLAYING_STREAMING:
	    return 2;
	case PLAYING:
	case PAUSED:
	    if (!playlistTitle.isEmpty())
		return 5;
	    return 4;
	default:
	    return 1;
	}
    }

    @Override public String getLine(int index)
    {
	if (index < 0)
	    return "";
	switch(mode)
	{
	case STOPPED:
	    return "";
	case PLAYING_STREAMING:
	    return index == 0?playlistTitle:"";
	case PLAYING:
	case PAUSED:
	    switch(index)
	    {
	    case 0:
		return getControlStr();
	    case 1:
	    return Utils.getTimeStr(timeSec);
	    case 2:
		return trackTitle;
	    case 3:
		return playlistTitle;
	    default:
		return "";
	    }
	default:
	    return "";
	}
    }

    /*
    @Override public boolean onInputEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case ENTER:
		return onEnter(event);
	    }
	return super.onInputEvent(event);
    }
    */

    @Override public String getAreaName()
    {
	return strings.controlAreaName();
    }
    /*
    @Override public void announceLine(int index, String line)
    {
    }
    */

    private boolean onEnter(KeyboardEvent event)
    {
	return false;
    }

    private String getControlStr()
    {
	if (mode == Mode.PAUSED)
	    return "<< [" + pauseResume + "] " + stop + "  >>";
	return "<<  " + pauseResume + "  " + stop + "  >>";
    }
}
