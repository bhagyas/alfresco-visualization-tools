Alfresco Visualization Tools
=============================

> 
> Hello from Berlin!
> Alfresco DevCon Hackathon in Berlin 2012 November 5th.

Introduction
------------

Alfresco Visualization Tools (AVT) provides a platform to bring content visualization to Alfresco.

It also includes dashlets to view and visualize content within Alfresco repositories using D3.js and Simile Project.

Following visualizations are provided along with this project.

 - Alfresco Activity Feed Timeline
 - Alfresco Site Content Visualization 

More visualizations are coming. You can also contribute your visualizations.

Since AVT is built against Alfresco Community 4.2.b, you need Java SDK 7 installed; you can easily switch between different JDK versions using the command on MacOSX

 `alias jdk6="export JAVA_HOME='/usr/libexec/java_home -v1.6'"`
 `alias jdk7="export JAVA_HOME='/usr/libexec/java_home -v1.7'"`
 (in `~/.bash_profile`)

Building AVT
------------
This project uses alfresco-maven SDK. (https://artifacts.alfresco.com/nexus/content/repositories/alfresco-docs/alfresco-lifecycle-aggregator/latest/index.html)

To generate the AMP files, run the following command from the project root

- `MAVEN_OPTS="-Xms256m -Xmx2G -XX:PermSize=300m" mvn clean package -DskipTests`

To run tests, run the following command

- `MAVEN_OPTS="-Xms256m -Xmx2G -XX:PermSize=300m" mvn clean test`
(check repository-amp/src/test/java/org/alfresco/repo/visualization for - a currently empty - example)

Running AVT
------

To run Alfresco Repository AMP
- `cd repository-amp`
- `MAVEN_OPTS="-Xms256m -Xmx2G -XX:PermSize=300m" mvn clean integration-test -Pamp-to-war -DskipTests`

If you want to drop the Alfresco DB and contentstore, add the `-Ppurge` build option.

 To run Alfresco Share AMP, open up a separate shell (connects to repository AMP)

- `cd share-amp`
- `MAVEN_OPTS="-Xms256m -Xmx2G -XX:PermSize=300m" mvn clean integration-test -Pamp-to-war -Ppurge -DskipTests -Djetty.port=8888`

Testing AVT
------
- Login onto http://localhost:8888/share-amp (admin/admin)
- Click on 'Customize Dashlet' on the page top right
- Click on 'Add Dashlet' at the bottom of the page
- Drag 'n Drop the 'Timeline' dashlet in the page layout at the bottom of the page (preferably in the main column of the page, since the timeline is wide)
- Save the page

At this point you should see a timeline appearing in your page; anytime you update a content, the event should be mapped into the timeline


Notes
------

Please note that you should be connected to the internet at the time you are running the Timeline dashlet, as the dashlet currently uses the Simile libraries hosted on their servers.

Resources
----------
- D3.js (http://d3js.org)
- Timeline - Simile Project (http://simile-widgets.org/timeline/)
- Maven Alfresco SDK (https://artifacts.alfresco.com/nexus/content/repositories/alfresco-docs/alfresco-lifecycle-aggregator/latest/index.html)

Please feel free to contribute or raise an issue in the GitHub related to this project.

- Bhagya (bhagyas) (http://about.me/bhagyas)
- Axel (AFaust) (http://axel-faust.de)
- Maurizio (maoo) (http://session.it)
- Nathan (ntmcminn) (http://nathanmcminn.com)
- Gabriele (mindthegab) (http://mindthegab.com)


With love.