import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Graph Library Class
 * BFS-related methods, which are generic with respect to vertex and edge types.
 *
 * Using a "path tree" using a Graph during BFS, where the root of the path tree is the center of the universe and all
 * actors that have been in a movie with the center of the universe are children of the root. Other actors that have
 * been a movie with a child of the root are grandchildren of the root and so on.
 *
 * Unlike a standard tree, edges in your path tree should point from a child to its parent. This way if you are given a
 * vertex, you can follow edges back to the root (the center of the universe).
 *
 * @author Carter Kruse & John DeForest, Dartmouth CS 10, Spring 2022
 */

public class GraphLibrary
{
    public static <V, E> Graph<V, E> bfs(Graph<V, E> g, V source)
    {
        Graph<V, E> bfsGraph = new AdjacencyMapGraph<V, E>();
        bfsGraph.insertVertex(source);

        Set<V> visited = new HashSet<V>();
        Queue<V> queue = new LinkedList<V>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty())
        {
            V u = queue.remove();

            for (V v: g.outNeighbors(u))
            {
                if (!visited.contains(v))
                {
                    queue.add(v);
                    visited.add(v);
                    bfsGraph.insertVertex(v);
                    bfsGraph.insertDirected(v, u, g.getLabel(u, v));
                }
            }
        }

        return bfsGraph;
    }

    public static <V, E> List<V> getPath(Graph<V, E> tree, V v)
    {
        ArrayList<V> path = new ArrayList<V>();
        V currentVertex = v;
        path.add(currentVertex);

        while (tree.outDegree(currentVertex) > 0)
        {
            for (V u: tree.outNeighbors(currentVertex))
            {
                path.add(u);
                currentVertex = u;
            }
        }

        return path;
    }


    public static <V, E> Set<V> missingVertices(Graph<V, E> graph, Graph<V, E> subgraph)
    {
        Set<V> missingVertices = new HashSet<V>();

        for (V vertex: graph.vertices())
        {
            if (!subgraph.hasVertex(vertex))
            {
                missingVertices.add(vertex);
            }
        }

        return missingVertices;
    }

    /**
     * Find the average distance-from-root in a shortest path tree.
     * Note: do this without enumerating all the paths! Hint: think tree recursion...
     */
    public static <V, E> double averageSeparation(Graph<V,E> tree, V root)
    {
        ArrayList<Integer> totalSumOfPaths = new ArrayList<>();
        totalSumOfPaths.add(0);

        summation(tree, root, 0, totalSumOfPaths);

        return ((double) totalSumOfPaths.get(0)) / (tree.numVertices() - 1);
    }

    public static <V, E> ArrayList<Integer> summation(Graph<V, E> tree, V vertex, int pathDistance, ArrayList<Integer> totalSumOfPaths)
    {
//        System.out.println("Vertex " + vertex);
//        System.out.println("In Degree " + tree.inDegree(vertex));
//        System.out.println("Path Distance " + pathDistance);
//        System.out.println("Total Sum " + totalSumOfPaths);
//        System.out.println();

        if (tree.inDegree(vertex) > 0)
        {
            for (V v: tree.inNeighbors(vertex))
            {
                totalSumOfPaths.set(0, totalSumOfPaths.get(0) + pathDistance + 1);
                summation(tree, v, pathDistance + 1, totalSumOfPaths);
            }
        }

        return totalSumOfPaths;
    }

    /**
     * Takes a random walk from a vertex, up to a given number of steps.
     * So a 0-step path only includes start, while a 1-step path includes start and one of its out-neighbors,
     * and a 2-step path includes start, an out-neighbor, and one of the out-neighbor's out-neighbors.
     *
     * Stops earlier if no step can be taken (i.e., reach a vertex with no out-edge).
     *
     * @param g Graph to walk on.
     * @param start Initial Vertex (Assumed to be in graph.)
     * @param steps Max number of steps.
     * @return A list of vertices starting with start, each with an edge to the sequentially next in the list;
     * null if start isn't in graph
     */
    public static <V, E> List<V> randomWalk(Graph<V, E> g, V start, int steps)
    {
        // Boolean value to check to see if start is in the graph.
        boolean graphHasStart = false;

        // Cycle through the vertices in the Graph and add them to the ArrayList.
        for (V vertex: g.vertices())
        {
            // If there is a vertex equal to start, we set the boolean value equal to true.
            if (vertex.equals(start))
            {
                graphHasStart = true;
                break;
            }
        }

        // If start is not in the graph, we simply return null.
        if (!graphHasStart)
        {
            return null;
        }

        // Otherwise... we create a new ArrayList to hold the path of the random walk.
        List<V> pathList = new ArrayList<V>();

        // Adding the start node to the ArrayList.
        pathList.add(start);

        // Cycling through the number of steps specified.
        for (int i = 0; i < steps; i += 1)
        {
            /* If the node does not have any out-neighbors, we return the ArrayList.
            The 'start' node is updated for each iteration of the for loop.
             */
            if (g.outDegree(start) == 0)
            {
                return pathList;
            }

            // Otherwise...
            else
            {
                // We create a maxValue which we use later for choosing a random variable out of the Iterator.
                double maxValue = -1;

                // Setting the nextChoice (temporary variable) to the start node.
                V nextChoice = start;

                // Iterating through the outNeighbors of the start node.
                for (V vertex: g.outNeighbors(start))
                {
                    // Creating a new random variable that is used to choose a random outNeighbor.
                    double randomValue = Math.random();

                    // If the random variable is greater than the maxValue...
                    if (randomValue >= maxValue)
                    {
                        // We maxValue to the randomValue, and choose the vertex we are at as the nextChoice.
                        maxValue = randomValue;
                        nextChoice = vertex;
                    }
                }

                // Adding the nextChoice vertex to the pathList.
                pathList.add(nextChoice);

                // Setting the start node to the nextChoice so that we can consider its neighbors.
                start = nextChoice;
            }
        }

        // Returning the ArrayList.
        return pathList;
    }

    /**
     * Orders vertices in decreasing order by their in-degree.
     *
     * @param g Graph
     * @return List of vertices sorted by in-degree, decreasing (i.e., largest at index 0).
     */
    public static <V, E> List<V> verticesByInDegree(Graph<V, E> g)
    {
        // Create a new ArrayList to later return once it has been sorted.
        List<V> verticesByDegree = new ArrayList<V>();

        // Cycle through the vertices in the Graph and add them to the ArrayList.
        for (V vertex: g.vertices())
        {
            verticesByDegree.add(vertex);
        }

        // Sort the vertices in the ArrayList by inDegree, with reverse order (largest at index 0).
        verticesByDegree.sort(Comparator.comparingInt(g::inDegree).reversed());

        // Return the ArrayList.
        return verticesByDegree;
    }

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

        System.out.println();
        System.out.println(erdosGraph);

        System.out.println();
        System.out.println(bfs(erdosGraph, "Kevin Bacon"));
        System.out.println();
        System.out.println(getPath(bfs(erdosGraph, "Kevin Bacon"), "Charlie"));
        System.out.println();
        System.out.println(missingVertices(erdosGraph, bfs(erdosGraph, "Kevin Bacon")));
        System.out.println();
        System.out.println(averageSeparation(bfs(erdosGraph, "Kevin Bacon"), "Kevin Bacon"));
    }
}
