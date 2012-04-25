// MusicObject class
// 20120418

import java.io.*;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

enum Mode {
  MODE_NONE, MODE_ARTIST, MODE_ALBUM_ARTIST, MODE_ALBUM, MODE_TRACK,
  MODE_TITLE, MODE_YEAR, MODE_GENRE, MODE_TRACK_LENGTH
}

public class MusicObject implements Serializable {
  
  private int id;
  private String artist;
  private String album_artist;
  private String album;
  private int track;
  private String title;
  private int year;
  private String genre;
  private int track_length;
  
  public int getId() {return id;}
  public String getArtist() {return artist;}
  public String getAlbumArtist() {return album_artist;}
  public String getAlbum() {return album;}
  public int getTrack() {return track;}
  public String getTitle() {return title;}
  public int getYear() {return year;}
  public String getGenre() {return genre;}
  public int getTrackLength() {return track_length;}
  
  // setters... needed for factory method :(
  public void setId(int id) {this.id = id;}
  public void setArtist(String artist) {this.artist = artist;}
  public void setAlbumArtist(String album_artist) {this.album_artist = album_artist;}
  public void setAlbum(String album) {this.album = album;}
  public void setTrack(int track) {this.track = track;}
  public void setTitle(String title) {this.title = title;}
  public void setYear(int year) {this.year = year;}
  public void setGenre(String genre) {this.genre = genre;}
  public void setTrackLength(int track_length) {this.track_length = track_length;}
  
  private MusicObject(){}                        
  
  public static MusicObject buildFromFile(File f, int id) {
    final MusicObject mobj = new MusicObject();
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      
      DefaultHandler handler = new DefaultHandler() {
        Mode mode = Mode.MODE_NONE;
        
        public void startElement(String uri, String localName,String qName, 
                Attributes attributes) throws SAXException {
          //System.out.println("Start Element :" + qName);
          if (qName.equalsIgnoreCase("SONG")) {}
          if (qName.equalsIgnoreCase("ARTIST")) {mode = Mode.MODE_ARTIST;}
          if (qName.equalsIgnoreCase("ALBUM_ARTIST")) {mode = Mode.MODE_ALBUM_ARTIST;}
          if (qName.equalsIgnoreCase("ALBUM")) {mode = Mode.MODE_ALBUM;}
          if (qName.equalsIgnoreCase("TRACK")) {mode = Mode.MODE_TRACK;}
          if (qName.equalsIgnoreCase("TITLE")) {mode = Mode.MODE_TITLE;}
          if (qName.equalsIgnoreCase("YEAR")) {mode = Mode.MODE_YEAR;}
          if (qName.equalsIgnoreCase("GENRE")) {mode = Mode.MODE_GENRE;}
          if (qName.equalsIgnoreCase("TRACK_LENGTH")) {mode = Mode.MODE_TRACK_LENGTH;}
        }
       
        public void endElement(String uri, String localName,
          String qName) throws SAXException {
          //System.out.println("End Element :" + qName);
        }
       
        public void characters(char ch[], int start, int length) throws SAXException {
          // read info
          switch(mode) {
            case MODE_ARTIST:
              mobj.setArtist(new String(ch));
              break;
            case MODE_ALBUM_ARTIST:
              mobj.setAlbumArtist(new String(ch));
              break;
            case MODE_ALBUM:
              mobj.setAlbum(new String(ch));
              break;
            case MODE_TRACK:
              mobj.setTrack(Integer.parseInt(new String(ch)));
              break;
            case MODE_TITLE:
              mobj.setTitle(new String(ch));
              break;
            case MODE_YEAR:
              mobj.setYear(Integer.parseInt(new String(ch)));
              break;
            case MODE_GENRE:
              mobj.setGenre(new String(ch));
              break;
            case MODE_TRACK_LENGTH:
              mobj.setTrackLength(Integer.parseInt(new String(ch)));
              break;
            default:
              break; // unnecessary, but for symmetry
          }
        }
      };
       
      saxParser.parse(f, handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    mobj.setId(id);
    return mobj;
  }
}