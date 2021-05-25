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
import org.luwrain.core.events.InputEvent.Modifiers;
import org.luwrain.core.events.InputEvent.Special;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase
{
    static private final int
	STEP_VOLUME = 5,
	STEP_JUMP = 5000;

    static private final InputEvent
	KEY_VOLUME_PLUS = new InputEvent('+', EnumSet.of(Modifiers.SHIFT)),
	KEY_VOLUME_MINUS = new InputEvent('_', EnumSet.of(Modifiers.SHIFT)),
    	KEY_JUMP_FORWARD = new InputEvent('='),
	KEY_JUMP_BACKWARD = new InputEvent('-');

    private final App app;
    private final Player player;
    private EditableListArea albumsArea = null;
    private ListArea playlistArea = null;
    private ControlArea controlArea = null;
    private AlbumItem[] tracks = new AlbumItem[0];

    MainLayout(App app, Player player)
    {
	super(app);
	NullCheck.notNull(player, "player");
	this.app = app;
	this.player = player;
	final ActionInfo
	actionVolumePlus = action("volume-plus", app.getStrings().actionVolumePlus(), KEY_VOLUME_PLUS, ()->{ return actVolume(STEP_VOLUME); }),
	actionVolumeMinus = action("volume-minus", app.getStrings().actionVolumeMinus(), KEY_VOLUME_MINUS, ()->{ return actVolume(-1 * STEP_VOLUME); }),
	actionJumpForward = action("jump-forward", app.getStrings().actionJumpForward(), KEY_JUMP_FORWARD, ()->{ return app.getPlayer().jump(STEP_JUMP); }),
	actionJumpBackward = action("jump-backward", app.getStrings().actionJumpBackward(), KEY_JUMP_BACKWARD, ()->{ return app.getPlayer().jump(-1 * STEP_JUMP); });
	{
	    final EditableListArea.Params params = new EditableListArea.Params();
	    params.context = getControlContext();
	    params.model = app.getAlbums();
	    params.name = app.getStrings().albumsAreaName();
	    params.clickHandler = (area, index, obj)->app.starting.play((Album)obj);
	    params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		    @Override public boolean isSectionItem(Object item) { return MainLayout.this.isSectionItem(item); }
		};
	    params.transition = new ListUtils.DoubleLevelTransition(params.model) {
		    @Override public boolean isSectionItem(Object item) { return MainLayout.this.isSectionItem(item); }
		};
	    params.clipboardSaver = (area, model, appearance, fromIndex, toIndex, clipboard)->{
		final List<Album> a = new ArrayList();
		final List<String> s = new ArrayList();
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
	    this.albumsArea = new EditableListArea(params);
	}
	final Actions albumsActions = actions(
					      action("add-album", app.getStrings().actionAddAlbum(), new InputEvent(Special.INSERT), MainLayout.this::actAddAlbum),
					      actionJumpForward, actionJumpBackward,
					      actionVolumePlus, actionVolumeMinus
					      );
	{
	    final ListArea.Params params = new ListArea.Params();
	    params.context = getControlContext();
	    params.model = new PlaylistModel();
	    params.appearance = new ListUtils.DefaultAppearance(params.context);//new PlaylistAppearance(luwrain, base);
	    params.clickHandler = null;
	    params.name = app.getStrings().playlistAreaName();
	    this.playlistArea = new ListArea(params);
	}
	final Actions playlistActions = actions(
						actionJumpForward, actionJumpBackward,
						actionVolumePlus, actionVolumeMinus
						);
	final ControlArea.Callback controlCallback = new ControlArea.Callback(){};
	this.controlArea = new ControlArea(getControlContext(), controlCallback, app.getStrings(), "ПАУЗА", "СТОП");
	final Actions controlActions = actions(
					       actionJumpForward, actionJumpBackward,
					       actionVolumePlus, actionVolumeMinus
					       );
	setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, albumsArea, albumsActions, playlistArea, playlistActions, controlArea, controlActions);
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

    private boolean actVolume(int step)
    {
	final int level = app.getPlayer().getVolume() + step;
	if (level <0 || level > 100)
	    return false;
	app.getPlayer().setVolume(level);
	return true;
    }

    void onNewPlaylist(Playlist  playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	this.tracks = new AlbumItem[playlist.getTrackCount()];
	for(int i = 0;i < tracks.length;i++)
	    this.tracks[i] = new AlbumItem(playlist.getTrackUrl(i), app.trackInfoMap);
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

    private String getTrackTextAppearance(String trackUrl)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	return Utils.getTrackTextAppearanceWithMap(trackUrl, app.trackInfoMap);
    }

boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (item instanceof Album)
		    {
			final Album album = (Album)item;
			return album.isSection();
		    }
		    return false;
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
