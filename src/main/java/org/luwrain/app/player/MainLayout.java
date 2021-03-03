/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private final Player player;
    private EditableListArea albumsArea = null;
    private ListArea playlistArea = null;
    private ControlArea controlArea = null;

    private AlbumItem[] tracks = new AlbumItem[0];

    MainLayout(App app, Player player)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(player, "player");
	this.app = app;
	this.player = player;
	this.albumsArea = new EditableListArea(createAlbumsParams()){
		final Actions actions = actions(
						action("add-album", app.getStrings().actionAddAlbum(), new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actAddAlbum)
						);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.getHooks().onAlbumsInput(event, selected()))
			return true;
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent( event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
	    };
	this.playlistArea = new ListArea(createPlaylistParams()){
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    /*
		      if (app.getLuwrain().xRunHooks("luwrain.app.player.areas.playlist.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), null}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
		      return true;
		    */
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };
	final ControlArea.Callback controlCallback = new ControlArea.Callback(){};
	this.controlArea = new ControlArea(app.getLuwrain(), controlCallback, app.getStrings(), "ПАУЗА", "СТОП"){
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    /*
		      if (app.getLuwrain().xRunHooks("luwrain.app.player.areas.control.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event)}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
		      return true;
		    */
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}		    
	    };
    }

    private boolean actAddAlbum()
    {
	final Album.Type type = app.getConv().newAlbumType();
	if (type == null)
	return true;
	if (type == Album.Type.SECTION)
	{
	    final Album album = new Album();
	    album.setType(Album.Type.SECTION);
	    final String title = app.getConv().newSectionTitle();
	    if (title == null)
		return true;
	    album.setTitle(title);
	    final int index = app.getAlbums().addAlbum(albumsArea.selectedIndex(), album);
	    albumsArea.refresh();
	    albumsArea.select(index, false);
	    return true;
	}
	final String title = app.getConv().newAlbumTitle();
	if (title == null)
	    return true;
			final Album album = new Album();
		album.setType(type);
		album.setTitle(title);
	switch(type)
	{
	    case STREAMING:
	    {
		final String url = app.getConv().newStreamingAlbumUrl();
		if (url == null)
		    return true;
				album.getProps().setProperty("url", url);
				break;
	    }
	case DIR:
	    {
		final File path = app.getConv().newDirAlbumPath();
		if (path == null)
		    return true;
		album.getProps().setProperty("path", path.getAbsolutePath());
		break;
	    }
	default:
	    return true;
	     }
	final int index = app.getAlbums().addAlbum(albumsArea.selectedIndex(), album);
		albumsArea.refresh();
		albumsArea.select(index, false);
	return true;
    }

    void onNewPlaylist(Playlist  playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	final String[]  urls = playlist.getTracks();
	this.tracks = new AlbumItem[urls.length];
	for(int i = 0;i < urls.length;i++)
	    this.tracks[i] = new AlbumItem(urls[i], app.trackInfoMap);
	app.fillTrackInfoMap(playlist, playlistArea);
	playlistArea.reset(false);
	playlistArea.refresh();
			    /*
		    if (Utils.isStreamingPlaylist(playlist))
			controlArea.setMode(ControlArea.Mode.PLAYING_STREAMING); else
			controlArea.setMode(ControlArea.Mode.PLAYING);
		    //FIXME:controlArea.setPlaylistTitle(playlist.getPlaylistTitle());
		    controlArea.setTrackTitle("");
		    controlArea.setTrackTime(0);
			    */
    }

    private EditableListArea.Params createAlbumsParams()
    {
	final EditableListArea.Params params = new EditableListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = app.getAlbums();
	params.name = app.getStrings().albumsAreaName();
	params.clickHandler = (area, index, obj)->app.getHooks().onAlbumPlay(obj);
	params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (item instanceof Album)
		    {
			final Album album = (Album)item;
			return album.isSection();
		    }
		    return false;
		    		}
	    };
	params.transition = new ListUtils.DoubleLevelTransition(params.model){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (item instanceof Album)
		    {
			final Album album = (Album)item;
			return album.isSection();
		    }
		    return false;
		}
	    };
	params.clipboardSaver = (area, model, appearance, fromIndex, toIndex, clipboard)->{
	    final List<Album> a = new LinkedList();
	    final List<String> s = new LinkedList();
	    for(int i = fromIndex; i < toIndex;i++)
	    {
		final Album album = (Album)model.getItem(i);
		a.add(album);
		final String text = appearance.getScreenAppearance(album, EnumSet.noneOf(EditableListArea.Appearance.Flags.class));
		s.add(text.substring(appearance.getObservableLeftBound(album), appearance.getObservableRightBound(album)));
	    }
	    clipboard.set(a.toArray(new Album[a.size()]), s.toArray(new String[s.size()]));
	    return true;
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

    private int getPlaylistLen()
    {
	if (!player.hasPlaylist())
	    return 0;
	return player.getPlaylist().getTrackCount();
    }

    private String[] getPlaylistUrls()
    {
	if (!player.hasPlaylist())
	    return new String[0];
	return player.getPlaylist().getTracks();
    }

    private String getTrackTextAppearance(String trackUrl)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	return Utils.getTrackTextAppearanceWithMap(trackUrl, app.trackInfoMap);
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, albumsArea, playlistArea, controlArea);
    }

    private class PlaylistModel implements EditableListArea.Model
    {
	@Override public boolean addToModel(int pos, java.util.function.Supplier supplier)
	{
	    NullCheck.notNull(supplier, "supplier");
	    return false;
	}
	@Override public boolean removeFromModel(int indexFrom, int indexTo)
	{
	    return false;
	}
	@Override public void refresh()
	{
	} 
	@Override public Object getItem(int index)
	{
	    if (index < 0 || index >= tracks.length)
		return "";
	    return tracks[index];
	}
	@Override public int getItemCount()
	{
	    return tracks.length;
	}
    }
}
