import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

//please take a look the ../Document.txt, included all variables and functions with purposes and dependencies
public class graph_visualisation extends JPanel{
    // this class focus on perform force_directed algorithm to generate good graph layout and animation with the graph operations
    private graph my_graph;
    // the width and height of visualisation part of window
    private final int MAX_WIDTH;
    private final int MAX_HEIGHT;
    // r of the node circle
    private final int radius;
    // thickness of the edge line
    private final int stroke;
    // line.distance = graph.edge.distance * dis_scalar
    private final double dis_scalar;
    // the user generated new graph layout or not
    private boolean draw = true;

    // the queue for the animation how the force_directed generate new layout
    private ConcurrentLinkedQueue<ArrayList<point>> animation_q;
    // the array for all nodes and edges
    private ArrayList<point> nodes;
    // the constant for draw line with colour and corrent position
    private final String line_colour[] = {"#EFBB00", "#7B2182", "#318C2C", "#5BC5F2", "#F088B6", "#0069B4","#E30613"};
    private final double line_offset[] = {- 2.5, -1.5, - 0.5, + 0.5, + 1.5, + 2.5, + 3.5};
    // the index of the Lastest user selected nodes
    private int curr_index;

    private class point{
        // in this class, the nodes are stored in a ArrayList with x and y coordinate(in logic space, the coordinate only meaningful in the force_directed computation)
        // since the graph contain multiple edges between the same node u and v
        // but the number of edges between u and v will hugely impact the resulf of force_directed algorithm
        // so, I only keep one edge if there are multiple edges between u and v into the compute_edges
        // then also keep a version with all edges for visualation
        // in short, compute_edges for force_directed algorithm and visual_edges for visualation
        String name;
        int index;
        boolean closure;
        double x;
        double y;
        Color colour;
        ArrayList<line> compute_edges;
        ArrayList<line> visual_edges;
        public point(String in_name, int in_index, int in_x, int in_y){
            this.name = in_name;
            this.index = in_index;
            this.closure = false;
            this.x = in_x;
            this.y = in_y;
            this.colour = Color.WHITE;
            compute_edges = new ArrayList<line>();
            visual_edges = new ArrayList<line>();
        }
        public point(String in_name, int in_index,boolean in_closure, double in_x, double in_y, Color in_colour, ArrayList<line> in_compute_edges, ArrayList<line> in_visual_edges){
            // to create a copy
            this.name = in_name;
            this.index = in_index;
            this.closure = in_closure;
            this.x = in_x;
            this.y = in_y;
            this.colour = in_colour;
            compute_edges = new ArrayList<line>();
            for(line l : in_compute_edges)
                compute_edges.add(new line(l.from, l.to, l.distance, l.colour));
            visual_edges = new ArrayList<line>();
            for(line l : in_visual_edges)
                visual_edges.add(new line(l.from, l.to, l.distance, l.colour));
        }
    }
    private class line{
        // to store the edge info, the from and to is the int index of the node
        int from;
        int to;
        double distance;
        int colour;
        public line(int in_from, int in_to, double in_dis, int in_colour){
            this.from = in_from;
            this.to = in_to;
            this.distance = in_dis;
            this.colour = in_colour;
        }
    }
    private double euclidean_dis(double x1, double y1, double x2, double y2){
        double x = x1 - x2, y = y1 - y2;
        if(Math.sqrt((x * x) + (y * y)) < 1E-3) return 1E-3;
        return Math.sqrt((x * x) + (y * y));
    }
    private double[] set_unit_vector(double x1, double y1, double x2, double y2){
        double x = x1 - x2, y = y1 - y2;
        double dis = euclidean_dis(x1, y1, x2, y2);
        double[] vector = new double[2];
        if(dis == 0){
            vector[0] = 0;
            vector[1] = 0;
        }
        else{
            vector[0] = x / dis;
            vector[1] = y / dis;
        }
        //System.out.println("    E_dis "+dis);
        return vector;
    }
    public void Force_Directed_Spring_Embedder(){
        // in short, in a big for loop, 
        // for(each node -> u to all other nodes -> v) compute repulsive_force that push u away from v
        // for(each edge of u -> e) compute the attractive_force that cancel the repulsive_force of e.to(v) to u, then push u close to v
        // update the new coordinate of u multiply with some cooling factor(act like temperature in the big bang, start super high then keep decreasing until the force is too low)
        // this is the standard Force_Directed_Spring_Embedder
        // final result should be the nodes will close the adjacent nodes, but away from the non-adjacent nodes, the distance between all(u -> v) look uniform(simply speaking is look good)
        // 
        // for my version, I add collusion detection, and center gravity
        // if not the nodes will always overlapped and the graph just turn to a ball
        // luckly, this one is working
        double distance, ideal_dis = 0.4 * Math.sqrt(MAX_WIDTH * MAX_HEIGHT), cooling_factor = MAX_WIDTH / 10, center_gravity = 0.02 * ideal_dis, repulsion_constant = 2, spring_constant = 3;
        double [] displacement_vector, unit_vector, repulsive_force, attractive_force, max_position = {0,0}, min_position = {10000,10000}, maximum_force = {10000, 10000};
        point v, u, update;
        ArrayList<point> next_nodes = new ArrayList<point>(nodes);
        draw = true;
        for(int i=0; i<100000 && (maximum_force[0] > 1E-10 || maximum_force[1] > 1E-10); i++){
            maximum_force = new double[2];
            for(int j=0; j<nodes.size(); j++){
                repulsive_force = new double[2];
                attractive_force = new double[2];
                displacement_vector = new double[2];
                for(int k=0; k<nodes.size(); k++){
                    if(j == k) continue;
                    v = nodes.get(k);
                    u = nodes.get(j);
                    distance = euclidean_dis(v.x, v.y, u.x, u.y);
                    unit_vector = set_unit_vector(v.x, v.y, u.x, u.y);
                    repulsive_force[0] = repulsive_force[0] - (unit_vector[0] * repulsion_constant/(distance * distance));
                    repulsive_force[1] = repulsive_force[1] - (unit_vector[1] * repulsion_constant/(distance * distance));
                    if(distance < 2 * radius){
                        double overlap = (radius * 2) - distance;
                        repulsive_force[0] = repulsive_force[0] - (overlap/distance * unit_vector[0]);
                        repulsive_force[1] = repulsive_force[1] - (overlap/distance * unit_vector[1]);
                    }
                }
                for(line k : nodes.get(j).compute_edges){
                    v = nodes.get(k.to);
                    u = nodes.get(j);
                    distance = euclidean_dis(v.x, v.y, u.x, u.y);
                    unit_vector = set_unit_vector(u.x, u.y, v.x, v.y);
                    attractive_force[0] = attractive_force[0] + (unit_vector[0] * spring_constant * Math.log(distance / ideal_dis * 0.5));
                    attractive_force[1] = attractive_force[1] + (unit_vector[1] * spring_constant * Math.log(distance / ideal_dis * 0.5));
                    unit_vector = set_unit_vector(v.x, v.y, u.x, u.y);
                    attractive_force[0] = attractive_force[0] + (unit_vector[0] * repulsion_constant/(distance * distance));
                    attractive_force[1] = attractive_force[1] + (unit_vector[1] * repulsion_constant/(distance * distance));
                }
                update = next_nodes.get(j);
                if(max_position[0] < update.x) max_position[0] = update.x;
                if(max_position[1] < update.y) max_position[1] = update.y;
                if(min_position[0] > update.x) min_position[0] = update.x;
                if(min_position[1] > update.y) min_position[1] = update.y;
                displacement_vector[0] = cooling_factor * (repulsive_force[0] + attractive_force[0] + (center_gravity * - update.x));
                displacement_vector[1] = cooling_factor * (repulsive_force[1] + attractive_force[1] + (center_gravity * - update.y));
                maximum_force[0] = Math.max(maximum_force[0], displacement_vector[0]);
                maximum_force[1] = Math.max(maximum_force[1], displacement_vector[1]);
                
                double len = Math.hypot(displacement_vector[0], displacement_vector[1]);
                if (len > 0){
                    double cap = Math.min(len, cooling_factor);
                    displacement_vector[0] = displacement_vector[0] / len * cap;
                    displacement_vector[1] = displacement_vector[1] / len * cap;
                }
                update.x += displacement_vector[0];
                update.y += displacement_vector[1];
            }
            cooling_factor *= 0.995;
            if(i % 15 == 0){
                // to store the position of all nodes at this moment into the animation_q
                ArrayList<point> temp = new ArrayList<point>();
                for(point j : nodes){
                    temp.add(new point(j.name, j.index, j.closure, j.x, j.y, j.colour, j.compute_edges, j.visual_edges));
                }
                animation_q.add(temp);
            }
            nodes.clear();
            nodes.addAll(next_nodes);
        }
    }
    public void Force_Directed_FR(){
        // variant of the Force_Directed_Spring_Embedder, simpler computation, but it not working, just turn the graph to a ball
        double distance, ideal_dis = 0.4 * Math.sqrt(MAX_WIDTH * MAX_HEIGHT), cooling_factor = MAX_WIDTH / 10, center_gravity = 0.02 * ideal_dis;
        double [] displacement_vector, unit_vector, repulsive_force, attractive_force, max_position = {0,0}, min_position = {10000,10000}, maximum_force = {10000, 10000};
        point v, u, update;
        ArrayList<point> next_nodes = new ArrayList<point>(nodes);
        draw = true;
        for(int i=0; i<10000 && (maximum_force[0] > 1E-10 || maximum_force[1] > 1E-10); i++){
            maximum_force = new double[2];
            for(int j=0; j<nodes.size(); j++){
                repulsive_force = new double[2];
                attractive_force = new double[2];
                displacement_vector = new double[2];
                for(int k=0; k<nodes.size(); k++){
                    if(j == k) continue;
                    v = nodes.get(k);
                    u = nodes.get(j);
                    distance = euclidean_dis(v.x, v.y, u.x, u.y);
                    unit_vector = set_unit_vector(v.x, v.y, u.x, u.y);
                    repulsive_force[0] = repulsive_force[0] - (unit_vector[0] * (ideal_dis * ideal_dis)/distance);
                    repulsive_force[1] = repulsive_force[1] - (unit_vector[1] * (ideal_dis * ideal_dis)/distance);
                    // if(distance < 2 * radius){
                    //     double overlap = (radius * 2) - distance;
                    //     repulsive_force[0] = repulsive_force[0] - (overlap/distance * unit_vector[0]);
                    //     repulsive_force[1] = repulsive_force[1] - (overlap/distance * unit_vector[1]);
                    // }
                }
                for(line k : nodes.get(j).compute_edges){
                    v = nodes.get(k.to);
                    u = nodes.get(j);
                    distance = euclidean_dis(v.x, v.y, u.x, u.y);
                    unit_vector = set_unit_vector(u.x, u.y, v.x, v.y);
                    attractive_force[0] = attractive_force[0] + (unit_vector[0] * ((distance * distance)/ideal_dis));
                    attractive_force[1] = attractive_force[1] + (unit_vector[1] * ((distance * distance)/ideal_dis));
                }
                update = next_nodes.get(j);
                if(max_position[0] < update.x) max_position[0] = update.x;
                if(max_position[1] < update.y) max_position[1] = update.y;
                if(min_position[0] > update.x) min_position[0] = update.x;
                if(min_position[1] > update.y) min_position[1] = update.y;
                displacement_vector[0] = cooling_factor * (repulsive_force[0] + attractive_force[0] + (center_gravity * - update.x));
                displacement_vector[1] = cooling_factor * (repulsive_force[1] + attractive_force[1] + (center_gravity * - update.y));
                maximum_force[0] = Math.max(maximum_force[0], displacement_vector[0]);
                maximum_force[1] = Math.max(maximum_force[1], displacement_vector[1]);
                
                double len = Math.hypot(displacement_vector[0], displacement_vector[1]);
                if (len > 0){
                    double cap = Math.min(len, cooling_factor);
                    displacement_vector[0] = displacement_vector[0] / len * cap;
                    displacement_vector[1] = displacement_vector[1] / len * cap;
                }
                update.x += displacement_vector[0];
                update.y += displacement_vector[1];
            }
            cooling_factor *= 0.999; 
            if(i % 15 == 0){
                ArrayList<point> temp = new ArrayList<point>();
                for(point j : nodes){
                    temp.add(new point(j.name, j.index, j.closure, j.x, j.y, j.colour, j.compute_edges, j.visual_edges));
                }
                animation_q.add(temp);
            }
            nodes.clear();
            nodes.addAll(next_nodes);
            System.out.println(i);
        }
    }
    public void set_point_colour(int source, int end){
        // change the colour of both source and end, meaning the nodes were searched in the searching stage in the graph.Dijkstra_Algorithms
        point u = nodes.get(source), v = nodes.get(end);
        //line l;
        u.colour = Color.LIGHT_GRAY;
        v.colour = Color.LIGHT_GRAY;
        curr_index = v.index;
    }
    public void set_corrent_colour(int source, int end, int colour){
        // change the colour of both source and end to the colour(line_colour[colour])
        // meaning the nodes were the corrent path in the resulting stage in the graph.Dijkstra_Algorithms
        // the colour is the actual used edge colour(corresponding to the line colour)
        point u = nodes.get(source), v = nodes.get(end);
        //line l;
        // u.colour = Color.GREEN;
        // v.colour = Color.GREEN;
        u.colour = Color.decode(line_colour[colour]);
        v.colour = Color.decode(line_colour[colour]);
        curr_index = v.index;
    }
    public void reset_colour(){
        // reset all nodes' colour back to initial stage;
        point current;
        for(int i=0; i<nodes.size(); i++){
            current = nodes.get(i);
            current.colour = Color.WHITE;
            // for(int j=0; j<current.edges.size(); j++){
            //     current.edges.get(j).colour = Color.BLACK;
            // }
        }
        curr_index = -1;
        repaint();
    }
    public void set_closure(int index, boolean in_closure){
        // set the closure of u to true, visual effect is put a cross on it
        nodes.get(index).closure = in_closure;
        curr_index = index;
    }
    public void set_current(int index){
        // visual effect is put a large circle to the current node
        curr_index = index;
    }
    public void animation_start(){
        // the animation for Force_Directed algorithm, to show where the new layout came from
        Timer t = new Timer(33, e -> {
            //System.out.println("Q size = "+animation_q.size());
            ArrayList<point> frame = animation_q.remove();
            if((frame) != null){
                nodes = frame;
                repaint();
            }
            if(animation_q.isEmpty()){
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }
    protected void paintComponent(Graphics g){
        // the draw everything to the GUI, the logical space x and y of each node will convert back to int proportional to the screen size
        // draw the line with different colour First, then the node, then the circle or cross, finally the name of the station
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int font_size = Math.toIntExact(Math.round(MAX_WIDTH * MAX_HEIGHT * 1.17876803E-5));
        Font font = new Font("Sans-serif", Font.BOLD, font_size);
        if(draw == true){
            double max_x = 0, max_y = 0, min_x = 1000, min_y = 1000;
            for(point i : nodes){
                max_x = Math.max(i.x, max_x);
                max_y = Math.max(i.y, max_y);
                min_x = Math.min(i.x, min_x);
                min_y = Math.min(i.y, min_y);
            }
            double[] logic_gap = {Math.abs(max_x) + Math.abs(min_x), Math.abs(max_y) + Math.abs(min_y)};
            double[] panel_gap = {MAX_WIDTH - (2 * radius), MAX_HEIGHT - (2 * radius)};
            // double ratio = Math.min(panel_gap[0] / logic_gap[0], panel_gap[1] / logic_gap[1]);
            // double[] offset = {radius + Math.abs(ratio * min_x) , radius + Math.abs(ratio * min_y)};
            double[] ratio = {panel_gap[0] / logic_gap[0], panel_gap[1] / logic_gap[1]};
            double[] offset = {radius + Math.abs(ratio[0] * min_x) , radius + Math.abs(ratio[1] * min_y)};
            for(point i : nodes){
                for(line j : i.visual_edges){
                    point v = nodes.get(j.to);
                    // int x1 = (int)(i.x * ratio + offset[0]), y1 = (int)(i.y * ratio + offset[1]);
                    // int x2 = (int)(v.x * ratio + offset[0]), y2 = (int)(v.y * ratio + offset[1]);
                    int x1 = (int)(i.x * ratio[0] + offset[0]), y1 = (int)(i.y * ratio[1] + offset[1]);
                    int x2 = (int)(v.x * ratio[0] + offset[0]), y2 = (int)(v.y * ratio[1] + offset[1]);
                    int colour_offset_y = (int) Math.round(stroke * line_offset[j.colour]);
                    int colour_offset_x = (int) Math.round(0.2 * (stroke * line_offset[j.colour]));
                    g2.setStroke(new BasicStroke(stroke));
                    g2.setColor(Color.decode(line_colour[j.colour]));
                    g2.drawLine(x1 + colour_offset_x, y1 + colour_offset_y, x2 + colour_offset_x, y2 + colour_offset_y);
                }
            }
            for(point i : nodes){
                // int x = (int) Math.round(i.x * ratio + offset[0]);
                // int y = (int) Math.round(i.y * ratio + offset[1]);
                int x = (int) Math.round(i.x * ratio[0] + offset[0]);
                int y = (int) Math.round(i.y * ratio[1] + offset[1]);
                g2.setColor(i.colour);
                g2.fillOval(x - radius, y - radius, radius*2, radius*2);
                g2.setStroke(new BasicStroke(stroke - 1));
                g2.setColor(Color.BLACK);
                g2.drawOval(x - radius, y - radius, radius*2, radius*2);
                if(i.closure == true){
                    //x = x - radius;
                    //y = y - radius;
                    int r = radius * 2;
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(stroke + 1));
                    g2.drawLine(x - r, y + r, x + r, y - r);
                    g2.drawLine(x - r, y - r, x + r, y + r);
                }
            }
            if(curr_index != -1){
                point i = nodes.get(curr_index);
                int x = (int) Math.round(i.x * ratio[0] + offset[0]);
                int y = (int) Math.round(i.y * ratio[1] + offset[1]);
                g2.setStroke(new BasicStroke(stroke + 2));
                g2.setColor(Color.ORANGE);
                g2.drawOval(x - (radius * 2), y - (radius * 2), radius* 4, radius* 4);
            }
            for(point i : nodes){
                int name_x = (int) Math.round((i.x * ratio[0] + offset[0]) - (radius * 1));
                int name_y = (int) Math.round((i.y * ratio[1] + offset[1]) - (radius * 1.5));
                if(name_y <= Math.round(MAX_HEIGHT * 0.02))
                    name_y = name_y + (int)Math.round(radius * 3.5);
                
                g2.setFont(font);
                g2.setColor(Color.BLACK);
                g2.drawString(i.name, name_x+1, name_y+1);
                g2.drawString(i.name, name_x-1, name_y+1);
                g2.drawString(i.name, name_x+1, name_y-1);
                g2.drawString(i.name, name_x-1, name_y-1);
                g2.setColor(Color.WHITE);
                g2.drawString(i.name, name_x, name_y);
            }
        }
        else{
            g2.setStroke(new BasicStroke(stroke));
            for(point i : nodes){
                g2.setColor(i.colour);
                g2.fillOval((int)i.x - radius, (int)i.y - radius, radius*2, radius*2);
                g2.setColor(Color.BLACK);
                g2.drawOval((int)i.x - radius, (int)i.y - radius, radius*2, radius*2);
                for(line j : i.visual_edges){
                    point v = nodes.get(j.to);
                    g2.setColor(Color.BLACK);
                    g2.drawLine((int)i.x, (int)i.y, (int)v.x, (int)v.y);
                }
            }
        }
        
    }
    public graph_visualisation(graph in_graph, int width, int height){
        this.my_graph = in_graph;
        this.MAX_WIDTH = width;
        this.MAX_HEIGHT = height;
        this.radius = Math.toIntExact(Math.round(MAX_WIDTH * MAX_HEIGHT * 0.00001227883365)); // 10/(829 * 1228) = 0.00000982306691, 15/(829 * 1228) = 0.00001473460038, 12.5/(829 * 1228) = 0.00001227883365
        this.stroke = Math.toIntExact(Math.round(MAX_WIDTH * MAX_HEIGHT * 0.000003929226767)); // 2/(829 * 1228) = 0.00000196461338, 3/(829 * 1228) = 0.000002946920076, 4/(829 * 1228) = 0.000003929226767
        this.dis_scalar = Math.toIntExact(Math.round(MAX_WIDTH * MAX_HEIGHT * 0.00009823066919 * 1));
        this.nodes = new ArrayList<point>();
        this.curr_index = -1;
        this.animation_q = new ConcurrentLinkedQueue<ArrayList<point>>();
        //System.out.println(MAX_HEIGHT * MAX_WIDTH);
        ArrayList<graph.station> stations = my_graph.get_stationArray();
        HashMap<String, Integer> map = my_graph.get_mapping();
        ArrayList<graph.edge>[][] matrix = my_graph.get_matrix();
        for(graph.station i : stations){
            int x = (int)(Math.random() * MAX_WIDTH);
            int y = (int)(Math.random() * MAX_HEIGHT);
            while(x <= radius || x >= (MAX_WIDTH-radius)){
                x = (int)(Math.random() * MAX_WIDTH);
            }
            while(y <= radius || y >= (MAX_HEIGHT-radius)){
                y = (int)(Math.random() * MAX_HEIGHT);
            }
            nodes.add(new point(i.name, map.get(i.name), x, y));
        }
        for(int i=0; i<my_graph.get_matrix_size(); i++){
            for(int j=0; j<my_graph.get_matrix_size(); j++){
                if(matrix[i][j] == null) continue;
                graph.edge temp = null;
                for(graph.edge k : matrix[i][j]){
                    if(temp == null || k.distance > temp.distance)
                        temp = k;
                }
                nodes.get(i).compute_edges.add(new line(i, j, temp.distance * dis_scalar, temp.colour));
                for(graph.edge k : matrix[i][j]){
                    nodes.get(i).visual_edges.add(new line(i, j, k.distance * dis_scalar, k.colour));
                }
            }
        }
    }
}
