/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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

final class MainLayout
{
    private final App app;
    private final Player player;
    private ListArea albumsArea = null;
    private ListArea playlistArea = null;
    private ControlArea controlArea = null;

    MainLayout(App app, Player player)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(player, "player");
	this.app = app;
	this.player = player;
	this.albumsArea = new ListArea(createAlbumsParams()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.getLuwrain().xRunHooks("luwrain.app.player.areas.albums.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), selected()}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			return true;
		    if (event.isSpecial() && !event.isModified())
			if (app.onInputEvent(this, event))
			    return true;
		    return super.onInputEvent( event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };

	this.playlistArea = new ListArea(createPlaylistParams()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.getLuwrain().xRunHooks("luwrain.app.player.areas.playlist.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), null}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			return true;
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };

	final ControlArea.Callback controlCallback = new ControlArea.Callback(){};
	this.controlArea = new ControlArea(app.getLuwrain(), controlCallback, app.getStrings(), "ПАУЗА", "СТОП"){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.getLuwrain().xRunHooks("luwrain.app.player.areas.control.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event)}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			return true;
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
			}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
	    };
    }

    private ListArea.Params createAlbumsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = app.getAlbums();
	params.name = app.getStrings().treeAreaName();
	//	params.clickHandler = clickHandler;
	params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };
		params.transition = new ListUtils.DoubleLevelTransition(params.model){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };
	return params;
    }

    private ListArea.Params createPlaylistParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new PlaylistModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context);//new PlaylistAppearance(luwrain, base);
	//	params.clickHandler = clickHandler;
	params.name = app.getStrings().playlistAreaName();
	return params;
    }

                int getPlaylistLen()
    {
	if (!player.hasPlaylist())
	    return 0;
	return player.getPlaylist().getTrackCount();
    }


        String[] getPlaylistUrls()
    {
	if (!player.hasPlaylist())
	    return new String[0];
	return player.getPlaylist().getTracks();
    }

        String getTrackTextAppearance(String trackUrl)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	return Utils.getTrackTextAppearanceWithMap(trackUrl, app.trackInfoMap);
    }





    private class PlaylistModel implements EditableListArea.EditableModel
    {
	@Override public boolean clearList()
	{
	    return false;
	}
	@Override public boolean addToList(int pos,Clipboard clipboard)
	{
	    return false;
	}
	@Override public boolean removeFromList(int index)
	{
	    return false;
	}
	@Override public void refresh()
	{
	} 
	@Override public Object getItem(int index)
	{
	    if (index < 0 || index >= getPlaylistLen())
		return "";
	    return new AlbumItem(getPlaylistUrls()[index], getTrackTextAppearance(getPlaylistUrls()[index]));
	}
	@Override public int getItemCount()
	{
	    return getPlaylistLen();
	}
    }
}
