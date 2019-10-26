import java.awt.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.util.PriorityQueue;
import java.util.Queue;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class BackHome
{
	static boolean							explored	= false;

	static Comparator<ArrayDeque<Integer>>	comparator	= new Comparator<ArrayDeque<Integer>>()
														{

															@Override
															public int compare(ArrayDeque<Integer> o1, ArrayDeque<Integer> o2)
															{
																int i = MainClass.position_i;
																int j = MainClass.position_j;
																int temp;

																ArrayDeque<Integer> tempo1 = new ArrayDeque<>(o1);
																ArrayDeque<Integer> tempo2 = new ArrayDeque<>(o2);

																double cost1 = tempo1.size() * 10;
																while (!tempo1.isEmpty())
																{
																	temp = tempo1.removeFirst();

																	if (temp == MainClass.NORTH)
																	{
																		i += 2;
																	}
																	else if (temp == MainClass.EAST)
																	{
																		j += 2;
																	}
																	else if (temp == MainClass.SOUTH)
																	{
																		i -= 2;
																	}
																	else if (temp == MainClass.WEST)
																	{
																		j -= 2;
																	}

																	cost1 += manhattanDistance(i, j, MainClass.home);
																}

																i = MainClass.position_i;
																j = MainClass.position_j;

																double cost2 = tempo2.size() * 10;
																while (!tempo2.isEmpty())
																{
																	temp = tempo2.removeFirst();

																	if (temp == MainClass.NORTH)
																	{
																		i += 2;
																	}
																	else if (temp == MainClass.EAST)
																	{
																		j += 2;
																	}
																	else if (temp == MainClass.SOUTH)
																	{
																		i -= 2;
																	}
																	else if (temp == MainClass.WEST)
																	{
																		j -= 2;
																	}

																	cost2 += manhattanDistance(i, j, MainClass.home);
																}

																return (int) (cost1 - cost2);
															}

														};

	public static void action()
	{

		int max_i = 11;
		int max_j = MainClass.position_j + 1;
		boolean end = true;

		for (int i = 12; i >= 0 && end; i--)
		{
			for (int j = 0; j < MainClass.map[0].length - 1; j++)
			{
				if (MainClass.map[i][j] == 9)
				{
					max_i = i + 1;
					end = false;
					break;
				}
			}
		}

		for (int j = 0; j < MainClass.map[0].length - 1; j++)
		{
			if (MainClass.map[max_i][j] == 0)
			{
				MainClass.map[max_i][j] = 7;
			}
		}

		for (int i = 0; i < MainClass.map.length - 1; i++)
		{
			if (MainClass.map[i][max_j] == 0)
			{
				MainClass.map[i][max_j] = 7;
			}
		}

		while (!explored)
		{
			end = true;
			for (int i = 0; i <= max_i && end; i++)
			{
				for (int j = 0; j <= max_j; j++)
				{
					//					if ((i % 2 == 1) && (j % 2 == 1) && (MainClass.map[i][j] == 0))
					//					{
					//						explored = false;
					//						end = false;
					//						break;
					//					}
					//					else
					//					{
					//						explored = true;
					//					}
					if (MainClass.map[i][j] == 9)
					{
						if ((MainClass.map[MainClass.position_i + 1][MainClass.position_j] != 1)
								&& (MainClass.map[MainClass.position_i + 2][MainClass.position_j] == 0)) // North is empty
						{
							explored = false;
							end = false;
							break;
						}
						else if ((MainClass.map[MainClass.position_i][MainClass.position_j + 1] != 1)
								&& (MainClass.map[MainClass.position_i][MainClass.position_j + 2] == 0)) // East is empty
						{
							explored = false;
							end = false;
							break;
						}
						else if ((MainClass.map[MainClass.position_i - 1][MainClass.position_j] != 1)
								&& (MainClass.map[MainClass.position_i - 2][MainClass.position_j] == 0)) // South is empty
						{
							explored = false;
							end = false;
							break;
						}
						else if ((MainClass.map[MainClass.position_i][MainClass.position_j - 1] != 1)
								&& (MainClass.map[MainClass.position_i][MainClass.position_j - 2] == 0)) // West is empty
						{
							explored = false;
							end = false;
							break;
						}
						else
						{
							explored = true;
						}
					}

				}
			}

			if (!explored)
			{
				MovingAround.action();
			}
		}

		Button.LEDPattern(3);

		goTo(MainClass.GOAL_I, MainClass.GOAL_J);

		MainClass.home = true;

		goTo(MainClass.HOME_POSITION_I, MainClass.HOME_POSITION_J);

		Delay.msDelay(2000000000);
		//Thread.yield();
	}

	private static void goTo(int x, int y)
	{
		boolean found = false;

		PriorityQueue<ArrayDeque<Integer>> agenda = new PriorityQueue<ArrayDeque<Integer>>(1, comparator);

		int i = MainClass.position_i;
		int j = MainClass.position_j;

		ArrayDeque<Integer> directions = new ArrayDeque<Integer>();

		while (!found)
		{

			if ((i == x) && (j == y))
			{
				found = true;
				Sound.beep();
			}
			else
			{
				if ((MainClass.map[i + 1][j] != 1) && ((MainClass.map[i + 2][j] == 9) || (MainClass.map[i + 2][j] == 3))) // North
				{
					directions.addLast(MainClass.NORTH);
					agenda.add(new ArrayDeque<>(directions));
					directions.removeLast();
				}
				if ((MainClass.map[i][j + 1] != 1) && ((MainClass.map[i][j + 2] == 9) || (MainClass.map[i][j + 2] == 3))) // East
				{
					directions.addLast(MainClass.EAST);
					agenda.add(new ArrayDeque<>(directions));
					directions.removeLast();
				}
				if ((MainClass.map[i - 1][j] != 1) && ((MainClass.map[i - 2][j] == 9) || (MainClass.map[i - 2][j] == 3))) // South
				{
					directions.addLast(MainClass.SOUTH);
					agenda.add(new ArrayDeque<>(directions));
					directions.removeLast();
				}
				if ((MainClass.map[i][j - 1] != 1) && ((MainClass.map[i][j - 2] == 9) || (MainClass.map[i][j - 2] == 3))) // West
				{
					directions.addLast(MainClass.WEST);
					agenda.add(new ArrayDeque<>(directions));
					directions.removeLast();
				}

				directions = new ArrayDeque<Integer>(agenda.poll());

				i = MainClass.position_i;
				j = MainClass.position_j;

				for (int num : directions)
				{
					if (num == MainClass.NORTH)
					{
						i += 2;
					}
					else if (num == MainClass.EAST)
					{
						j += 2;
					}
					else if (num == MainClass.SOUTH)
					{
						i -= 2;
					}
					else if (num == MainClass.WEST)
					{
						j -= 2;
					}
				}
			}
		}

		for (int num : directions)
		{
			MovingAround.changeDirectionNum(num);
			moveBlock();
			moveBlock();
		}
	}

	private static void moveBlock()
	{
		MainClass.pilot.setLinearSpeed(150);
		MainClass.pilot.forward();
		Delay.msDelay(MovingAround.MOVE_BLOCK_DELAY);
		MainClass.pilot.stop();
		
		if (MainClass.map[MainClass.position_i][MainClass.position_j] == 0)
			MainClass.map[MainClass.position_i][MainClass.position_j] = 9;

		if (MainClass.directionNum == MainClass.NORTH)
		{
			++MainClass.position_i;
		}
		else if (MainClass.directionNum == MainClass.EAST)
		{
			++MainClass.position_j;
		}
		else if (MainClass.directionNum == MainClass.SOUTH)
		{
			--MainClass.position_i;
		}
		else if (MainClass.directionNum == MainClass.WEST)
		{
			--MainClass.position_j;
		}
	}

	static double manhattanDistance(int i, int j, boolean k)
	{
		if (k)
			return Math.sqrt((i - MainClass.HOME_POSITION_I) * (i - MainClass.HOME_POSITION_I)
					+ (j - MainClass.HOME_POSITION_J) * (j - MainClass.HOME_POSITION_J));
		else
			return Math.sqrt((i - MainClass.GOAL_I) * (i - MainClass.GOAL_I) + (j - MainClass.GOAL_J) * (j - MainClass.GOAL_J));
	}
}