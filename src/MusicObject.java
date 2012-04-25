// MusicObject class
// 20120418

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

enum Mode {
  MODE_NONE, MODE_ARTIST, MODE_ALBUM_ARTIST, MODE_ALBUM, MODE_TRACK,
  MODE_TITLE, MODE_YEAR, MODE_GENRE, MODE_TRACK_LENGTH, MODE_SONG
}

public class MusicObject implements Serializable {
  
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
  public static final SAXParserFactory factory = SAXParserFactory.newInstance();
  public static SAXParser saxParser;
  public static final DefaultHandler handler = new DefaultHandler() {
    Mode mode = Mode.MODE_NONE;
    
    public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {
      //System.out.println("Start Element :" + qName);
      if (localName.equalsIgnoreCase("SONG")) {mode = Mode.MODE_SONG;}
      if (localName.equalsIgnoreCase("ARTIST")) {mode = Mode.MODE_ARTIST;}
      if (localName.equalsIgnoreCase("ALBUM_ARTIST")) {mode = Mode.MODE_ALBUM_ARTIST;}
      if (localName.equalsIgnoreCase("ALBUM")) {mode = Mode.MODE_ALBUM;}
      if (localName.equalsIgnoreCase("TRACK")) {mode = Mode.MODE_TRACK;}
      if (localName.equalsIgnoreCase("TITLE")) {mode = Mode.MODE_TITLE;}
      if (localName.equalsIgnoreCase("YEAR")) {mode = Mode.MODE_YEAR;}
      if (localName.equalsIgnoreCase("GENRE")) {mode = Mode.MODE_GENRE;}
      if (localName.equalsIgnoreCase("TRACK_LENGTH")) {mode = Mode.MODE_TRACK_LENGTH;}
    }
   
    public void endElement(String uri, String localName,
      String qName) throws SAXException {
      //System.out.println("End Element :" + qName);
    }
   
    public void characters(char ch[], int start, int length) throws SAXException {
      String s = new String(Arrays.copyOfRange(ch, start, start+length));
      int x = 0;
      //System.out.println("mode: " + mode + " s: " + s);
      // read info
      switch(mode) {
        case MODE_ARTIST:
          MusicObject.mobj.setArtist(s);
          break;
        case MODE_ALBUM_ARTIST:
          MusicObject.mobj.setAlbumArtist(s);
          break;
        case MODE_ALBUM:
          MusicObject.mobj.setAlbum(s);
          break;
        case MODE_TRACK:
          x = 0; // default
          try { x = Integer.parseInt(s); }
          catch(Exception ex) {}
          MusicObject.mobj.setTrack(x);
          break;
        case MODE_TITLE:
          MusicObject.mobj.setTitle(s);
          break;
        case MODE_YEAR:
          x = 0; // default
          try { x = Integer.parseInt(s); }
          catch(Exception ex) {}
          MusicObject.mobj.setYear(x);
          break;
        case MODE_GENRE:
          MusicObject.mobj.setGenre(s);
          break;
        case MODE_TRACK_LENGTH:
          x = 0; // default
          try { x = Integer.parseInt(s); }
          catch(Exception ex) {}
          MusicObject.mobj.setTrackLength(x);
          break;
        default:
          break; // unnecessary, but for symmetry
      }
      mode = Mode.MODE_NONE;
    }
    
    public void error(SAXParseException ex) {} 
    public void fatalError(SAXParseException ex) {}        
    public void warning(SAXParseException ex) {}
  };
    
  
  static {
    try {
      factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
      saxParser = factory.newSAXParser();
    } catch (Exception ex) {}
  }
  
  private MusicObject(){}                        
  
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
    try {      
      //InputSource fis = new InputSource(new FileInputStream(f));
      InputSource fis = new InputSource();
      Reader cr = new InputStreamReader(new FileInputStream(f));
      fis.setCharacterStream(cr);
      //fis.setEncoding("US-ASCII");
      saxParser.parse(fis, handler);
    } catch (Exception e) {
      System.out.println(f.getName());
      //e.printStackTrace();
    }
    
    MusicObject.mobj.setId(id);
    return MusicObject.mobj;
  }
}