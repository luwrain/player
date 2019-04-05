/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class App implements Application, MonoApp
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;

    private ListArea albumsArea = null;
    private ListArea playlistArea = null;
    private ControlArea controlArea = null;
    private AreaLayoutHelper layout = null;

    private final Album startingAlbum;

    App()
    {
	startingAlbum = null;
    }

    App(String[] args)
    {
	startingAlbum = null;
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
	this.actionLists = new ActionLists(strings);
	createAreas();
	this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
	    }, new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, albumsArea, playlistArea, controlArea));
	base.setListener(playlistArea, controlArea);
	/*FIXME:
	if (base.getCurrentPlaylist() != null)
	    base.setNewCurrentPlaylist(playlistArea, base.getCurrentPlaylist());
	*/
	return new InitResult();
    }

    private void createAreas()
    {
	final ListArea.Params albumsParams = new ListArea.Params();
	albumsParams.context = new DefaultControlEnvironment(luwrain);
	albumsParams.model = base.albumsModel;
	albumsParams.name = strings.treeAreaName();
	albumsParams.clickHandler = (area, index, obj)->actions.onAlbumClick(playlistArea, obj);

	albumsParams.appearance = new ListUtils.DoubleLevelAppearance(albumsParams.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };

	albumsArea = new ListArea(albumsParams){
	    
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (luwrain.xRunHooks("luwrain.app.player.areas.albums.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), selected()}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    if (!base.player.hasPlaylist())
				return false;
			    luwrain.setActiveArea(playlistArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}

		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			if (ActionEvent.isAction(event, "add-playlist"))
			    return actions.onAddAlbum(this);
			if (ActionEvent.isAction(event, "delete-playlist"))
			    return actions.onDeleteAlbum(this);
			return false;
		    case PROPERTIES:
			return onPlaylistProps();
		    default:
			return super.onSystemEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actionLists.getPlaylistsActions();
		}
	    };

	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlEnvironment(luwrain);
	params.model = base.newPlaylistModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context);//new PlaylistAppearance(luwrain, base);
	params.clickHandler = (area, index, obj)->base.playPlaylistItem(index);
	params.name = strings.playlistAreaName();

	playlistArea = new ListArea(params){

		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");

		    		    if (luwrain.xRunHooks("luwrain.app.player.areas.playlist.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), null}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(controlArea);
			    return true;
			case BACKSPACE:
			    luwrain.setActiveArea(albumsArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}

		@Override public boolean onSystemEvent(EnvironmentEvent event)
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
			return super.onSystemEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actionLists.getPlaylistActions();
		}
	    };

	final ControlArea.Callback controlCallback = new ControlArea.Callback(){
	    };

	controlArea = new ControlArea(luwrain, controlCallback, strings, "ПАУЗА", "СТОП"){

		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    		    		    if (luwrain.xRunHooks("luwrain.app.player.areas.control.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event)}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(albumsArea);
			    return true;
			case BACKSPACE:
			    luwrain.setActiveArea(playlistArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}

		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}

	    };
    }

    private boolean onPlaylistProps()
    {
	final Object obj = albumsArea.selected();
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	final Album playlist = (Album)obj;
	final FormArea area = new FormArea(new DefaultControlEnvironment(luwrain), strings.playlistPropertiesAreaName()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
case ESCAPE:
    layout.closeTempLayout();
    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
case OK:
    {
	final String title = getEnteredText("title").trim();
	if (title.isEmpty())
	{
	    luwrain.message("Название не может быть пустым", Luwrain.MessageType.ERROR);
	    return true;
	}
	//playlist.sett.setTitle(title);
	albumsArea.refresh();
	layout.closeTempLayout();
	return true;
    }
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	area.addEdit("title", strings.playlistPropertiesAreaTitle(), playlist.getTitle());
	/*
	if (playlist.sett instanceof Settings.DirectoryPlaylist)
	{
	    final Settings.DirectoryPlaylist sett = (Settings.DirectoryPlaylist)playlist.sett;
	    area.addEdit("path", "Каталог с файлами:", sett.getPath(""));
	}
	if (playlist.sett instanceof Settings.StreamingPlaylist)
	{
	    final Settings.StreamingPlaylist sett = (Settings.StreamingPlaylist)playlist.sett;
	    area.addEdit("url", "URL потока вещания:", sett.getUrl(""));//FIXME:
	}
	*/
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
