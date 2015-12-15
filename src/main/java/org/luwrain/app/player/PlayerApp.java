/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import java.util.*;
import java.io.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.popups.*;

class PlayerApp implements Application, Actions
{
static public final String STRINGS_NAME = "luwrain.player";

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private TreeArea treeArea;
    private PlayerArea controlArea;
    private SimpleArea docArea;

    private String arg = null;

    PlayerApp()
    {
	arg = null;
    }

    PlayerApp(String arg)
    {
	this.arg = arg;
	NullCheck.notNull(arg, "arg");
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	System.out.println("start");
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	//	System.out.println("here");
	this.luwrain = luwrain;
	if (!base.init(luwrain))
	    return false;
	createAreas();
	return true;
    }

    @Override public boolean onTreeClick(Object obj)
    {
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	base.onPlaylistClick((Playlist)obj);
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getTreeModel();
	treeParams.name = "Альбомы и станции";//FIXME:
	treeParams.clickHandler = (area, obj)->actions.onTreeClick(obj);

	treeArea = new TreeArea(treeParams){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	controlArea = new PlayerArea(luwrain, this, strings);
	base.setListener(controlArea);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT, treeArea, controlArea);
    }

    @Override public void closeApp()
    {
	base.removeListener();
	luwrain.closeApp();
    }
}
