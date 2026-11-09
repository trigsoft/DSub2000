package github.paroj.dsub2000.fragments;

import org.junit.Test;

import github.paroj.dsub2000.domain.MusicDirectory;
import github.paroj.dsub2000.domain.MusicDirectory.Entry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SelectDirectoryFragmentTest {
	@Test
	public void recognizesAlbumWhenFolderNameDiffers() {
		MusicDirectory folder = new MusicDirectory();
		folder.setName("2017 Album");
		folder.addChild(track("Album", "Artist"));
		folder.addChild(track(" album ", "Artist"));

		assertTrue(SelectDirectoryFragment.hasAlbumTrackMetadata(folder));
	}

	@Test
	public void rejectsMixedAlbumsAndMissingAlbumMetadata() {
		MusicDirectory folder = new MusicDirectory();
		folder.addChild(track("First album", "Artist"));
		folder.addChild(track("Second album", "Artist"));
		assertFalse(SelectDirectoryFragment.hasAlbumTrackMetadata(folder));

		folder = new MusicDirectory();
		folder.addChild(track("First album", "Artist"));
		folder.addChild(track(" ", "Artist"));
		assertFalse(SelectDirectoryFragment.hasAlbumTrackMetadata(folder));
	}

	@Test
	public void rejectsNestedFoldersButAllowsMissingArtist() {
		MusicDirectory folder = new MusicDirectory();
		folder.addChild(track("Album", "Artist"));
		Entry childFolder = new Entry();
		childFolder.setDirectory(true);
		folder.addChild(childFolder);
		assertFalse(SelectDirectoryFragment.hasAlbumTrackMetadata(folder));

		folder = new MusicDirectory();
		folder.addChild(track("Album", null));
		assertTrue(SelectDirectoryFragment.hasAlbumTrackMetadata(folder));
	}

	private Entry track(String album, String artist) {
		Entry entry = new Entry();
		entry.setAlbum(album);
		entry.setArtist(artist);
		return entry;
	}
}
