// Client class
// 20120429

import java.util.*;

interface MapFunction<T, U> {
  U operate(T val);
}

interface ReduceFunction<T, U> {
  U operate(T val);
}

public class Client {
  
  // other stuff
  // ...
  
  
  // map-reduce stuff
  public static List<U> replaceValues(List<T> list, MapFunction<T, U> f) {
    List<U> alist = new ArrayList<U>();
    for(T t : list)
      alist.add(f.operate(t));
    return alist;
  }
  
  public static <U> reduceValues(List<T> list, ReduceFunction<T, U> f) {
    return ReduceFunction.operate(list);
  }
  
  // examples:
  /*
   // assume alist = list of all MusicObjects where artist == "Opeth"
   List<Integer> track_lengths = replaceValues(alist, new MapFunction<MusicObject,Integer>() {
     public Integer operate(MusicObject m) {return m.getTrackLength();}
   });
   Integer avg_length = reduceValues(track_lengths, new ReduceFunction<Integer, List<Integer>>() {
     public Integer operate(List<Integer> list) {
       int total = list.getSize();
       int accum = 0;
       for(Integer i : list) accum += i;
       return accum / total;
     }
   });
   System.out.println("Average track length in seconds for songs by Opeth is: " + avg_length);
  */
  
}