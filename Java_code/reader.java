import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//please take a look the ../Document.txt, included all variables and functions with purposes and dependencies
public class reader {
    private File file_Obj;
    private Scanner file_Reader;
    private graph body;
    private final String separate = ",";
    private final int MAX_COLOUR = 7;
    private final String line_colour[] = {"yellow", "purple", "green", "lightblue", "pink", "darkblue", "red"};

    public void stataion_reading(){
        /*
         * Skip the first line
         * Read the data line by line, separate the data into four part
         * Extra loop to map the colour to the colour index
         * 
        */
        String data = file_Reader.nextLine();
        int colour_index;
        while(file_Reader.hasNextLine()){
            colour_index = -1;
            data = file_Reader.nextLine();
            String[] arr = data.split(separate, -1);
            for(int i=0;i<MAX_COLOUR;i++){
                if(line_colour[i].equals(arr[2])){
                    colour_index = i;
                    break;
                }
            }
            if(colour_index == -1) System.out.println("Invalid colour line");
            else{
                //System.out.println("\nFrom _ "+arr[0]+"\nTo _ "+arr[1]+"\nColour_line "+arr[2]+" Index "+colour_index+"\nDistance "+arr[3]);
                body.add_edges(arr[0], arr[1], Double.parseDouble(arr[3]) , colour_index);
            } 
        }
    }
    public void walking_reading(){
        /*
         * read the first line, put the station name into array
         * for each line, outer_position = u, inner_position = v (where v = all other nodes)
         * add the distance from u to v into graph
         */
        String data = file_Reader.nextLine();
        String[] station_name = data.split(separate, -1);
        String[] arr;
        int outer_position = 1, inner_position;
        while(file_Reader.hasNext()){
            data = file_Reader.nextLine();
            arr = data.split(separate, -1);
            inner_position = 0;
            for(String j : arr){
                if(inner_position == 0){
                    inner_position++;
                    continue;
                }
                body.add_walking(station_name[outer_position], station_name[inner_position], Double.parseDouble(j));
                inner_position++;
            }
            //System.err.println("\n\n\n");
            outer_position++;
        }
    }
    public reader(String file_name, graph in_body){
        /*
         * Create the obj of File with input FILE_name, then put it into Scanner.
         * If the file not found, throw error massage. 
         */
        try {
            this.file_Obj = new File(file_name);
            this.file_Reader = new Scanner(file_Obj);
            this.body = in_body;
        } catch (FileNotFoundException e) {
            System.out.println("File undefined");
            e.printStackTrace();
        }
        
    }
}
