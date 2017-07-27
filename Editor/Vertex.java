package Points2Map;
import java.util.*;

public class Vertex extends Vector3D implements Comparable<Vertex>{

    private static Random generator = new Random();
    
	public int pos;
	public boolean active=true;
	public final Mesh mesh;
	private Vector3D normal;

	public Vertex(Mesh m) {
		mesh=m;
    }

    public Vertex(Mesh m, int i) {
		this(m);
		pos=i;
    }
	
	public Vertex(Mesh m, Vertex v) {
		super(v);
		mesh=m;
    }

    public Vertex(Mesh m,double x, double y, double z) {
        super(x,y,z);
		mesh=m;
    }

    public Vertex(Mesh m, double x, double y, double z, int i) {
        this(m,x,y,z);
		pos=i;
    }
	
	public void setNormal(Vector3D n) {
		normal=n;
	}
	
	public Vector3D getNormal() {
		return normal;
	}
	
	public int compareTo( Vertex v ) {
		int ret=0;
		if(p[0]<v.p[0])
			ret=-1;
		else if(p[0]>v.p[0])
			ret=1;
		else
			if(p[1]<v.p[1])
				ret=-1;
			else if(p[1]>v.p[1])
				ret=1;
			else
				if(p[2]<v.p[2])
					ret=-1;
				else if(p[2]>v.p[2])
					ret=1;
				else
					ret=0;
		if(active ^ v.active)ret=1;
		return ret;
    }

	/* Returns the position of a vertex in the mesh */
	public int getPos() {
		return pos;
	}
	
	/* Sets the position of a vertex in the mesh */
 	public void setPos(int p) {
		pos=p;
	}

	public Vertex merge(Vertex that) {
		if(this==that)return this;
		mesh.remove(this);
		active=false;
		return that;
	}
	
	public String longDescription() {
		String ret="Vertex Nr. "+pos+" ("+p[0]+", "+p[1]+", "+p[2]+")\n";
		return ret;
	}
	
	@Override public String toString() {
		return "Vertex Nr. "+pos+" ("+p[0]+", "+p[1]+", "+p[2]+")\n";
	}

}