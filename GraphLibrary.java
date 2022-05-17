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
    /**
     * BFS
     * Using BFS to find the shortest path tree for a current center of the universe. Returns the path tree as a graph.
     *
     * @param g The graph to perform the BFS algorithm on.
     * @param source The source node for the BFS algorithm.
     */
    public static <V, E> Graph<V, E> bfs(Graph<V, E> g, V source)
    {
        // Creating a new AdjacencyMapGraph, and inserting the source node.
        Graph<V, E> bfsGraph = new AdjacencyMapGraph<V, E>();
        bfsGraph.insertVertex(source);

        // Creating a new HashSet for the visited nodes.
        Set<V> visited = new HashSet<V>();

        // Creating a new LinkedList for the queue.
        Queue<V> queue = new LinkedList<V>();

        // Adding the source node to the Set and Queue.
        visited.add(source);
        queue.add(source);

        // While the queue is not still empty...
        while (!queue.isEmpty())
        {
            // Remove an element from the queue.
            V u = queue.remove();

            // Cycling through the outNeighbors of the removed element.
            for (V v: g.outNeighbors(u))
            {
                // Checking to make sure that the visited set does not contain the neighbor.
                if (!visited.contains(v))
                {
                    // Adding the element v to the queue and to visited.
                    queue.add(v);
                    visited.add(v);

                    // Inserting a new vertex in the graph, and inserting a directed edge with the appropriate label.
                    bfsGraph.insertVertex(v);
                    bfsGraph.insertDirected(v, u, g.getLabel(u, v));
                }
            }
        }

        // Returning the graph.
        return bfsGraph;
    }

    /**
     * Get Path
     * Given a shortest path tree and a vertex, we construct a path from the vertex back to the center of the universe.
     *
     * @param tree The path tree to construct the path from.
     * @param v The vertex from which to construct a path.
     */
    public static <V, E> List<V> getPath(Graph<V, E> tree, V v)
    {
        // Creating a new ArrayList to hold the path.
        ArrayList<V> path = new ArrayList<V>();

        // Adding the current vertex to the ArrayList.
        V currentVertex = v;
        path.add(currentVertex);

        // While the outDegree of the current vertex is greater than zero...
        while (tree.outDegree(currentVertex) > 0)
        {
            // Go through the outNeighbors of the current vertex...
            for (V u: tree.outNeighbors(currentVertex))
            {
                // Add it to the path and reset the current vertex.
                path.add(u);
                currentVertex = u;
            }
        }

        // Return the path, given as an ArrayList.
        return path;
    }

    /**
     * Missing Vertices
     * Given a graph and a subgraph (here the shortest path tree), determine which vertices are in the graph but not
     * the subgraph (here, not reached by BFS).
     *
     * @param graph The main graph, which has all the vertices.
     * @param subgraph The subgraph, which has only some vertices (presumably).
     */
    public static <V, E> Set<V> missingVertices(Graph<V, E> graph, Graph<V, E> subgraph)
    {
        // Creating a new HashSet to hold the Set of missing vertices.
        Set<V> missingVertices = new HashSet<V>();

        // For each vertex in the vertices of the main graph...
        for (V vertex: graph.vertices())
        {
            // If the subgraph does not contain the vertex...
            if (!subgraph.hasVertex(vertex))
            {
                // Add the vertex to the set of missing vertices.
                missingVertices.add(vertex);
            }
        }

        // Return the set of missing vertices.
        return missingVertices;
    }

    /**
     * Average Separation
     * Finding the average distance-from-root in a shortest path tree. We do this without enumerating all the paths.
     *
     * @param tree The shortest path tree from which to calculate the distance.
     * @param root The root to use when finding the average separation.
     */
    public static <V, E> double averageSeparation(Graph<V,E> tree, V root)
    {
        // Creating an ArrayList to ensure that the value is not reset when going through the tree via the helper function.
        ArrayList<Integer> totalSumOfPaths = new ArrayList<>();

        // Only the first value of the ArrayList is updated.
        totalSumOfPaths.add(0);

        // Using the helper function to update the ArrayList.
        summation(tree, root, 0, totalSumOfPaths);

        // Returning the average distance-from-root by dividing the sum by the number of (other) vertices.
        return ((double) totalSumOfPaths.get(0)) / (tree.numVertices() - 1);
    }

    /**
     * Summation
     * Helper function for the averageSeparation() method.
     *
     * @param tree The shortest path tree from which to calculate the distance.
     * @param vertex The vertex we calculate the distance for.
     * @param pathDistance The path distance, which increases by one as we move away from the root.
     * @param totalSumOfPaths The ArrayList which holds the total sum of all the paths.
     */
    public static <V, E> void summation(Graph<V, E> tree, V vertex, int pathDistance, ArrayList<Integer> totalSumOfPaths)
    {
        // Checking to make sure the inDegree of a given vertex is non-zero.
        if (tree.inDegree(vertex) > 0)
        {
            // For each element in the inNeighbors, according to the vertex.
            for (V v: tree.inNeighbors(vertex))
            {
                // Set the total sum of the paths equal to the appropriate value, based on the path distance.
                totalSumOfPaths.set(0, totalSumOfPaths.get(0) + pathDistance + 1);

                // Recursively call the helper function for the averageSeparation() method.
                summation(tree, v, pathDistance + 1, totalSumOfPaths);
            }
        }
    }

    /**
     * Vertices By In Degree
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

    /**
     * Testing Method
     * For comments, please see the Kevin Bacon Game code, specifically the initializeMainGraph() method, which uses the
     * same structure as this testing code.
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

        System.out.println("Actor Map: " + actorMap);
        System.out.println("Movie Map: " + movieMap);
        System.out.println("Connections Map: " + connectionsMap);

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

        /* To test the functions, rather than hard-coding the addition of vertices and edges, as asked, we used the
        algorithm we implemented in initializeMainGraph() for the Kevin Bacon Game.
         */
        System.out.println();
        System.out.println("Erdos Graph: " + erdosGraph);

        System.out.println();
        System.out.println("BFS Graph: " + bfs(erdosGraph, "Kevin Bacon"));
        System.out.println();
        System.out.println("Path (Charlie To Kevin): " + getPath(bfs(erdosGraph, "Kevin Bacon"), "Charlie"));
        System.out.println();
        System.out.println("Missing Vertices: " + missingVertices(erdosGraph, bfs(erdosGraph, "Kevin Bacon")));
        System.out.println();
        System.out.println("Average Separation: " + averageSeparation(bfs(erdosGraph, "Kevin Bacon"), "Kevin Bacon"));
    }
}
