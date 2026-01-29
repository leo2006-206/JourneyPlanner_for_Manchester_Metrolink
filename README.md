# Example Video

[Watch demo video](Demo.mp4)

# Summer_Project

run the command in Java_code directory

compile java file:
javac -d ../Java_execute *.java

execute the program:
java -cp ../Java_execute Main

How to use the program:
!!! For all textbox input, must press ENTER after typing, to verify the input !!!

1. run the program.
2. click the graph_visualisation button to generate a good graph layout

3. path_finding: 
    1. click the PATH_FINDING button
    2. enter the source station name and press ENTER
    3. enter the end station name and press ENTER
    4. if both inputs are valid, the three mode buttons will enable
    5. click the path mode you want (Shortest, Fewest Colour change, Walking)
    6. click the Start_Processing button
    7. when the animation and text massage finished, click the Result button
    8. click the Result Path button
    9. when the animation and text massage finished, click the Back Home button
4. delay_update:
    1. click the UPDATE_DELAY button
    2. enter the source station name and press ENTER (onlu u to v where u and v are adjacency station)
    3. the textbox will show all the edges from source to other adjacency station
    4. enter the end station name and press ENTER
    5. enter the colour of the edge and press ENTER
    6. the delay textbox will unlock
    7. remove the word, enter the delay and press ENTER(New time = original time + delay)
    8. the Update Delay button will unlock, click to update change
    9. the textbox will show the original time and the new time
    10. click the Update successfully button to back Home
5. closure_update:
    1. click the UPDATE_CLOSURE button
    2. enter the target station name and press ENTER
    3. the label will show the Current State
    4. click the update button to change the closure state(if current == open, update to closed; if current == closed, update to open)
    5. the label will show the new Current State
    6. click the Back Home button
6. graph_visualisation:
    1. click the graph_visualisation button and watch the animation of generating new graph layout
