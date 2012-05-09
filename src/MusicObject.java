// MusicObject class
// 20120418

import java.io.*;
import java.util.*;

enum Mode {
  MODE_NONE, MODE_ARTIST, MODE_ALBUM_ARTIST, MODE_ALBUM, MODE_TRACK,
  MODE_TITLE, MODE_YEAR, MODE_GENRE, MODE_TRACK_LENGTH, MODE_SONG
}

public class MusicObject implements Serializable, Comparable {
  
  private String id;
  private String artist;
  private String album_artist;
  private String album;
  private int track;
  private String title;
  private int year;
  private String genre;
  private int track_length;
  
  public String getId() {return id;}
  public String getArtist() {return artist;}
  public String getAlbumArtist() {return album_artist;}
  public String getAlbum() {return album;}
  public int getTrack() {return track;}
  public String getTitle() {return title;}
  public int getYear() {return year;}
  public String getGenre() {return genre;}
  public int getTrackLength() {return track_length;}
  
  // setters... needed for factory method :(
  public void setId(String id) {this.id = id;}
  public void setArtist(String artist) {this.artist = artist;}
  public void setAlbumArtist(String album_artist) {this.album_artist = album_artist;}
  public void setAlbum(String album) {this.album = album;}
  public void setTrack(int track) {this.track = track;}
  public void setTitle(String title) {this.title = title;}
  public void setYear(int year) {this.year = year;}
  public void setGenre(String genre) {this.genre = genre;}
  public void setTrackLength(int track_length) {this.track_length = track_length;}
  
  public static MusicObject mobj;
  
  private MusicObject(){}                        
  
  public MusicObject(String id, String artist, String album_artist, String album,
                     int track, String title, int year, String genre, int track_length) {
    this.id = id;
    this.artist = artist;
    this.album_artist = album_artist;
    this.album = album;
    this.track = track;
    this.title = title;
    this.year = year;
    this.genre = genre;
    this.track_length = track_length;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<mobj " + id + "\n");
    sb.append("  artist: " + artist + "\n");
    sb.append("  album_artist: " + album_artist + "\n");
    sb.append("  album: " + album + "\n");
    sb.append("  track: " + track + "\n");
    sb.append("  title: " + title + "\n");
    sb.append("  year: " + year + "\n");
    sb.append("  genre: " + genre + "\n");
    sb.append("  track_length: " + track_length + "\n");
    sb.append(">");
    return sb.toString();
  }
  
  public static MusicObject buildFromFile(File f, String id) {
    MusicObject.mobj = new MusicObject();
    Scanner sc = null;
    try {
      sc = new Scanner(f);
      while(sc.hasNextLine()) {
        String line = sc.nextLine().trim();
        if(line.equals("<?xml ?>")) continue;
        if(line.equals("<song>")) continue;
        if(line.equals("</song>")) continue;
        if(line.startsWith("<artist>")) {
          line = line.replaceFirst("<artist>", "");
          line = line.replaceFirst("</artist>", "");
          mobj.setArtist(line);
          continue;
        }
        if(line.startsWith("<album_artist>")) {
          line = line.replaceFirst("<album_artist>", "");
          line = line.replaceFirst("</album_artist>", "");
          mobj.setAlbumArtist(line);
          continue;
        }
        if(line.startsWith("<album>")) {
          line = line.replaceFirst("<album>", "");
          line = line.replaceFirst("</album>", "");
          mobj.setAlbum(line);
          continue;
        }
        if(line.startsWith("<track>")) {
          line = line.replaceFirst("<track>", "");
          line = line.replaceFirst("</track>", "");
          mobj.setTrack(Integer.parseInt(line));
          continue;
        }
        if(line.startsWith("<title>")) {
          line = line.replaceFirst("<title>", "");
          line = line.replaceFirst("</title>", "");
          mobj.setTitle(line);
          continue;
        }
        if(line.startsWith("<year>")) {
          line = line.replaceFirst("<year>", "");
          line = line.replaceFirst("</year>", "");
          mobj.setYear(Integer.parseInt(line));
          continue;
        }
        if(line.startsWith("<genre>")) {
          line = line.replaceFirst("<genre>", "");
          line = line.replaceFirst("</genre>", "");
          mobj.setGenre(line);
          continue;
        }
        if(line.startsWith("<track_length>")) {
          line = line.replaceFirst("<track_length>", "");
          line = line.replaceFirst("</track_length>", "");
          mobj.setTrackLength(Integer.parseInt(line));
          continue;
        }
      }
    } catch (Exception ex) {
      return MusicObject.mobj;
    }
    sc.close();
    MusicObject.mobj.setId(id);
    return MusicObject.mobj;
  }
  
  public boolean validate() {
    //System.out.println(this);
    if(this.id == null) return false;
    if(this.artist == null) return false;
    if(this.album_artist == null) return false;
    if(this.album == null) return false;
    if(this.track == 0) return false;
    if(this.title == null) return false;
    if(this.year == 0) return false;
    if(this.genre == null) return false;
    if(this.track_length == 0) return false;
    return true;
  }
    
  public boolean matches(HashMap<String, String> filter) {
    boolean match = true;
    for(Map.Entry<String, String> e : filter.entrySet()) {
      //System.out.println(this.toString());
      switch(e.getKey()) {
        case "ARTIST":
          if(this.artist != null) {
            //System.out.println(e.getValue() + " ? " + this.artist);
            match = (this.artist.equals(e.getValue()));
          } else
            match = false;
          break;
        case "ALBUM_ARTIST":
          if(this.album_artist != null)
            match = (this.album_artist.equals(e.getValue()));
          else
            match = false;
          break;
        case "ALBUM":
          if(this.artist != null)
            match = (this.album.equals(e.getValue()));
          else
            match = false;
          break;
        case "TRACK":
          match = (Integer.toString(this.track).equals(e.getValue()));
          break;
        case "TITLE":
          if(this.title != null)
            match = (this.title.equals(e.getValue()));
          else
            match = false;
          break;
        case "YEAR":
          match = (Integer.toString(this.year).equals(e.getValue()));
          break;
        case "GENRE":
          if(this.genre != null)
            match = (this.genre.equals(e.getValue()));
          else
            match = false;
          break;
        case "TRACK_LENGTH":
          match = (Integer.toString(this.track_length).equals(e.getValue()));
          break;
        default:
          break;
      }
      if(!match) break;
    }
    return match;
  }
  
  /**
   * This method compares two MusicObjects. The method only compares the
   * equality of the following fields:<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;String id;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;String artist;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;String album_artist;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;String album;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;int track;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;String title;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;int year;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;String genre;<br />
   *    &nbsp;&nbsp;&nbsp;&nbsp;int track_length;<br />
   * Upon comparing the values in the specified fields, this method returns
   * an integer based value, signaling what fields are equal and what fields
   * are not. The returned value should be considered a binary value, and the
   * bits set tell which fields are not equal. For example:<br />
   * <br />
   * 19 = 0x13: signals that the fields, id, artist, and track are not equal
   * 0 = 0x0: signals all fields are equal
   * -1 = 0xFFFFFFFF: signals that the object provided is not a MusicObject
   * @param o the object that will be compared to this
   * @return 0 if objects are equal, -1 if o is not a MusicObject, or the
   *    following binary values or'd together otherwise:
   *      0x1: id is not equal
   *      0x2: artist is not equal
   *      0x4: album_artist is not equal
   *      0x8: album is not equal
   *      0x10: track is not equal
   *      0x20: title is not equal
   *      0x40: year is not equal
   *      0x80: genre is not equal
   *      0x100: track_length is not equal
   */
  public int compareTo(Object o){
    if(!(o instanceof MusicObject)){
      return -1;
    }
    int returnValue = 0;
    MusicObject m = (MusicObject)o;
    if(!m.id.equals(id)){
      returnValue |= 0x1;
    }
    if(!m.artist.equals(artist)){
      returnValue |= 0x2;
    }
    if(!m.album_artist.equals(album_artist)){
      returnValue |= 0x4;
    }
    if(!m.album.equals(album)){
      returnValue |= 0x8;
    }
    if(m.track != track){
      returnValue |= 0x10;
    }
    if(!m.title.equals(title)){
      returnValue |= 0x20;
    }
    if(m.year != year){
      returnValue |= 0x40;
    }
    if(!m.genre.equals(genre)){
      returnValue |= 0x80;
    }
    if(m.track_length != track_length){
      returnValue |= 0x100;
    }
    return returnValue;
  }
  
  public boolean equals(Object o) {
    return this.compareTo(o) == 0;
  }
}