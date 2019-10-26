import lejos.hardware.Button;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class GoingBack
{
	
	public boolean takeControl()
	{
		if ((MainClass.sampleR[0] == MainClass.GREEN) || (MainClass.trapped))
		{

			if (MainClass.sampleR[0] == MainClass.GREEN)
				MainClass.map[MainClass.position_i][MainClass.position_j] = MainClass.GREEN_BLOCK;
			else
				MainClass.map[MainClass.position_i][MainClass.position_j] = 9;

			Button.LEDPattern(2);
			return true;
		}
		else
		{
			Button.LEDPattern(1);
			return false;
		}

	}

	
	public static void action()
	{		
		int numDirect = MainClass.pathBack.pop();

		numDirect += 2;

		numDirect = numDirect % 4;

		MovingAround.changeDirectionNum(numDirect);
		moveBlock();
		moveBlock();

		MainClass.trapped = false;
		MainClass.colorR.fetchSample(MainClass.sampleR, 0);
	}

	private static void moveBlock()
	{
		MainClass.pilot.setLinearSpeed(150);
		MainClass.pilot.forward();
		Delay.msDelay(MovingAround.MOVE_BLOCK_DELAY); 
		MainClass.pilot.stop();

		//MainClass.map[MainClass.position_i][MainClass.position_j] = 9;

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
}
