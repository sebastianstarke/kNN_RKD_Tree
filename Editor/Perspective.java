package Points2Map;

import javax.swing.event.*;

public class Perspective {

    private Vector3D fromPoint;
    private Vector3D atPoint;
    private Vector3D upVector;
	private double fromAtDist;

    private double viewAngle;
    private double tanHalfAngle;

    private int screenScaleX;
    private int screenScaleY;
    private double screenScaleXHalf;
    private double screenScaleYHalf;
    private double aspectRatio;

    private double factor2DX;
    private double factor2DY;

    // Transformation matrix V
    private Vector3D aRow;
    private Vector3D bRow;
    private Vector3D cRow;

    public Perspective() {

        upVector = new Vector3D(0, 1, 0);
        atPoint = new Vector3D(0, 0, 0);
        fromPoint = new Vector3D(1, 1, 1);

        screenScaleX = 1;
        screenScaleY = 1;
        screenScaleXHalf = (double)screenScaleX / 2.0;
        screenScaleYHalf = (double)screenScaleY / 2.0;
        aspectRatio = 1.0;
 
        viewAngle = 45;
        tanHalfAngle = Math.tan(viewAngle * Math.PI / 360.0);

        updateFactors();

        updateTransformation();
    }

    public void setFrom(Vector3D newFrom) {
        fromPoint.p[0] = newFrom.p[0];
        fromPoint.p[1] = newFrom.p[1];
        fromPoint.p[2] = newFrom.p[2];
        updateTransformation();
    }

    public void setAt(Vector3D newAt) {
        atPoint.p[0] = newAt.p[0];
        atPoint.p[1] = newAt.p[1];
        atPoint.p[2] = newAt.p[2];
        updateTransformation();
    }

    public void setUp(Vector3D newUp) {
        upVector = newUp;
        updateTransformation();
    }

    public Vector3D getFrom() {
        return new Vector3D(fromPoint.p[0], fromPoint.p[1], fromPoint.p[2]);
    }

    public Vector3D getAt() {
        return new Vector3D(atPoint.p[0], atPoint.p[1], atPoint.p[2]);
    }

    public Vector3D getUp() {
        return new Vector3D(upVector.p[0], upVector.p[1], upVector.p[2]);
    }

    public double getAngle() {
        return viewAngle;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setScreenScale(int x, int y) {
        screenScaleX = x;
        screenScaleY = y;
        screenScaleXHalf = (double)screenScaleX / 2.0;
        screenScaleYHalf = (double)screenScaleY / 2.0;
		aspectRatio = (double)x / (double)y;
        updateFactors();
    }

    public void setAngle(double newAngle) {
        viewAngle = newAngle;
        if (viewAngle > 179) {
            viewAngle = 179.0;
		} else if (viewAngle < .5) {
            viewAngle = .5;
        }
        tanHalfAngle = Math.tan(viewAngle * Math.PI / 360.0);
        updateFactors();
    }
	
	public void chFocalLength(double percentage) {
        double scaleFactor = percentage / 100.0;
		setAngle(viewAngle*scaleFactor);
		// zoom(100.0/scaleFactor);
	}

	public void vertigo(double percentage) {
		double t=tanHalfAngle;
		chFocalLength(percentage);
		if(t!=tanHalfAngle){
			if(!zoom(10000.0/percentage)){
				tanHalfAngle=t;
				updateFactors();
			}
		}
	}

    public boolean zoom(double percentage) {
        double scaleFactor = percentage / 100.0;
        boolean validScale = false;
        Vector3D lineOfSight = atPoint.sub(fromPoint);

        if (percentage < 100) {
            if (lineOfSight.getLength() > .0000000001) {
                validScale = true;
            }
        } else {
            if (lineOfSight.getLength() < 1000000000) {
                validScale = true;
            }
        }
        if (validScale) {
            // Zoom - ((F - A) * scale) + A
            fromPoint.decBy(atPoint);
            fromPoint.scalarMultiplyBy(scaleFactor);
            fromPoint.incBy(atPoint);
			updateTransformation();
			return true;
        }
		return false;
    }

    public void translate(double rightPercent, double upPercent) {
        Vector3D VectorLoS = atPoint.sub(fromPoint);

        Vector3D horizon = upVector.cross(VectorLoS);
        Vector3D centerLine = VectorLoS.cross(horizon);

        Vector3D offsets = (horizon.scalarMultiply(rightPercent)).add(centerLine.scalarMultiply(upPercent));

        atPoint.incBy(offsets);
        fromPoint.incBy(offsets);

        updateTransformation();
    }

    public void rotate(double upDegrees, double rightDegrees) {

        fromPoint.decBy(atPoint);
		Vector3D horizon = upVector.cross(fromPoint);

        if (upDegrees != 0) {
            fromPoint = fromPoint.rotate(horizon, upDegrees);
            upVector = upVector.rotate(horizon, upDegrees);
        }

        if (rightDegrees != 0) {
			Vector3D centerLine = horizon.cross(fromPoint);
            fromPoint = fromPoint.rotate(centerLine, rightDegrees);
            upVector = upVector.rotate(centerLine, rightDegrees);
        }

        fromPoint.incBy(atPoint);

        updateTransformation();
    }

    public Vector3D WorldToEye(Vector3D worldPoint) {
        // World point - F
        Vector3D factors = worldPoint.sub(fromPoint);
	
        return new Vector3D(factors.dot(aRow),
                              factors.dot(bRow),
                              factors.dot(cRow));
    }

    public Vector3D EyeToWorld(Vector3D eyePoint) {
	// eyePoint * V^T
	return new Vector3D((eyePoint.p[0] * aRow.p[0] +
 					     eyePoint.p[1] * bRow.p[0] +
					     eyePoint.p[2] * cRow.p[0]),
					    (eyePoint.p[0] * aRow.p[1] +
					     eyePoint.p[1] * bRow.p[1] +
					     eyePoint.p[2] * cRow.p[1]),
					    (eyePoint.p[0] * aRow.p[2] +
		                 eyePoint.p[1] * bRow.p[2] +
                         eyePoint.p[2] * cRow.p[2]));
    }

    public Vector3D EyeTo2D(Vector3D toConvert) {
        return new Vector3D((factor2DX * toConvert.p[0] / toConvert.p[2]) + screenScaleXHalf, 
		              (factor2DY * toConvert.p[1] / toConvert.p[2]) + screenScaleYHalf,
			      toConvert.p[2]);
    }

    public Vector3D WorldTo2D(Vector3D worldPoint) {
		// World point - F
        Vector3D factors = worldPoint.sub(fromPoint);
	
        double z = factors.dot(cRow);
        if (z == 0.0) {
            System.err.println("Divide by zero avoided in WorldTo2D()");
            z = Float.MIN_VALUE;
        }
        Vector3D ret= new Vector3D((factor2DX * factors.dot(aRow) / z) + screenScaleXHalf, 
							(factor2DY * factors.dot(bRow) / z) + screenScaleYHalf,z-fromPoint.getLength());
		return ret;
    }

    public Vector3D imgToWorld(double x, double y,double z) {
		return imgToWorldCenteredInEye(x,y,z).add(fromPoint);
    }

    public Vector3D imgToWorldCenteredInEye(double x, double y,double z) {
		Vector3D eyePoint=new Vector3D((x-screenScaleXHalf)/factor2DX*z,(y-screenScaleYHalf)/factor2DY*z,z);
		Vector3D worldPoint=EyeToWorld(eyePoint);
		return worldPoint;
    }

    public Vector3D EyeToClip(Vector3D toConvert) {
        return new Vector3D(toConvert.p[0] / (tanHalfAngle * aspectRatio),
                              toConvert.p[1] / tanHalfAngle,
                              toConvert.p[2]);
    }

    public Vector3D WorldToClip(Vector3D worldPoint) {
        Vector3D factors = worldPoint.sub(fromPoint);
	
        return new Vector3D(factors.dot(aRow) / (tanHalfAngle * aspectRatio),
                              factors.dot(bRow) / tanHalfAngle,
                              factors.dot(cRow));
    }

    public Vector3D ClipTo2D(Vector3D toConvert) {
        return new Vector3D((screenScaleXHalf * toConvert.p[0] / toConvert.p[2]) + screenScaleXHalf,
                              (-screenScaleYHalf * toConvert.p[1] / toConvert.p[2]) + screenScaleYHalf,
                              toConvert.p[2]-fromPoint.dist(atPoint));
    }

	public Vector3D clipToPoint(Vector3D startPoint) {
		Vector3D result=new Vector3D();
		if(clipToPoint(startPoint,result))
			return result;
		else
			return null;
    }

	public boolean clipToPoint(Vector3D startPoint,Vector3D result) {
        double[] f={startPoint.p[0]-fromPoint.p[0],startPoint.p[1]-fromPoint.p[1],startPoint.p[2]-fromPoint.p[2]};
		double[] s={(f[0]*aRow.p[0]+f[1]*aRow.p[1]+f[2]*aRow.p[2])/tanHalfAngle/aspectRatio,(f[0]*bRow.p[0]+f[1]*bRow.p[1]+f[2]*bRow.p[2])/tanHalfAngle,(f[0]*cRow.p[0]+f[1]*cRow.p[1]+f[2]*cRow.p[2])};
		double szz=s[2]*s[2];
        if((s[0]*s[0]>szz)||(s[1]*s[1]>szz)||(s[2]<0)) {
			return false;
        }
        result.set((screenScaleXHalf * s[0]/s[2]) + screenScaleXHalf,(-screenScaleYHalf * s[1]/s[2]) + screenScaleYHalf,s[2]);
		return true;
    }

	public double getPointToProjPlaneDist(Vector3D startPoint) {
		return (startPoint.p[0]-fromPoint.p[0])*cRow.p[0]+(startPoint.p[1]-fromPoint.p[1])*cRow.p[1]+(startPoint.p[2]-fromPoint.p[2])*cRow.p[2];
    }

    private void updateTransformation() {
		fromAtDist=fromPoint.dist(atPoint);
        cRow = atPoint.sub(fromPoint);
        aRow = cRow.cross(upVector);
        bRow = aRow.cross(cRow);
        aRow.normalize();
        bRow.normalize();
        cRow.normalize();
    }

    private void updateFactors() {
        factor2DX = (double)screenScaleX/(2.0 * aspectRatio * tanHalfAngle);
        factor2DY = (double)-screenScaleY/(2.0 * tanHalfAngle);
    }
}