// Client class
// 20120429

import java.util.*;
import java.io.*;

interface MapFunction<T, U> {
  U operate(T val);
}

interface ReduceFunction<T, U> {
  U operate(T val);
}

public class Client {
  private ArrayList<Socket> servers = new ArrayList<Socket>();
  private final String SERVER_LIST_FILE_NAME = "servers.txt";
  
  public Client() {
    try {
      File f = new File(SERVER_LIST_FILE_NAME);
      Scanner sc = new Scanner(f);
      while(sc.hasNextLine()) {
        Scanner tokenizer = new Scanner(sc.nextLine());
        servers.add(new Socket(tokenizer.next(), Integer.parseInt(tokenizer.next())));
      }
    } catch (Exception ex) {
      System.out.println("Something went wrong:");
      ex.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    // set up connections
    Client c = new Client();
    
    // send out requests
    // store
    System.out.println("Adding a new MusicObject")
    MusicObject mobj = new MusicObject(UUID.randomUUID(), "Some Artist", "Some Album Artist",
                                       "Some Album", 3, "Some Title", 2012, "Some Genre", 323);
    c.store(mobj);
    
    // map reduce 1
    System.out.println("\n\nMap/Reduce #1: average length in seconds of a song by Opeth:");
    HashMap<String, String> filter = new HashMap<String, String>();
    filter.put("ARTIST", "Opeth");
    ArrayList<MusicObject> alist = c.query("ALL", filter);
    // map
    List<Integer> track_lengths = replaceValues(alist, new MapFunction<MusicObject, Integer> () {
      public Integer operate(MusicObject mobj) {return mobj.getTrackLength()}
    });
    // reduce
    Integer avg_length = reduceValues(track_lengths, new ReduceFunction<List<Integer>, Integer>() {
      public Integer operate(List<Integer> list) {
        int total = list.getSize();
        int accum = 0;
        for(Integer i : list) accum += i;
        return accum / total;
      }
    });
    System.out.println("Average length is: " + avg_length);
    
    // map reduce 2
    System.out.println("\n\nMap/Reduce #2: most active recording year for the Foo Fighters:");
    HashMap<String, String> filter = new HashMap<String, String>();
    filter.put("ARTIST", "Foo Fighters");
    ArrayList<MusicObject> alist = c.query("ALL", filter);
    // map
    List<Integer> years = replaceValues(alist, new MapFunction<MusicObject, Integer> () {
      public Integer operate(MusicObject mobj) {return mobj.getYear()}
    });
    // reduce
    Integer median_year = reduceValues(years, new ReduceFunction<List<Integer>, Integer>() {
      public Integer operate(List<Integer> list) {
        HashMap<Integer, Integer> accum = new HashMap<Integer, Integer>();
        for(Integer i : list) {
          if(accum.get(i) == 0) accum.put(i, 1);
          else accum.put(i, accum.get(i) + 1);
        }
        Integer median = 0;
        for(Map.Entry<Integer, Integer> e : accum) {
          if(median == 0) median = e.getKey();
          if(e.getValue() > accum.get(median)) median = e.getKey();
        }
        return accum;
      }
    });
    System.out.println("Median year is: " + median);
    
    // map reduce 3
    System.out.println("\n\nMap/Reduce #3: Longest track on St. Elsewhere by Gnarls Barkley");
    HashMap<String, String> filter = new HashMap<String, String>();
    filter.put("ARTIST", "Gnarls Barkley");
    filter.put("ALBUM", "St. Elsewhere");
    ArrayList<MusicObject> alist = c.query("ALL", filter);
    // reduce
    MusicObject longest = reduceValues(track_lengths, new ReduceFunction<List<MusicObject>, MusicObject>() {
      public Integer operate(List<MusicObject> list) {
        MusicObject retval = null;
        for(MusicObject m : list) {
          if(retval == null) retval = m;
          else if(m.getTrackLength() > retval.getTrackLength())
            retval = m;
        }
        return m;
      }
    });
    System.out.println("Longest track is: " + longest);
    
    // kill
    System.out.println("\n\nKilling a server...");
    c.kill();
    
    // map reduce 3 again
    System.out.println("\n\nMap/Reduce #3: Longest track on St. Elsewhere by Gnarls Barkley");
    HashMap<String, String> filter = new HashMap<String, String>();
    filter.put("ARTIST", "Gnarls Barkley");
    filter.put("ALBUM", "St. Elsewhere");
    ArrayList<MusicObject> alist = c.query("ALL", filter);
    // reduce
    MusicObject longest = reduceValues(track_lengths, new ReduceFunction<List<MusicObject>, MusicObject>() {
      public Integer operate(List<MusicObject> list) {
        MusicObject retval = null;
        for(MusicObject m : list) {
          if(retval == null) retval = m;
          else if(m.getTrackLength() > retval.getTrackLength())
            retval = m;
        }
        return m;
      }
    });
    System.out.println("Longest track is: " + longest);
  }
  
  
  // query
  public ArrayList<MusicObject> query(String limit, HashMap<String, String> filter) {
    Socket req = servers.get(Math.floor(Math.random() * 3)); // random server to request from
    ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
    PrintWriter pwo = new PrintWriter(req.getOutputStream());
    
    // construct query
    StringBuilder sb = new StringBuilder("GET ");
    sb.append(limit + " ");
    for(Map.Entry<String, String> e : filter.entrySet()) {
      sb.append(e.getKey() + "='" + e.getValue() + "', ")
    }
    String query = sb.toString();
    query = query.subString(0, query.length() - 2);
    System.out.println(query);
    
    // send query
    pwo.println(query);
    
    // read in response
    ArrayList<MusicObject> retval = (ArrayList<MusicObject>)ois.readObject();
    
    // return it
    return retval;
  }
  
  // store
  public void store(MusicObject mobj) {
    Socket req = servers.get(Math.floor(Math.random() * 3)); // random server to request from
    PrintWriter pwo = new PrintWriter(req.getOutputStream());
  
    // construct query
    StringBuilder sb = new StringBuilder("PUT ");
    sb.append((System.nanoTime() + (Math.floor(Math.random() * 10000000000))) + ", ");
    sb.append(mobj.getId() + ", ");
    sb.append(mobj.getArtist() + ", ");
    sb.append(mobj.getAlbumArtist() + ", ");
    sb.append(mobj.getAlbum() + ", ");
    sb.append(mobj.getTrack() + ", ");
    sb.append(mobj.getTitle() + ", ");
    sb.append(mobj.getYear() + ", ");
    sb.append(mobj.getGenre() + ", ");
    sb.append(mobj.getTrackLength());
    String query = sb.toString();
    System.out.println(query);
    
    // send query
    pwo.println(query);
  }
  
  // kill
  public void kill() {
    Socket req = servers.get(Math.floor(Math.random() * 3));
    PrintWriter pwo = new PrintWriter(req.getOutputStream());
    
    // construct query
    String query = "KILL"
    
    // send query
    pwo.println(query);
  }
    
  // map-reduce stuff
  public List<U> replaceValues(List<T> list, MapFunction<T, U> f) {
    List<U> alist = new ArrayList<U>();
    for(T t : list)
      alist.add(f.operate(t));
    return alist;
  }
  
  public U reduceValues(List<T> list, ReduceFunction<T, U> f) {
    return ReduceFunction.operate(list);
  }
}
