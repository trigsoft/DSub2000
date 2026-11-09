/*
  This file is part of Subsonic.
	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with Subsonic. If not, see <http://www.gnu.org/licenses/>.
	Copyright 2015 (C) Scott Jackson
*/

package github.paroj.dsub2000.adapter;

import android.content.Context;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.List;

import github.paroj.dsub2000.domain.Artist;
import github.paroj.dsub2000.domain.MusicDirectory.Entry;
import github.paroj.dsub2000.domain.MusicFolder;
import github.paroj.dsub2000.view.ArtistView;
import github.paroj.dsub2000.view.BasicListView;
import github.paroj.dsub2000.view.FastScroller;
import github.paroj.dsub2000.view.SongView;
import github.paroj.dsub2000.view.UpdateView;

public class ArtistAdapter extends SectionAdapter<Serializable> implements FastScroller.BubbleTextGetter {
	public static int VIEW_TYPE_SONG = 3;
	public static int VIEW_TYPE_ARTIST = 4;
	public static int VIEW_TYPE_MUSIC_FOLDER = 5;

	public ArtistAdapter(Context context, List<Serializable> artists, OnItemClickedListener listener) {
		super(context, artists);
		this.onItemClickedListener = listener;
	}

	@Override
	public UpdateView.UpdateViewHolder onCreateSectionViewHolder(ViewGroup parent, int viewType) {
		UpdateView updateView = null;
		if(viewType == VIEW_TYPE_ARTIST) {
			updateView = new ArtistView(context);
		} else if(viewType == VIEW_TYPE_SONG) {
			updateView = new SongView(context);
		} else if(viewType == VIEW_TYPE_MUSIC_FOLDER) {
			updateView = new BasicListView(context);
		}

		return new UpdateView.UpdateViewHolder(updateView);
	}

	@Override
	public void onBindViewHolder(UpdateView.UpdateViewHolder holder, Serializable item, int viewType) {
		UpdateView view = holder.getUpdateView();
		if(viewType == VIEW_TYPE_ARTIST) {
			view.setObject(item);
		} else if(viewType == VIEW_TYPE_SONG) {
			SongView songView = (SongView) view;
			Entry entry = (Entry) item;
			songView.setObject(entry, checkable && !entry.isVideo());
		} else if(viewType == VIEW_TYPE_MUSIC_FOLDER) {
			view.setObject(((MusicFolder) item).getName());
		}
	}

	@Override
	public int getItemViewType(Serializable item) {
		if(item instanceof Artist) {
			return VIEW_TYPE_ARTIST;
		} else if(item instanceof MusicFolder) {
			return VIEW_TYPE_MUSIC_FOLDER;
		} else {
			return VIEW_TYPE_SONG;
		}
	}

	@Override
	public String getTextToShowInBubble(int position) {
		Object item = getItemForPosition(position);
		if(item instanceof Artist) {
			return getNameIndex(((Artist) item).getName(), true);
		} else {
			return null;
		}
	}
}
