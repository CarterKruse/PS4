import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Kevin Bacon Game
 * Social network analysis, with variations of the Kevin Bacon game.
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
        this.node = node;
        this.scanner = scanner;
        this.mainGraph = new AdjacencyMapGraph<>();

        try
        {
            initializeMainGraph(actorMapFile, movieMapFile, connectionsFile);
            constructErdosGraph();
        }

        catch (IOException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Initializing the main Adjacency Map Graph from the input files.
     */
    public void initializeMainGraph(String actorMapFile, String movieMapFile, String connectionsFile) throws IOException
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

            if (array.length > 0)
            {
                int actorID = Integer.parseInt(array[0]);
                String actorName = array[1];
                actorMap.put(actorID, actorName);
            }
        }

        actorMapInput.close();

        input = "";
        while ((input = movieMapInput.readLine()) != null && input.length() != 0)
        {
            String[] array = input.split("\\|");

            if (array.length > 0)
            {
                int movieID = Integer.parseInt(array[0]);
                String movieName = array[1];
                movieMap.put(movieID, movieName);
            }
        }

        movieMapInput.close();

        input = "";
        while ((input = connections.readLine()) != null && input.length() != 0)
        {
            String[] array = input.split("\\|");

            if (array.length > 0)
            {
                int movieID = Integer.parseInt(array[0]);
                int actorID = Integer.parseInt(array[1]);

                if (!connectionsMap.containsKey(movieID))
                    connectionsMap.put(movieID, new ArrayList<Integer>());

                connectionsMap.get(movieID).add(actorID);
            }
        }

        connections.close();

        // System.out.println(actorMap);
        // System.out.println(movieMap);
        // System.out.println(connectionsMap);

        for (int ID : actorMap.keySet())
        {
            if (actorMap.get(ID) != null)
                mainGraph.insertVertex(actorMap.get(ID));
        }

        for (int ID : connectionsMap.keySet())
        {
            String movie = movieMap.get(ID);

            for (int firstActorID : connectionsMap.get(ID))
            {
                for (int secondActorID : connectionsMap.get(ID))
                {
                    String firstActor = actorMap.get(firstActorID);
                    String secondActor = actorMap.get(secondActorID);

                    if (firstActor != secondActor && !mainGraph.hasEdge(firstActor, secondActor))
                    {
                        Set<String> edgeSetOfMovies = new HashSet<String>();
                        edgeSetOfMovies.add(movie);
                        mainGraph.insertUndirected(firstActor, secondActor, edgeSetOfMovies);
                    }

                    else if (firstActor != secondActor && mainGraph.hasEdge(firstActor, secondActor))
                    {
                        if (!mainGraph.getLabel(firstActor, secondActor).contains(movie))
                            mainGraph.getLabel(firstActor, secondActor).add(movie);
                    }
                }
            }
        }
    }

    /**
     * Creates the "Erdos Graph" (simplified path tree) using Graph Library bfs function.
     */
    public void constructErdosGraph()
    {
        erdosGraph = GraphLibrary.bfs(mainGraph, node);
    }

    /**
     * Initialize Command Line Interface
     */
    public void initializeCommandLine()
    {
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game");
        System.out.println();
        System.out.println(node + " is now the center of the acting universe, connected to " + erdosGraph.numVertices()
                + "/" + mainGraph.numVertices() + " actors with average separation " + GraphLibrary.averageSeparation(erdosGraph, node));
    }

    /**
     * Sets a node to the center of the universe, reconstructing the erdosGraph tree.
     */
    public void setCenterOfUniverse(String node)
    {
        if (mainGraph.hasVertex(node))
        {
            this.node = node;
            erdosGraph = GraphLibrary.bfs(mainGraph, node);
            System.out.println(node + " is now the center of the acting universe, connected to " + erdosGraph.numVertices()
                    + "/" + mainGraph.numVertices() + " actors with average separation " + GraphLibrary.averageSeparation(erdosGraph, node));
        }

        else
        {
            System.err.println("Invalid Center Of Universe");
        }
    }

    /**
     * Displays a list of all the actors with infinite separation from the center (not connected).
     */
    public void displayInfinitelySeparatedActors()
    {
        System.out.println(GraphLibrary.missingVertices(mainGraph, erdosGraph));
    }

    /**
     * Displays information for a given actor and their path to the center of the universe.
     */
    public void displayActorInformation(String actor)
    {
        if (mainGraph.hasVertex(actor) && erdosGraph.hasVertex(actor))
        {
            List<String> shortestPath = GraphLibrary.getPath(erdosGraph, actor);

            System.out.println(actor + "'s number is " + (shortestPath.size() - 1));

            String currentActor = actor;

            for (String nextActor : shortestPath)
            {
                if (currentActor != nextActor)
                {
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
     * Displays a list of the actors sorted by degree, with degree between low and high.
     */
    public void displayActorsByDegree(int low, int high)
    {
        PriorityQueue<String> actorsByDegree = new PriorityQueue<String>((String firstActor, String secondActor) -> mainGraph.outDegree(firstActor) - mainGraph.outDegree(secondActor));

        for (String actor: mainGraph.vertices())
        {
            if (mainGraph.outDegree(actor) <= high && mainGraph.outDegree(actor) >= low)
                actorsByDegree.add(actor);
        }

        List<String> sortedActorsByDegree = new ArrayList<String>();

        while (!actorsByDegree.isEmpty())
        {
            sortedActorsByDegree.add(actorsByDegree.poll());
        }

        System.out.println(sortedActorsByDegree);
    }

    /**
     * Displays a list of the actors sorted by non-infinite separation from the current center, with separation between
     * low and high.
     */
    public void displayActorsBySeparation(int low, int high)
    {
        PriorityQueue<String> actorsBySeparation = new PriorityQueue<String>((String firstActor, String secondActor) ->
                GraphLibrary.getPath(erdosGraph, firstActor).size() - GraphLibrary.getPath(erdosGraph, secondActor).size());

        for (String actor: erdosGraph.vertices())
        {
            if (GraphLibrary.getPath(erdosGraph, actor).size() <= high && GraphLibrary.getPath(erdosGraph, actor).size() >= low)
                actorsBySeparation.add(actor);
        }

        List<String> sortedActorsBySeparation = new ArrayList<String>();

        while (!actorsBySeparation.isEmpty())
        {
            sortedActorsBySeparation.add(actorsBySeparation.poll());
        }

        System.out.println(sortedActorsBySeparation);
    }

    /**
     * Displays a list of the top (positive number) or bottom (negative number) centers of the universe, sorted by
     * average separation.
     */
    public void displaySortedCentersOfUniverse(int value)
    {
        PriorityQueue<String> centersOfUniverse;

        if (value > 0)
        {
            centersOfUniverse = new PriorityQueue<String>((String firstActor, String secondActor) ->
                    (int) (findAverageSeparation(firstActor) - findAverageSeparation(secondActor)));
        }

        else if (value < 0)
        {
            centersOfUniverse = new PriorityQueue<String>((String firstActor, String secondActor) ->
                    (int) (findAverageSeparation(secondActor) - findAverageSeparation(firstActor)));
        }

        else
        {
            // TODO
            centersOfUniverse = new PriorityQueue<String>((String firstActor, String secondActor) ->
                    (int) (findAverageSeparation(firstActor) - findAverageSeparation(secondActor)));
        }

        // TODO - Still need to check everything underneath this line (for this method).
        for (String actor: mainGraph.vertices())
        {
            centersOfUniverse.add(actor);
        }

        List<String> sortedCentersOfUniverse = new ArrayList<String>();

        for (int i = 0; i < value; i += 1)
            sortedCentersOfUniverse.add(centersOfUniverse.poll());

        System.out.println(sortedCentersOfUniverse);
    }


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
        if (input == null || input.length() <= 0)
        {
            System.err.println("Invalid Input");
            return true;
        }

        String[] inputInfo = input.strip().split(" ");
        // for (String i: inputInfo)
            // System.out.println("\t" + i);

        if (inputInfo.length < 1)
        {
            System.err.println("Invalid Input");
            return true;
        }

        String command = inputInfo[0].toLowerCase();

        if (command.equals("q"))
        {
            return false;
        }

        else if (command.equals("i"))
        {
            displayInfinitelySeparatedActors();
            return true;
        }

        else if (inputInfo.length < 2)
        {
            System.err.println("Invalid Input");
            return true;
        }

        String information = "";

        for (int i = 1; i < inputInfo.length; i += 1)
        {
            information += inputInfo[i] + " ";
        }

        if (command.equals("p"))
        {
            information = information.stripTrailing();
            // REPETITIVE
            if (mainGraph.hasVertex(information))
            {
                displayActorInformation(information);
            }

            else
            {
                System.err.println("Invalid Input");
            }
        }

        if (command.equals("u"))
        {
            information = information.stripTrailing();
            if (mainGraph.hasVertex(information))
            {
                setCenterOfUniverse(information);
            }

            else
            {
                System.err.println("Invalid Input");
            }
        }

        if (command.equals("d"))
        {
            if (inputInfo.length != 3)
            {
                System.err.println("Invalid Input");
                return true;
            }

            try
            {
                int lowValue = Integer.parseInt(information.split(" ")[0]);
                int highValue = Integer.parseInt(information.split(" ")[1]);
                displayActorsByDegree(lowValue, highValue);
            }

            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        if (command.equals("s"))
        {
            if (inputInfo.length != 3)
            {
                System.err.println("Invalid Input");
                return true;
            }

            try
            {
                int lowValue = Integer.parseInt(information.split(" ")[0]);
                int highValue = Integer.parseInt(information.split(" ")[1]);
                displayActorsBySeparation(lowValue, highValue);
            }

            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        if (command.equals("c"))
        {
            if (inputInfo.length != 2)
            {
                System.err.println("Invalid Input");
                return true;
            }

            try
            {
                int value = Integer.parseInt(information);
                displaySortedCentersOfUniverse(value);
            }

            catch (Exception e)
            {
                System.err.println("Invalid Input");
                // System.err.println("\t" + e.getMessage());
            }
        }

        return true;
    }

    public void run()
    {
        initializeCommandLine();
        System.out.println();

        System.out.println("Kevin Bacon game >");
        String input = scanner.nextLine();

        boolean continueGame = acceptInput(input);

        while (continueGame)
        {
            System.out.println();
            System.out.println(node + " game >");
            input = scanner.nextLine();

            continueGame = acceptInput(input);
        }
    }

    /**
     * Main
     */
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        String actorMapFile = "PS4/actorsTest.txt";
        String movieMapFile = "PS4/moviesTest.txt";
        String connectionsFile = "PS4/movie-actorsTest.txt";

        KevinBaconGame game = new KevinBaconGame("Kevin Bacon", scanner, actorMapFile, movieMapFile, connectionsFile);
        game.run();
    }
}