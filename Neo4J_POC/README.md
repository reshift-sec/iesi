
## NEO4J POC

### What

This is a proof of concept showing how Neo4J could be used for the use-case dependency management between actions in scripts.
It consists of:
- **Neo4J Embedded** : shows how a Neo4J server can be ran from within the Java code, this will need to be placed in a new Iesi service that will be running in the background / as a service / or at least is up for the duration of use.
- **Storing data** : a YAML file with a similar layout as the existing Iesi script metadata file is read in and parsed into script - actions - actionparameters and loaded as such in the graph db (including relations and properties)
- **Reading data** : this stored data is than again retrieved and a query is executed that will show all nodes ordered by the longest dependency paths first. Within this dependency path, the actions are ordered so that the dependency is first. Then the java code will be able to read the actions one by one on the order they should be executed (ignoring already treated actions). This logic should still be reviewed for more complex situations.

More details are in the code comments.

### How to run

#### Prerequisites
* Download Neo4J ([Overview](https://neo4j.com/download-center/#panel2-2) |  [Windows 3.5.4](https://dls2gnud23o1w.cloudfront.net/artifact.php?name=neo4j-community-3.5.4-windows.zip) | [Unix 3.5.4](https://dls2gnud23o1w.cloudfront.net/artifact.php?name=neo4j-community-3.5.4-unix.tar.gz)) and extract to your disk
* Iesi distribution, used for libraries. If not, you need to download libraries manually like SnakeYaml

Note: since Neo4j libs are approx 100mb, you can download any of the above packages during installation of Iesi and use the neo4j libs folder instead of packaging all these libs with iesi.

#### Run
You can run from your Java IDE using the pom file for dependencies, or with the JAR file:
```
mkdir poc
copy TestNeo4Jdriver.jar .
copy my_script.yml .
java -cp "Neo4jPoc.jar;path-to\neo4j-community-3.5.4_???\lib\*;path-to\Iesi\lib\*" org.poc.main
```
Note: the script will create the Neo4J database in the folder where you start this script! It also assumes this folder holds the my_script.yml file which contains the script and actions to be loaded. You can alter this file to define another script and actions. 

#### Visualisation
The code includes a Bolt connector, which will enable connecting from a GUI. This is not required for the functionalityn but enables you to have a visual overview of what happened in the Neo4J DB. 

You can use the browser GUI that comes with Neo4J, just launch a Neo4J instance:

`path-to\neo4j-community-3.5.4_???\bin\neo4j.bat console`

Open your browser at [http://localhost:7474/browser/](http://localhost:7474/browser/). 
Don't connect to the default instance, but to port 7688 (user and pass is neo4j), this is our embedded server.

