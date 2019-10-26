import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;
import java.util.PriorityQueue;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import lejos.hardware.lcd.*;

public class MainClass
{

	static EV3LargeRegulatedMotor	LEFT_MOTOR		= new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor	RIGHT_MOTOR		= new EV3LargeRegulatedMotor(MotorPort.D);
	static EV3LargeRegulatedMotor	motor			= new EV3LargeRegulatedMotor(MotorPort.B);

	static Wheel					wheel1			= WheeledChassis.modelWheel(LEFT_MOTOR, 56).offset(65);
	static Wheel					wheel2			= WheeledChassis.modelWheel(RIGHT_MOTOR, 56).offset(-65);

	static Chassis					chassis			= new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);

	static MovePilot				pilot			= new MovePilot(chassis);

	static EV3IRSensor				irsensor		= new EV3IRSensor(SensorPort.S3);
	static SampleProvider			ir				= irsensor.getDistanceMode();

	static String[]					direction		= { "North", "East", "South", "West" };
	//                                                   0         1       2        3

	final static int				NORTH			= 0;
	final static int				EAST			= 1;
	final static int				SOUTH			= 2;
	final static int				WEST			= 3;

	final static int				WALL			= 1;
	final static int				GREEN_BLOCK		= 2;
	final static int				GOAL			= 3;

	final static int				GREEN			= 1;
	final static int				RED				= 0;
	final static int				HOME_POSITION_I	= 1;
	final static int				HOME_POSITION_J	= 1;
	static int						GOAL_I;
	static int						GOAL_J;
	static boolean					home			= false;

	static int						directionNum;

	static int[][]					map				= new int[13][19];

	final static int				POSITION		= 5;

	static int						position_i;
	static int						position_j;

	static EV3						ev3brick		= (EV3) BrickFinder.getLocal();
	static Keys						buttons			= ev3brick.getKeys();

	static EV3GyroSensor			gyroSensor		= new EV3GyroSensor(SensorPort.S2);
	static SampleProvider			gs				= gyroSensor.getAngleMode();
	static float[]					sampleG			= new float[gs.sampleSize()];

	static EV3ColorSensor			colorsensorR	= new EV3ColorSensor(SensorPort.S1);

	static SampleProvider			colorR			= colorsensorR.getColorIDMode();

	static float[]					sampleR			= new float[colorR.sampleSize()];

	static Stack<Integer>			pathBack		= new Stack<Integer>();

	static boolean					trapped			= false;

	static EV3UltrasonicSensor		ultraSonic		= new EV3UltrasonicSensor(SensorPort.S4);
	static SampleProvider			us				= ultraSonic.getDistanceMode();

	public static void main(String[] args)
	{
		sampleR[0] = -1;

		Stop stop = new Stop();
		stop.setDaemon(true);
		stop.start();

		GyroSensor gyro = new GyroSensor();
		gyro.setDaemon(true);
		gyro.start();

		motor.setSpeed(1000);
		motor.forward();

		for (int i = 0; i < 19; i++)
		{
			MainClass.map[0][i] = 1;
			MainClass.map[12][i] = 1;
		}

		for (int i = 0; i < 13; i++)
		{
			MainClass.map[i][0] = 1;
			MainClass.map[i][18] = 1;
		}

		position_i = 1;
		position_j = 1;

		directionNum = 0; //North

		Output output = new Output();
		output.setDaemon(true);
		output.start();

		Recalibrate reset = new Recalibrate();
		reset.setDaemon(true);
		reset.start();

		buttons.waitForAnyPress();

		while (!Button.ESCAPE.isDown())
		{
			Button.LEDPattern(1);

			MovingAround.action();

		}
	}

}

class Stop extends Thread
{

	Stop()
	{

	}

	public void run()
	{

		while (!Button.ESCAPE.isDown())
		{
		}
		System.exit(0);
	}

}

class GyroSensor extends Thread
{

	public void run()
	{
		while (!Button.ESCAPE.isDown())
		{
			MainClass.gs.fetchSample(MainClass.sampleG, 0);
		}
	}
}

class Output extends Thread
{

	static GraphicsLCD	g			= BrickFinder.getDefault().getGraphicsLCD();

	final int			MIN_X		= 0;
	final int			MIN_Y		= 10;
	final int			MAX_WIDTH	= 177;
	final int			MAX_HEIGHT	= 96;

	public void run()
	{

		while (!Button.ESCAPE.isDown())
		{
			//  Testing Screen Below
			//			LCD.clear(6);
			//
			//			LCD.drawInt(MainClass.map[MainClass.position_i - 1][MainClass.position_j - 1], 8, 4);
			//			LCD.drawInt(MainClass.map[MainClass.position_i - 1][MainClass.position_j], 9, 4);
			//			LCD.drawInt(MainClass.map[MainClass.position_i - 1][MainClass.position_j + 1], 10, 4);
			//			LCD.drawInt(MainClass.map[MainClass.position_i][MainClass.position_j - 1], 8, 3);
			//			LCD.drawInt(MainClass.POSITION, 9, 3);
			//			LCD.drawInt(MainClass.map[MainClass.position_i][MainClass.position_j + 1], 10, 3);
			//			LCD.drawInt(MainClass.map[MainClass.position_i + 1][MainClass.position_j - 1], 8, 2);
			//			LCD.drawInt(MainClass.map[MainClass.position_i + 1][MainClass.position_j], 9, 2);
			//			LCD.drawInt(MainClass.map[MainClass.position_i + 1][MainClass.position_j + 1], 10, 2);
			//
			//			LCD.drawString(MainClass.direction[MainClass.directionNum], 0, 6);
			//
			//			MainClass.gs.fetchSample(MainClass.sampleG, 0);
			//
			//			LCD.drawString("Gyro : " + MainClass.sampleG[0], 0, 7);

			initialise();

			printWall();

			printRobot();

			printGreenRed();

			Delay.msDelay(500);
			LCD.clear();
		}

	}

	private void printGreenRed()
	{
		for (int i = 0; i < MainClass.map.length; i++)
		{
			for (int j = 0; j < MainClass.map[0].length; j++)
			{
				if (MainClass.map[i][j] == 2)
				{
					g.drawRect((j / 2 * 19) + 4 + 5, (12 - i) / 2 * 16 + MIN_Y + 5 + 3, 5, 5);
				}

				if (MainClass.map[i][j] == 3)
				{
					g.drawRect((j / 2 * 19) + 4 + 5, (12 - i) / 2 * 16 + MIN_Y + 5 + 3, 5, 5);
					g.fillRect((j / 2 * 19) + 4 + 5, (12 - i) / 2 * 16 + MIN_Y + 5 + 3, 5, 5);
				}
				if (MainClass.map[i][j] == 9)
				{
					if (i % 2 == 1)
					{
						if (j % 2 == 1)
						{
							g.drawRoundRect((j / 2 * 19) + 4 + 5, (12 - i) / 2 * 16 + MIN_Y + 4 + 4, 3, 3, 1, 1);
						}
						else
						{
							g.drawRoundRect((j / 2 * 19) - 1, (12 - i) / 2 * 16 + MIN_Y + 4 + 4, 3, 3, 1, 1);
						}
					}
					else
					{
						g.drawRoundRect((j / 2 * 19) + 4 + 5, (12 - i) / 2 * 16 + MIN_Y - 1, 3, 3, 1, 1);
					}
				}
			}
		}
	}

	private void printRobot()
	{
		if (MainClass.position_i % 2 == 1)
		{
			if (MainClass.position_j % 2 == 1)
			{
				g.drawRoundRect((MainClass.position_j / 2 * 19) + 4 + 5, (12 - MainClass.position_i) / 2 * 16 + MIN_Y + 4 + 4, 5, 5, 3, 3);
			}
			else
			{
				g.drawRoundRect((MainClass.position_j / 2 * 19) - 1, (12 - MainClass.position_i) / 2 * 16 + MIN_Y + 4 + 4, 5, 5, 3, 3);
			}
		}
		else
		{
			g.drawRoundRect((MainClass.position_j / 2 * 19) + 4 + 5, (12 - MainClass.position_i) / 2 * 16 + MIN_Y - 1, 5, 5, 3, 3);
		}

	}

	private void printWall()
	{
		for (int i = 0; i < MainClass.map.length; i++)
		{
			for (int j = 0; j < MainClass.map[0].length; j++)
			{
				if (MainClass.map[i][j] == 1)
				{
					if (i % 2 == 0)
					{
						if (j % 2 == 1)
						{
							g.fillRect((j / 2 * 19) + 4, (12 - i) / 2 * 16 + MIN_Y, 15, 4);
						}
						else
						{
							g.fillRect((j / 2 * 19), (12 - i) / 2 * 16 + MIN_Y, 4, 4);
						}
					}
					else
					{
						if (j % 2 == 0)
						{
							g.fillRect((j / 2 * 19), (12 - i) / 2 * 16 + MIN_Y + 4, 4, 15);
						}
					}
				}
			}
		}
	}

	private void initialise()
	{
		g.drawRect(MIN_X, MIN_Y, MAX_WIDTH - 2, MAX_HEIGHT);

		for (int i = 0; i < 9; i++)
		{
			g.drawRect(i * 19, MIN_Y, 19, MAX_HEIGHT);
			g.drawRect(i * 19, MIN_Y, 4, MAX_HEIGHT);
		}

		for (int j = 0; j < 6; j++)
		{
			g.drawRect(MIN_X, MIN_Y + (j * 16), MAX_WIDTH - 2, 16);
			g.drawRect(MIN_X, MIN_Y + (j * 16), MAX_WIDTH - 2, 4);
		}
	}

}

class Recalibrate extends Thread
{
	public void run()
	{
		while (!Button.ESCAPE.isDown())
		{
			if (Button.LEFT.isDown())
			{
				MainClass.gyroSensor.reset();
				MovingAround.previousSampleG = 0;

			}
		}
	}
}
