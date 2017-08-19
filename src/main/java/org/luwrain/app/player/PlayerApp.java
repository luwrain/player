/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import java.util.*;
import java.io.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.popups.*;

public class PlayerApp implements Application, MonoApp
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;

    private ListArea playlistsArea = null;
    private ListArea playlistArea = null;
    private ControlArea controlArea = null;
    private AreaLayoutHelper layout = null;

    private final Playlist startingPlaylist;

    public PlayerApp()
    {
	startingPlaylist = null;
    }

    public PlayerApp(String[] args)
    {
	startingPlaylist = null;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	if (base.player == null)
	    return new InitResult(InitResult.Type.FAILURE);
	this.actions = new Actions(luwrain, base, strings);
	createAreas();
	this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
	    }, new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, playlistsArea, playlistArea, controlArea));
	base.setListener(new Listener(luwrain, this, controlArea));
	if (base.getCurrentPlaylist() != null)
	    base.setNewCurrentPlaylist(playlistArea, base.getCurrentPlaylist());
	if (startingPlaylist != null)
	    base.player.play(startingPlaylist.toGeneralPlaylist(), 0, 0);
	return new InitResult();
    }

    private void createAreas()
    {
	final ListArea.Params playlistsParams = new ListArea.Params();
	playlistsParams.context = new DefaultControlEnvironment(luwrain);
	playlistsParams.model = base.playlistsModel;
	playlistsParams.name = strings.treeAreaName();
	playlistsParams.clickHandler = (area, index, obj)->actions.onPlaylistsClick(playlistArea, obj);

	playlistsParams.appearance = new ListUtils.DoubleLevelAppearance(playlistsParams.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };

	playlistsArea = new ListArea(playlistsParams){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (commonKeys(event))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(playlistArea);
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			if (ActionEvent.isAction(event, "add-playlist"))
			    return actions.onAddPlaylist();
			return false;
		    case PROPERTIES:
			return onPlaylistProps();
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getPlaylistsActions();
		}
	    };

	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlEnvironment(luwrain);
	params.model = base.newPlaylistModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context);//new PlaylistAppearance(luwrain, base);
	params.clickHandler = (area, index, obj)->onPlaylistClick(index, obj);
	params.name = strings.playlistAreaName();

	playlistArea = new ListArea(params){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (commonKeys(event))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(controlArea);
			    return true;
			case BACKSPACE:
			    luwrain.setActiveArea(playlistsArea);
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case ACTION:
			return false;
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getPlaylistActions();
		}
	    };

	controlArea = new ControlArea(luwrain, this, base, strings);
    }

    private boolean onPlaylistClick(int index, Object item)
    {
	NullCheck.notNull(item, "item");
	return base.playPlaylistItem(index);
    }

    void pauseResume()
    {
	actions.pauseResume();
    }

    void stop()
    {
	actions.stop();
    }

    void prevTrack()
    {
	base.prevTrack();
    }

    void nextTrack()
    {
	base.nextTrack();
    }

    boolean commonKeys(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isModified())
	    return false;
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case F5:
		pauseResume();
		return true;
	    case ESCAPE:
	    case F6:
		stop();
		return true;
	    case F7:
		prevTrack();
		return true;
	    case F8:
		nextTrack();
		return true;
	    default:
		return false;
	    }
	switch(event.getChar())
	{
	    case '-':
		actions.jump(-5000);
		return true;
	    case '=':
		actions.jump(5000);
		return true;
	case '[':
	    actions.jump(-60000);
	    return true;
	case ']':
	    actions.jump(60000);
	    return true;
	default:
	    return false;
	}
    }

    void onNewPlaylist(org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	base.setNewCurrentPlaylist(playlistArea, playlist);
    }

    private boolean onPlaylistProps()
    {
	final Object obj = playlistsArea.selected();
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	final Playlist playlist = (Playlist)obj;
	final FormArea area = new FormArea(new DefaultControlEnvironment(luwrain), strings.playlistPropertiesAreaName()) {
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
case ESCAPE:
    layout.closeTempLayout();
    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			//		    case OK:
	/*
	playlistInEdit.setPlaylistTitle(area.getEnteredText("title"));
	playlistInEdit.setPlaylistUrl(area.getEnteredText("url"));
	playlistInEdit = null;
	playlistsModel.setPlaylists(player.loadRegistryPlaylists());
	*/
			//			return closeTreeProperties(true );
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
	area.addEdit("title", strings.playlistPropertiesAreaTitle(), playlist.getPlaylistTitle());
	//	area.addEdit("url", strings.playlistPropertiesAreaUrl(), playlist.getPlaylistUrl());
	layout.openTempArea(area);
	return true;
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
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
