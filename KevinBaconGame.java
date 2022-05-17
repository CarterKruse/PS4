import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Kevin Bacon Game
 * Social network analysis, with variations of the Kevin Bacon game.
 *
 * In the Kevin Bacon game, the vertices are actors and the edge relationship is "appeared together in a movie".
 * The goal is to find the shortest path between two actors. Traditionally the goal is to find the shortest path to
 * Kevin Bacon, but we'll allow anybody to be the center of the acting universe. We'll also do some analyses to help
 * find better Bacons. Since "degree" means the number of edges to/from a vertex, I'll refer to the number of steps
 * away as the "separation" rather than the common "degrees of separation".
 *
 * The easiest way to play the Kevin Bacon game is to do a breadth-first search (BFS), as covered in lecture.
 * This builds a tree of shortest paths from every actor who can reach Kevin Bacon back to Kevin Bacon. More generally,
 * given a root, BFS builds a shortest-path tree from every vertex that can reach the root back to the root. It is a
 * tree where every vertex points to its parent, and the parent is the next vertex in a shortest path to the root. For
 * the purposes of this assignment, we will store the tree as a directed graph. Once the tree is constructed, we can
 * find the vertex for an actor of interest, and follow edges back to the root, tracking movies (edge labels) and
 * actors (vertices) along the way.
 *
 * @author Carter Kruse & John DeForest, Dartmouth CS 10, Spring 2022
 */
public class KevinBaconGame
{
    // Instance Variables
    private String node;
    private Scanner scanner;
    private Graph<String, Set<String>> mainGraph;
    private Graph<String, Set<String>> erdosGraph;

    /**
     * Constructor
     * Creating the corresponding graphs for the Kevin Bacon game.
     */
    public KevinBaconGame(String node, Scanner scanner, String actorMapFile, String movieMapFile, String connectionsFile)
    {
        // Assigning the instance variables appropriately, given the constructor.
        this.node = node;
        this.scanner = scanner;
        this.mainGraph = new AdjacencyMapGraph<>();

        // Try to initialize the mainGraph and construct the "erdosGraph", a simplified graph for shortest paths.
        try
        {
            initializeMainGraph(actorMapFile, movieMapFile, connectionsFile);
            constructErdosGraph();
        }

        // Catch the IOException and print out the message.
        catch (IOException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Initialize Main Graph
     * Initializing the main Adjacency Map Graph from the input files.
     */
    public void initializeMainGraph(String actorMapFile, String movieMapFile, String connectionsFile) throws IOException
    {
        // Creating new Maps for the actors, movies, and connections.
        Map<Integer, String> actorMap = new HashMap<>();
        Map<Integer, String> movieMap = new HashMap<>();
        Map<Integer, List<Integer>> connectionsMap = new HashMap<>();

        // Initializing the BufferedReaders for the actors, movies, and connections.
        BufferedReader actorMapInput = new BufferedReader(new FileReader(actorMapFile));
        BufferedReader movieMapInput = new BufferedReader(new FileReader(movieMapFile));
        BufferedReader connections = new BufferedReader(new FileReader(connectionsFile));

        String input;

        // Reading in the input from the actor input and making sure the input is there.
        while ((input = actorMapInput.readLine()) != null && input.length() != 0)
        {
            // Splitting the input based on the | symbol.
            String[] array = input.split("\\|");

            // Checking to make sure the array length is greater than zero.
            if (array.length > 0)
            {
                // Adding the actorID and actorName to the actorMap.
                int actorID = Integer.parseInt(array[0]);
                String actorName = array[1];
                actorMap.put(actorID, actorName);
            }
        }

        // Closing the actorMapInput.
        actorMapInput.close();

        input = "";

        // Reading in the input from the movie input and making sure the input is there.
        while ((input = movieMapInput.readLine()) != null && input.length() != 0)
        {
            // Splitting the input based on the | symbol.
            String[] array = input.split("\\|");

            // Checking to make sure the array length is greater than zero.
            if (array.length > 0)
            {
                // Adding the movieID and movieName to the movieMap.
                int movieID = Integer.parseInt(array[0]);
                String movieName = array[1];
                movieMap.put(movieID, movieName);
            }
        }

        // Closing the actorMapInput.
        movieMapInput.close();

        input = "";

        // Reading in the input from the movie input and making sure the input is there.
        while ((input = connections.readLine()) != null && input.length() != 0)
        {
            // Splitting the input based on the | symbol.
            String[] array = input.split("\\|");

            // Checking to make sure the array length is greater than zero.
            if (array.length > 0)
            {
                // Adding the movieID and movieName to the movieMap.
                int movieID = Integer.parseInt(array[0]);
                int actorID = Integer.parseInt(array[1]);

                // Check to see if the connectionsMap already contains the key.
                if (!connectionsMap.containsKey(movieID))
                    connectionsMap.put(movieID, new ArrayList<Integer>());

                connectionsMap.get(movieID).add(actorID);
            }
        }

        // Closing the actorMapInput.
        connections.close();

        // Cycling through the IDs of the actorMap.
        for (int ID : actorMap.keySet())
        {
            // If the actorName is not null...
            if (actorMap.get(ID) != null)
                mainGraph.insertVertex(actorMap.get(ID)); // Add a new vertex to the mainGraph.
        }

        // Cycling through the IDs of the connectionsMap.
        for (int ID : connectionsMap.keySet())
        {
            // Creating a new String with the name of the movie, given the ID.
            String movie = movieMap.get(ID);

            // Cycling through the connectionsMap for various actors.
            for (int firstActorID : connectionsMap.get(ID))
            {
                for (int secondActorID : connectionsMap.get(ID))
                {
                    // Extracting the name of the actors from the actorMap, given their IDs.
                    String firstActor = actorMap.get(firstActorID);
                    String secondActor = actorMap.get(secondActorID);

                    // Checking to make sure the first and second actor are not the same and that there is no current edge.
                    if (firstActor != secondActor && !mainGraph.hasEdge(firstActor, secondActor))
                    {
                        // Creating a new Set to hold the list of movies.
                        Set<String> edgeSetOfMovies = new HashSet<String>();

                        // Adding the movie to the Set.
                        edgeSetOfMovies.add(movie);

                        // Inserting an undirected edge from the firstActor to the secondActor with the appropriate label.
                        mainGraph.insertUndirected(firstActor, secondActor, edgeSetOfMovies);
                    }

                    // Checking to make sure the first and second actor are not the same and that there is a current edge.
                    else if (firstActor != secondActor && mainGraph.hasEdge(firstActor, secondActor))
                    {
                        // Checking to make sure the edge does not already contain the movie.
                        if (!mainGraph.getLabel(firstActor, secondActor).contains(movie))
                            mainGraph.getLabel(firstActor, secondActor).add(movie); // Adding the movie to the label.
                    }
                }
            }
        }
    }

    /**
     * Construct Erdos Graph
     * Creates the "Erdos Graph" (simplified path tree) using Graph Library bfs function.
     */
    public void constructErdosGraph()
    {
        // Uses the Graph Library bfs function, according to the mainGraph and a given node.
        erdosGraph = GraphLibrary.bfs(mainGraph, node);
    }

    /**
     * Initialize Command Line Interface
     */
    public void initializeCommandLine()
    {
        // Print statements for starting the command line, given based on the problem set.
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game");
        System.out.println();
        System.out.println(node + " is now the center of the acting universe, connected to " + (erdosGraph.numVertices() - 1)
                + "/" + mainGraph.numVertices() + " actors with average separation " + GraphLibrary.averageSeparation(erdosGraph, node));
    }

    /**
     * Set Center Of Universe
     * Sets a node to the center of the universe, reconstructing the erdosGraph tree.
     *
     * @param node The node to set to the center of the universe.
     */
    public void setCenterOfUniverse(String node)
    {
        // Checking to make sure the main graph has the node as a vertex.
        if (mainGraph.hasVertex(node))
        {
            // If so, set the node appropriately.
            this.node = node;

            // Reconstruct the erdosGraph and print out the new line about the center of the universe.
            erdosGraph = GraphLibrary.bfs(mainGraph, node);
            System.out.println(node + " is now the center of the acting universe, connected to " + (erdosGraph.numVertices() - 1)
                    + "/" + mainGraph.numVertices() + " actors with average separation " + GraphLibrary.averageSeparation(erdosGraph, node));
        }

        // Otherwise, indicate that an invalid input was given.
        else
        {
            System.err.println("Invalid Center Of Universe");
        }
    }

    /**
     * Display Infinitely Separated Actors
     * Displays a list of all the actors with infinite separation from the center (not connected).
     */
    public void displayInfinitelySeparatedActors()
    {
        // Using the Graph Library to print out the missing vertices from the mainGraph and erdosGraph.
        System.out.println(GraphLibrary.missingVertices(mainGraph, erdosGraph));
    }

    /**
     * Display Actor Information
     * Displays information for a given actor and their path to the center of the universe.
     */
    public void displayActorInformation(String actor)
    {
        // Checking to make sure that both the mainGraph and erdosGraph have the vertex.
        if (mainGraph.hasVertex(actor) && erdosGraph.hasVertex(actor))
        {
            // Creating a new List that holds the shortest path for a given actor.
            List<String> shortestPath = GraphLibrary.getPath(erdosGraph, actor);

            // Printing out the actor's number (the length of the shortest path).
            System.out.println(actor + "'s number is " + (shortestPath.size() - 1));

            // Setting currentActor (which will be modified) to the actor.
            String currentActor = actor;

            // Cycling through the actors in the shortestPath List.
            for (String nextActor : shortestPath)
            {
                // Making sure the currentActor is not equal to the nextActor (end of the cycle).
                if (currentActor != nextActor)
                {
                    // Printing out the output as appropriate and re-assigning the currentActor.
                    System.out.println(currentActor + " appeared in " + mainGraph.getLabel(currentActor, nextActor) + " with " + nextActor);
                    currentActor = nextActor;
                }
            }
        }

        else if (mainGraph.hasVertex(actor))
        {
            System.out.println(actor + "'s number is \u221e (infinity)");
        }

        else
        {
            System.err.println("No Actor Found");
        }
    }

    /**
     * Display Actors By Degree
     * Displays a list of the actors sorted by degree, with degree between low and high.
     */
    public void displayActorsByDegree(int low, int high)
    {
        // Creating a new PriorityQueue to sort the actors by degree (anonymous function used).
        PriorityQueue<String> actorsByDegree = new PriorityQueue<String>((String firstActor, String secondActor) -> mainGraph.outDegree(firstActor) - mainGraph.outDegree(secondActor));

        // Cycling through the actors in the vertices of the mainGraph.
        for (String actor : mainGraph.vertices())
        {
            // Checking to make sure that the outDegree is between the low and high.
            if (mainGraph.outDegree(actor) <= high && mainGraph.outDegree(actor) >= low)
                actorsByDegree.add(actor); // Add the actor the PriorityQueue.
        }

        // Creating a new ArrayList to hold the sorted actors by degree.
        List<String> sortedActorsByDegree = new ArrayList<String>();

        // Cycling through while the PriorityQueue is not empty.
        while (!actorsByDegree.isEmpty())
            sortedActorsByDegree.add(actorsByDegree.poll()); // Adding the elements in order.

        // Printing out the sorted actors by degree.
        System.out.println(sortedActorsByDegree);
    }

    /**
     * Display Actors By Separation
     * Displays a list of the actors sorted by non-infinite separation from the current center, with separation between
     * low and high.
     */
    public void displayActorsBySeparation(int low, int high)
    {
        // Creating a new PriorityQueue to sort the actors by separation (anonymous function used).
        PriorityQueue<String> actorsBySeparation = new PriorityQueue<String>((String firstActor, String secondActor) ->
                GraphLibrary.getPath(erdosGraph, firstActor).size() - GraphLibrary.getPath(erdosGraph, secondActor).size());

        // Cycling through the actors in the vertices of the erdosGraph.
        for (String actor : erdosGraph.vertices())
        {
            // Checking to make sure that the separation is between the low and high.
            if (GraphLibrary.getPath(erdosGraph, actor).size() <= high && GraphLibrary.getPath(erdosGraph, actor).size() >= low)
                actorsBySeparation.add(actor); // Add the actor to the PriorityQueue.
        }

        // Creating a new ArrayList to hold the sorted actors by separation.
        List<String> sortedActorsBySeparation = new ArrayList<String>();

        // Cycling through while the PriorityQueue is not empty.
        while (!actorsBySeparation.isEmpty())
            sortedActorsBySeparation.add(actorsBySeparation.poll()); // Adding the elements in order.

        // Printing out the sorted actors by separation.
        System.out.println(sortedActorsBySeparation);
    }

    /**
     * Display Best Erdos
     * Based on the short assignment; shows the best Bacon/Erdos based on inDegree.
     */
    public void displayBestErdos(int value)
    {
        for (int i = 0; i < value; i += 1)
            System.out.println(GraphLibrary.verticesByInDegree(mainGraph).get(i));
    }

    /**
     * Display Worst Erdos
     * Based on the short assignment; shows the best Bacon/Erdos based on inDegree.
     */
    public void displayWorseErdos(int value)
    {
        ArrayList<String> verticesInDegree = (ArrayList<String>) GraphLibrary.verticesByInDegree(mainGraph);
        for (int i = 0; i < value; i += 1)
            System.out.println(verticesInDegree.get(verticesInDegree.size() - 1 - i));
    }

    /**
     * Display Sorted Centers Of Universe
     * Displays a list of the top (positive number) or bottom (negative number) centers of the universe, sorted by
     * average separation.
     */
    public void displaySortedCentersOfUniverse(int value)
    {
        // Creating a new Comparator, rather than using an anonymous function.
        class CenterOfUniverseComparator implements Comparator<String>
        {
            @Override
            public int compare(String firstActor, String secondActor)
            {
                // The compare() function is based on whether value is greater than or less than zero.
                if (value > 0)
                    return (int) (Math.signum(findAverageSeparation(firstActor) - findAverageSeparation(secondActor)));
                else
                    return (int) (Math.signum(findAverageSeparation(secondActor) - findAverageSeparation(firstActor)));
            }
        }

        // Creating a new PriorityQueue to sort the centers (using the Comparator).
        PriorityQueue<String> centersOfUniverse = new PriorityQueue<String>(new CenterOfUniverseComparator());

        // Cycling through the actors in the vertices of the erdosGraph.
        for (String actor : erdosGraph.vertices())
            centersOfUniverse.add(actor); // Add the actor to the PriorityQueue.

        // Creating a new ArrayList to hold the sorted actors by separation.
        List<String> sortedCentersOfUniverse = new ArrayList<String>();

        // Creating a new ArrayList to hold the missing vertices (infinite separation).
        ArrayList<String> missingVerticesFromUniverse = new ArrayList<String>();

        // Adding the missing vertices to the ArrayList.
        for (String vertex: GraphLibrary.missingVertices(mainGraph, erdosGraph))
            missingVerticesFromUniverse.add(vertex);

        // Cycling through up to the absolute value of the inputted value.
        for (int i = 0; i < Math.abs(value); i += 1)
        {
            // If the PriorityQueue is not empty...
            if (!centersOfUniverse.isEmpty())
                sortedCentersOfUniverse.add(centersOfUniverse.poll()); // Adding the elements in order.
            else
                sortedCentersOfUniverse.add(missingVerticesFromUniverse.remove(0)); // Otherwise, add elements from missing vertices.
        }

        // Printing out the sorted centers of the universe.
        System.out.println(sortedCentersOfUniverse);
    }

    /**
     * Find Average Separation
     * Helper function for displaySortedCentersOfUniverse.
     *
     * @param node The node from which to construct an erdosGraph and find average separation.
     */
    public double findAverageSeparation(String node)
    {
        return GraphLibrary.averageSeparation(GraphLibrary.bfs(mainGraph, node), node);
    }

    /**
     * Accept Input
     * Based on the command line rules for the game interface.
     */
    public boolean acceptInput(String input)
    {
        // Checking to see if the input is null or is nothing.
        if (input == null || input.length() <= 0)
        {
            System.err.println("Invalid Input");
            return true;
        }

        // Stripping whitespace from the input, then creating a new String array that is split by spaces.
        String[] inputInfo = input.strip().split(" ");

        // If the length of the array is less than one, it is an invalid input.
        if (inputInfo.length < 1)
        {
            System.err.println("Invalid Input");
            return true;
        }

        // The "command" is given by the first letter of the input, which we convert to lower case.
        String command = inputInfo[0].toLowerCase();

        // If the command is 'q', we simply pass false to exit the program.
        if (command.equals("q"))
        {
            return false;
        }

        // If the command is 'i', we display the infinitely separated actors.
        else if (command.equals("i"))
        {
            displayInfinitelySeparatedActors();
            return true;
        }

        // Otherwise, if the length of the String array is less than two, it is an invalid input.
        else if (inputInfo.length < 2)
        {
            System.err.println("Invalid Input");
            return true;
        }

        // Creating a new string with the information (input = command + information).
        String information = "";

        // Extracting the information and placing it into a single String, cycling through the inputInfo array.
        for (int i = 1; i < inputInfo.length; i += 1)
        {
            information += inputInfo[i] + " ";
        }

        // If the command is 'p'...
        if (command.equals("p"))
        {
            // We strip any trailing whitespace.
            information = information.stripTrailing();

            // Check to make sure the mainGraph contains the vertex.
            if (mainGraph.hasVertex(information))
            {
                // If so, we display the actor information.
                displayActorInformation(information);
            }

            // Otherwise, it is an invalid input.
            else
            {
                System.err.println("Invalid Input");
            }
        }

        // If the command is 'u'...
        if (command.equals("u"))
        {
            // We strip any trailing whitespace.
            information = information.stripTrailing();

            // Check to make sure the mainGraph contains the vertex.
            if (mainGraph.hasVertex(information))
            {
                // If so, we set the center of the universe appropriately.
                setCenterOfUniverse(information);
            }

            // Otherwise, it is an invalid input.
            else
            {
                System.err.println("Invalid Input");
            }
        }

        // If the command is 'd'...
        if (command.equals("d"))
        {
            // If the length of the inputInfo array is not three, it is an invalid input.
            if (inputInfo.length != 3)
            {
                System.err.println("Invalid Input");
                return true;
            }

            // Otherwise, we try-catch.
            try
            {
                // Extracting the low and high values by splitting the information on the spaces.
                int lowValue = Integer.parseInt(information.split(" ")[0]);
                int highValue = Integer.parseInt(information.split(" ")[1]);

                // Displaying the actors by degree passing the low and high values.
                displayActorsByDegree(lowValue, highValue);
            }

            // Catching any exception, indicating it is an invalid input.
            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        // If the command is 's'...
        if (command.equals("s"))
        {
            // If the length of the inputInfo array is not three, it is an invalid input.
            if (inputInfo.length != 3)
            {
                System.err.println("Invalid Input");
                return true;
            }

            // Otherwise, we try-catch.
            try
            {
                // Extracting the low and high values by splitting the information on the spaces.
                int lowValue = Integer.parseInt(information.split(" ")[0]);
                int highValue = Integer.parseInt(information.split(" ")[1]);

                // Displaying the actors by separation passing the low and high values.
                displayActorsBySeparation(lowValue, highValue);
            }

            // Catching any exception, indicating it is an invalid input.
            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        // if the command is 'c'...
        if (command.equals("c"))
        {
            // We strip any trailing whitespace.
            information = information.stripTrailing();

            // Checking to make sure the information is not null and the information length is positive.
            if (information == null || information.length() <= 0)
            {
                // It is an invalid input.
                System.err.println("Invalid Input");
                return true;
            }

            // Otherwise, we try-catch.
            try
            {
                // Extracting the values by from parsing the information.
                int value = Integer.parseInt(information);

                // Displaying the sorted centers of the universe.
                displaySortedCentersOfUniverse(value);
            }

            // Catching any exception, indicating it is an invalid input.
            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        // if the command is 'e'...
        if (command.equals("e"))
        {
            // We strip any trailing whitespace.
            information = information.stripTrailing();

            // Checking to make sure the information is not null and the information length is positive.
            if (information == null || information.length() <= 0)
            {
                // It is an invalid input.
                System.err.println("Invalid Input");
                return true;
            }

            // Otherwise, we try-catch.
            try
            {
                // Extracting the values by from parsing the information.
                int value = Integer.parseInt(information);

                // Displaying the best Erdos/Bacons.
                displayBestErdos(value);
            }

            // Catching any exception, indicating it is an invalid input.
            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        // if the command is 'l'...
        if (command.equals("l"))
        {
            // We strip any trailing whitespace.
            information = information.stripTrailing();

            // Checking to make sure the information is not null and the information length is positive.
            if (information == null || information.length() <= 0)
            {
                // It is an invalid input.
                System.err.println("Invalid Input");
                return true;
            }

            // Otherwise, we try-catch.
            try
            {
                // Extracting the values by from parsing the information.
                int value = Integer.parseInt(information);

                // Displaying the worst Erdos/Bacons.
                displayWorseErdos(value);
            }

            // Catching any exception, indicating it is an invalid input.
            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        // Otherwise, simply return true so that the game continues.
        return true;
    }

    /**
     * Run
     * Code that runs the Kevin Bacon Game, as prompted.
     */
    public void run()
    {
        // Initializing the command line.
        initializeCommandLine();
        System.out.println();

        // Reading input from the scanner.
        System.out.println("Kevin Bacon game >");
        String input = scanner.nextLine();

        // Making sure 'q' was not pressed.
        boolean continueGame = acceptInput(input);

        while (continueGame)
        {
            // Continuously reading in input and appropriately responding.
            System.out.println();
            System.out.println(node + " game >");
            input = scanner.nextLine();

            continueGame = acceptInput(input);
        }
    }

    /**
     * Main Method
     */
    public static void main(String[] args)
    {
        // Creating a new Scanner based on System.in.
        Scanner scanner = new Scanner(System.in);

        // The file names for the text files.
        String actorMapFile = "PS4/actors.txt";
        String movieMapFile = "PS4/movies.txt";
        String connectionsFile = "PS4/movie-actors.txt";

        // Initializing a new KevinBaconGame and running it.
        KevinBaconGame game = new KevinBaconGame("Kevin Bacon", scanner, actorMapFile, movieMapFile, connectionsFile);
        game.run();
    }
}
