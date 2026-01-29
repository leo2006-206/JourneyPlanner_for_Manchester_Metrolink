import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.*;
import java.util.ArrayList;

//please take a look the ../Document.txt, included all variables and functions with purposes and dependencies
enum main_state{
    // main state of the GUI
    HOME(0),
    PATH_FINDING(1),
    UPDATE_DELAY(2),
    UPDATE_CLOSURE(3);
    private int index;
    main_state(int in){
        this.index = in;
    }
    public int get_index(){
        return index;
    }
}
enum sub_state{
    // the sub state inside the PATH_FINDINF state
    PATH_INPUT(0),
    PATH_PROCESSING(1),
    PATH_OUTPUT(2);

    private int index;
    sub_state(int in){
        this.index = in;
    }
    public int get_index(){
        return index;
    }
}
public class window{
    // to provide a GUI, include two main part: visualisation and menu
    // visualisation is the drawing of graph layout, allow to animate path_finding searching, resulting and the animation of graph transformation
    // all in the graph_visualisation class
    // menu is the part for GUI to interact with user, I divided to two parts: main state and sub state
    private graph main_graph;
    private graph_visualisation visualation;
    private JFrame body;
    private JPanel menu;
    private JPanel context[];
    private CardLayout menu_text;
    private CardLayout path_finding_stage;
    private main_state current;
    
    private sub_state sub_current;
    private ArrayList<JLabel> searching_label;
    private JTextArea resulting_label;
    private JButton result_path;
    private JButton path_finding_end;

    private JTextArea delay_update_label;

    // constant, additionally the size of the GUI(frame and all swing items) are proportional to the user screen
    private final int WINDOW_WIDTH;
    private final int WINDOW_HEIGHT;
    private final int MAX_STATES = 4;
    private final double WINDOW_TO_FONT_SIZE = 0.000015070409;

    // some placeholders as parameters enter to the graph.API_function
    private String source, end, colour;
    private double delay;
    private boolean closure;
    
    private void parameter_initialise(){
        // initialise placeholders
        source = null;
        end = null;
        colour = null;
        delay = 0;
        closure = false;
    }
    private int resize(int num, double fraction){
        return (int) (fraction * num);
    }
    private Font setFont(double fraction){
        // For all Swing_item functions, they just return a swing item with some default setup
        int size = Math.toIntExact(Math.round(WINDOW_WIDTH * WINDOW_HEIGHT * WINDOW_TO_FONT_SIZE * fraction * 0.8));
        return new Font("Sans-serif", Font.BOLD, size);
    }
    private JLabel setLabel(String text, double font_size, float alignment){
        // For all Swing_item functions, they just return a swing item with some default setup
        JLabel label = new JLabel(text);
        label.setFont(setFont(font_size));
        label.setAlignmentX(alignment);
        return label;
    }
    private JLabel setTitle(String text){
        // For all Swing_item functions, they just return a swing item with some default setup
        JLabel label = setLabel(text, 1, Component.CENTER_ALIGNMENT);
        label.setIcon(new ImageIcon("128px-Metrolink_Icon_2022.png"));
        label.setIconTextGap(0);
        return label;
    }
    private JTextField textbox(String text, Dimension size){
        // For all Swing_item functions, they just return a swing item with some default setup
        JTextField box = new JTextField();
        box.setToolTipText(text);
        box.setFont(setFont(1));
        // box.setBackground(null);
        // box.setForeground(null);
        box.setMargin(new Insets(3, 5, 3, 5));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.setMinimumSize(size);
        box.setPreferredSize(size);
        box.setMaximumSize(size);
        return box;
    }
    private JButton setButton(String text, Dimension size){
        // For all Swing_item functions, they just return a swing item with some default setup
        JButton but = new JButton();
        but.setText(text);
        but.setFont(setFont(1));
        but.setAlignmentX(Component.CENTER_ALIGNMENT);            
        but.setMinimumSize(size);
        but.setPreferredSize(size);
        but.setMaximumSize(size);
        return but;
    }
    private JPanel home_page(){
        // home_page allow user to select the functionality page they looking looking forward
        // include four button{path_finding, update_delay, update_closure, graph_visualisation(that generate a new graph layout)}
        JPanel home = new JPanel();
        JButton button[] = new JButton[4];
        Dimension but_size = new Dimension(resize(WINDOW_WIDTH, (0.2 * 0.8)), resize(WINDOW_HEIGHT, 0.1));

        home.setLayout(new BoxLayout(home, BoxLayout.Y_AXIS));

        home.add(setTitle("Journey Planner"));
        home.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));

        for(int i=0; i<4; i++){
            button[i] = new JButton();
            button[i].setFont(setFont(1));
            button[i].setAlignmentX(Component.CENTER_ALIGNMENT);            
            button[i].setMinimumSize(but_size);
            button[i].setPreferredSize(but_size);
            button[i].setMaximumSize(but_size);
            home.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.1)));
            home.add(button[i]);
        }

        button[0].setText(main_state.PATH_FINDING.toString());
        button[0].addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(current == main_state.HOME){
                    menu_text.show(menu, main_state.PATH_FINDING.toString());
                    current = main_state.PATH_FINDING;
                    parameter_initialise();
                    result_path.setEnabled(false);
                    path_finding_end.setEnabled(false);
                    for(int i=0; i<searching_label.size(); i++){
                        searching_label.get(i).setText(null);
                    }
                    resulting_label.setText(null);
                }
            }
        });

        button[1].setText(main_state.UPDATE_DELAY.toString());
        button[1].addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(current == main_state.HOME){
                    menu_text.show(menu, main_state.UPDATE_DELAY.toString());
                    current = main_state.UPDATE_DELAY;
                    parameter_initialise();
                }
            }
        });

        button[2].setText(main_state.UPDATE_CLOSURE.toString());
        button[2].addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(current == main_state.HOME){
                    menu_text.show(menu, main_state.UPDATE_CLOSURE.toString());
                    current = main_state.UPDATE_CLOSURE;
                    parameter_initialise();
                }
            }
        });

        button[3].setText("Graph_Visualation");
        button[3].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                visualation.Force_Directed_Spring_Embedder();
                //visualation.Force_Directed_FR();
                //visualation.repaint();
                visualation.animation_start();
                visualation.reset_colour();
            }
        });
        return home;
    }

    private JPanel path_finding_page(){
        // path_finding_page actually is a JPanel contains three pages from the sub state pages
        JPanel path_finding = new JPanel(new CardLayout());
        path_finding_stage = (CardLayout) path_finding.getLayout();
        JPanel stages[] = new JPanel[3];
        for(int i=0; i<3; i++){
            stages[i] = new JPanel();
            stages[i].setLayout(new BoxLayout(stages[i], BoxLayout.Y_AXIS));
            stages[i].add(setTitle("Path Finding"));
            stages[i].add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        }
        stages[0] = path_finding_input(stages[0], path_finding, path_finding_stage);
        stages[1] = path_finding_process(stages[1], path_finding, path_finding_stage);
        stages[2] = path_finding_output(stages[2], path_finding, path_finding_stage);
        
        for(sub_state i : sub_state.values()){
            path_finding.add(stages[i.get_index()], i.toString());
        }
        sub_current = sub_state.PATH_INPUT;
        path_finding_stage.show(path_finding, sub_state.PATH_INPUT.toString());
        return path_finding;
    }

    private JPanel path_finding_input(JPanel in, JPanel path_finding, CardLayout path_finding_stage){
        // path_finding_input allow user to enter input , select the path_finding mode
        // before all inputs are valid user is not allow to click the button for call a actual path_finding enquiry
        // with visualation that where is the valid input station on graph_visualisation
        JPanel page = in;
        Dimension textbox_size = new Dimension(resize(WINDOW_WIDTH, (0.2 * 0.8)), resize(WINDOW_HEIGHT, 0.05));
        Dimension but_size = new Dimension(resize(WINDOW_WIDTH, (0.2 * 0.8)), resize(WINDOW_HEIGHT, 0.1));

        JButton shortest = setButton("Shortest Path", but_size);
        JButton fewest = setButton("Fewest Change Path", but_size);
        JButton walking = setButton("Walking Path", but_size);

        page.add(setLabel("Enter the source", 1, Component.CENTER_ALIGNMENT));
        JTextField source_in = textbox("Enter the source", textbox_size);
        JLabel validation_source = setLabel("Waiting for input", 1, Component.CENTER_ALIGNMENT);
        validation_source.setHorizontalAlignment(SwingConstants.CENTER);
        source_in.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(main_graph.station_validation(source_in.getText())){
                    //node light
                    validation_source.setText("<html>Valid Station</html>");
                    source = source_in.getText();
                    if(source != null && end != null){
                        if(source.equals(end)){
                            validation_source.setText("<html>Please Try Again</html>");//<br> == \n
                            source = null;
                            shortest.setEnabled(false);
                            fewest.setEnabled(false);
                            walking.setEnabled(false);
                        }
                        else{
                            shortest.setEnabled(true);
                            fewest.setEnabled(true);
                            walking.setEnabled(true);  
                        }
                    }
                }
                else{
                    validation_source.setText("<html>Please Try Again</html>");//<br> == \n
                    source = null;
                    shortest.setEnabled(false);
                    fewest.setEnabled(false);
                    walking.setEnabled(false);
                }
            }
        });
        page.add(source_in);
        page.add(validation_source);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));

        page.add(setLabel("Enter the end", 1, Component.CENTER_ALIGNMENT));
        JTextField end_in = textbox("Enter the end", textbox_size);
        JLabel validation_end = setLabel("Waiting for input", 1, Component.CENTER_ALIGNMENT);
        validation_end.setHorizontalAlignment(SwingConstants.CENTER);
        end_in.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(main_graph.station_validation(end_in.getText())){
                    //node light
                    validation_end.setText("<html>Valid Station</html>");
                    end = end_in.getText();
                    if(source != null && end != null){
                        if(source.equals(end)){
                            validation_end.setText("<html>Please Try Again</html>");//<br> == \n
                            end = null;
                            shortest.setEnabled(false);
                            fewest.setEnabled(false);
                            walking.setEnabled(false);
                        }
                        else{
                            shortest.setEnabled(true);
                            fewest.setEnabled(true);
                            walking.setEnabled(true);  
                        }
                    }
                }
                else{
                    validation_end.setText("<html>Please Try Again</html>");//<br> == \n
                    end = null;
                    shortest.setEnabled(false);
                    fewest.setEnabled(false);
                    walking.setEnabled(false);
                }
            }
        });
        page.add(end_in);
        page.add(validation_end);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));

        shortest.setEnabled(false);
        shortest.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                main_graph.Dijkstra(source, end, 1);
                sub_current = sub_state.PATH_PROCESSING;
                path_finding_stage.show(path_finding, sub_state.PATH_PROCESSING.toString());
                source_in.setText(null);
                end_in.setText(null);
                validation_source.setText("Waiting for input");
                validation_end.setText("Waiting for input");
                shortest.setEnabled(false);
                fewest.setEnabled(false);
                walking.setEnabled(false);
            }
        });
        fewest.setEnabled(false);
        fewest.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                main_graph.Dijkstra(source, end, 2);
                sub_current = sub_state.PATH_PROCESSING;
                path_finding_stage.show(path_finding, sub_state.PATH_PROCESSING.toString());
                source_in.setText(null);
                end_in.setText(null);
                validation_source.setText("Waiting for input");
                validation_end.setText("Waiting for input");
                shortest.setEnabled(false);
                fewest.setEnabled(false);
                walking.setEnabled(false);
            }
        });
        walking.setEnabled(false);
        walking.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                main_graph.Dijkstra(source, end, 3);
                sub_current = sub_state.PATH_PROCESSING;
                path_finding_stage.show(path_finding, sub_state.PATH_PROCESSING.toString());
                source_in.setText(null);
                end_in.setText(null);
                validation_source.setText("Waiting for input");
                validation_end.setText("Waiting for input");
                shortest.setEnabled(false);
                fewest.setEnabled(false);
                walking.setEnabled(false);
            }
        });
        page.add(shortest);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        page.add(fewest);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        page.add(walking);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        return page;
    }

    private JPanel path_finding_process(JPanel in, JPanel path_finding, CardLayout path_finding_stage){
        // path_finding_process show how the program searching during the path_finding(it look slow, only becasue animation purpose)
        // after animation finished, user allow to click the result_path button and goto next page
        JPanel page = in;
        Dimension curr_label_size = new Dimension(resize(WINDOW_WIDTH, 0.2 * 0.98), resize(WINDOW_HEIGHT, 0.1));
        Dimension sub_label_size = new Dimension(resize(WINDOW_WIDTH, 0.2 * 0.98), resize(WINDOW_HEIGHT, 0.05));
        JButton start = setButton("Start_Processing", new Dimension(resize(WINDOW_WIDTH, 0.8 * 0.2), resize(WINDOW_HEIGHT, 0.05)));
        this.result_path = setButton("Result", new Dimension(resize(WINDOW_WIDTH, 0.8 * 0.2), resize(WINDOW_HEIGHT, 0.05)));
        JLabel current_searching = setLabel(null, 1, Component.CENTER_ALIGNMENT);
        
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                main_graph.searching_q_start();
            }
        });
        page.add(start);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));

        current_searching.setMinimumSize(curr_label_size);
        current_searching.setPreferredSize(curr_label_size);
        current_searching.setMaximumSize(curr_label_size);
        current_searching.setText(null);
        searching_label.add(searching_label.size(), current_searching);
        page.add(current_searching);
        for(int i=0; i<10; i++){
            JLabel label = setLabel(null, 0.7, Component.CENTER_ALIGNMENT);
            label.setMinimumSize(sub_label_size);
            label.setPreferredSize(sub_label_size);
            label.setMaximumSize(sub_label_size);
            //label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            label.setText(null);
            searching_label.add(searching_label.size(), label);
            page.add(label);
        }
        result_path.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                if(sub_current == sub_state.PATH_PROCESSING){
                    sub_current = sub_state.PATH_OUTPUT;
                    path_finding_stage.show(path_finding, sub_state.PATH_OUTPUT.toString());
                }
            }
        });
        result_path.setEnabled(false);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(result_path);
        return page;
    }
    
    private JPanel path_finding_output(JPanel in, JPanel path_finding, CardLayout path_finding_stage){
        // path_finding_output show the final result path with animation(it look slow, only becasue animation purpose)
        // after animation finished, user allow to click the path_finding_end button and back to home page
        JPanel page = in;
        Dimension but_size = new Dimension(resize(WINDOW_WIDTH, 0.8 * 0.2), resize(WINDOW_HEIGHT, 0.05));
        Dimension label_result = new Dimension(resize(WINDOW_WIDTH, 0.2 * 0.96), resize(WINDOW_HEIGHT, 0.6));
        JScrollPane body = new JScrollPane(resulting_label,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JButton start = setButton("Result Path", but_size);
        this.path_finding_end = setButton("Back Home", but_size);

        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                main_graph.resulting_q_start();
            }
        });
        page.add(start);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));

        resulting_label.setLineWrap(true);
        resulting_label.setWrapStyleWord(true);
        resulting_label.setEditable(false);
        resulting_label.setFont(setFont(0.6));

        body.setMinimumSize(label_result);
        body.setPreferredSize(label_result);
        body.setMaximumSize(label_result);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(body);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));

        path_finding_end.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(sub_current == sub_state.PATH_OUTPUT && current == main_state.PATH_FINDING){
                    sub_current = sub_state.PATH_INPUT;
                    current = main_state.HOME;
                    menu_text.show(menu, main_state.HOME.toString());
                    path_finding_stage.show(path_finding, sub_state.PATH_INPUT.toString());
                    visualation.reset_colour();
                    visualation.repaint();
                }
            }
        });
        page.add(path_finding_end);
        return page;
    }
    
    private JPanel update_delay_page(){
        // update_delay_page allow user to update time of one of the edge, with some animation and many input validations to aovid invalid input
        JPanel page = new JPanel();
        Dimension textbox_size = new Dimension(resize(WINDOW_WIDTH, (0.2 * 0.8)), resize(WINDOW_HEIGHT, 0.05));
        Dimension scroll_size = new Dimension(resize(WINDOW_WIDTH, 0.2 * 0.90), resize(WINDOW_HEIGHT, 0.4));
        JScrollPane scrollpane = new JScrollPane(delay_update_label, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        delay_update_label.setLineWrap(true);
        delay_update_label.setWrapStyleWord(true);
        delay_update_label.setEditable(false);
        delay_update_label.setFont(setFont(0.6));

        scrollpane.setMinimumSize(scroll_size);
        scrollpane.setPreferredSize(scroll_size);
        scrollpane.setMaximumSize(scroll_size);
        scrollpane.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextField source_in = textbox("Enter the source", textbox_size);
        JTextField end_in = textbox("Enter the end", textbox_size);
        JTextField colour_in = textbox("Enter the colour", textbox_size);
        JTextField delay_in = textbox("Enter the delay", textbox_size);
        JButton submit = setButton("Update Delay", new Dimension(resize(WINDOW_WIDTH, 0.8 * 0.2), resize(WINDOW_HEIGHT, 0.1)));
        delay_in.setEditable(false);
        delay_in.setText("New time = original time + delay");
        submit.setEnabled(false);

        source_in.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                delay_update_label.setText(null);
                if(main_graph.station_validation(source_in.getText())){
                    source = source_in.getText();
                    main_graph.edge_printing(source);
                }
                else{
                    source = null;
                    delay_in.setEditable(false);
                    submit.setEnabled(false);
                    delay_update_label.setText("Please Try Agains");
                }
            }
        });
        end_in.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                if(main_graph.station_validation(end_in.getText())){
                    end = end_in.getText();
                    if(main_graph.delay_update(source, end, colour, 0)){
                        delay_in.setEditable(true);
                    }
                    else{
                        delay_in.setEditable(false);
                        submit.setEnabled(false);
                    }
                }
                else{
                    end = null;
                    delay_in.setEditable(false);
                    submit.setEnabled(false);
                }
            }
        });
        colour_in.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                if(main_graph.delay_update(source, end, colour_in.getText(), 0)){
                    colour = colour_in.getText();
                    delay_in.setEditable(true);
                }
                else{
                    colour = null;
                    delay_in.setEditable(false);
                    submit.setEnabled(false);
                }
            }
        });
        delay_in.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                if(validation_double(delay_in.getText()) && Double.parseDouble(delay_in.getText()) > 0){
                    delay = Double.parseDouble(delay_in.getText());
                    submit.setEnabled(true);
                }
                else{
                    delay = 0;
                    submit.setEnabled(false);
                }
            }
        });
        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                if(submit.getText() == "Update Delay"){
                    String temp = delay_update_label.getText();
                    delay_update_label.setText("!!!Updated!!!\n");
                    if(main_graph.delay_update(source, end, colour, delay)){
                        submit.setText("Update successfully");
                        //main_graph.edge_printing(source);
                    }
                    else{
                        submit.setText("Update unsuccessfully");
                        delay_update_label.setText(temp);
                    }
                }
                else{
                    // reset page when back home
                    current = main_state.HOME;
                    menu_text.show(menu, main_state.HOME.toString());
                    source_in.setText(null);
                    end_in.setText(null);
                    colour_in.setText(null);
                    delay_in.setText(null);
                    delay_update_label.setText(null);
                    delay_in.setEditable(false);
                    delay_in.setText("New time = original time + delay");
                    submit.setEnabled(false);
                    submit.setText("Update Delay");
                    visualation.reset_colour();
                    visualation.repaint();
                }
            }
        });

        page.add(setTitle("Delay Update"));
        //page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(setLabel("Enter the source station", 1, Component.CENTER_ALIGNMENT));
        page.add(source_in);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(scrollpane);
        //page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(setLabel("Enter the end station", 1, Component.CENTER_ALIGNMENT));
        page.add(end_in);
        //page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(setLabel("Enter the line colour", 1, Component.CENTER_ALIGNMENT));
        page.add(colour_in);
        //page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(setLabel("Enter the delay", 1, Component.CENTER_ALIGNMENT));
        page.add(delay_in);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.02)));
        page.add(submit);
        
        return page;
    }
    
    private JPanel update_closure_page(){
        // update_closure_page allow user to update the closure of the state
        JPanel page = new JPanel();
        Dimension textbox_size = new Dimension(resize(WINDOW_WIDTH, (0.2 * 0.8)), resize(WINDOW_HEIGHT, 0.05));
        Dimension but_size = new Dimension(resize(WINDOW_WIDTH, 0.2 * 0.8), resize(WINDOW_HEIGHT, 0.1));
        
        JTextField target_in = textbox("Enter the target", textbox_size);
        JLabel validation_target = setLabel("Waiting for input", 1, Component.CENTER_ALIGNMENT);
        JLabel station_state = setLabel(" ", 1, Component.CENTER_ALIGNMENT);
        JButton update_closure = setButton("Update State", but_size);
        JButton back = setButton("Back Home", but_size);
        update_closure.setEnabled(false);

        validation_target.setHorizontalAlignment(SwingConstants.CENTER);
        target_in.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(main_graph.station_validation(target_in.getText())){
                    source = target_in.getText();
                    validation_target.setText("<html>"+target_in.getText()+" currrent state</html>");
                    update_closure.setEnabled(true);
                    if(main_graph.get_station_closure(source)){
                        station_state.setText("!!!Closed!!!");
                        update_closure.setText("Update to Open");
                        closure = false;
                    }
                    else{
                        station_state.setText("!!!Open!!!");
                        update_closure.setText("Update to Closure");
                        closure = true;
                    }
                }
                else{
                    update_closure.setEnabled(false);
                    validation_target.setText("Please Try Again");
                    update_closure.setText("Update State");
                    station_state.setText(null);
                    source = null;
                }
            }
        });
        update_closure.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                main_graph.closure_update(source, closure);
                if(main_graph.get_station_closure(source)){
                    station_state.setText("!!!Closed!!!");
                    update_closure.setText("Update to Open");
                    closure = false;
                }
                else{
                    station_state.setText("!!!Open!!!");
                    update_closure.setText("Update to Closure");
                    closure = true;
                }
            }
        });
        back.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                target_in.setText(null);
                update_closure.setEnabled(false);
                validation_target.setText("Waiting for input");
                station_state.setText("");
                update_closure.setText("Update State");
                current = main_state.HOME;
                menu_text.show(menu, current.toString());
                visualation.reset_colour();
            }
        });

        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(setTitle("Update Closure"));
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        page.add(setLabel("Enter the target", 1, Component.CENTER_ALIGNMENT));
        page.add(target_in);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        page.add(validation_target);
        page.add(station_state);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        page.add(update_closure);
        page.add(Box.createVerticalStrut(resize(WINDOW_HEIGHT, 0.05)));
        page.add(back);

        return page;
    }
    
    private boolean validation_double(String input){
        // verify String input is a valid double number
        try{
            Double.parseDouble(input);
            return true;
        }
        catch(NumberFormatException ex){
            return false;
        }
    }
    public void searching_add_text(String text){
        // API for graph, add text to searching_label during searching animation
        String next_text = searching_label.get(0).getText();
        String temp;
        searching_label.get(0).setText(text);
        for(int i=1; i<searching_label.size(); i++){
            temp = searching_label.get(i).getText();
            searching_label.get(i).setText(next_text);
            next_text = temp;
        }
    }
    public void resulting_add_text(String text){
        // API for graph, add text to resulting_label during resulting animation
        resulting_label.setText(resulting_label.getText() + text);
    }
    public void set_result_path_enable(){
        // API for graph, setEnable to true after searching animation finished
        result_path.setEnabled(true);
    }
    public void set_path_finding_end_enable(){
        // API for graph, setEnable to true after resulting animation finished
        path_finding_end.setEnabled(true);
    }
    public void delay_edges_printing_add_text(String text){
        // API for graph.edge_printing(String target), to print the text on GUI
        delay_update_label.setText(delay_update_label.getText() + text);
    }
    public window(graph in){
        this.main_graph = in;
        this.body = new JFrame("Journey Planner");
        this.menu = new JPanel(new CardLayout());
        this.menu_text = (CardLayout) menu.getLayout();
        this.context = new JPanel[MAX_STATES];
        this.searching_label = new ArrayList<JLabel>();
        this.resulting_label = new JTextArea();
        this.result_path = null;
        this.path_finding_end = null;
        this.delay_update_label = new JTextArea();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.WINDOW_WIDTH = (int) (0.9 * screen.width);
        this.WINDOW_HEIGHT = (int) (0.864 * screen.height);

        this.visualation = new graph_visualisation(main_graph, (int) (0.8 * WINDOW_WIDTH), WINDOW_HEIGHT);

        body.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        body.setLayout(null);
        body.setMinimumSize(new Dimension((int) (0.9 * screen.width), (int) (0.9 * screen.height)));
        body.setPreferredSize(new Dimension((int) (0.9 * screen.width), (int) (0.9 * screen.height)));
        body.setMaximumSize(new Dimension((int) (0.9 * screen.width), (int) (0.9 * screen.height)));
        body.setLocationRelativeTo(null);
        body.setResizable(true);

        visualation.setSize((int) (0.8 * WINDOW_WIDTH), WINDOW_HEIGHT);
        visualation.setLocation(0, 0);
        visualation.setBorder(BorderFactory.createMatteBorder(5,5,5,5,Color.GRAY));
        visualation.setBackground(null);
         

        menu.setSize((int) (0.2 * WINDOW_WIDTH), WINDOW_HEIGHT);
        menu.setLocation((int) (0.8 * WINDOW_WIDTH), 0);

        context[0] = home_page();

        context[1] = path_finding_page();
        //context[1].setBackground(Color.CYAN);

        context[2] = update_delay_page();
        //context[2].setBackground(Color.RED);

        context[3] = update_closure_page();
        //context[3].setBackground(Color.ORANGE);

        for(main_state i : main_state.values()){
            menu.add(context[i.get_index()], i.toString());
        }

        body.add(visualation, BorderLayout.CENTER);
        body.add(menu, BorderLayout.EAST);
        body.pack();
        menu_text.show(menu, "HOME");
        current = main_state.HOME;
        body.setVisible(true);
    }
    public graph_visualisation get_graph_visualisation(){
        return this.visualation;
    }
}