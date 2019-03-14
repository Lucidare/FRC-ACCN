package org.usfirst.frc.team2198.robot;

import edu.wpi.first.wpilibj.IterativeRobot;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.sql.Time;

//Camera Vision Classes
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

//Motor Controller Drivers
// disable for 2019 demo
// import com.ctre.CANTalon; 							// Talon SRX Driver

//Camera Drivers
import edu.wpi.cscore.AxisCamera;					// FRC SixAxis Camera Driver
import edu.wpi.cscore.CvSink;						// FRC SixAxis Display Driver
import edu.wpi.cscore.CvSource;						// FRC SixAxis Capture Driver

import edu.wpi.first.wpilibj.AnalogInput;			// FRC Analog Sensor Driver
import edu.wpi.first.wpilibj.CameraServer;			// FRC USB Camera Driver
import edu.wpi.first.wpilibj.Compressor;			// Add classes needed to use compressor
import edu.wpi.first.wpilibj.DoubleSolenoid;		// Add classes needed to use double solenoids
import edu.wpi.first.wpilibj.IterativeRobot;		// Needed for robot to run, does networking
import edu.wpi.first.wpilibj.Joystick;				// Adds joystick support, allowing any joystick to be used
import edu.wpi.first.wpilibj.RobotDrive;			// Object oriented drive classes
import edu.wpi.first.wpilibj.RobotDrive.MotorType;	// Lets us define motor type			// Adds camera support
import edu.wpi.first.wpilibj.Talon;					// Talon SR Driver
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.VictorSP;				// Victor SP Driver

// 2019 demo addition
import edu.wpi.first.wpilibj.Spark;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	
	////////////////////////
	// OBJECT DECLERATION //
	////////////////////////
	
	// MOTOR CONTROLLERS
	
	// MODIFICATION FOR 2019 DEMO
	//CANTalon frontLeftMotor = new CANTalon(10); 
	//CANTalon rearLeftMotor = new CANTalon(11);
	//CANTalon frontRightMotor = new CANTalon(5);
	//CANTalon rearRightMotor = new CANTalon(6);

	
	Spark frontLeftMotor = new Spark(7);
	Spark rearLeftMotor = new Spark(8);
	Spark frontRightMotor = new Spark(9);
	Spark rearRightMotor = new Spark(6);

	VictorSP shooter = new VictorSP(3);
	VictorSP feeder = new VictorSP(5);
	VictorSP collector = new VictorSP(0);

	Talon hangMotor1 = new Talon(1);
	Talon hangMotor2 = new Talon(2);
	Talon vibrator = new Talon(6);
	
	// PNEUMATICS
	
	DoubleSolenoid gearPiston = new DoubleSolenoid(0,1); // Solenoid on ports 0 and 1
	Compressor compressor = new Compressor(0);
	
	// DRIVE TRAIN
	
	// Making a RobotDrive object makes is very easy to implement tankdrive or arcade drive
	RobotDrive drive = new RobotDrive(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
	

	// JOYSTICKS
	// This supports XBox mapping, use an emulator to convert DualShock to XBOX on the driver laptop
	Joystick driverStick = new Joystick(0);		// Joystick 1 corresponds to driver controller
	Joystick operatorStick = new Joystick(1);	// Joystick 2 corresponds to operator controller
	
	// SENSORS
	
	/*
	 * 	creates the ultra object and assigns ultra to be an ultrasonic sensor which uses DigitalOutput 1 for 
  	 * 	the echo pulse and DigitalInput 1 for the trigger pulse
	 */
	
	Ultrasonic rightDistance = new Ultrasonic(0,1);
	Ultrasonic leftDistance = new Ultrasonic(2,3);
	
	
	
	/////////////////////////
	// AUTONOMOUS SELECTOR //
	/////////////////////////
	
	// Scrapped in final version
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	//SendableChooser<String> chooser = new SendableChooser<>();
	
	//////////////////////////
	// VARIABLE DECLERATION //
	//////////////////////////
	
	int reverseControls = 0; // Variable to reverse drive direction
	double leftAxis; // left joystick axis
	double rightAxis; // right joystick axis
	int shooterIdle = 0;
	
	int slowDrive = 0; // prevent driver from crashing
	
	/////////////////
	// CAMERA CODE //
	/////////////////
	
	Thread visionThread; // Create new thread for SIXAXIS CAMERA

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
				
		//////////////////////////////
		// REVERSE NECESSARY MOTORS //
		//////////////////////////////
		
		drive.setInvertedMotor(MotorType.kFrontLeft, false);
    	drive.setInvertedMotor(MotorType.kRearLeft, false);
    	drive.setInvertedMotor(MotorType.kFrontRight, false);
    	drive.setInvertedMotor(MotorType.kRearRight, false);
    	
    	shooter.setInverted(true);
    	feeder.setInverted(true);
		
    	/////////////////////////////
    	// MOTOR CONTROLLER SAFETY //
    	/////////////////////////////
    	
    	/*
    	 *  The safety mode built into the motor controllers ensures
    	 *  that if there is no command to keep	the motor running, 
    	 *  the motor controller shuts off. This ensures it doesn't run
    	 *  if you forgot to stop the motor. We're disabling this as it
    	 *  makes the autonomous programming much easier.
    	 */
			  
		// discuss autonomous impact
    	shooter.setSafetyEnabled(false);
    	feeder.setSafetyEnabled(false);
    	
    	drive.setSafetyEnabled(false);
    	
    	////////////////////////
    	// INITIALIZE SENSORS //
    	////////////////////////
    	
    	
    	//Automatic mode will send ping by itself 4 times every second. 
    	 
    	leftDistance.setAutomaticMode(true); // turns on automatic mode
    	rightDistance.setAutomaticMode(true); // turns on automatic mode
    	
		/////////////////
		// CAMERA CODE //
		/////////////////
		
		// INITIALIZE USB CAMERA
    	CameraServer.getInstance().startAutomaticCapture();
		
		
		// SIXAXIS CAMERA 
    	// Sixaxis IP address: 10.21.98.11
    	
		visionThread = new Thread(() -> {
			// Get the Axis camera from CameraServer
			AxisCamera camera = CameraServer.getInstance().addAxisCamera("10.21.98.11");
			// Set the resolution
			camera.setResolution(640, 480);

		});
		
		 visionThread.setDaemon(true);
		 visionThread.start();
		
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		//autoSelected = chooser.getSelected();
		 autoSelected = SmartDashboard.getString("Auto Selector",
		 defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
		
		double range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
	}

	/**
	 * This function is called periodically during autonomous
	 */

	@Override
	public void autonomousPeriodic() {
		
		// PLEASE make sure you have an emergency exit condition after 15s
		// >> restart robot code from driver station

		double range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
		switch (autoSelected) {
		case customAuto:
			//Custom

			// go fast while further then 40
	    	while ((range > 40.0)) {
	    		drive.tankDrive(0.6, 0.6);
	        	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
			}
			
			// slow down a bit
	    	while ((range > 18.0)) {
	    		drive.tankDrive(0.4, 0.4);
	        	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
			}
			
			// release the gear
	    	while ((range > 16.0)) {
	    		drive.tankDrive(0.4, 0.4);
	    		gearPiston.set(DoubleSolenoid.Value.kReverse);
	    		range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
	    	}
	    	gearPiston.set(DoubleSolenoid.Value.kReverse);
	    	/*while ((range > 10.0)) {
	    		drive.tankDrive(-0.30, -0.30);
	        	range = ultra.getRangeInches(); // reads the range on the ultrasonic sensor
	    	}
			*/
			
			//stop
			drive.tankDrive(0.0, 0.0);
			
			// make sure gear is output
	    	gearPiston.set(DoubleSolenoid.Value.kReverse);
			
			// wait
	    	Timer.delay(0.60);
	    	range = rightDistance.getRangeInches();
			
			// move back
	    	while (range < 40) {
	    	drive.tankDrive(-0.4, -0.4);
	    	gearPiston.set(DoubleSolenoid.Value.kForward);
	    	range = rightDistance.getRangeInches();
	    	}
			
			// stop
	    	range = rightDistance.getRangeInches();
	    	drive.tankDrive(0.0, 0.0);
	    	
	    	break;
		
	    	
		case defaultAuto:
		default:
		
			// Get sensor value
			range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
		    // Drive forward
	    	drive.tankDrive(0.60, 0.60);
	    	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
			// keep going forward
			Timer.delay(2.25);
			//rotate
	    	drive.tankDrive(0.65, -0.65);
	    	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
			// wait
			Timer.delay(0.45);
	    	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
			
			// start slowing as we approach peg
	    	while ((range > 40.0)) {
	    		drive.tankDrive(0.6, 0.6);
	        	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
	    	}
	    	while ((range > 18.0)) {
	    		drive.tankDrive(0.4, 0.4);
	        	range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
	    	}
	    	while ((range > 16.0)) {
	    		drive.tankDrive(0.4, 0.4);
	    		gearPiston.set(DoubleSolenoid.Value.kReverse);
	    		range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
			}
			
			// release gear sequence
	    	gearPiston.set(DoubleSolenoid.Value.kReverse);
	    	/*while ((range > 10.0)) {
	    		drive.tankDrive(-0.30, -0.30);
	        	range = ultra.getRangeInches(); // reads the range on the ultrasonic sensor
	    	}
	    	*/
	    	
	    	drive.tankDrive(0.0, 0.0);
	    	gearPiston.set(DoubleSolenoid.Value.kReverse);
		
			// back away
	    	Timer.delay(1.00);
	    	range = rightDistance.getRangeInches();
	    	while (range < 40) {
	    	drive.tankDrive(-0.4, -0.4);
	    	gearPiston.set(DoubleSolenoid.Value.kForward);
	    	range = rightDistance.getRangeInches();
	    	}
	    	range = rightDistance.getRangeInches();
	    	
	    
	    	drive.tankDrive(0.65, -0.65);
	    	Timer.delay(0.35);
	    	drive.tankDrive(-0.60, -0.60);
	    	
	    	// 3s can hit the hopper
	    	// 2.5s stops right before hopper
	    	
	    	Timer.delay(2.50);
	    	drive.tankDrive(0.0, 0.0);
			break;
			


		}
	
	
	}


	/**
	 * This function is called periodically during operator control
	 */

	@Override
	public void teleopPeriodic() {
		
		// Release gear and open door by extending piston
		if (driverStick.getRawButton(3)) {
			gearPiston.set(DoubleSolenoid.Value.kForward);
		}
		
		// Close gear doors by retracting piston
    	else if (driverStick.getRawButton(2)) {
			gearPiston.set(DoubleSolenoid.Value.kReverse);
		}
		
		// turn solenoid off
    	else  {
			gearPiston.set(DoubleSolenoid.Value.kOff);
		} 
    	
    	
    	// Enable reverse drive mode for shooting
    	if (driverStick.getRawButton(4)){
    		reverseControls = 1;
		}
    	
    	// Disable reverse drive mode for gears
		else if (driverStick.getRawButton(1)){
			reverseControls = 0;
		}
		
		// Enable low sensitivity when close to pegs
		// lowers speed when Weyman scores to boost precision
    	if (operatorStick.getRawButton(4)) {
    		slowDrive = 1;
		}
		
		// Allow full power - high sensitivity
    	else if (operatorStick.getRawButton(2)) {
    		slowDrive = 0;
    	}
		
		// When trigger pressed halfway, enable collector
    	if (driverStick.getRawAxis(3) > 0.4) {
    		collector.set(0.6);
    		
		}
		

		// otherwise, disable collector
    	else {
    		collector.set(0.0);
    	}
    	
    	// read range from the sensor variable
    	double range = rightDistance.getRangeInches(); // reads the range on the ultrasonic sensor
    	
    	
		// Assign axis directly as joysticks
		// read double from joystick axis, and store in variable. Invert so up is forward

		leftAxis = driverStick.getRawAxis(1)*-1;  // Create double leftAxis, and assign it value of left Y axis 
    	rightAxis = driverStick.getRawAxis(5)*-1; // Create double rightAxis, and assign value of right Y axis
		
		// Regular direction with full speed
    	if ((reverseControls == 0) && (slowDrive == 0)) {
			// tank drive example
			drive.tankDrive(leftAxis, rightAxis);
			
			// arcade drive example
    	    // drive.arcadeDrive(driverStick.getRawAxis(1), driverStick.getRawAxis(4));
		}
		
		// Regular direction with slowing when approaching peg
    	else if ((reverseControls == 0) && (slowDrive == 1)) {
    		if (range < 30) {
    			drive.tankDrive(leftAxis*0.5, rightAxis*0.5);
    		}
    		else {
    			drive.tankDrive(leftAxis, rightAxis);
    		}
		}
		
		// referse direction
    	else if ((reverseControls == 1) && (slowDrive  == 0)) {
    		drive.tankDrive(rightAxis*-1, leftAxis*-1);
    	}
		
		// always go slow
    	else if ((reverseControls == 1) && (slowDrive == 1)) {
    		drive.tankDrive(rightAxis*-0.5, leftAxis*-0.5);
		}
		
		// if confused, just go normally
    	else {
    		drive.tankDrive(leftAxis, rightAxis);
    	}
      
    	// slowly lower hanging (for test use)
    	if (driverStick.getRawButton(7)) {
    		hangMotor1.set(-0.25);
    		hangMotor2.set(-0.25);
		}
		// in normal use, change hanging motor speed with axis
    	else {
    		hangMotor1.set(driverStick.getRawAxis(2));
    		hangMotor2.set(driverStick.getRawAxis(2));
    	} 	
		
		// if shooting and fuel is stuck, vibrate the platform
    	vibrator.set(operatorStick.getRawAxis(3)*0.5);

		// let operator power feeder (to shooter)
    	if (operatorStick.getRawButton(3)) {
    		feeder.set(0.80);
    	}
    	
    	else {
    		feeder.set(0.0);
    	}
		
		// pre-run the shooter at low speed (for consecutive shots)
    	if (operatorStick.getRawButton(2)) {
    		shooterIdle = 1;
    	}
		
		// shoot at full speed, and turn off motor
    	if (operatorStick.getRawButton(1)) {
    		shooter.set(1.0);
    		shooterIdle = 0;
		}
		
		// off condition - low speed or off
    	else if (shooterIdle == 1) {
    		shooter.set(0.25);
    	}
    	else {
    		shooter.set(0.0);
    	}
    	
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}

