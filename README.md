UW-MadisonInteractiveMap

Project 3 from CS400: Group of 8 -> 2 subgroups of 4 containing...

Data Wrangler  > created the locations.gv file of a mock graph in DOT format and created java classes to parse through the .gv file
Algorithm Engineer > created the graph data structure with associated funtions and implemented kruskal's algorithm to find the minimum spanning tree
Backend Developer(me) > integrated info from locations.gv file into graph
Frontend Developer > created a command loop for the user - command line interaction
Description

An interactive user - command line map that loads a graph of some of UW-Madison's buildings from a .gv file (building distances/weights and locations are not based on reality) and uses kruskal's algorithm to find shortest path's between buildings..



image of a demo run of the uw madison interactive map

Capabilities

Load info from a DOT structured graph file
Add/remove buildings (nodes)
Add/delete roads (edges)
Find the shortet path between two buildings
Find and print the minimum spanning tree to reach all buildings in graph
View all buildings with where you can get to and from, and at what distance (cost/weight)
