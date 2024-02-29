/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

import static org.luwrain.app.player.Utils.*;
import static org.luwrain.core.DefaultEventResponse.*;

class ControlArea extends NavigationArea
{
    enum Mode {STOPPED, PAUSED, PLAYING, PLAYING_STREAMING};

    private final App app;
    private final Strings strings;

    private Mode mode = Mode.STOPPED;
    private String playlistTitle = "";//FIXME:must be album
    private String trackTitle = "";
    private long timeSec = 0;
    private String[] content = new String[]{""};

    ControlArea(App app, ControlContext controlContext, Strings strings)
    {
	super(controlContext);
	this.app = app;
	this.strings = strings;
    }

        @Override public boolean onInputEvent(InputEvent event)
    {
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case ARROW_LEFT:
		return app.getPlayer().jump(-1 * MainLayout.STEP_JUMP);
	    case ALTERNATIVE_ARROW_LEFT:
	    if (!app.getPlayer().jump(-1 * MainLayout.STEP_JUMP))
		return false;
	    app.setEventResponse(text(Sounds.OK, getTimeStr(timeSec)));
	    return true;
	    case ARROW_RIGHT:
		return app.getPlayer().jump(MainLayout.STEP_JUMP);
			    case ALTERNATIVE_ARROW_RIGHT:
	    if (!app.getPlayer().jump(MainLayout.STEP_JUMP))
		return false;
	    app.setEventResponse(text(Sounds.OK, getTimeStr(timeSec)));
	    return true;
	    case ARROW_UP:
		return app.getPlayer().prevTrack();
	    case ARROW_DOWN:
		return app.getPlayer().nextTrack();
	    }
	return super.onInputEvent(event);
    }

    void setMode(Mode mode)
    {
	NullCheck.notNull(mode, "mode");
	this.mode = mode;
	updateText();
    }

    void setPlaylistTitle(String value)
    {
	NullCheck.notNull(value, "value");
	this.playlistTitle = value;
	updateText();
    }

    void setTrackTitle(String value)
    {
	NullCheck.notNull(value, "value");
	this.trackTitle = value;
	updateText();
    }

    void setTrackTime(long msec)
    {
	if (msec < 0)
	    throw new IllegalArgumentException("msec (" + msec + ") may not be negative");
	final long sec = msec / 1000;
	if (sec != timeSec)
	{
	    timeSec = sec;
	    updateText();
	}
    }

    @Override public int getLineCount()
    {
	return content.length;
	    }

    @Override public String getLine(int index)
    {
	if (index < 0)
	    return "";
	return content[index];
    }


    @Override public String getAreaName()
    {
	return strings.controlAreaName();
    }

    private void updateText()
    {
	switch(mode)
	{
	case STOPPED:
	    this.content = new String[]{""};
	    break;
	case PLAYING:
	case PAUSED:
	    this.content = new String[]{
		playlistTitle, trackTitle,
		getTimeStr(timeSec),
		""};
	    break;
	case PLAYING_STREAMING:
	    this.content = new String[]{playlistTitle, ""};
	    break;
	}
	setHotPoint(0, content.length - 1);
	redraw();

    }
    }
