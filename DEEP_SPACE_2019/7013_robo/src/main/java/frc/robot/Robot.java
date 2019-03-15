/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.CameraServer; // FRC USB Camera Driver
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.cscore.CameraServerJNI;
// import edu.wpi.first.wpilibj.Encoder;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  // private double myRange = -1;
  private double toggle = 1.0;
  private double left_joy;
  private double right_joy;
  private double arcade_turn;
  private boolean first_auto = true;

  // Victor for functionality
  VictorSPX winch = new VictorSPX(4);
  VictorSPX intake = new VictorSPX(5);
  VictorSPX flapper = new VictorSPX(6);

  // Set of sparks for drive motors
  Spark rearLeftDrive = new Spark(0);
  Spark frontLeftDrive = new Spark(1);
  Spark rearRightDrive = new Spark(2);
  Spark frontRightDrive = new Spark(3);

  // Encoder test = new Encoder(2, 2, false, Encoder.EncodingType.k4X);
  // Encoder test = new Encoder(sourceA, sourceB, reverseDirection, encodingType)
  // Encoder test = new Encoder(sourceA, sourceB)
  // utroni

  DigitalInput bottomLimitSwitch = new DigitalInput(1);
  DigitalInput topLimitSwitch = new DigitalInput(0);
  // DigitalInput test = new DigitalInput(2);

  Counter encoder = new Counter(new DigitalInput(2));
  // Counter

  // Ultrasound 1 ports
  // Ultrasound ping port (Digital output)
  public static final int PING_ZERO = 1;
  // Ultrasound listen port (digital input)
  public static final int LISTEN_ZERO = 1;
  // Ultrasound ping port (Digital output)
  public static final int PING_ONE = 2;
  // Ultrasound listen port (digital input)
  public static final int LISTEN_ONE = 2;

  // DEADZONE constant
  public static final double DEAD_ZONE = 0.05;

  // SPEED_SCALING
  public double SPEED_SCALING = 1.0;

  public static final int TIME = 700; // 800 ms

  public int prev_val;
  // private int position = 0;
  private double start_time = 0;
  private double start_auto = 0;
  Timer timer = new Timer();
  private int time_locked = 0;

  // SENSORS

  /*
   * creates the ultra object and assigns ultra to be an ultrasonic sensor which
   * uses DigitalOutput 1 for the echo pulse and DigitalInput 1 for the trigger
   * pulse
   */
  // Ultrasonic ultrasonicSensor = new Ultrasonic(1, 0);
  RobotDrive drive = new RobotDrive(frontLeftDrive, rearLeftDrive, frontRightDrive, rearRightDrive);
  Joystick driverControl = new Joystick(0); // Keep track of what controller is being used
  Joystick driverControl2 = new Joystick(1);

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    // rearRightDrive.setInverted(true);
    // frontRightDrive.setInverted(true);
    // INITIALIZE USB CAMERA
    UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
    camera.setVideoMode(PixelFormat.kYUYV, 640, 360, 30);

    UsbCamera camera2 = CameraServer.getInstance().startAutomaticCapture();
    camera2.setVideoMode(PixelFormat.kYUYV, 640, 360, 30);

    CameraServerJNI.setLogger((level, file, line, msg) -> {
      System.out.println("CS: " + msg);
    }, 9);
    // encoder.reset();
    // prev_val = encoder.get();

    timer.start();

    // Enable automatic sampling for ultrasound
    // ultrasonicSensor.setAutomaticMode(true);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    // drive.setSafetyEnabled(false);
    SPEED_SCALING = 0.6;
    start_auto = timer.get();
    first_auto = true;
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
    default:
      if (timer.get() < start_auto + 2) {
        System.out.println("autoing\n");
        if (first_auto) {
          start_auto = timer.get();
          first_auto = false;
        }
        drive.tankDrive(-0.6, -0.6);
      } else {
        // Retrieve sample from ultrasound and update it to myRange
        // Uncomment one to use your prefered unit
        // myRange = ultrasonicSensor.getRangeInches();
        // System.out.printf("Range is %f", myRange);
        // myRange = ultrasonicSensor.getRangeMM();

        // position += encoder.get();

        // System.out.printf("switch 0 bot %b\n", bottomLimitSwitch.get());
        // System.out.printf("switch 1 top %b\n", topLimitSwitch.get());
        // System.out.printf("Diff = %d\n", (encoder.get() - prev_val));
        // prev_val = encoder.get();
        // System.out.printf("Encoder value %d\n", encoder.get());
        // System.out.printf("Encoder value \n");
        // encoder.reset();
        // System.out.printf("Position %d\n", position);

        // System.out.printf("testing %b\n", test.get());
        // if (test.get()) {
        // System.out.printf("1\n");
        // } else {
        // System.out.printf("0\n");
        // }

        // drive.tankDrive((double) driverControl.getRawAxis(1)*(-1.0)*(0.2), (double)
        // driverControl.getRawAxis(5)*(-1.0)*(0.2));
        // left_joy = (double) driverControl.getRawAxis(1) * (toggle);
        // right_joy = (double) driverControl.getRawAxis(3) * (toggle);
        // if (Math.abs(left_joy) > DEAD_ZONE || Math.abs(right_joy) > DEAD_ZONE) {|
    left_joy = (double) driverControl.getRawAxis(1);
    right_joy = (double) driverControl.getRawAxis(3) * (toggle);
    arcade_turn = (double) driverControl.getRawAxis(2);
    if (Math.abs(left_joy) > DEAD_ZONE || Math.abs(right_joy) > DEAD_ZONE || Math.abs(arcade_turn) > DEAD_ZONE) {
          // scales outside of deadzone (DEAD_ZONE - 100%) to 0 - 100%
          if (left_joy > 0) {
            left_joy = (left_joy - DEAD_ZONE) / (1 - DEAD_ZONE);
          } else {
            left_joy = (left_joy + DEAD_ZONE) / (1 - DEAD_ZONE);
          }
          if (right_joy > 0) {
            right_joy = (right_joy - DEAD_ZONE) / (1 - DEAD_ZONE);
          } else {
            right_joy = (right_joy + DEAD_ZONE) / (1 - DEAD_ZONE);
          }

          left_joy *= SPEED_SCALING;
          right_joy *= SPEED_SCALING;
          // arcade_turn *= SPEED_SCALING;

          // if (toggle == -1.0) {
          //   drive.tankDrive(left_joy, right_joy);
          // } else {
          //   drive.tankDrive(right_joy, left_joy);
          // }
          
      if (toggle == -1.0) {
        rearLeftDrive.set(-left_joy + arcade_turn);
        frontLeftDrive.set(-left_joy + arcade_turn);
        rearRightDrive.set(left_joy + arcade_turn);
        frontRightDrive.set(left_joy + arcade_turn);
      } else {
        rearLeftDrive.set(left_joy + arcade_turn);
        frontLeftDrive.set(left_joy + arcade_turn);
        rearRightDrive.set(-left_joy + arcade_turn);
        frontRightDrive.set(-left_joy + arcade_turn);
      }

        } else {
          drive.tankDrive(0, 0);
        }

        // raw axis was previously 1 and 5, but controller settings to logitech dual
        // action got swaped so it's 1 and 3 now?
        // buttons got swapped too was 4 and 1 now 4 and 2
        if (driverControl2.getRawButton(4) && topLimitSwitch.get()) {
          winch.set(ControlMode.PercentOutput, 0.5);
          System.out.printf("winch1\n");
        } else if (driverControl2.getRawButton(2) && bottomLimitSwitch.get()) {
          System.out.printf("winch2\n");
          winch.set(ControlMode.PercentOutput, -0.5);
        } else {
          winch.set(ControlMode.PercentOutput, 0);
        }

        if (driverControl2.getRawButton(5)) {
          intake.set(ControlMode.PercentOutput, 1);
          System.out.printf("intake\n");
        } else if (driverControl2.getRawButton(6)) {
          System.out.printf("outtake\n");
          intake.set(ControlMode.PercentOutput, -1);
        } else {
          intake.set(ControlMode.PercentOutput, 0);
        }

        if (driverControl2.getRawButton(7)) {
          flapper.set(ControlMode.PercentOutput, 0.6);
        } else if (driverControl2.getRawButton(8)) {
          flapper.set(ControlMode.PercentOutput, -0.6);
        } else {
          flapper.set(ControlMode.PercentOutput, 0);
        }

        if (driverControl2.getRawButton(3) && (time_locked == 0)) {
          start_time = timer.get();
          time_locked = -1;
          // System.out.printf("flapper on\n");
        } else if (driverControl2.getRawButton(1) && (time_locked == 0)) {
          start_time = timer.get();
          time_locked = 1;
          // System.out.printf("flapper off\n");
          // flapper.set(ControlMode.PercentOutput, -1);
        }

        if ((time_locked != 0) && (timer.get() < start_time + 0.4)) {
          flapper.set(ControlMode.PercentOutput, time_locked);
        } else {
          time_locked = 0;
        }

        // System.out.printf("time %f\n",timer.get());

        // 10 = start button, toggle direction
        if (driverControl.getRawButtonReleased(10)) {
          toggle *= -1;
          System.out.printf("toggled %f\n", toggle);
        }
      }
      break;
    }
  }

  @Override
  public void teleopInit() {
    super.teleopInit();
    SPEED_SCALING = 1.0;
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    // Retrieve sample from ultrasound and update it to myRange
    // Uncomment one to use your prefered unit
    // myRange = ultrasonicSensor.getRangeInches();
    // System.out.printf("Range is %f", myRange);
    // myRange = ultrasonicSensor.getRangeMM();

    // position += encoder.get();

    // System.out.printf("switch 0 bot %b\n", bottomLimitSwitch.get());
    // System.out.printf("switch 1 top %b\n", topLimitSwitch.get());
    // System.out.printf("Diff = %d\n", (encoder.get() - prev_val));
    // prev_val = encoder.get();
    // System.out.printf("Encoder value %d\n", encoder.get());
    // System.out.printf("Encoder value \n");
    // encoder.reset();
    // System.out.printf("Position %d\n", position);

    // System.out.printf("testing %b\n", test.get());
    // if (test.get()) {
    // System.out.printf("1\n");
    // } else {
    // System.out.printf("0\n");
    // }

    // drive.tankDrive((double) driverControl.getRawAxis(1)*(-1.0)*(0.2), (double)
    // driverControl.getRawAxis(5)*(-1.0)*(0.2));
    // left_joy = (double) driverControl.getRawAxis(1) * (toggle);
    // right_joy = (double) driverControl.getRawAxis(3) * (toggle);
    // if (Math.abs(left_joy) > DEAD_ZONE || Math.abs(right_joy) > DEAD_ZONE) {
    left_joy = (double) driverControl.getRawAxis(1);
    right_joy = (double) driverControl.getRawAxis(3) * (toggle);
    arcade_turn = (double) driverControl.getRawAxis(2);
    if (Math.abs(left_joy) > DEAD_ZONE || Math.abs(right_joy) > DEAD_ZONE || Math.abs(arcade_turn) > DEAD_ZONE) {
      // scales outside of deadzone (DEAD_ZONE - 100%) to 0 - 100%
      // if (left_joy > 0) {
      // left_joy = (left_joy - DEAD_ZONE) / (1 - DEAD_ZONE);
      // } else {
      // left_joy = (left_joy + DEAD_ZONE) / (1 - DEAD_ZONE);
      // }
      // if (right_joy > 0) {
      // right_joy = (right_joy - DEAD_ZONE) / (1 - DEAD_ZONE);
      // } else {
      // right_joy = (right_joy + DEAD_ZONE) / (1 - DEAD_ZONE);
      // }
      System.out.printf("%f %f\n", left_joy, right_joy);
      left_joy *= SPEED_SCALING;
      right_joy *= SPEED_SCALING;

      // if (toggle == -1.0) {
      //   drive.tankDrive(left_joy, right_joy);
      // } else {
      //   drive.tankDrive(right_joy, left_joy);
      // }
      if (toggle == -1.0) {
        rearLeftDrive.set(-left_joy + arcade_turn);
        frontLeftDrive.set(-left_joy + arcade_turn);
        rearRightDrive.set(left_joy + arcade_turn);
        frontRightDrive.set(left_joy + arcade_turn);
      } else {
        rearLeftDrive.set(left_joy + arcade_turn);
        frontLeftDrive.set(left_joy + arcade_turn);
        rearRightDrive.set(-left_joy + arcade_turn);
        frontRightDrive.set(-left_joy + arcade_turn);
      }

    } else {
      drive.tankDrive(0, 0);
    }

    // raw axis was previously 1 and 5, but controller settings to logitech dual
    // action got swaped so it's 1 and 3 now?
    // buttons got swapped too was 4 and 1 now 4 and 2
    if (driverControl2.getRawButton(4) && topLimitSwitch.get()) {
      winch.set(ControlMode.PercentOutput, 0.5);
      // System.out.printf("winch1\n");
    } else if (driverControl2.getRawButton(2) && bottomLimitSwitch.get()) {
      // System.out.printf("winch2\n");
      winch.set(ControlMode.PercentOutput, -0.5);
    } else {
      winch.set(ControlMode.PercentOutput, 0);
    }

    if (driverControl2.getRawButton(5)) {
      intake.set(ControlMode.PercentOutput, 1);
      // System.out.printf("intake\n");
    } else if (driverControl2.getRawButton(6)) {
      // System.out.printf("outtake\n");
      intake.set(ControlMode.PercentOutput, -1);
    } else {
      intake.set(ControlMode.PercentOutput, 0);
    }

    if (driverControl2.getRawButton(7)) {
      flapper.set(ControlMode.PercentOutput, 0.6);
    } else if (driverControl2.getRawButton(8)) {
      flapper.set(ControlMode.PercentOutput, -0.6);
    } else {
      flapper.set(ControlMode.PercentOutput, 0);
    }

    if (driverControl2.getRawButton(3) && (time_locked == 0)) {
      start_time = timer.get();
      time_locked = -1;
      // System.out.printf("flapper on\n");
    } else if (driverControl2.getRawButton(1) && (time_locked == 0)) {
      start_time = timer.get();
      time_locked = 1;
      // System.out.printf("flapper off\n");
      // flapper.set(ControlMode.PercentOutput, -1);
    }

    if ((time_locked != 0) && (timer.get() < start_time + 0.4)) {
      flapper.set(ControlMode.PercentOutput, time_locked);
    } else {
      time_locked = 0;
    }

    // System.out.printf("time %f\n",timer.get());

    // 10 = start button, toggle direction
    if (driverControl.getRawButtonReleased(10)) {
      toggle *= -1;
      System.out.printf("toggled %f\n", toggle);
    }

    // 9 = back button, toggle speed
    if (driverControl.getRawButtonReleased(9)) {
      if (SPEED_SCALING == 1.0) {
        SPEED_SCALING = 0.6;
      } else {
        SPEED_SCALING = 1.0;
      }
      System.out.printf("speed %f\n", SPEED_SCALING);
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
