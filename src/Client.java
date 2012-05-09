// Client class
// 20120429

import java.util.*;
import java.io.*;
import java.net.*;

interface MapFunction<T, U> {
  U operate(T val);
}

interface ReduceFunction<U, V> {
  V operate(U val);
}

class MapReduce<T, U, W, V> {
  public MapFunction<T,U> map;
  public ReduceFunction<W,V> reduce;
  
  public List<U> callMap(List<T> val) {
    List<U> alist = new ArrayList<U>();
    for (T t : val) {
      alist.add(this.map.operate(t));
    }
    return alist;
  }
  
  public V callReduce(W val) { return this.reduce.operate(val); }
}

public class Client {
  private ArrayList<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
  private final String SERVER_LIST_FILE_NAME = "servers.txt";
  private final int CONNECTION_TIMEOUT = 2000;

  public Client() {
    try {
      File f = new File(SERVER_LIST_FILE_NAME);
      Scanner sc = new Scanner(f);
      while(sc.hasNextLine()) {
        Scanner tokenizer = new Scanner(sc.nextLine());
        servers.add(new InetSocketAddress(tokenizer.next(), Integer.parseInt(tokenizer.next())));
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
    System.out.println("Adding a new MusicObject");
    MusicObject mobj = new MusicObject(UUID.randomUUID().toString(), "Some Artist", "Some Album Artist",
                                       "Some Album", 3, "Some Title", 2012, "Some Genre", 323);
    //c.store(mobj);
    
    try{Thread.sleep(500);} catch (Exception ex){}
    
    // kill
    /*
    System.out.println("\n\nKilling a server...");
    c.kill();
    */
    
    try{Thread.sleep(500);} catch (Exception ex){}
    
    // map reduce 1
    MapReduce<MusicObject, Integer, List<Integer>, Integer> mr1 = new MapReduce<MusicObject, Integer, List<Integer>, Integer>();
    mr1.map = new MapFunction<MusicObject, Integer>() {
      public Integer operate(MusicObject mobj) {return mobj.getTrackLength();}
    };
    mr1.reduce = new ReduceFunction<List<Integer>, Integer>() {
      public Integer operate(List<Integer> list) {
        int total = list.size();
        if(total == 0) return 0;
        int accum = 0;
        for(Integer i : list) accum += i;
        return accum / total;
      }
    };
    System.out.println("\n\nMap/Reduce #1: average length in seconds of a song by Opeth:");
    HashMap<String, String> filter = new HashMap<String, String>();
    filter.put("ARTIST", "Opeth");
    ArrayList<MusicObject> alist = null;
    while(alist == null) {
      try{
        alist = c.query("ALL", filter);
      } catch (Exception ex) {
        alist = null;
        continue;
      }
    }
    // map
    List<Integer> track_lengths = mr1.callMap(alist);
    // reduce
    Integer avg_length = mr1.callReduce(track_lengths);
    System.out.println("Average length is: " + avg_length);
    
    try{Thread.sleep(500);} catch (Exception ex){}
    
    // map reduce 2
    MapReduce<MusicObject, Integer, List<Integer>, Integer> mr2 = new MapReduce<MusicObject, Integer, List<Integer>, Integer>();
    mr2.map = new MapFunction<MusicObject, Integer>() {
      public Integer operate(MusicObject mobj) {return mobj.getYear();}
    };
    mr2.reduce = new ReduceFunction<List<Integer>, Integer>() {
      public Integer operate(List<Integer> list) {
        HashMap<Integer, Integer> accum = new HashMap<Integer, Integer>();
        for(Integer i : list) {
          if(accum.get(i) == 0) accum.put(i, 1);
          else accum.put(i, accum.get(i) + 1);
        }
        Integer median = 0;
        for(Map.Entry<Integer, Integer> e : accum.entrySet()) {
          if(median == 0) median = e.getKey();
          if(e.getValue() > accum.get(median)) median = e.getKey();
        }
        return median;
      }
    };
    System.out.println("\n\nMap/Reduce #2: most active recording year for the Foo Fighters:");
    filter = new HashMap<String, String>();
    filter.put("ARTIST", "Foo Fighters");
    alist = null;
    while(alist == null) {
      try{
        alist = c.query("ALL", filter);
      } catch (Exception ex) {
        alist = null;
        continue;
      }
    }
    // map
    List<Integer> years = mr2.callMap(alist);
    // reduce
    Integer median_year = mr2.callReduce(years);
    System.out.println("Median year is: " + median_year);
    
    try{Thread.sleep(500);} catch (Exception ex){}
    
    // map reduce 3
    MapReduce<MusicObject, Integer, List<MusicObject>, MusicObject> mr3 = new MapReduce<MusicObject, Integer, List<MusicObject>, MusicObject>();
    mr3.reduce = new ReduceFunction<List<MusicObject>, MusicObject>() {
      public MusicObject operate(List<MusicObject> list) {
        MusicObject retval = null;
        for(MusicObject m : list) {
          if(retval == null) retval = m;
          else if(m.getTrackLength() > retval.getTrackLength())
            retval = m;
        }
        return retval;
      }
    };
    System.out.println("\n\nMap/Reduce #3: Longest track on St. Elsewhere by Gnarls Barkley");
    filter = new HashMap<String, String>();
    filter.put("ARTIST", "Gnarls Barkley");
    filter.put("ALBUM", "St. Elsewhere");
    alist = null;
    while(alist == null) {
      try{
        alist = c.query("ALL", filter);
      } catch (Exception ex) {
        alist = null;
        continue;
      }
    }
    // reduce
    MusicObject longest = mr3.callReduce(alist);
    System.out.println("Longest track is: " + longest);
    
    try{Thread.sleep(500);} catch (Exception ex){}
    
    // kill
    System.out.println("\n\nKilling a server...");
    c.kill();
    
    try{Thread.sleep(500);} catch (Exception ex){}
    
    // map reduce 3 again
    System.out.println("\n\nMap/Reduce #3 (after kill): Longest track on St. Elsewhere by Gnarls Barkley");
    filter = new HashMap<String, String>();
    filter.put("ARTIST", "Gnarls Barkley");
    filter.put("ALBUM", "St. Elsewhere");
    alist = null;
    while(alist == null) {
      try{
        alist = c.query("ALL", filter);
      } catch (Exception ex) {
        alist = null;
        continue;
      }
    }
    // reduce
    longest = mr3.callReduce(alist);
    System.out.println("Longest track is: " + longest);
  }
  
  
  // query
  public ArrayList<MusicObject> query(String limit, HashMap<String, String> filter) {
    Socket req = null;
    while(req == null) {
      try {
        InetSocketAddress sa = servers.get((int)Math.floor(Math.random() * servers.size()));
        System.out.println("Trying address " + sa);
        req = new Socket();
        req.connect(sa, CONNECTION_TIMEOUT);
      } catch (Exception ex) {
        req = null;
      }
    }
    ObjectInputStream ois = null;
    //PrintWriter pwo = null;
    DataOutputStream dos = null;
    try {
      System.out.println("Grabbing dos");
      dos = new DataOutputStream(req.getOutputStream());
      //pwo = new PrintWriter(req.getOutputStream());
    } catch (EOFException ex) {
      return null;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    // construct query
    StringBuilder sb = new StringBuilder("GET ");
    sb.append(limit + " ");
    for(Map.Entry<String, String> e : filter.entrySet()) {
      sb.append(e.getKey() + "='" + e.getValue() + "', ");
    }
    String query = sb.toString();
    query = query.substring(0, query.length() - 2);
    System.out.println(query);
    
    // send query
    try{dos.writeUTF(query);} catch(Exception ex) {}
    
    // read in response
    ArrayList<MusicObject> retval = new ArrayList<MusicObject>();
    try {
      System.out.println("Grabbing ois");
      ois = new ObjectInputStream(req.getInputStream());
      Object ret = ois.readObject();

      @SuppressWarnings("unchecked")
      ArrayList<Object> ret2 = (ArrayList<Object>) ret;

      for(Object o : ret2) {
        retval.add((MusicObject)o);
      }
    } catch (EOFException ex) {
      return null;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    // return it
    try{req.close();} catch(Exception ex) {}
    return retval;
  }
  
  // store
  public void store(MusicObject mobj) {
    Socket req = null;
    while(req == null) {
      try {
        InetSocketAddress sa = servers.get((int)Math.floor(Math.random() * servers.size()));
        req = new Socket();
        req.connect(sa, CONNECTION_TIMEOUT);
      } catch (Exception ex) {
        req = null;
      }
    }
    //PrintWriter pwo = null;
    DataOutputStream dos = null;
    ObjectInputStream ois = null;
    try {
      //pwo = new PrintWriter(req.getOutputStream());
      dos = new DataOutputStream(req.getOutputStream());
      ois = new ObjectInputStream(req.getInputStream());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    // construct query
    StringBuilder sb = new StringBuilder("PUT ");
    sb.append((System.nanoTime() + (Math.floor(Math.random() * 1000000))) + ", ");
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
    //pwo.println(query);
    try{
      dos.writeUTF(query);
      ois.readObject();
      req.close();
    } catch(Exception ex) {}
  }
  
  // kill
  public void kill() {
    Socket req = null;
    while(req == null) {
      try {
        InetSocketAddress sa = servers.get((int)Math.floor(Math.random() * servers.size()));
        req = new Socket();
        req.connect(sa, CONNECTION_TIMEOUT);
        System.out.println("Killing node: " + sa);
      } catch (Exception ex) {
        req = null;
      }
    }
    //PrintWriter pwo = null;
    DataOutputStream dos = null;
    try {
      dos = new DataOutputStream(req.getOutputStream());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    // construct query
    String query = "KILL";
    
    // send query
    //pwo.println(query);
    try{
      dos.writeUTF(query);
      req.close();
    } catch(Exception ex) {}
  }
}
