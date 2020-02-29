/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private static final String kLeftAuto = "Left Auto";
  private static final String kRightAuto = "Right Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  Spark m_rearLeft = new Spark(0);
  Spark m_frontLeft = new Spark(1);
  SpeedControllerGroup m_left = new SpeedControllerGroup(m_frontLeft, m_rearLeft);
 
  Spark m_rearRight = new Spark(2);
  Spark m_frontRight = new Spark(3);
  SpeedControllerGroup m_right = new SpeedControllerGroup(m_frontRight, m_rearRight);

  DifferentialDrive nitro = new DifferentialDrive(m_left, m_right);

  VictorSPX m_climber = new VictorSPX(4);
  VictorSPX m_intake = new VictorSPX(5);
  VictorSPX m_arm = new VictorSPX (6);

  Joystick logitech1 = new Joystick(0);

  DigitalInput bottomArmLS = new DigitalInput(0);
  DigitalInput topArmLS = new DigitalInput(1);
  DigitalInput topClimbLS = new DigitalInput(2);
  DigitalInput bottomClimbLS = new DigitalInput(3);
  
  Ultrasonic distance = new Ultrasonic(4,5);
  
  double range;
  int turnStep = 0;
  double speedScale = 1.0;
  // RobotDrive drive = new RobotDrive();
  Timer test = new Timer();
  int timer;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("Center Auto", kCustomAuto);
    m_chooser.addOption("Left Auto", kLeftAuto);
    m_chooser.addOption("Right Auto", kRightAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    
    //Automatic mode will send ping by itself 4 times every second.
    distance.setAutomaticMode(true); // turns on automatic mode
    distance.setEnabled(true); 
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    System.out.println(m_chooser.getSelected()); 
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    timer = 0;
    range = distance.getRangeInches(); // reads the range on the ultrasonic sensor
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    double speed = 0;
    switch (m_autoSelected) {
      case kCustomAuto:
        break;
      case kDefaultAuto:
        range = distance.getRangeInches();
        speed = 0.55;

       if (range < 20) { 
         timer += 1;
       }

       if (timer > 100 && timer < 200) {
         speed = 0.4;
         m_arm.set(ControlMode.PercentOutput, -0.2);
       } else if (timer > 200 && timer < 300) {
         speed = 0;
        m_intake.set(ControlMode.PercentOutput, 0.8);
       } else if (timer > 300) {
         speed = 0;
         m_intake.set(ControlMode.PercentOutput, 0);
       }
        // left is negative cause its hooked up backwards
       nitro.tankDrive(-speed, speed);
       break;

      case kLeftAuto:
        range = distance.getRangeInches();
        speed = 0.55;
        
        if (range < 60) {
          turnStep++;
        }
        switch (turnStep) {
          case 1:
            if (timer < 20) {
              nitro.tankDrive(-speed + 0.05, speed);
            } else if (timer >= 20 && timer < 40) {
              nitro.tankDrive(-speed, speed + 0.05);
            } else {
              turnStep++;
            }
            timer += 1;
            break;
          case 2:
            if (timer < 20) {
              nitro.tankDrive(-speed, speed + 0.05);
            } else if (timer >= 20 && timer < 40) {
              nitro.tankDrive(-speed + 0.05, speed);
            } else {
              turnStep++;
              timer = 0;
            }
            timer += 1;
            break;
        }

        if (range < 20) { 
          timer += 1;
        }

        if (timer > 100 && timer < 200) {
          speed = 0.4;
          m_arm.set(ControlMode.PercentOutput, -0.2);
        } else if (timer > 200 && timer < 300) {
          speed = 0;
          m_intake.set(ControlMode.PercentOutput, 0.8);
        } else if (timer > 300) {
          speed = 0;
          m_intake.set(ControlMode.PercentOutput, 0);
        }
        // left is negative cause its hooked up backwards
       nitro.tankDrive(-speed, speed);
       break;
      case kRightAuto:
        range = distance.getRangeInches();
        speed = 0.55;
        
        if (range < 60) {
          turnStep++;
        }
        switch (turnStep) {
          case 1:
            if (timer < 20) {
              nitro.tankDrive(-speed, speed + 0.05);
            } else if (timer >= 20 && timer < 40) {
              nitro.tankDrive(-speed + 0.05, speed);
            } else {
              turnStep++;
            }
            timer += 1;
            break;
          case 2:
            if (timer < 20) {
              nitro.tankDrive(-speed, speed + 0.05);
            } else if (timer >= 20 && timer < 40) {
              nitro.tankDrive(-speed + 0.05, speed);
            } else {
              turnStep++;
              timer = 0;
            }
            timer += 1;
            break;
        }

        if (range < 20) { 
          timer += 1;
        }

        if (timer > 100 && timer < 200) {
          speed = 0.4;
          m_arm.set(ControlMode.PercentOutput, -0.2);
        } else if (timer > 200 && timer < 300) {
          speed = 0;
          m_intake.set(ControlMode.PercentOutput, 0.8);
        } else if (timer > 300) {
          speed = 0;
          m_intake.set(ControlMode.PercentOutput, 0);
        }
        // left is negative cause its hooked up backwards
        nitro.tankDrive(-speed, speed);
        break;
      default:
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */ 
  @Override
  public void teleopPeriodic() {

    double left_stick = -logitech1.getRawAxis(1);
    double right_stick = -logitech1.getRawAxis(3);
    double arcade_turn = logitech1.getRawAxis(2);

    // change for different drives
    nitro.arcadeDrive(-arcade_turn*speedScale, -left_stick*speedScale);
    //nitro.tankDrive(left_stick*-0.7,right_stick*0.7);

    if (logitech1.getRawButton(4) && topClimbLS.get()) {
      m_climber.set(ControlMode.PercentOutput, 0.5);
    } else if (logitech1.getRawButton(2) && bottomClimbLS.get()) {
      m_climber.set(ControlMode.PercentOutput, -0.5);
    } else {
      m_climber.set(ControlMode.PercentOutput, 0);
    }
//a is couter clockwize y is clockwize
    if (logitech1.getRawButton(1) && topArmLS.get()) {
      m_intake.set(ControlMode.PercentOutput, 0.96);
    } else if (logitech1.getRawButton(3) && bottomArmLS.get()) {
      m_intake.set(ControlMode.PercentOutput, -1);
    } else {
      m_intake.set(ControlMode.PercentOutput, 0);
      // m_arm.set(0);
    }
    if (logitech1.getRawButton(6)) { 
      m_arm.set(ControlMode.PercentOutput, 0.6);
    } else if (logitech1.getRawButton(5)) {
      m_arm.set(ControlMode.PercentOutput, -0.45);
    } else {
      m_arm.set(ControlMode.PercentOutput, 0);
    }

    if (logitech1.getRawButtonReleased(10)) {
      if (speedScale == 1.0) {
        speedScale = 0.7;
      } else {
        speedScale = 1.0;
      }
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {

  }
}
