// MusicObject class
// Jacob Peck
// 20120418

import java.io.*;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
  
  private MusicObject(int id, String artist, String album_artist, String album,
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
  
  public static MusicObject buildFromFile(File f, int id) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      
      DefaultHandler handler = new DefaultHandler() {
        
        public void startElement(String uri, String localName,String qName, 
                Attributes attributes) throws SAXException {
          //System.out.println("Start Element :" + qName);
          if (qName.equalsIgnoreCase("SONG")) {
            
          }
          if (qName.equalsIgnoreCase("ARTIST")) {
            
          }
          if (qName.equalsIgnoreCase("ALBUM_ARTIST")) {
            
          }
          if (qName.equalsIgnoreCase("ALBUM")) {
            
          }
          if (qName.equalsIgnoreCase("TRACK")) {
            
          }
          if (qName.equalsIgnoreCase("TITLE")) {
            
          }
          if (qNname.equalsIgnoreCase("YEAR")) {
          
          }
          if (qName.equalsIgnoreCase("GENRE")) {
            
          }
          if (qName.equalsIgnoreCase("TRACK_NAME")) {
            
          }
        }
       
        public void endElement(String uri, String localName,
          String qName) throws SAXException {
          //System.out.println("End Element :" + qName);
        }
       
        public void characters(char ch[], int start, int length) throws SAXException {
          // read info
        }
      };
       
      saxParser.parse(f, handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return new MusicObject(id, artist, album_artist, album, track, title, year, genre, track_length);
  }
}