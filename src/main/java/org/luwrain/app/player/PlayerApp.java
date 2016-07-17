/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

public class PlayerApp implements Application, MonoApp, Actions
{
    static private final int MAIN_LAYOUT_INDEX = 0;
    static private final int PLAYLIST_PROPERTIES_LAYOUT_INDEX = 1;

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private TreeArea treeArea;
    private ListArea playlistArea;
    private ControlArea controlArea;
    private FormArea propertiesFormArea;
    private AreaLayoutSwitch layouts;

    private String arg = null;

    public PlayerApp()
    {
	arg = null;
    }

    public PlayerApp(String arg)
    {
	NullCheck.notNull(arg, "arg");
	this.arg = arg;
    }

    public PlayerApp(String[] args)
    {
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, treeArea, playlistArea, controlArea));
	layouts.add(new AreaLayout(propertiesFormArea));
	base.setListener(controlArea);
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getTreeModel();
	treeParams.name = strings.treeAreaName();
	treeParams.clickHandler = (area, obj)->onTreeClick(obj);

	treeArea = new TreeArea(treeParams){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (actions.commonKeys(event))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    actions.goToPlaylist();
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
			closeApp();
			return true;
		    case PROPERTIES:
			return onTreeProperties();
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	final ListArea.Params params = new ListArea.Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.model = base.getPlaylistModel();
	params.appearance = new DefaultListItemAppearance(params.environment);
	params.clickHandler = (area, index, obj)->onPlaylistClick(index, obj);
	params.name = strings.playlistAreaName();
	params.loadRegularFlags(luwrain.getRegistry());

	playlistArea = new ListArea(params){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			       {
			       case TAB:
				   goToControl();
				   return true;
			       case BACKSPACE:
				   goToTree();
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
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	controlArea = new ControlArea(luwrain, this, strings);

	propertiesFormArea = new FormArea(new DefaultControlEnvironment(luwrain), strings.playlistPropertiesAreaName());
    }

    private boolean onPlaylistClick(int index, Object item)
    {
	NullCheck.notNull(item, "item");
	return base.playPlaylistItem(index);
    }

    private boolean onTreeClick(Object obj)
    {
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	base.onPlaylistClick((Playlist)obj);
	return true;
    }

    @Override public boolean onJump(long offsetMsec)
    {
	base.onJump(offsetMsec);
	return true;
    }

    @Override public void pauseResume()
    {
	base.pauseResume();
    }

    @Override public void stop()
    {
	base.stop();
    }

    @Override public void prevTrack()
    {
    }

    @Override public void nextTrack()
    {
    }

    @Override public boolean commonKeys(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (!event.isSpecial() || event.isModified())
	    return false;
	switch(event.getSpecial())
	{
	case F6:
	    base.stop();
	    return true;
	default:
	    return false;
	}
    }

    @Override public void refreshPlaylist()
    {
	playlistArea.refresh();
    }

    private boolean onTreeProperties()
    {
	final Object obj = treeArea.selected();
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	base.fillPlaylistProperties((Playlist)obj, propertiesFormArea);
	luwrain.announceActiveArea();
	layouts.show(PLAYLIST_PROPERTIES_LAYOUT_INDEX);
	return true;
    }

    @Override public void goToTree()
    {
	luwrain.setActiveArea(treeArea);
    }

    @Override public void goToPlaylist()
    {
	luwrain.setActiveArea(playlistArea);
    }

    @Override public void goToControl()
    {
	luwrain.setActiveArea(controlArea);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }

    @Override public Base getBase()
    {
	return base;
    }

    @Override public void closeApp()
    {
	base.removeListener();
	luwrain.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
