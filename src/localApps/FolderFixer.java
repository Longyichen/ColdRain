package localApps;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import tables.M3USong;
import tables.M3UTable;
import tables.MetaSong;
import toolkit.LogMaker;
import toolkit.MethodInvoker;
import toolkit.MisUtils;
import toolkit.NewFileWriter;

public class FolderFixer {

    /*
     * Input1: A Folder (For albums) Input2: A M3U Playlist (or a folder containing M3U playlists
     * Output: new M3U files with updated M3U playlist Base Method: 1x folder, 1x m3u playlist, 1x
     * output m3u.
     */
    public static void main(String[] args)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
        CannotReadException, IOException, TagException, ReadOnlyFileException,
        InvalidAudioFrameException, NoSuchMethodException, SecurityException {
        String folderAddr = args[0];
        String m3uAddr = args[1];
        LogMaker.logs("srcFolder: "+folderAddr);
        ArrayList<String> toCheckM3Us = MethodInvoker.singlizeInput(m3uAddr, ".m3u");
        ArrayList<String> sampleFlacList = MethodInvoker.singlizeInput(folderAddr, ".flac");
        Iterator<String> flacIt = sampleFlacList.iterator();
        // step 1: get album name
        MetaSong sampleMetaSong = new MetaSong(flacIt.next());
        String albumName = sampleMetaSong.getAlbum();
        LogMaker.logs("Checked Album- " + albumName);
        // step 2: calculate new folder name
        File origFolder = new File(folderAddr);
        File newFolder = new File(origFolder.getParent(),albumName);
        LogMaker.logs("new folder path: "+newFolder);
        // step 3: rename all song in m3u to new folder
        for (String singleM3U : toCheckM3Us) {
            fixAFolderInM3U(folderAddr, singleM3U, newFolder.toString(), null);
        }
        // step 4: rename the folder for src files.
        MisUtils.renameFolder(origFolder.toString(), newFolder.toString());

    }

    public static void fixAFolderInM3U(String srcFolder, String toFixM3U, String newFolder,
        String outputFolderAddr) throws NoSuchMethodException, SecurityException,
        IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
        // 1: if OutputFolder not specified, use srcFolder
        if (outputFolderAddr == null || outputFolderAddr.length() == 0) {
            outputFolderAddr = srcFolder;
        }
        // 2: get all M3USong in the m3u
        M3UTable tempM3UTable = new M3UTable(toFixM3U);
        // 3: figuring out new folder name
        // 4: to M3USong
        ArrayList<M3USong> m3uSongList = tempM3UTable.getSongArrrayList();
        for (M3USong workingSong : m3uSongList) {
            String preSrc = workingSong.getSrc();
            String afterSrc = preSrc.replace(srcFolder, newFolder);
            workingSong.setSrc(afterSrc);
            LogMaker.logs("Playlist song: "+afterSrc);
        }
        // 5: pack back into M3UTable.
        tempM3UTable.setSongArrrayList(m3uSongList);
        // 6: write into M3U, replace original file.
        NewFileWriter.writeAM3U(tempM3UTable, toFixM3U);
    }

}
