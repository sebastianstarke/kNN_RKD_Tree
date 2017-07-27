package Points2Map;

public class Vector3D implements Cloneable {

	/* Declares the datastructe for the vector's coordinates */
    public double[] p=new double[3];
    
	public Vector3D() {
        p[0] = 0;
        p[1] = 0;
        p[2] = 0;
    }

	public Vector3D(Vector3D rhs) {
		this.p[0] = rhs.p[0];
		this.p[1] = rhs.p[1];
		this.p[2] = rhs.p[2];
    }

    public Vector3D(double x, double y, double z) {
        this.p[0] = x;
        this.p[1] = y;
        this.p[2] = z;
    }

    public Vector3D(double[] x) {
        this.p[0] = x[0];
        this.p[1] = x[1];
        this.p[2] = x[2];
    }

    public Vector3D set(double x, double y, double z) {
        this.p[0] = x;
        this.p[1] = y;
        this.p[2] = z;
		return this;
    }

    public Vector3D set(Vector3D rhs) {
		this.p[0] = rhs.p[0];
		this.p[1] = rhs.p[1];
		this.p[2] = rhs.p[2];
		return this;
    }

    public Vector3D setMin(Vector3D rhs) {
		this.p[0] = Math.min(this.p[0],rhs.p[0]);
		this.p[1] = Math.min(this.p[1],rhs.p[1]);
		this.p[2] = Math.min(this.p[2],rhs.p[2]);
		return this;
    }

    public Vector3D setMax(Vector3D rhs) {
		this.p[0] = Math.max(this.p[0],rhs.p[0]);
		this.p[1] = Math.max(this.p[1],rhs.p[1]);
		this.p[2] = Math.max(this.p[2],rhs.p[2]);
		return this;
    }

    public Vector3D add(Vector3D rhs) {
        return (new Vector3D(p[0] + rhs.p[0], p[1] + rhs.p[1], p[2] + rhs.p[2]));
    }

    public void incBy(Vector3D rhs) {
        this.p[0] += rhs.p[0];
        this.p[1] += rhs.p[1];
        this.p[2] += rhs.p[2];
    }

    public void incBy(double dx, double dy, double dz) {
        this.p[0] += dx;
        this.p[1] += dy;
        this.p[2] += dz;
    }

    public Vector3D sub(Vector3D rhs) {
        return (new Vector3D(p[0] - rhs.p[0], p[1] - rhs.p[1], p[2] - rhs.p[2]));
    }

    public void decBy(Vector3D rhs) {
        this.p[0] -= rhs.p[0];
        this.p[1] -= rhs.p[1];
        this.p[2] -= rhs.p[2];
    }

    public void decBy(double dx, double dy, double dz) {
        this.p[0] -= dx;
        this.p[1] -= dy;
        this.p[2] -= dz;
    }

    public Vector3D scalarMultiply(double scalar) {
        return (new Vector3D(p[0] * scalar, p[1] * scalar, p[2] * scalar));
    }

    public void scalarMultiplyBy(double scalar) {
        p[0] *= scalar;
        p[1] *= scalar;
        p[2] *= scalar;
    }

    public Vector3D scalarDivide(double scalar) {
        return (new Vector3D(p[0] / scalar, p[1] / scalar, p[2] / scalar));
    }

    public void scalarDivideBy(double scalar) {
        p[0] /= scalar;
        p[1] /= scalar;
        p[2] /= scalar;
    }

    public Vector3D cross(Vector3D rhs) {
        Vector3D rval = new Vector3D();
		rval.p[0] = (p[1] * rhs.p[2]) - (p[2] * rhs.p[1]);
		rval.p[1] = (p[2] * rhs.p[0]) - (p[0] * rhs.p[2]);
		rval.p[2] = (p[0] * rhs.p[1]) - (p[1] * rhs.p[0]);
        return rval;
    }

    public void crossBy(Vector3D rhs) {
		set((p[1] * rhs.p[2]) - (p[2] * rhs.p[1]),(p[2] * rhs.p[0]) - (p[0] * rhs.p[2]),(p[0] * rhs.p[1]) - (p[1] * rhs.p[0]));
    }

	public void square() {
		p[0]*=p[0];
		p[1]*=p[1];
		p[2]*=p[2];
	}

	public Vector3D getSquare() {
		Vector3D ret=new Vector3D(this);
		ret.square();
		return ret;
	}
	
    public double getSum() {
        return (p[0]  + p[1] + p[2]);
    }

    public double getSquaredLength() {
        return ((p[0] * p[0]) + (p[1] * p[1]) + (p[2] * p[2]));
    }

    public double getLength() {
        return Math.sqrt(getSquaredLength());
    }

    /* Computes the squared euclidean distance between two vectors */
    public double squaredDist(Vector3D that) {
        double dx=p[0]-that.p[0];
        double dy=p[1]-that.p[1];
        double dz=p[2]-that.p[2];
		return (dx * dx) + (dy * dy) + (dz * dz);
    }

    /* Computes the euclidean distance between two vectors */
    public double dist(Vector3D that) {
		return Math.sqrt(squaredDist(that));
    }


    public double dot(Vector3D that) {
		return (p[0] * that.p[0] + p[1] * that.p[1] + p[2] * that.p[2]);
    }

    public void normalize() {
       double length = getLength();
       p[0] /= length;
       p[1] /= length;
       p[2] /= length;
    }

    public void rotateX(double degrees) {
        double radians = degrees * Math.PI / 180.0;
        double cosAngle = Math.cos(radians);
        double sinAngle = Math.sin(radians);
        double origY = p[1];
        p[1] =	p[1] * cosAngle - p[2] * sinAngle;
        p[2] = origY * sinAngle + p[2] * cosAngle;
    }

    public void rotateY(double degrees) {
        double radians = degrees * Math.PI / 180.0;
        double cosAngle = Math.cos(radians);
        double sinAngle = Math.sin(radians);
        double origX = p[0];
        p[0] =	p[0] * cosAngle + p[2] * sinAngle;
        p[2] = p[2] * cosAngle - origX * sinAngle;
    }

    public void rotateZ(double degrees) {
        double radians = degrees * Math.PI / 180.0;
        double cosAngle = Math.cos(radians);
        double sinAngle = Math.sin(radians);
        double origX = p[0];
        p[0] =	p[0] * cosAngle - p[1] * sinAngle;
        p[1] = origX * sinAngle + p[1] * cosAngle;
    }

    public Vector3D rotate(Vector3D axis, double degrees) {
		double radians = degrees * Math.PI / 180.0;
		double cosAngle = Math.cos(radians);
		double sinAngle = Math.sin(radians);

		Vector3D ret = new Vector3D();
		Vector3D w = new Vector3D(axis);
		w.normalize();
		Vector3D vCrossW = this.cross(w);
		w.scalarMultiplyBy(this.dot(w));

		ret.p[0] = w.p[0] + (this.p[0] - w.p[0]) * cosAngle - vCrossW.p[0] * sinAngle;
		ret.p[1] = w.p[1] + (this.p[1] - w.p[1]) * cosAngle - vCrossW.p[1] * sinAngle;
		ret.p[2] = w.p[2] + (this.p[2] - w.p[2]) * cosAngle - vCrossW.p[2] * sinAngle;

        return ret;
    }
	
	public void projectToLine(Vector3D p1, Vector3D p2) {
		double maxSqrLength=p2.squaredDist(p1);
		double scalarProd=0.0;
		if(maxSqrLength!=0)
			scalarProd=((p[0]-p1.p[0])*(p2.p[0]-p1.p[0])+(p[1]-p1.p[1])*(p2.p[1]-p1.p[1])+(p[2]-p1.p[2])*(p2.p[2]-p1.p[2]))/maxSqrLength;
//		if (scalarProd<0.0) scalarProd=0.0;
//		if (scalarProd>1.0) scalarProd=1.0;
		this.p[0]=scalarProd*p1.p[0]+(1.0-scalarProd)*p2.p[0];
		this.p[1]=scalarProd*p1.p[1]+(1.0-scalarProd)*p2.p[1];
		this.p[2]=scalarProd*p1.p[2]+(1.0-scalarProd)*p2.p[2];
	}
	
	@Override public String toString() {
		return "("+p[0]+", "+p[1]+", "+p[2]+")";
	}
}