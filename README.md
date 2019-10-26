# Robot Maze Navigation and Mapping

## Setup of the Course:

* **The Maze:**
  
  * Walls that make up the maze are ~30cm high.
  
  * The width of the path is between ~30cm.
  * Doorways are at least 29cm wide.
  
* **The Floor:**

  * The floor can be viewed as a set of 30 x 30cm coloured tiles.
  * Given this, the maze will be 6 tiles long by 9 tiles wide.
  * In between the tiles, there will either be a wall (~10cm thickness) or a ~10cm space to travel across. 
  * The robot can travel over white tiles.
  * Green tiles in the maze represent "*no go*" zones that your robot should not cross. Green tiles will be placed randomly throughout the maze.

* **The Path:**
  
  * The starting tile, the home tile, will be the bottom leftmost tile.
  * The robot should start and finish their run at this home tile.
  * The robot will encounter an end tile. This will be red. When this is found, the robot must travel the shortest path back to the home tile.
  
## What the Robot does:

The Robot can travel through the Maze, mapping each cell it traverses through. When it reaches a green tile or there is no unexplored tiles next to it, it successfully traverses back through the maze, until it finds an unexplored neighboring tile to continue it's adventure. When the robot finally reaches the red tile, it first thinks if it knows the fastest path back. If it knows the fastest path back, it will calculate it using the A* Search Algorithm and start going back. If it does not know the fastest path back, it will explore the path of the Maze where the fastest path might be. Then go back to the red tile and then do the A* Search and traverse back to the home tile.

## Robot Setup:

The robot used in this project was from Lego Mindstorms EV3.

The program was written in Java and the lejos library was used to program the robot.

The robot used with this code, had the Infrared sensor looking at the front, the Ultrasonic sensor looking at the back, the Colour sensor looking at the ground and the Gyro sensor on the main body. Two big Motors where used for the movement.