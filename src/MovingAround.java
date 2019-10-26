import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import java.util.PriorityQueue;

public class MovingAround
{

	protected static final int	MOVE_BLOCK_DELAY	= 1320;

	private static final int	GYRO_ROTATION_R		= 86;

	private static final int	GYRO_ROTATION_L		= 83;

	static float[]				sampleIR			= new float[MainClass.ir.sampleSize()];
	static float[]				sampleUS			= new float[MainClass.us.sampleSize()];

	static float				previousSampleG		= 0;
	static boolean				once				= true;

	public static void action()
	{
		MainClass.colorR.fetchSample(MainClass.sampleR, 0);

		if ((MainClass.sampleR[0] == MainClass.GREEN) || (MainClass.trapped))
		{

			if (MainClass.sampleR[0] == MainClass.GREEN)
				MainClass.map[MainClass.position_i][MainClass.position_j] = MainClass.GREEN_BLOCK;
			else
				MainClass.map[MainClass.position_i][MainClass.position_j] = 9;

			Button.LEDPattern(2);

			GoingBack.action();
		}
		else if ((MainClass.sampleR[0] == MainClass.RED) && once)
		{
			MainClass.map[MainClass.position_i][MainClass.position_j] = MainClass.GOAL;
			MainClass.GOAL_I = MainClass.position_i;
			MainClass.GOAL_J = MainClass.position_j;
			Button.LEDPattern(3);
			once = false;

			BackHome.action();
		}
		else
		{
			Button.LEDPattern(1);

			if (MainClass.map[MainClass.position_i][MainClass.position_j] != 9)
			{
				double_Scan();
			}

			if (!BackHome.explored)
				checkDirection();
		}
	}

	protected static void double_Scan()
	{
		Delay.msDelay(100);

		MainClass.ir.fetchSample(sampleIR, 0);

		//usFetchSample();
		sampleUS[0] = -1;
		updateMap();

		Delay.msDelay(100);

		incrementDirectionNum();

		Delay.msDelay(100);

		MainClass.ir.fetchSample(sampleIR, 0);

		usFetchSample();

	}

	private static void single_Scan()
	{
		if ((MainClass.map[MainClass.position_i][MainClass.position_j] != 9)
				&& ((MainClass.position_i % 2 != 1) || (MainClass.position_j % 2 != 1)))
		{

			Delay.msDelay(100);

			incrementDirectionNum();

			Delay.msDelay(100);

			MainClass.ir.fetchSample(sampleIR, 0);

			usFetchSample();

			decrementDirectionNum();

			moveBlock();
		}
	}

	private static void usFetchSample()
	{
		for (int i = 0; i < 20; i++)
		{
			MainClass.us.fetchSample(sampleUS, 0);
			updateMap();
			Delay.msDelay(10);
		}
	}

	protected static void checkDirection()
	{
		if ((MainClass.map[MainClass.position_i + 1][MainClass.position_j] != 1)
				&& (MainClass.map[MainClass.position_i + 2][MainClass.position_j] == 0)) // North is empty
		{
			changeDirectionNum(MainClass.NORTH);
			MainClass.pathBack.push(MainClass.NORTH);
			moveBlock();

			single_Scan();
		}
		else if ((MainClass.map[MainClass.position_i][MainClass.position_j + 1] != 1)
				&& (MainClass.map[MainClass.position_i][MainClass.position_j + 2] == 0)) // East is empty
		{
			changeDirectionNum(MainClass.EAST);
			MainClass.pathBack.push(MainClass.EAST);
			moveBlock();

			single_Scan();
		}
		else if ((MainClass.map[MainClass.position_i - 1][MainClass.position_j] != 1)
				&& (MainClass.map[MainClass.position_i - 2][MainClass.position_j] == 0)) // South is empty
		{
			changeDirectionNum(MainClass.SOUTH);
			MainClass.pathBack.push(MainClass.SOUTH);
			moveBlock();

			single_Scan();
		}
		else if ((MainClass.map[MainClass.position_i][MainClass.position_j - 1] != 1)
				&& (MainClass.map[MainClass.position_i][MainClass.position_j - 2] == 0)) // West is empty
		{
			changeDirectionNum(MainClass.WEST);
			MainClass.pathBack.push(MainClass.WEST);
			moveBlock();

			single_Scan();
		}
		else
		{
			MainClass.trapped = true;
		}
	}

	protected static void moveBlock()
	{
		MainClass.pilot.setLinearSpeed(150);
		MainClass.pilot.forward();
		Delay.msDelay(MOVE_BLOCK_DELAY);
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

	protected static void changeDirectionNum(int num)
	{

		int difference = num - MainClass.directionNum;

		if (difference == 3) //Turn Left once
		{
			turnLeft(-1);
		}
		else if (difference == -3) //Turn Right once
		{
			turnRight(1);
		}
		else if (difference > 0) // Turning Right
		{
			turnRight(difference);
		}
		else if (difference < 0) //Turning Left
		{
			turnLeft(difference);
		}

		MainClass.directionNum = num;
	}

	private static void updateMap()
	{

		if ((sampleIR[0] < 18) && (sampleIR[0] != -1))
		{

			if (MainClass.directionNum == MainClass.NORTH)
				MainClass.map[MainClass.position_i + 1][MainClass.position_j] = MainClass.WALL;
			else if (MainClass.directionNum == MainClass.EAST)
				MainClass.map[MainClass.position_i][MainClass.position_j + 1] = MainClass.WALL;
			else if (MainClass.directionNum == MainClass.SOUTH)
				MainClass.map[MainClass.position_i - 1][MainClass.position_j] = MainClass.WALL;
			else if (MainClass.directionNum == MainClass.WEST)
				MainClass.map[MainClass.position_i][MainClass.position_j - 1] = MainClass.WALL;
		}

		if ((sampleUS[0] < 0.40) && (sampleUS[0] != -1))
		{
			if (MainClass.directionNum == MainClass.NORTH)
				MainClass.map[MainClass.position_i - 1][MainClass.position_j] = MainClass.WALL;
			else if (MainClass.directionNum == MainClass.EAST)
				MainClass.map[MainClass.position_i][MainClass.position_j - 1] = MainClass.WALL;
			else if (MainClass.directionNum == MainClass.SOUTH)
				MainClass.map[MainClass.position_i + 1][MainClass.position_j] = MainClass.WALL;
			else if (MainClass.directionNum == MainClass.WEST)
				MainClass.map[MainClass.position_i][MainClass.position_j + 1] = MainClass.WALL;
		}

	}

	private static void incrementDirectionNum()
	{
		MainClass.directionNum++;

		if (MainClass.directionNum == 4)
			MainClass.directionNum = 0;

		turnRight(1);
	}

	private static void decrementDirectionNum()
	{
		MainClass.directionNum--;

		if (MainClass.directionNum == -1)
			MainClass.directionNum = 3;

		turnLeft(-1);
	}

	private static void turnLeft(int difference)
	{
		MainClass.LEFT_MOTOR.setSpeed(100);
		MainClass.RIGHT_MOTOR.setSpeed(100);

		MainClass.RIGHT_MOTOR.forward();
		MainClass.LEFT_MOTOR.backward();

		while (MainClass.sampleG[0] < (previousSampleG - (GYRO_ROTATION_L * difference)))
		{
		}

		MainClass.pilot.stop();

		previousSampleG += 90 * (-difference);
	}

	private static void turnRight(int difference)
	{
		MainClass.LEFT_MOTOR.setSpeed(100);
		MainClass.RIGHT_MOTOR.setSpeed(100);

		MainClass.LEFT_MOTOR.forward();
		MainClass.RIGHT_MOTOR.backward();

		while (MainClass.sampleG[0] > (previousSampleG - (GYRO_ROTATION_R * difference)))
		{
		}

		MainClass.pilot.stop();

		previousSampleG -= 90 * difference;
	}

}
