package org.poc;

//import org.neo4j.driver.v1.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.configuration.BoltConnector;

import java.io.*;
import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.*;

import org.yaml.snakeyaml.Yaml;

import static org.neo4j.graphdb.Label.label;


public class main {
    private static enum RelTypes implements RelationshipType
    {
        HAS_ACTION,
        DEPENDS_ON,
        HAS_PARAMETER
    }

    public static void main(String[] args) {


        //
        // CONNECT TO EXTERNAL NEO4J
        //
        /*
        Driver driver = GraphDatabase.driver("bolt://localhost:7688", AuthTokens.basic("neo4j", "neo4j"));
        try (Session session = driver.session()) {
            StatementResult rs = session.run("CREATE (n:Person { name: 'Andy', title: 'Developer' }) RETURN n");
        }
        driver.close();
        */


        //
        // LAUNCH EMBEDDED NEO4J
        //
        printTime("Bringing up Neo4J embedded instance");

        //note you can also provide options for the db to be creted: https://neo4j.com/docs/java-reference/current/tutorials-java-embedded/
        File databaseDirectory = new File("neo4j.db");

        //GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( databaseDirectory );

        //to have gui:
        BoltConnector bolt = new BoltConnector( "0" );

        GraphDatabaseService graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder( databaseDirectory )
                .setConfig( bolt.type, "BOLT" )
                .setConfig( bolt.enabled, "true" )
                .setConfig( bolt.listen_address, "localhost:7688" )
                .newGraphDatabase();

        printTime("Neo4J started");

        // make sure neo4j closed when program ends
        registerShutdownHook( graphDb );

        //
        // READ YAML
        //
        Metadata metadata = readYaml("my_script.yml");

        // Will hold scriptname we load so we read it out again alter
        String scriptName = "";

        //
        // PARSE AND STORE IN NEO4J
        //
        try ( Transaction tx = graphDb.beginTx() )
        {
            //SCRIPT
            Script script = metadata.getData();
            System.out.println("Parsing YAML. Found script with name " + script.getName());
            //remember to use it in our retrieval demo:
            scriptName = script.getName();

            // Check if this script is not yet in our DB
            Iterator findIt = graphDb.findNodes( label("Script"), "name", script.getName());
            if(findIt.hasNext()) {
                System.out.println("ATTENTION: A script with this name is already loaded in the DB. SKIPPING CREATION.");

            } else {
                System.out.println("-Script not yet in DB, creating..");

                // INSERT INTO DB BASED ON YAML DATA

                Node scriptNode = graphDb.createNode( label("Script"));
                scriptNode.setProperty( "name", script.getName() );
                scriptNode.setProperty( "description", script.getDescription() );

                //ACTIONS
                List<Action> actions = script.getActions();
                for (Action action : actions) {
                    System.out.println("--Found action with name " + action.getName());

                    Node actionNode = graphDb.createNode( label("Action"));
                    actionNode.setProperty( "name", action.getName() );
                    actionNode.setProperty( "description", action.getDescription() );
                    actionNode.setProperty( "type", action.getType() );

                    //ADD SCRIPT RELATION
                    Relationship relationship = scriptNode.createRelationshipTo(actionNode, RelTypes.HAS_ACTION );


                    if(action.getDepends_on() != null) {
                        //add relation to existing action
                        String parentActionName = action.getDepends_on();
                        Node parentActionNode = graphDb.findNodes( label("Action"), "name", parentActionName).next(); //NOTE THIS NOW ASSUMES THERE IS ONLY 1 NODE
                        actionNode.createRelationshipTo(parentActionNode, RelTypes.DEPENDS_ON );
                    }

                    //PARAMETERS
                    List<ActionParameter> actionParameters = action.getParameters();
                    if(actionParameters != null) {
                        for (ActionParameter actionParameter : actionParameters) {
                            System.out.println("---Found actionParameter with name " + actionParameter.getName());

                            Node actionParameterNode = graphDb.createNode(label("ActionParameter"));
                            actionParameterNode.setProperty("name", actionParameter.getName());
                            actionParameterNode.setProperty("value", actionParameter.getValue());

                            //add relation to action
                            actionNode.createRelationshipTo(actionParameterNode, RelTypes.HAS_PARAMETER);
                        }
                    }
                }
            }

            //commit
            tx.success();

        }



        //
        // READ OUT NEO4J SCRIPT
        //
        // The difficulty now is to show dependency paths without overlap and duplicate subpaths
        // Note: The below approach might not cover all edge cases yet.
        try ( Transaction tx = graphDb.beginTx() )
        {
            System.out.println("");
            System.out.println("Retrieving script " + scriptName + " from DB and deducting actions to run and their order..");

            // get script actions: we will show all dependency paths, ordered by length
            // Note at the end we union with the full list of actions of this script so we do not miss the ones without dependencies
            Result rs = graphDb.execute("match (s:Script {name:'" + scriptName + "'})-[:HAS_ACTION]->(a:Action) " +
                    "match actionPath=(:Action)<-[:DEPENDS_ON*]-(a) " +
                    "with nodes(actionPath) as actions, length(actionPath) as num " +
                    "order by num desc " +
                    "return actions " +
                    "union all match (s:Script {name:'" + scriptName + "'})-[:HAS_ACTION]->(actions:Action) " +
                    "return actions"
            );

            // Our ordered actions list
            List <Node> actions = new ArrayList<Node>();

            // now this result set will also show subpaths, so we need to ignore already handled actions
            // we can do this since they are ordered in dependency path length
            while (rs.hasNext()) {
                Map<String, Object> result = rs.next();
                //System.out.println(result);
                if (result.get("actions") instanceof List<?>){
                    List<Node> nodeList = (List<Node>) result.get("actions");
                    Iterator it = nodeList.iterator();
                    while (it.hasNext()) {
                        Node action = (Node) it.next();
                        //System.out.println(action.getProperty("name"));
                        if (!actions.contains(action)) {
                            actions.add(action);
                        }
                    }
                } else {
                    // individual node
                    Node action = (Node) result.get("actions");
                    //System.out.println(action.getProperty("name"));
                    if (!actions.contains(action)) {
                        actions.add(action);
                    }
                }

                
            }

            tx.success();

            // Now we have the order to execute
            System.out.println("Execute these actions in order as listed: ");
            for(Node action : actions) {
                System.out.println("-" + action.getProperty("name"));
            }


        }


        try {
            System.out.println("Waiting a long time before exit so you use a neo4j browser and connect to this instance for viewing...");
            Thread.sleep(600000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static Metadata readYaml(String yamlFileName) {

        Yaml yaml = new Yaml();

        try {
            String currentDir = System.getProperty("user.dir");
            File initialFile = new File(currentDir + "\\" + yamlFileName);
            InputStream inputStream = null;

            inputStream = new FileInputStream(initialFile);
            if(inputStream == null) {

                System.out.println("COULD NOT OPEN FILE!");
                return null;
            }
            Metadata metadata = (Metadata) yaml.load(inputStream);
            return metadata;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    private static void printTime(String comment) {
        Date date= new Date();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        System.out.println(ts + " - " + comment);
    }
}