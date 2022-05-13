import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ErdosNumbers
{
    /* You may find it useful to create maps for mapping IDs to actor names and IDs to movie names.
    You can also use a map to figure out which actors appeared in each movie, and can use that information to add the
    appropriate edges to the graph. This may take a little thought, but try it by hand on the small data set given above.
     */

    public static void main(String[] args) throws IOException
    {
        Map<Integer, String> actorMap = new HashMap<>();
        Map<Integer, String> movieMap = new HashMap<>();
        Map<Integer, List<Integer>> connectionsMap = new HashMap<>();

        BufferedReader actorMapInput = new BufferedReader(new FileReader("PS4/actorsTest.txt"));
        BufferedReader movieMapInput = new BufferedReader(new FileReader("PS4/moviesTest.txt"));
        BufferedReader connections = new BufferedReader(new FileReader("PS4/movie-actorsTest.txt"));

        String input;
        while ((input = actorMapInput.readLine()) != null && input.length() != 0)
        {
            String[] array = input.split("\\|");

            int actorID = Integer.parseInt(array[0]);
            String actorName = array[1];
            actorMap.put(actorID, actorName);
        }

        actorMapInput.close();

        input = "";
        while ((input = movieMapInput.readLine()) != null && input.length() != 0)
        {
            String[] array = input.split("\\|");

            int movieID = Integer.parseInt(array[0]);
            String movieName = array[1];
            movieMap.put(movieID, movieName);
        }

        movieMapInput.close();

        input = "";
        while ((input = connections.readLine()) != null && input.length() != 0)
        {
            String[] array = input.split("\\|");

            int movieID = Integer.parseInt(array[0]);
            int actorID = Integer.parseInt(array[1]);

            if (!connectionsMap.containsKey(movieID))
                connectionsMap.put(movieID, new ArrayList<Integer>());

            connectionsMap.get(movieID).add(actorID);
        }

        connections.close();

        System.out.println(actorMap);
        System.out.println(movieMap);
        System.out.println(connectionsMap);

        AdjacencyMapGraph<String, Set<String>> erdosGraph = new AdjacencyMapGraph<String, Set<String>>();

        for (int ID: actorMap.keySet())
        {
            if (actorMap.get(ID) != null)
                erdosGraph.insertVertex(actorMap.get(ID));
        }

        for (int ID: connectionsMap.keySet())
        {
            String movie = movieMap.get(ID);

            for (int firstActorID: connectionsMap.get(ID))
            {
                for (int secondActorID: connectionsMap.get(ID))
                {
                    String firstActor = actorMap.get(firstActorID);
                    String secondActor = actorMap.get(secondActorID);

                    if (firstActor != secondActor && !erdosGraph.hasEdge(firstActor, secondActor))
                    {
                        Set<String> edgeSetOfMovies = new HashSet<String>();
                        edgeSetOfMovies.add(movie);
                        erdosGraph.insertUndirected(firstActor, secondActor, edgeSetOfMovies);
                    }

                    else if (firstActor != secondActor && erdosGraph.hasEdge(firstActor, secondActor))
                    {
                        if (!erdosGraph.getLabel(firstActor, secondActor).contains(movie))
                            erdosGraph.getLabel(firstActor, secondActor).add(movie);
                    }
                }
            }
        }

        System.out.println(erdosGraph);
    }
}
