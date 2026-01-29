import java.util.Scanner;

//please take a look the ../Document.txt, included all variables and functions with purposes and dependencies
public class Main{
    // to initialise and setup all the classes, also provide a CUI version of the program
    private reader my_Reader;
    private graph my_Graph;
    private window my_Window;
    private Scanner my_Scanner;
    private final String line_colour[] = {"yellow", "purple", "green", "lightblue", "pink", "darkblue", "red"};
    private final int MAX_COLOUR = 7;
    public static void main(String[] args){
        Main now = new Main();
        now.running();
    }
    private void running(){
        // the menu of the program
        int op;
        String temp;
        while(true){
            System.out.println("\nEnter 1 for path_finding, 2 for update delay, 3 for update closure, 4 for station printing, 0 for terminate program");
            temp = my_Scanner.nextLine();
            if(validation_int(temp, 0, 4))
                op = Integer.parseInt(temp);
            else{
                System.out.println("Error: invalided input");
                continue;
            }
            if(op == 1) path_finding();
            else if(op == 2) update_delay();
            else if(op == 3) update_closure();
            else if(op == 4) my_Graph.matrix_printing();
            else if(op == 0) break;
        }
        my_Scanner.close();
    }
    private void path_finding(){
        // for user to enter inputs to perform path_finding operations on graph 
        //my_Graph.matrix_printing();
        String source, end, temp;
        int mode;
        System.out.println("Enter the source");
        source = my_Scanner.nextLine();
        System.out.println("Enter the end");
        end = my_Scanner.nextLine();
        System.out.println("Enter 1 for shortest path, 2 for fewest change path, 3 for walking path");
        temp = my_Scanner.nextLine();
        if(validation_int(temp, 1, 4)){
            mode = Integer.parseInt(temp);
        }
        else{
            System.out.println("Error: invalided input");
            return;
        }
        if(!my_Graph.Dijkstra(source, end, mode))
            System.err.println("Invalid enquiry");
        return;
    }
    private void update_delay(){
        // for user to update delay
        String source, end, colour, temp;
        double delay;
        System.out.println("Enter the source");
        source = my_Scanner.nextLine();
        if(!my_Graph.edge_printing(source)) return;
        System.out.println("Enter the end");
        end = my_Scanner.nextLine();
        System.out.println("Enter the colour");
        colour = my_Scanner.nextLine();
        System.out.println("Enter the delay, new time = original time + delay");
        temp = my_Scanner.nextLine();
        if(validation_double(temp) && Double.parseDouble(temp) > 0){
            delay = Double.parseDouble(temp);
        }
        else{
            System.out.println("Error: invalided input");
            return;
        }
        if(my_Graph.delay_update(source, end, colour, delay)){
            System.out.println("Update successfully");
            my_Graph.edge_printing(source);
        }
        else{
            System.out.println("Update unsuccessfully");
            my_Graph.edge_printing(source);
        }
    }
    private void update_closure(){
        // for user to update closure
        String target, temp;
        boolean next_state;
        System.out.println("Enter the target station name");
        target = my_Scanner.nextLine();
        System.out.println("Enter the new state of station, 1 for closed, 0 for for open");
        temp = my_Scanner.nextLine();
        if(validation_int(temp, 0, 1)){
            if(Integer.parseInt(temp) == 1) next_state = true;
            else next_state = false;
        }
        else{
            System.out.println("Error: invalided input");
            return;
        }
        if(my_Graph.closure_update(target, next_state)){
            System.out.println("Update successfully");
        }
        else{
            System.out.println("Update unsuccessfully");
        }
    }
    private boolean validation_int(String input, int from, int to){
        // ensure the user input is valid
        try{
            int test = Integer.parseInt(input);
            if(test >= from && test <= to) return true;
            else return false;
        }
        catch(NumberFormatException ex){
            return false;
        }
    }
    private boolean validation_double(String input){
        // ensure the user input is valid
        try{
            Double.parseDouble(input);
            return true;
        }
        catch(NumberFormatException ex){
            return false;
        }
    }
    Main(){
        // setup everything
        this.my_Graph = new graph();
        this.my_Scanner = new Scanner(System.in);
        this.my_Reader = new reader("../Metrolink_times_linecolour.csv", my_Graph);
        my_Reader.stataion_reading();
        this.my_Reader = new reader("../walktimes.csv", my_Graph);
        my_Reader.walking_reading();
        this.my_Window = new window(my_Graph);
        my_Graph.set_visual(my_Window.get_graph_visualisation());
        my_Graph.set_window(my_Window);
    }
}