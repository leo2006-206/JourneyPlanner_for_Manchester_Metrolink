import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.Timer;

//please take a look the ../Document.txt, included all variables and functions with purposes and dependencies
public class graph {
    // include mainly two part: graph construction and graph operation

    // graph construction, to construct the graph(stations and edges between stations) by getting date from reader.java
    // each station have a corresponding int index(like the second name of the station), this help the program to easily access the station or edge by array lookup
    // each edge have colour(the line colour) and distance(the time cost via this edge), all edges store in the matrix(2d array)
    // example: mapping.get(station_name "a") -> get index 0, then station_array.get(0) -> get the station information of station "a"
    // example: to get the edges from "Bury" to "Victoria", for(edge e : matrix[mapping.get("Bury")][mapping.get("Victoria")])
    
    // the 2d array store the whole undirected graph, matrix[a][b] store a ArrayList of edges from station a to station a, where a and b is the index of the corresponding station.
    private ArrayList<edge> matrix[][];
    // the 2d array that store the garph of the walking time between each station
    private double walking_matrix[][];
    // the size of matrix and walking_matrix, since i always double the size of both matrix, therefore the matrix_size != the number of nodes
    private int matrix_size;
    // the number of used row in the matrix, it will differ to the num_actual_nodes only if some nodes are removed, but since no removing needed, therefore num_used_row == num_actual_nodes
    private int num_used_row;
    // the number of actual nodes in the matrix, it will differ to the num_used_row only if some nodes are removed, but since no removing needed, therefore num_used_row == num_actual_nodes
    private int num_actual_nodes;
    // the HashMap that take the name of the station(String) and return the index of the station
    private HashMap<String, Integer> mapping;
    // the 1d ArrayList that store all the info of the station, the station info will store at the index position. 
    private ArrayList<station> station_array;

    // graph operation, mainly contain path_finding and visualisation of the process

    // the string of the corresponding colour, all colour in the graph is a int store in edge
    private final String line_colour[] = {"yellow", "purple", "green", "lightblue", "pink", "darkblue", "red"};
    // the hex colour of the corresponding colour, use to set font colour
    private final String font_colour[] = {"#EFBB00", "#7B2182", "#318C2C", "#5BC5F2", "#F088B6", "#0069B4","#E30613"};
    // the constant of maximum colour number
    private final int MAX_COLOUR = 7;
    // graph_visualisation obj to setup graph and perform visualisation
    private graph_visualisation visual;
    // window obj to valid the input, visual the path_finding processes and print the result
    private window ui;
    // the queue for the path_finding searching animation
    private ConcurrentLinkedQueue<travel_node> searching_q;
    // the queue for the path_finding resulting animation
    private ConcurrentLinkedQueue<travel_node> resulting_q;
    // let the program know the current path_finding mode
    private int curr_mode;

    public class edge{
        // to store the information of the connection between station
        int colour;
        double distance;
        edge(int in_colour, double in_distance){
            this.colour = in_colour;
            this.distance = in_distance;
        }
    }
    public class station{
        // to store the information of the station
        String name;
        boolean closure;
        boolean removed;

        station(String in_name){
            this.name = in_name;
            this.closure = false;
            this.removed = false;
        }
    }
    public class travel_node{
        // to store info when operating path_finding Dijkstra_Algorithms
         int index;
         double cumulative_distance;
         edge line;
         travel_node from;
         travel_node(int in_index, double in_distance, edge in_line, travel_node in_from){
            this.index = in_index;
            this.cumulative_distance = in_distance;
            this.line = in_line;
            this.from = in_from;
         }
    }
    private class resulting_state{
        // to store info when performing visualisation of path_finding processing and resulting
        int current_colour;
        int total_change;
        double total_distance;
        resulting_state(){
            current_colour = -1;
            total_change = 0;
            total_distance = 0;
        }
    }
    public class Node_Comparator implements Comparator<travel_node>{
        // Comparator for PriorityQueue
        public int compare(travel_node x, travel_node y){
            if(x.cumulative_distance < y.cumulative_distance) return -1;
            if(x.cumulative_distance > y.cumulative_distance) return 1;
            return 0;
        }
    }
    private void matrix_enlarge(){
        // double the size of the both matrix and walking_matrix, update the matrix_size
        int increased_size = matrix_size * 2;

        ArrayList<edge> new_matrix[][] = new ArrayList[increased_size][increased_size];
        double new_walking_matrix[][] = new double[increased_size][increased_size];
        if(matrix != null){
            for(int i=0;i<matrix_size;i++)
                for(int j=0;j<matrix_size;j++){
                    new_matrix[i][j] = matrix[i][j];
                }
        }
        
        this.matrix_size = increased_size;
        this.matrix = new_matrix;
        this.walking_matrix = new_walking_matrix;
        return;
    }
    private boolean add_node_to_mapping(String in_name){
        // if in_name not in mapping, add to mapping with a index, also add a new station info into station_array of in_name
        // then num_used_row++ and num_actual_nodes++, if num_actual_nodes > matrix_size, call matrix_enlarge()
        if(mapping.containsKey(in_name) == false){
            mapping.put(in_name, num_used_row);
            station_array.add(new station(in_name));
            num_used_row++;
            num_actual_nodes++;
            //System.out.println("actual_nodes = "+num_actual_nodes+" matrix_size = "+matrix_size+" used = "+num_used_row);
            if(num_actual_nodes > matrix_size)
                matrix_enlarge();
            return true;
        }
        return false;
        
    }
    public void add_edges(String source, String end, double distance, int colour){
        // API for the reader.java to input the data, if source or end not in mapping, call add_node_to_mapping(String in_name), 
        // then add the edge into the matrix[source_index][end_index] and matrix[end_index][source_index]
        if(mapping.get(source) == null){
            add_node_to_mapping(source);
        }
        if(mapping.get(end) == null){
            add_node_to_mapping(end);
        }
        int a = mapping.get(source);
        int b = mapping.get(end);
        //System.out.println(source+" "+a+" & "+end+" "+b);
        if(matrix[a][b] == null && a != b)
            matrix[a][b] = new ArrayList<edge>();
        if(matrix[b][a] == null)
            matrix[b][a] = new ArrayList<edge>();
        edge ele = new edge(colour, distance);
        matrix[a][b].add(matrix[a][b].size(), ele);
        matrix[b][a].add(matrix[b][a].size(), ele);
        return;
    }
    public void add_walking(String source, String end, double distance){
        // API for the reader.java to input the data, add the walking time to walking_matrix[source.index][end_index] (directed graph)
        if(!mapping.containsKey(source) || !mapping.containsKey(end) || distance < 0) return;
        walking_matrix[mapping.get(source)][mapping.get(end)] = distance;
        //System.err.println(walking_matrix[mapping.get(source)][mapping.get(end)]+" from "+source+" "+mapping.get(source)+" to "+end);
        return;
    }
    public void matrix_printing(){
        // API for CUI, to print all the nodes(stations) with edges
        if(num_actual_nodes <= 0){
            System.out.println("Empty graph");
            return;
        }
        System.out.println("Num_nodes = "+num_actual_nodes);
        for(int i=0;i<num_used_row;i++){
            edge_printing(station_array.get(i).name);
        }
        return;
    }
    public boolean edge_printing(String target){
        // API for both CUI and GUI, to print all the edges of target(CUI), 
        // change the colour of target and adjacent nodes with text massage enter to window
        if(num_actual_nodes <= 0){
            System.out.println("Empty graph");
            return false;
        }
        else if(!mapping.containsKey(target)){
            System.out.println("Station undefined");
            return false;
        }
        else{
            int index = mapping.get(target);
            station current = station_array.get(index);
            if(current.closure == true){
                System.out.println("\nNode name: "+current.name+" index: "+index+" !!!Closed!!!");
                ui.delay_edges_printing_add_text("Source = "+current.name+" !!!Closed!!!\n\n");
            }
            else{
                System.out.println("\nNode name: "+current.name+" index: "+index);
                ui.delay_edges_printing_add_text("Source = "+current.name+"\n\n");
            }
            for(int i=0;i<num_used_row;i++){
                if(matrix[index][i] == null) continue;
                for(int j=0; j<matrix[index][i].size(); j++){
                    System.out.println("        Node "+current.name+" to Node "+station_array.get(i).name+" distance "+matrix[index][i].get(j).distance+" on line "+line_colour[matrix[index][i].get(j).colour]);
                    ui.delay_edges_printing_add_text(current.name+" to "+station_array.get(i).name+"\nwith time "+matrix[index][i].get(j).distance+" on line "+line_colour[matrix[index][i].get(j).colour]+"\n\n");
                    // visual.set_point_colour(index, i);
                    // visual.repaint();
                    searching_q_add(new travel_node(index, 0, null, new travel_node(i, 0, null, null)));
                }
            }
            delay_q_start();
            return true;
        }
    }
    public boolean station_validation(String target){
        // API for both CUI and GUI, to check do the target exist, give a circle to the target if exist(GUI)
        if(mapping.containsKey(target)){
            visual.set_current(mapping.get(target));
            visual.repaint();
            return true;
        }
        return false;
    }
    public boolean get_station_closure(String target){
        visual.set_current(mapping.get(target));
        visual.repaint();
        return station_array.get(mapping.get(target)).closure;
    }
    public ArrayList<station> get_stationArray(){
        return station_array;
    }
    public ArrayList<edge>[][] get_matrix(){
        return matrix;
    }
    public int get_matrix_size(){
        return matrix_size;
    }
    public HashMap<String, Integer> get_mapping(){
        return mapping;
    }
    /*
     * the input input validation of in_colout and in_delay already in the Main.java
     * if(source || end not exist) return false
     * iterate all edges(a arraylist) from source to end
     *      if(find the path with corrent colour)
     *          update delay, then reutrn true
     * return false(edge not find)
     */
    public boolean delay_update(String source, String end, String in_colour, double in_delay){
        // API for both CUI and GUI, to update the time of only one edge, new time = original time + delay
        if(!mapping.containsKey(source) || !mapping.containsKey(end)) return false;
        int colour_index = -1;
        for(int i = 0; i < MAX_COLOUR; i++)
            if(line_colour[i].equals(in_colour)){
                colour_index = i;
                break;
            }
        int a = mapping.get(source), b = mapping.get(end);
        for(edge path : matrix[a][b]){
            if(path.colour == colour_index){
                path.distance = path.distance + in_delay;
                if(in_delay > 0){
                    visual.set_corrent_colour(b, a, path.colour);
                    visual.repaint();
                    ui.delay_edges_printing_add_text("Old: "+station_array.get(a).name+" to "+station_array.get(b).name+"\nwith time "+(path.distance - in_delay)+" on line "+line_colour[path.colour]+"\n\n");
                    ui.delay_edges_printing_add_text("New: "+station_array.get(a).name+" to "+station_array.get(b).name+"\nwith time "+path.distance+" on line "+line_colour[path.colour]);
                }
                return true;
            }
        }
        return false;
    }
    public boolean closure_update(String target, boolean next_state){
        // API for both CUI and GUI, to update the closure state of the station
        if(!mapping.containsKey(target)) return false;
        station current = station_array.get(mapping.get(target));
        current.closure = next_state;
        visual.set_closure(mapping.get(target), next_state);
        visual.repaint();
        return true;
    }
    /*
     * API of Dijkstra_Algorithm
     * if(source and end in the mapping && source != end)
     *      if(source || end closed) 
     *          return false
     *      call the actual Dijkstra_Algorithm
     * else return false with error output
     */
    public boolean Dijkstra(String source, String end, int mode){
        // API for both CUI and GUI, verify the inputs and make a path_finding enquiry(mode: 1 => shortest, 2 => fewest, 3 => walking)
        if(mapping.containsKey(source) && mapping.containsKey(end) && mapping.get(source) != mapping.get(end)){
            int source_index = mapping.get(source), end_index = mapping.get(end);
            if(station_array.get(source_index).closure == true){
                System.out.println("\nStation "+station_array.get(source_index).name+" already Closed");
                ui.resulting_add_text("Station "+station_array.get(source_index).name+" already Closed");
                return false;
            }
            else if(station_array.get(end_index).closure == true){
                System.out.println("\nStation "+station_array.get(end_index).name+" already Closed");
                ui.resulting_add_text("Station "+station_array.get(end_index).name+" already Closed");
                return false;
            }
            else if(mode == 1){
                curr_mode = 1;
                System.out.println("\nShortest path from "+source+" to "+end);
                Dijkstra_Algorithm_Shortest(source_index, end_index);
            }
                
            else if(mode == 2){
                curr_mode = 2;
                System.out.println("\nFewest colour change path from "+source+" to "+end);
                Dijkstra_Algorithm_Fewest(source_index, end_index);
            }
            else if(mode == 3){
                curr_mode = 3;
                System.out.println("\nWalking path from "+source+" to "+end);
                Dijkstra_Algorithm_Walking(source_index, end_index);
            }
            // else if(mode == 4){
            //     curr_mode = 1;
            //     for(int i=0; i<num_actual_nodes; i++){
            //         for(int j=0; j<num_actual_nodes; j++){
            //             if(i == j) continue;
            //             double a = Dijkstra_Algorithm_Shortest(i, j), b = Dijkstra_Algorithm_Fewest(i, j);
            //             if(a > b){
            //                 System.out.println("\n\n\n"+station_array.get(i).name+" to "+station_array.get(j).name+" | Shortest dis = "+a+" Fewest dis = "+b+"\n\n\n");
            //             }
            //             System.out.println(station_array.get(i).name+" to "+station_array.get(j).name+" | Shortest dis = "+a+" Fewest dis = "+b);
            //         }
            //     }
            // }
            return true;
        }
        return false;
    }
    private double Dijkstra_Algorithm_Shortest(int source, int end){
        // for all Dijkstra_Algorithms
        // in the searching stage, always add the current node to the searching_q, start from source to end(searching path)
        // in the resulting stage, always add the current node to the resulting_q, start from source to end(correct path)
        // print the result in CUI and GUI

        // in short, start the Dijkstra algorithm that to expand the node with lowest cumulative_distance in the queue(working on the matrix)

        /*
         * in detail,
         *  key components:
         *      node_queue: the main queue
         *      record: the 2D array to store all explored nodes
         *              each the station with different colour edges, record will store the station will all different colour
         *      result_stack: the stack to store the result and reverse the order
         *  1.  find all possible colour of source
         *  2.  for all possible colour of source:
         *          initialise the components
         *          add the source will colour
         *          while node_queue notEmpty and current != end:
         *              current = the node with least cumulative_distance in the node_queue
         *              add current to searching_q for animation and text massage for GUI
         *              for all current's adjacency nodes v:
         *                  for all edges from current to v:
         *                      if colour changed: distance+=2
         *                      if record[path.colour][v] unexplore || find a shorter path to v:
         *                          add to record
         *                          add to node_queue
         *          if result is empty || new result path shorter
         *              push into result_stack
         *  3.  for i in result_stack
         *          print result in CUI
         *          add result to resulting_q for animation and text massage for GUI   
         */
        PriorityQueue<travel_node> node_queue = new PriorityQueue<travel_node>(new Node_Comparator());
        ArrayList<travel_node> result_stack = new ArrayList<travel_node>();
        travel_node[][] record;
        travel_node current, result = null;
        boolean intermediate[] = new boolean[MAX_COLOUR];
        int current_colour = -1, total_change = 0;
        double total_distance = -1;
        for(int x=0; x<num_actual_nodes; x++){
            if(matrix[source][x] == null) continue;
            for(edge start : matrix[source][x]){
                if(intermediate[start.colour] == false)
                    intermediate[start.colour] = true;
            }
        }
        for(int x=0; x<MAX_COLOUR; x++){
            if(intermediate[x] = false) continue;
            node_queue.clear();
            record = new travel_node[MAX_COLOUR][num_actual_nodes];
            searching_q.clear();
            node_queue.offer(new travel_node(source, 0, new edge(x, 0), null));
            do{
                current = node_queue.poll();
                current_colour = current.line.colour;
                if(current.from != null) searching_q_add(current);
                //System.out.println("Current = "+station_array.get(current.index).name+" with index "+current.index+" with dis "+current.cumulative_distance+" current_color = "+current_colour+" __ q.size = "+node_queue.size());
                for(int i=0;i<num_actual_nodes;i++){
                    if(matrix[current.index][i] != null){
                    double dis;
                    for(edge path : matrix[current.index][i]){
                            dis = path.distance;
                            if(current_colour != path.colour) dis = dis + 2;
                            if(record[path.colour][i] == null || record[path.colour][i].cumulative_distance > (dis + current.cumulative_distance)){
                                record[path.colour][i] = new travel_node(i, dis + current.cumulative_distance, path, current);
                                node_queue.offer(record[path.colour][i]);
                            }
                        }
                    }  
                }
            }while(!node_queue.isEmpty() && current.index != end);
            //System.out.println("Total_dis = "+record[end].cumulative_distance);
            // if(record[end] == null){
            //     //System.out.println("Destination unfound");
            //     continue;
            // }
            travel_node temp = null;
            for(int i=0; i<MAX_COLOUR; i++){
                if(record[i][end] == null) continue;
                if(temp == null || temp.cumulative_distance > record[i][end].cumulative_distance)
                    temp = record[i][end];
            }
            if(total_distance == -1 || temp.cumulative_distance < total_distance){
                result = temp;
                total_distance = result.cumulative_distance;
                result_stack.clear();
                do{
                    result_stack.add(0, result);
                    result = result.from;
                }while(result != null);
                //System.out.println("        Stack size = "+result_stack.size());
            }
        }
        current_colour = -1;
        total_distance = 0;
        //System.out.println("stack size = "+result_stack.size());
        for(int i = 0; i < result_stack.size(); i++){
            result = result_stack.get(i);
            total_distance = total_distance + result.line.distance;
            if(result.from == null){
                current_colour = result_stack.get(i+1).line.colour;
                System.out.println("!!!Start from "+line_colour[current_colour]+" line!!!");
            }
            else if(current_colour != result.line.colour){
                current_colour = result.line.colour;
                System.out.println("!!!Change to "+line_colour[current_colour]+" line!!!");
                total_change++;
                total_distance = total_distance + 2;
            }
            if(result.from != null){
                System.out.println("    From "+station_array.get(result.from.index).name+" To "+station_array.get(result.index).name+" on line "+line_colour[result.line.colour]+" with dis "+result.cumulative_distance);
                resulting_q_add(result);
            }
            else{
                System.out.println("    Start from "+station_array.get(result.index).name);
            }
        }
        System.out.println("Total time = "+total_distance+"\nTotal line change = "+total_change);
        return total_distance;
    }
    private double Dijkstra_Algorithm_Fewest(int source, int end){
        /*
         *  This is a 0-1 BFS(or Dijkstra with only 0 and 1 cost)
         *  where the cumulative_dis here is the total time of linecolour changing to this node
         *  in the process, we don't care the total time/distance it take
         * in detail,
         *  key components:
         *      node_queue: the main queue
         *      record: the 2D array to store all explored nodes
         *              each the station with different colour edges, record will store the station will all different colour
         *      result_stack: the stack to store the result and reverse the order
         *  1.  find all possible colour of source
         *  2.  for all possible colour of source:
         *          initialise the components
         *          add the source will colour
         *          while node_queue notEmpty and current != end:
         *              current = the node with least cumulative_distance (the colour change time) in the node_queue
         *              add current to searching_q for animation and text massage for GUI
         *              for all current's adjacency nodes v:
         *                  for all edges from current to v:
         *                      if(path.colour == current.colour) cost = 0;
         *                      else cost = 1;
         *                      current_to_i_cost = cost + current.cumulative_cost;
         *                      if(record[i] == null || record[i].cost > current_to_i_cost)
         *                          if(cost == 1) node_queue.addLast();
         *                          else if(cost == 0) node_queue.addFirst();
         *                      this keep the queue always expand the node with 0 cost first
         *          if result is empty || new result path shorter
         *              push into result_stack
         *  3.  for i in result_stack
         *          print result in CUI
         *          add result to resulting_q for animation and text massage for GUI   
         */
        ArrayList<travel_node> node_queue = new ArrayList<travel_node>();
        ArrayList<travel_node> result_stack = new ArrayList<travel_node>();
        travel_node[][] record;
        travel_node current, result = null;
        boolean[] intermediate = new boolean[MAX_COLOUR];
        int current_colour = -1; 
        double total_dis = 0, fewest = -1;
        for(int x=0; x<num_actual_nodes; x++){
            if(matrix[source][x] == null) continue;
            for(edge start : matrix[source][x]){
                if(intermediate[start.colour] == false)
                    intermediate[start.colour] = true;
            }
        }
        for(int x=0; x<MAX_COLOUR; x++){
            if(intermediate[x] == false) continue;
            //System.out.println("Start from colour "+x+" "+line_colour[x]);
            record = new travel_node[MAX_COLOUR][num_actual_nodes];
            searching_q.clear();
            node_queue.clear();
            node_queue.add(node_queue.size(), new travel_node(source, 0, new edge(x, 0), null));
            do{
                current = node_queue.remove(0);
                if(current.from != null) searching_q_add(current);
                //System.out.println("    Current = "+station_array.get(current.index).name+" with index "+current.index+" with dis "+current.cumulative_distance+" __ q.size = "+node_queue.size());
                for(int i=0; i<num_actual_nodes; i++){
                    if(matrix[current.index][i] == null) continue;
                    double dis = 0;
                    for(edge path : matrix[current.index][i]){
                        if(current.line == null || current.line.colour == path.colour){
                            dis = current.cumulative_distance;
                            if(record[current.line.colour][i] == null || record[current.line.colour][i].cumulative_distance > dis){
                                record[current.line.colour][i] = new travel_node(i, dis, path, current);
                                node_queue.add(0, record[current.line.colour][i]);
                            }
                        }
                        else{
                            dis = current.cumulative_distance + 1;
                            if(record[path.colour][i] == null || record[path.colour][i].cumulative_distance > dis){
                                record[path.colour][i] = new travel_node(i, dis, path, current);
                                node_queue.add(node_queue.size(), record[path.colour][i]);
                            }
                        }
                    }
                }
            }while(!node_queue.isEmpty() && current.index != end);
            // if(record[][end] == null){
            //     System.out.println("Destination unfound");
            //     continue;
            // }
            travel_node temp = null;
            for(int i=0; i<7; i++){
                if(record[i][end] == null) continue;
                if(temp == null || temp.cumulative_distance > record[i][end].cumulative_distance){
                    temp = record[i][end];
                }
            }
            if(fewest == -1 || temp.cumulative_distance < fewest){
                result = temp;
                fewest = result.cumulative_distance;
                result_stack.clear();
                do{
                    result_stack.add(0, result);
                    result = result.from;
                }while(result != null);
                //System.out.println("        Stack size = "+result_stack.size());
            }
        }
        // if(record[end] == null){
        //     System.out.println("Destination unfound");
        //     return;
        // }
        current_colour = -1;
        //System.out.println("stack size = "+result_stack.size());
        for(int i = 0; i < result_stack.size(); i++){
            result = result_stack.get(i);
            total_dis = total_dis + result.line.distance;
            if(result.from == null){
                current_colour = result_stack.get(i+1).line.colour;
                System.out.println("!!!Start from "+line_colour[current_colour]+" line!!!");
            }
            else if(current_colour != result.line.colour){
                current_colour = result.line.colour;
                System.out.println("!!!Change to "+line_colour[current_colour]+" line!!!");
                total_dis = total_dis + 2;
            }
            if(result.from != null){
                System.out.println("    From "+station_array.get(result.from.index).name+" To "+station_array.get(result.index).name+" on line "+line_colour[result.line.colour]+" with dis "+total_dis);
                resulting_q_add(result);
            }
            else{
                System.out.println("    Start from "+station_array.get(result.index).name);
            }
        }
        System.out.println("Total time = "+total_dis+"\nTotal line change = "+result.cumulative_distance);
        return total_dis;
    }
    private void Dijkstra_Algorithm_Walking(int source, int end){
        
        PriorityQueue<travel_node> node_queue = new PriorityQueue<travel_node>(new Node_Comparator());
        ArrayList<travel_node> result_stack = new ArrayList<travel_node>();
        travel_node[] record = new travel_node[num_actual_nodes];
        travel_node current, result = null;
        double dis;
        record[source] = new travel_node(source, 0, null, null);
        node_queue.offer(record[source]);
        do{
            current = node_queue.poll();
            if(current.from != null) searching_q_add(current);
            //System.out.println("Current "+station_array.get(current.index).name+" with dis "+current.cumulative_distance+" q.size "+node_queue.size());
            for(int i = 0; i<num_actual_nodes; i++){
                dis = walking_matrix[current.index][i];
                if(record[i] == null || record[i].cumulative_distance > (dis + current.cumulative_distance)){
                    //System.out.println("    To "+station_array.get(i).name+" with dis "+dis + current.cumulative_distance);
                    record[i] = new travel_node(i, current.cumulative_distance + dis, null, current);
                    node_queue.offer(record[i]);
                }
            }
        }while(!node_queue.isEmpty() && current.index != end);
        //System.out.println("Total_dis = "+record[end].cumulative_distance);
        if(record[end] == null){
            System.out.println("Destination unfound");
            return;
        }
        result = record[end];
        do{
            result_stack.add(0, result);
            result = result.from;
        }while(result != null);
        //System.out.println("stack size = "+result_stack.size());
        for(int i = 0; i < result_stack.size(); i++){
            result = result_stack.get(i);
            if(result.from != null){
                System.out.println("    From "+station_array.get(result.from.index).name+" To "+station_array.get(result.index).name+" with dis "+result.cumulative_distance);
                resulting_q_add(result);
            }
            else
                System.out.println("Start from "+station_array.get(result.index).name);
        }
        System.out.println("Total time = "+result.cumulative_distance);
    }
    private void searching_q_add(travel_node in){
        searching_q.add(in);
    }
    private void resulting_q_add(travel_node in){
        resulting_q.add(in);
    }
    public void searching_q_start(){
        // for all q_start
        // actually is starting a Timer, for each X ms of time => perform some visualisation with text massage on the GUI(window and graph_visualisation)
        // API for GUI, to start the path_finding searching animation, calling graph_visualisation
        Timer t = new Timer(200, e -> {
            travel_node current = null;
            if((current = searching_q.poll()) != null){
                visual.set_point_colour(current.from.index, current.index);
                if(curr_mode == 1){
                    ui.searching_add_text("<html>"+station_array.get(current.from.index).name+" -> "+station_array.get(current.index).name+
                    "<br>Total Time "+current.cumulative_distance+"m, <font color="+font_colour[current.line.colour]+">"+line_colour[current.line.colour]+" line</font></html>");
                    visual.repaint(); 
                }
                else if(curr_mode == 2){
                    ui.searching_add_text("<html>"+station_array.get(current.from.index).name+" -> "+station_array.get(current.index).name+
                    "<br>Total Change "+current.cumulative_distance+", <font color="+font_colour[current.line.colour]+">"+line_colour[current.line.colour]+" line</font></html>");
                    visual.repaint();
                }
                else{
                    ui.searching_add_text("<html>"+station_array.get(current.from.index).name+" -> "+station_array.get(current.index).name+
                    "<br>Total Walking Time "+current.cumulative_distance+"</html>");
                    visual.repaint();
                }
                //System.out.println("Searching = "+station_array.get(current.index).name+" with index "+current.index+" with dis "+current.cumulative_distance+" __ q.size = "+searching_q.size());
            }
            if(searching_q.isEmpty()){
                ui.set_result_path_enable();
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }
    public void resulting_q_start(){
        // API for GUI, to start the path_finding resulting animation, calling graph_visualisation
        resulting_state temp = new resulting_state();
        Timer t = new Timer(333, e -> {
            travel_node current = null;
            //System.err.println("Curr = "+curr_mode);
            if((curr_mode == 1 || curr_mode == 2)){
                if((current = resulting_q.poll()) != null){
                    visual.set_corrent_colour(current.from.index, current.index, current.line.colour);
                    visual.repaint();
                    //System.out.println("Resulting = "+station_array.get(current.index).name+" with index "+current.index+" with dis "+current.cumulative_distance+" __ q.size = "+resulting_q.size());
                    temp.total_distance = temp.total_distance + current.line.distance;
                    if(temp.current_colour == -1){
                        temp.current_colour = current.line.colour;
                        ui.resulting_add_text("!!!Start from "+line_colour[temp.current_colour]+" line!!!\n");
                        ui.resulting_add_text("\nStart from "+station_array.get(current.from.index).name+"\n");
                    }
                    else if(temp.current_colour != current.line.colour){
                        temp.current_colour = current.line.colour;
                        ui.resulting_add_text("!!!Change to "+line_colour[temp.current_colour]+" line!!!\n");
                        temp.total_change++;
                        temp.total_distance = temp.total_distance + 2;
                    }
                    ui.resulting_add_text("\nFrom "+station_array.get(current.from.index).name+" To "+station_array.get(current.index).name+"\nOn line "+line_colour[current.line.colour]+" with time "+temp.total_distance+"\n");
                }
            }
            else if(curr_mode == 3){
                if((current = resulting_q.poll()) != null){
                    visual.set_corrent_colour(current.from.index, current.index, 2);
                    visual.repaint();
                    //System.out.println("Resulting = "+station_array.get(current.index).name+" with index "+current.index+" with dis "+current.cumulative_distance+" __ q.size = "+resulting_q.size());
                    temp.total_distance = temp.total_distance + current.cumulative_distance;
                    if(temp.current_colour == -1){
                        temp.current_colour = -2;
                        ui.resulting_add_text("\nStart from "+station_array.get(current.index).name+"\n");
                    }
                    ui.resulting_add_text("\nFrom "+station_array.get(current.from.index).name+" To "+station_array.get(current.index).name+"\nWith walking time "+temp.total_distance+"\n");
                }
            }
            if(resulting_q.isEmpty()){
                if(curr_mode == 1 || curr_mode == 2)
                    ui.resulting_add_text("\nTotal Time = "+temp.total_distance+"\nTotal line change = "+temp.total_change);
                else if(curr_mode == 3)
                    ui.resulting_add_text("\nTotal Time = "+temp.total_distance);
                ui.set_path_finding_end_enable();
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }
    public void delay_q_start(){
        // API for GUI, to start the edge_printing animation, calling graph_visualisation
        Timer t = new Timer(333, e -> {
            travel_node current = null;
            if((current = searching_q.poll()) != null){
                visual.set_point_colour(current.from.index, current.index);
                visual.repaint();
            }
            if(searching_q.isEmpty()){
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }
    /*
     * initialise the matrix to size 4
     * num_nodes = 0
     */
    graph(){
        this.matrix_size = 2;
        this.num_actual_nodes = 0;
        this.num_used_row = 0;
        this.mapping = new HashMap<String, Integer>();
        this.station_array = new ArrayList<station>();
        this.matrix_enlarge();
        this.visual = null;
        this.searching_q = new ConcurrentLinkedQueue<travel_node>();
        this.resulting_q = new ConcurrentLinkedQueue<travel_node>();
        this.ui = null;
        this.curr_mode = -1;
    }
    public void set_visual(graph_visualisation in){
        this.visual = in;
    }
    public void set_window(window in){
        this.ui = in;
    }
}
