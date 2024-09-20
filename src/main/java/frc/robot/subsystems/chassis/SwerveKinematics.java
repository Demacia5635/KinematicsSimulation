package frc.robot.subsystems.chassis;

import org.opencv.core.Scalar;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import frc.robot.Constants;

/**
 * Class to enhance WPILIB SwerveDriveKinematics
 * 
 * In sewerveToModuleState
 * - if omega is in low range - increase the rate
 * - if omega is non zero - change the vx/vy to compensate for the turn
 * 
 * In toChassisSpeeds
 * - do the reverse - if Omega is non zero - change the result vx/vy as
 * compensated
 */
public class SwerveKinematics extends SwerveDriveKinematics {

    public SwerveModuleState[] states;

    Translation2d[] moduleTranslationsMeters;

    /**
     * Constructor we use
     * 
     * @param moduleTranslationsMeters
     */
    public SwerveKinematics(Translation2d... moduleTranslationsMeters) {
        super(moduleTranslationsMeters);
        this.moduleTranslationsMeters = moduleTranslationsMeters;

    }

    private double getCycleDistance(double vel) {
        return vel * 0.02;
    }

    /**
     * Rotate the speeds counter to omega - to drive stright
     */
    public SwerveModuleState[] toSwerveModuleStates(ChassisSpeeds speeds, Pose2d curPose,
            SwerveModuleState[] prevStates) {
        System.out.println("SPEEDS ----------------------------------------------------------");
        System.out.println(speeds);
        Pose2d estimatedPose = new Pose2d(curPose.getX() + getCycleDistance(speeds.vxMetersPerSecond),
                curPose.getY() + getCycleDistance(speeds.vyMetersPerSecond),
                curPose.getRotation().plus(new Rotation2d(speeds.omegaRadiansPerSecond * 0.02))); // the estimated
                                                                                                  // pose2d of the
                                                                                                  // chassis location
                                                                                                  // and direction
        System.out.println("ESTIMATED POSE ----------------------------------------------------------");
        System.out.println(estimatedPose);
        SwerveModuleState[] newModuleStates = new SwerveModuleState[4];
        for (int i = 0; i < 4; i++) {
            Translation2d moduleEstimatedPos = estimatedPose.getTranslation().plus(
                    moduleTranslationsMeters[i].rotateBy(estimatedPose.getRotation()));// the
                                                                                       // module
                                                                                       // estimated
                                                                                       // pos
            Translation2d moduleLocationDifference = moduleEstimatedPos
                    .minus(curPose.getTranslation().plus(moduleTranslationsMeters[i].rotateBy(curPose.getRotation()))); // the
                                                                                                                        // delta
                                                                                                                        // x
                                                                                                                        // of
                                                                                                                        // the
                                                                                                                        // module
                                                                                                                        // between
                                                                                                                        // previous
                                                                                                                        // and
                                                                                                                        // estimate
            Rotation2d alpha = moduleLocationDifference.getAngle()
                    .minus(prevStates[i].angle.rotateBy(curPose.getRotation())); // finding alpha
            double radius = alpha.getRadians() != 0
                    ? moduleLocationDifference.getNorm() * Math.sin((Math.PI / 2) - alpha.getRadians())
                            / Math.sin(alpha.getRadians() * 2)
                    : 0;

            double moduleV = alpha.times(2 * radius).getRadians() / 0.02; // (2alpha * d * sin(0.5pi -
                                                                          // alpha)/sin(2alpha))/0.02 = Vn

            double startingModuleRadians = prevStates[i].angle.getRadians();
            double chassisDiffRadians = estimatedPose.getRotation().minus(curPose.getRotation()).getRadians();

            double moduleAngle = startingModuleRadians + (2 * alpha.getRadians()) - chassisDiffRadians;// d0 + 2alpha -
                                                                                                       // delta(Beta) =
                                                                                                       // Dn
            newModuleStates[i] = new SwerveModuleState(moduleV, new Rotation2d(moduleAngle));
        }

        System.out.println("MODULE STATES---------------------------------------------");
        for (SwerveModuleState state : newModuleStates) {
            System.out.println(state);
        }
        return newModuleStates;
    }

    /**
     * Rotate the speeds back to omega - to drive stright
     */
    @Override
    public ChassisSpeeds toChassisSpeeds(SwerveModuleState... moduleStates) {
        return new ChassisSpeeds();
    }

}