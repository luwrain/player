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
import org.luwrain.app.player.layouts.*;

final class MainLayout extends LayoutBase
{
    static final int
	STEP_VOLUME = 5,
	STEP_JUMP = 5000;

    static private final InputEvent
	KEY_PAUSE_RESUME = new InputEvent(' '),
	KEY_NEXT_TRACK  = new InputEvent('=', EnumSet.of(Modifiers.ALT)),
	KEY_PREV_TRACK = new InputEvent('-', EnumSet.of(Modifiers.ALT)),
	KEY_VOLUME_PLUS = new InputEvent('+', EnumSet.of(Modifiers.SHIFT)),
	KEY_VOLUME_MINUS = new InputEvent('_', EnumSet.of(Modifiers.SHIFT)),
    	KEY_JUMP_FORWARD = new InputEvent('='),
	KEY_JUMP_BACKWARD = new InputEvent('-');

    private final App app;
    private final Player player;
    final EditableListArea<Album> albumsArea;
    final ListArea<Track> playlistArea;
    final ControlArea controlArea ;
    private Track[] tracks = new Track[0];

    MainLayout(App app, Player player)
    {
	super(app);
	this.app = app;
	this.player = player;
	final ActionInfo
	actionPauseResume = action("pause-resume", app.getStrings().actionPauseResume(), KEY_PAUSE_RESUME, app.getPlayer()::pauseResume),
	actionNextTrack = action("next-track", app.getStrings().actionNextTrack(), KEY_NEXT_TRACK, ()->actTrack(1)),
	actionPrevTrack = action("prev-track", app.getStrings().actionPrevTrack(), KEY_PREV_TRACK, ()->actTrack(-1)),
	actionVolumePlus = action("volume-plus", app.getStrings().actionVolumePlus(), KEY_VOLUME_PLUS, ()->actVolume(STEP_VOLUME)),
	actionVolumeMinus = action("volume-minus", app.getStrings().actionVolumeMinus(), KEY_VOLUME_MINUS, ()->actVolume(-1 * STEP_VOLUME)),
	actionJumpForward = action("jump-forward", app.getStrings().actionJumpForward(), KEY_JUMP_FORWARD, ()->app.getPlayer().jump(STEP_JUMP)),
	actionJumpBackward = action("jump-backward", app.getStrings().actionJumpBackward(), KEY_JUMP_BACKWARD, ()->app.getPlayer().jump(-1 * STEP_JUMP));
	{
	    final EditableListArea.Params<Album> params = new EditableListArea.Params<>();
	    params.context = getControlContext();
	    params.model = app.getAlbums();
	    params.name = app.getStrings().albumsAreaName();
	    params.clickHandler = (area, index, obj)->app.starting.play((Album)obj);
	    params.appearance = new ListUtils.DoubleLevelAppearance<Album>(params.context){
		    @Override public boolean isSectionItem(Album album) { return MainLayout.this.isSectionItem(album); }
		};
	    params.transition = new ListUtils.DoubleLevelTransition<Album>(params.model) {
		    @Override public boolean isSectionItem(Album album) { return MainLayout.this.isSectionItem(album); }
		};
	    params.clipboardSaver = (area, model, appearance, fromIndex, toIndex, clipboard)->{
		final List<Album> a = new ArrayList<>();
		final List<String> s = new ArrayList<>();
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
	    this.albumsArea = new EditableListArea<Album>(params){
		    @Override public boolean onSystemEvent(SystemEvent event)
		    {
			if (event.getType() == SystemEvent.Type.REGULAR)
			    switch(event.getCode())
			    {
			    case PROPERTIES:
			    return onAlbumProperties();
			    }
			return super.onSystemEvent(event);
		    }
		};
	}
	final Actions albumsActions = actions(
					      action("add-album", app.getStrings().actionAddAlbum(), new InputEvent(Special.INSERT), MainLayout.this::actAddAlbum),
					      actionPauseResume,
					      actionNextTrack, actionPrevTrack,
					      actionJumpForward, actionJumpBackward,
					      actionVolumePlus, actionVolumeMinus
					      );
	this.playlistArea = new ListArea<>(listParams((params)->{
		    params.name = app.getStrings().playlistAreaName();
		    params.model = new ListUtils.ArrayModel<>(()->tracks);
		    params.clickHandler = (area, index, track)->{
			final Playlist playlist = app.getPlayer().getPlaylist();
			if (playlist == null)
			    return false;
			if (index >= playlist.getTrackCount())
			    return false;
			app.getPlayer().playTrack(index);
			return true;
		    };
		}));
	final Actions playlistActions = actions(
						actionPauseResume,
						actionNextTrack, actionPrevTrack,
						actionJumpForward, actionJumpBackward,
						actionVolumePlus, actionVolumeMinus
						);
	this.controlArea = new ControlArea(app, getControlContext(), app.getStrings());
	final Actions controlActions = actions(
					       actionPauseResume,
					       actionNextTrack, actionPrevTrack,
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
		album.setUrl(url);
		break;
	    }
	case DIR:
	    {
		final File path = app.getConv().dirAlbumPath();
		if (path == null)
		    return true;
		album.setPath(path.getAbsolutePath());
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

    private boolean actTrack(int pos)
    {
	final Playlist playlist = app.getPlayer().getPlaylist();
	if (playlist == null)
	    return false;
	int index = app.getPlayer().getTrackNum();
	if (index < 0 || index >= playlist.getTrackCount())
	    return false;
	index += pos;
	if (index < 0 || index >= playlist.getTrackCount())
	    return false;
	return app.getPlayer().playTrack(index);
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
	this.tracks = new Track[playlist.getTrackCount()];
	for(int i = 0;i < tracks.length;i++)
	    this.tracks[i] = new Track(playlist.getTrackUrl(i), app.trackInfoMap);
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

    private boolean onAlbumProperties()
    {
	final Album album = albumsArea.selected();
	if (album == null)
	    return false;
	final ActionHandler closing = ()->{
	    app.getAlbums().save();
		    app.setAreaLayout(this);
		    setActiveArea(albumsArea);
		    return true;
	};
	final LayoutBase layout;
	switch(album.getType())
	{
	case STREAMING:
layout = new StreamAlbumPropertiesLayout(app, album, closing);
break;
	case DIR:
layout = new DirAlbumPropertiesLayout(app, album, closing);
break;
	default:
	    return false;
	}
	app.setAreaLayout(layout);
	getLuwrain().announceActiveArea();
	return true;
    }

    private String getTrackTextAppearance(String trackUrl)
    {
	return Utils.getTrackTextAppearanceWithMap(trackUrl, app.trackInfoMap);
    }

boolean isSectionItem(Object item)
		{
		    if (item instanceof Album)
		    {
			final Album album = (Album)item;
			return album.isSection();
		    }
		    return false;
		}
}
