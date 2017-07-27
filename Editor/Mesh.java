package Points2Map;

import java.awt.LinearGradientPaint;
import java.awt.Panel;
import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.util.*;

import javax.lang.model.type.NoType;

public class Mesh extends AbstractList{

    public Vertex[] vertices;
	public byte[][] colors;
	
	/* k-Nearest-Neighbors */
	public MaxHeap[] kNearestNeighbors;
	
	/* Evaluation functions and variables for added neighbors */
	kNNevaluation kNNevaluationFunctions = new kNNevaluation();
	private Double addedNeighbors=0.0, notAddedNeighbors=0.0, addedNeighborsRound=0.0, notAddedNeighborsRound=0.0;
	
    private int numVertices, totalNumVertices;
	private Vector3D zVec=new Vector3D(0,0,1);
	private Vector3D boxMin=new Vector3D(0,0,0);
	private Vector3D boxMax=new Vector3D(0,0,0);
	private Vector3D boxMean=new Vector3D(0,0,0);
	private double maxRad;
    private Random generator = new Random();
	public double bound=Double.MAX_VALUE;
	
	/* Variables for QuickSort */
	int[] indexNeighbors_tmp;
	int indexNeighbor_swap;
	double[] sqDists_tmp;
	double sqDist_swap;
	
	
    public Mesh() {
		numVertices = 0;
		totalNumVertices = 0;
		vertices = new Vertex[8];
    }

	public double getMaxRadius() {
		return maxRad;
	}

	public Vector3D getMin() {
		return boxMin;
	}
	
	public Vector3D getMax() {
		return boxMax;
	}
	
	public Vector3D getMean() {
		return boxMean;
	}
	
	public int getNumVertices() {
		return numVertices;
	}
	
	public int size(){//AbstractList
		return getNumVertices();
	}

	public int getTotalNumVertices() {
		return totalNumVertices;
	}

	public Vertex getVertex(int i) {
		Vertex v=null;
		if(totalNumVertices>i) v=vertices[i];
		return v;
	}
	
	public Object get(int i) {//AbstractList
		return getVertex(i);
	}
	
	public Vertex getRndVertex() {
		return vertices[generator.nextInt(numVertices)];
	}
	
	public Vertex getRndTotalVertex() {
		return vertices[generator.nextInt(totalNumVertices)];
	}
	
    public void ensureVertexNr(int vnr) {
		if(vertices.length<vnr) {
			Vertex[] newv = new Vertex[vnr];
			System.arraycopy(vertices,0,newv,0,totalNumVertices);
			vertices=newv;
			if(colors!=null){
				byte[][] newc=new byte[vnr][];
				System.arraycopy(colors,0,newc,0,totalNumVertices);
				colors=newc;
			}
		}
    }

	public Vertex addVertex(Vertex v) {
		if(totalNumVertices==vertices.length)
			ensureVertexNr((int)(totalNumVertices*1.1+1));
		vertices[totalNumVertices]=v;
		if(totalNumVertices!=numVertices) {
			swap(totalNumVertices,numVertices);
		}
		totalNumVertices++;
		numVertices++;
		return v;
	}

	public Vertex addVertex(double x, double y, double z) {
		return addVertex(new Vertex(this,x,y,z));
	}
	
	public Vertex addVertex(double x, double y, double z, int r,int g, int b) {
		return addVertex(new Vertex(this,x,y,z),new byte[]{(byte)r,(byte)g,(byte)b});
	}

	public Vertex addVertex(Vertex v, byte[] col) {
		if(totalNumVertices==vertices.length)
			ensureVertexNr((int)(totalNumVertices*1.1+1));
		vertices[totalNumVertices]=v;
		colors[totalNumVertices]=col;
		if(totalNumVertices!=numVertices) {
			swap(totalNumVertices,numVertices);
		}
		totalNumVertices++;
		numVertices++;
		return v;
	}
	
	public void swap(Vertex v1, Vertex v2) {
		int pos1=v1.getPos();
		int pos2=v2.getPos();
		vertices[pos1]=v2;v2.setPos(pos1);
		vertices[pos2]=v1;v1.setPos(pos2);
		
		if(colors!=null){
			byte[] tmp=colors[pos1];
			colors[pos1]=colors[pos2];
			colors[pos2]=tmp;
		}
		
		if(kNearestNeighbors!=null){
			MaxHeap tmp = kNearestNeighbors[pos1];
			kNearestNeighbors[pos1]= kNearestNeighbors[pos2];
			kNearestNeighbors[pos2]= tmp;
		}
		
	}

	public void swap(int pos1, int pos2) {
		Vertex v1=vertices[pos1];
		Vertex v2=vertices[pos2];
		vertices[pos1]=v2;v2.setPos(pos1);
		vertices[pos2]=v1;v1.setPos(pos2);
		
		if(colors!=null){
			byte[] tmp=colors[pos1];
			colors[pos1]=colors[pos2];
			colors[pos2]=tmp;
		}
		
		if(kNearestNeighbors!=null){
			MaxHeap tmp=kNearestNeighbors[pos1];
			kNearestNeighbors[pos1]=kNearestNeighbors[pos2];
			kNearestNeighbors[pos2]=tmp;
		}
	}
	
	public boolean add(Object v) {//AbstractList
		if(v instanceof Vertex) {
			Vertex v2=addVertex((Vertex)v);
			return(v2!=null);
		} else {
			return false;
		}
	}

	public void add(int index, Object v) {//AbstractList
		if(v instanceof Vertex) {
			swap(addVertex((Vertex)v),getVertex(index));
		}
	}

	public Object set(int index, Object v) {//AbstractList
		if(index>=totalNumVertices) return null;
		if(v instanceof Vertex) {
			Vertex v2=vertices[index];
			Vertex v1=(Vertex)v;
			vertices[index]=v1;
			v1.setPos(index);
			return v2;
		} else {
			return null;
		}
	}
	
	public void remove(Vertex v) {
		if(!v.active)return;
		int pos=v.getPos();
		if(pos<numVertices){
			numVertices--;
			swap(pos,numVertices);
		}
		v.active=false;
	}

	public void markForRemoval(Vertex v) {
		v.active=false;
	}

	public synchronized static Mesh read(BufferedReader in, long size) {
		Mesh m=new Mesh();
		Vector3D mean=new Vector3D();
		String readString="";
        try {
            int inPoints = 0;
            StringTokenizer st;

			long actPos=0;
			long oldPos=0;
            readString = in.readLine().trim(); // Remove the 'OFF'
			actPos+=readString.length()+1;
            if ((!readString.equals("OFF"))&&(!readString.equals("COFF"))) {  // No off. So maybe an obj/pts/dat file...
				int i=0;
				System.out.println("reading points in obj/pts/dat file______");
				Vector3D min=null;
				Vector3D max=null;
				while ((readString = in.readLine()) != null)  {
					actPos+=readString.length()+1;
					if(readString.length()!=0) {
						st = new StringTokenizer(readString);
						int nr=st.countTokens();
						String s1,s2,s3;
						switch(nr) {
							case 0:
							case 1:
								break;
							case 2:
								s1=st.nextToken();
								s2=st.nextToken();
								try {  
									double d1 = Double.parseDouble(s1);  
									double d2 = Double.parseDouble(s2);  
									Vertex v= new Vertex(m,d1,d2,d2);
									if(min==null) min=new Vector3D(v);
									if(max==null) max=new Vector3D(v);
									min.setMin(v);
									max.setMax(v);
									m.addVertex(v);
									mean.incBy(v);
									i++;
								}  
								catch(NumberFormatException nfe) {}
								break;
							case 3:
								s1=st.nextToken();
								s2=st.nextToken();
								s3=st.nextToken();
								try {  
									double d1 = Double.parseDouble(s1);  
									double d2 = Double.parseDouble(s2);  
									double d3 = Double.parseDouble(s3);  
									Vertex v= new Vertex(m,d1,d2,d3);
									if(min==null) min=new Vector3D(v);
									if(max==null) max=new Vector3D(v);
									min.setMin(v);
									max.setMax(v);
									m.addVertex(v);
									mean.incBy(v);
									i++;
								}  
								catch(NumberFormatException nfe){}
								break;
							default:
								String tk=st.nextToken();
								if(tk.equals("f")||tk.equals("F")||tk.equals("#")||tk.equals("vt")||tk.equals("vn")||tk.equals("VT")||tk.equals("VN"))
									break;
								s1=st.nextToken();
								s2=st.nextToken();
								s3=st.nextToken();
								try {
									double d0= Double.parseDouble(tk);
									s3=s2;
									s2=s1;
									s1=tk;
								}catch(NumberFormatException nfe){}
								try {  
									double d1 = Double.parseDouble(s1);  
									double d2 = Double.parseDouble(s2);  
									double d3 = Double.parseDouble(s3);  
									Vertex v= new Vertex(m,d1,d2,d3);
									if(min==null) min=new Vector3D(v);
									if(max==null) max=new Vector3D(v);
									min.setMin(v);
									max.setMax(v);
									m.addVertex(v);
									mean.incBy(v);
									i++;
								}  
								catch(NumberFormatException nfe){}
								break;
						}
					}
					int progress=(int)(actPos*40/size-oldPos*40/size);
					for(;progress>0;progress--)
						System.out.print(".");
					oldPos=actPos;
				}
				System.out.println();
				System.out.println("Points: "+m.numVertices);
				System.out.println("Min: "+min);
				System.out.println("Max: "+max);
				m.boxMin=min;
				m.boxMax=max;
            } else {
				boolean hasCol=readString.equals("COFF");
				if(hasCol){
					m.colors=new byte[m.vertices.length][];
				}
				readString = in.readLine();
				st = new StringTokenizer(readString);
				
				inPoints   = Integer.parseInt(st.nextToken());
				int inPolygons = Integer.parseInt(st.nextToken());

				if ((inPoints > 0) && (inPolygons >= 0)) {
					m.ensureVertexNr(inPoints);
				} else return null;

				Vector3D min=null;
				Vector3D max=null;

				System.out.println("Loading Points_________________________");
				for (int i = 0;i < inPoints;i++) {
					readString=in.readLine();
					readString = readString.trim();
					while ((readString.length() < 1)||(readString.charAt(0)=='#')) {
						readString = in.readLine();
						readString = readString.trim();
					}
					st = new StringTokenizer(readString);
					Vertex v= new Vertex(m,Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),i);

					if(min==null) min=new Vector3D(v);
					if(max==null) max=new Vector3D(v);
					min.setMin(v);
					max.setMax(v);
					
					if(hasCol){
						byte[] col= {(byte)Integer.parseInt(st.nextToken()),(byte)Integer.parseInt(st.nextToken()),(byte)Integer.parseInt(st.nextToken())};
						m.addVertex(v,col);
					} else {
						m.addVertex(v);					
					}
					mean.incBy(v);

					if((int)(i*40/inPoints)>(int)((i-1)*40/inPoints)) System.out.print(".");
				}
				System.out.println();
				System.out.println("Points: "+m.numVertices);
				System.out.println("Min: "+min);
				System.out.println("Max: "+max);
				m.boxMin=min;
				m.boxMax=max;
			}
        } catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
        }
		mean.scalarDivideBy(m.getNumVertices());
		m.boxMean=mean;
		m.maxRad=Math.max(m.boxMin.dist(mean),m.boxMax.dist(mean));
		
		long timer;
		
		System.out.println("");	
		System.out.println("Start computing normals...");
        timer=System.currentTimeMillis();
		m.guessNormals(10);
		System.out.println("Finished computing normals in "+(System.currentTimeMillis()-timer)+"msec.");
		
        return m;
	}

    public synchronized boolean write(PrintWriter out) {

        boolean rval = true;

        try {
            out.println("OFF");
            out.println(numVertices + " 0 0");
            for (int i = 0; i < numVertices; i++) {
                out.println(vertices[i].p[0] + " " + vertices[i].p[1] + " " + vertices[i].p[2]);
            }
        } catch (Exception e) {
			e.printStackTrace();
            rval = false;
        }
        return rval;
    }

	
public synchronized void reduceResolution(double nrPixelPerUnit) {
		double[][] map;
		int[] colV=new int[4];
		int[] colV2=new int[4];
		Vector3D min=getMin();
		Vector3D max=getMax();
		int sizex=(int)((max.p[0]-min.p[0])*nrPixelPerUnit+3);
		int sizey=(int)((max.p[1]-min.p[1])*nrPixelPerUnit+3);
		double sizez=max.p[2]-min.p[2];

		System.out.println("Reducing Resolution____________________");
        map = new double[sizex][sizey];

		for(int i=0;i<getNumVertices();i++) {
			Vertex v=getVertex(i);
			int x=(int)((v.p[0]-min.p[0])*nrPixelPerUnit)+1;
			int y=(int)((v.p[1]-min.p[1])*nrPixelPerUnit)+1;
			double z=(v.p[2]-max.p[2]-1);
			double z2=map[x][y];
			if(z<z2) map[x][y]=z;
			if((int)(i*20/getNumVertices())>(int)((i-1)*20/getNumVertices())) System.out.print(".");
		}
		for(int i=getNumVertices()-1;i>=0;i--) {
			Vertex v=getVertex(i);
			int x=(int)((v.p[0]-min.p[0])*nrPixelPerUnit)+1;
			int y=(int)((v.p[1]-min.p[1])*nrPixelPerUnit)+1;
			double z=(v.p[2]-max.p[2]-1);
			
			double z2=map[x][y]; if(z>z2) remove(v);
			if((int)(i*20/getNumVertices())>(int)((i-1)*20/getNumVertices())) System.out.print(".");
		}
		System.out.println();
	}

	public  synchronized void extractGround(double nrPixelPerUnit) {
		double[][] map;
		int[] colV=new int[4];
		int[] colV2=new int[4];
		Vector3D min=getMin();
		Vector3D max=getMax();
		int sizex=(int)((max.p[0]-min.p[0])*nrPixelPerUnit+3);
		int sizey=(int)((max.p[1]-min.p[1])*nrPixelPerUnit+3);
		double sizez=max.p[2]-min.p[2];

		System.out.println("Extracting Ground Data_________________");
        map = new double[sizex][sizey];

		for(int i=0;i<getNumVertices();i++) {
			Vertex v=getVertex(i);
			int x=(int)((v.p[0]-min.p[0])*nrPixelPerUnit)+1;
			int y=(int)((v.p[1]-min.p[1])*nrPixelPerUnit)+1;
			double z=(v.p[2]-max.p[2]-1);
			double z2=map[x][y];
			if(z<z2) map[x][y]=z;
			if((int)(i*20/getNumVertices())>(int)((i-1)*20/getNumVertices())) System.out.print(".");
		}
		int num=getNumVertices();
		for(int i=num-1;i>=0;i--) {
			Vertex v=getVertex(i);
			int x=(int)((v.p[0]-min.p[0])*nrPixelPerUnit)+1;
			int y=(int)((v.p[1]-min.p[1])*nrPixelPerUnit)+1;
			double z=(v.p[2]-max.p[2]-1);
			
			double z2=map[x][y]; if(z>z2+1/nrPixelPerUnit) remove(v);
			z2=map[x-1][y]; if(z>z2+2/nrPixelPerUnit) remove(v);
			z2=map[x+1][y]; if(z>z2+2/nrPixelPerUnit) remove(v);
			z2=map[x][y-1]; if(z>z2+2/nrPixelPerUnit) remove(v);
			z2=map[x][y+1]; if(z>z2+2/nrPixelPerUnit) remove(v);
			z2=map[x-1][y-1]; if(z>z2+2.5/nrPixelPerUnit) remove(v);
			z2=map[x+1][y+1]; if(z>z2+2.5/nrPixelPerUnit) remove(v);
			if((int)(i*20/num)>(int)((i-1)*20/num)) System.out.print(".");
		}
		System.out.println();
	}
	
	// public void kdtreesort(int s,int e,int stepsize, Vector3D mn,Vector3D mx) {
		// if(e-s<=1) return;
		// int dim=0;
		// double diff=mx.p[0]-mn.p[0];
		// double diffy=mx.p[1]-mn.p[1];
		// double diffz=mx.p[2]-mn.p[2];
		// if(diffy>diff) {
			// dim=1;
			// diff=diffy;
		// }
		// if(diffz>diff) {
			// dim=2;
			// diff=diffz;
		// }
		
		// if(e-s<=2) {
			// if(vertices[s].p[dim]<vertices[s+stepsize].p[dim]) {
				// swap(s,s+stepsize);
				// return;
			// }
		// }
		
		// median(s,e,stepsize,dim);
		
		// Vector3D mx2=new Vector3D(mx);
		// mx2.p[dim]=vertices[s].p[dim];
		// Vector3D mn2=new Vector3D(mn);
		// mn2.p[dim]=vertices[s].p[dim];
		// kdtreesort(s+stepsize  ,e/2    ,stepsize*2,mn ,mx2);
		// kdtreesort(s+stepsize*2,e-e/2-1,stepsize*2,mn2,mx );
	// }
	
	// private void median(int s,int e,int stepsize,int dim) {
		// int R=(e-1-(1-e&1))*stepsize;
		// int L=(e-1-(e&1))*stepsize;
		// int d=-2*stepsize;
		// while (true) {
			// double pivot=vertices[s].p[dim];
			// int j=partition(pivot,s,L,R,d,dim);
			// if((j&stepsize)!=0) {
				// L=j+d;
				// if(L<0)	L=0;
			// } else {
				// if(j!=0) R=j+d; else break;
			// }
		// }
	// }
	
	// private int partition(double pivot,int s,int i,int j,int d,int dim) {
		// int di=d;
		// int dj=d;
		// int jold=j;
		// while(true) {
			// double ai=vertices[i+s].p[dim];
			// while (ai<pivot) {
				// i+=di;
				// if(i<0) {
					// i=0;
					// di=-di;
				// }
				// ai=vertices[i+s].p[dim];
			// }
			// double aj=vertices[j+s].p[dim];
			// while (aj>pivot) {
				// jold=j;
				// j+=dj;
				// aj=vertices[j+s].p[dim];
			// }
			// if((i==j)||(i==jold))
				// break;
			// swap(i+s,j+s);
			// jold=j;
			// if(ai!=pivot){
				// j+=dj;
				// if(j<0) {
					// dj=-dj;
					// j=dj/2;
				// }
			// }else{
				// i+=di;
				// if(i<0){
					// i=0;
					// di=-di;
				// }
			// }
		// }
		// return i;
	// }
	
	public void rndtreenormals(int s,int e,int nrUnsorted,Vector3D mn, Vector3D mx) {
		if(e-s<2*nrUnsorted){
			Vector3D M=new Vector3D();
			for (int j=s;j<e;j++)
				M.incBy(vertices[j]);
			M.scalarDivideBy(e-s);
			double M002=0;double M020=0;double M200=0;
			double M011=0;double M101=0;double M110=0;
			for (int j=s;j<e;j++) {
				Vector3D N=vertices[j].sub(M);
				M002+=N.p[2]*N.p[2];M020+=N.p[1]*N.p[1];M200+=N.p[0]*N.p[0];
				M011+=N.p[1]*N.p[2];M101+=N.p[0]*N.p[2];M110+=N.p[0]*N.p[1];
			}
			double[][] eig=Eigenvector.SolveEigenSystem(M200,M020,M002,M110,M101,M011);
			Vector3D norm=Eigenvector.eigVec3(eig);
			// Vector3D norm=new Vector3D();
			// for (int j=s;j<e-2;j++) {
				// Vector3D n1=vertices[j].sub(vertices[j+1]);
				// n1.scalarMultiplyBy(1/n1.getSquaredLength());
				// Vector3D n2=vertices[j].sub(vertices[j+2]);
				// n2.scalarMultiplyBy(1/n2.getSquaredLength());
				// n2.crossBy(n1);
				// if(norm.dot(n2)>0)
					// norm.incBy(n2);
				// else
					// norm.decBy(n2);
			// }
			norm.normalize();
			norm.scalarDivideBy(1.0+Eigenvector.eigVal3(eig)/(Eigenvector.eigVal1(eig)+Eigenvector.eigVal2(eig)));
			if(norm.dot(zVec)<0)norm.scalarMultiplyBy(-1.0);

			if(Double.isNaN(norm.p[0])||Double.isNaN(norm.p[1])||Double.isNaN(norm.p[2]))norm.set(0,0,0);

			for(int j=s;j<e;j++) {
				Vector3D n=vertices[j].getNormal();
				 if(n==null) {
					vertices[j].setNormal(norm);
				} else {
					if(n.dot(norm)>0) {
						// if(n.getSquaredLength()*2==Double.POSITIVE_INFINITY)
							// n.scalarMultiplyBy(0.25);
						n.incBy(norm);
						if(n.dot(zVec)<0)n.scalarMultiplyBy(-1.0);
					}else{
						n.decBy(norm);
						if(n.dot(zVec)<0)n.scalarMultiplyBy(-1.0);
					}
				}
			}
		} else {
			int dim=0;
			double diff=mx.p[0]-mn.p[0];
			double diffy=mx.p[1]-mn.p[1];
			double diffz=mx.p[2]-mn.p[2];
			if(diffy>diff) {
				dim=1;
				diff=diffy;
			}
			if(diffz>diff) {
				dim=2;
				diff=diffz;
			}
			// dim=(dim+generator.nextInt(2))%3;
			int k=centerpercentile(s,e,dim);
			Vector3D mx2=new Vector3D(mx);
			mx2.p[dim]=vertices[k].p[dim];
			Vector3D mn2=new Vector3D(mn);
			mn2.p[dim]=vertices[k].p[dim];
			rndtreenormals(s,k,nrUnsorted,mn,mx2);
			rndtreenormals(k,e,nrUnsorted,mn2,mx);
		}
}
	
	public void kdtreesort(int s,int e,Vector3D mn,Vector3D mx) {
		if(e-s<=1) return;
		int dim=0;
		double diff=mx.p[0]-mn.p[0];
		double diffy=mx.p[1]-mn.p[1];
		double diffz=mx.p[2]-mn.p[2];
		if(diffy>diff) {
			dim=1;
			diff=diffy;
		}
		if(diffz>diff) {
			dim=2;
			diff=diffz;
		}
		int k=median(s,e,dim);
		
		//swap(s,k);
		
		Vector3D mx2=new Vector3D(mx);
		mx2.p[dim]=vertices[k].p[dim];
		Vector3D mn2=new Vector3D(mn);
		mn2.p[dim]=vertices[k].p[dim];
		kdtreesort(s,k,mn,mx2);
		kdtreesort(k+1,e,mn2,mx);
	}
	
	private int median(int s,int e,int dim) {
		int L=s;
		int R=e-1;
		int k=s+((e-s)>>1);
		while((L!=k)||(R!=k)) {
			double pivot=vertices[k].p[dim];
			int j=partition(pivot,L,R,dim);
			if(j<k)
				L=j+1;
			else if(k<j)
				R=j-1;
			else
				L=R=k;
		}
		return k;
	}
	
	private int centerpercentile(int start,int end,int dim) {
		int L=start;
		int R=end-1;
		int k=start+((end-start)>>1);
		int k1=L+((k-L)>>1);
		int k2=k+((R-k)>>1);
		while(L!=R) {
			int i=generator.nextInt(R+1-L)+L;	
			double pivot=vertices[i].p[dim];
			int j=partition(pivot,L,R,dim);
			if(j<k1)
				L=j+1;
			else if(j>k2)
				R=j-1;
			else
				L=R=j;
		}
		return L;
	}
	
	private int partition(double pivot,int i,int j,int dim) {
		while(true) {
			while (vertices[i].p[dim]<pivot)
				i+=1;

			while (vertices[j].p[dim]>pivot)
				j-=1;
			if(i>=j)
				break;
			swap(i,j);
			if(vertices[i].p[dim]==pivot)
				j--;
			else
				i++;
		}
		return i;
	}

	private class kNN{
		final Vector3D v;
		PriorityQueue<PointWithSqDist> results;
		final int k;
		final double rsq;
		final int minIntervalLength;
		final Vector3D mn;
		final Vector3D mx;
		final Mesh mesh;
		
		private class PointWithSqDist implements Comparable<PointWithSqDist> {
			public int p;
			public double dist;
			public PointWithSqDist(int p,double dist) {
				this.p=p;
				this.dist=dist;
			}
			public int compareTo( PointWithSqDist v ) {
				return Double.compare(v.dist,dist);
			}
		}
		
		public kNN(Mesh mesh, Vector3D v0, int k,double rsq,int minIntervalLength) {
			this.mesh=mesh;
			if(k<1) k=1;
			if(rsq<0) rsq=0;
			if(minIntervalLength<2) minIntervalLength=2;
			this.v=v0;
			this.k=k;
			this.rsq=rsq*rsq;
			this.minIntervalLength=minIntervalLength;
			this.mn=mesh.boxMin;
			this.mx=mesh.boxMax;
			results=new PriorityQueue<PointWithSqDist>(10);
		}
		
		public kNN(Mesh m, Vector3D v,int k, double rsq) {
			this(m,v,k,rsq,64);
		}
		
		public kNN(Mesh m, Vector3D v,int k, int minIntervalLength) {
			this(m,v,k,Double.POSITIVE_INFINITY,minIntervalLength);
		}
		
		public kNN(Mesh m, Vector3D v,double rsq, int minIntervalLength) {
			this(m,v,Integer.MAX_VALUE,rsq,minIntervalLength);
		}
		
		public kNN(Mesh m, Vector3D v,int k) {
			this(m,v,k,Double.POSITIVE_INFINITY,64);
		}
		
		public kNN(Mesh m, Vector3D v,double rsq) {
			this(m,v,Integer.MAX_VALUE,rsq,64);
		}
		
		public void addInitialNeighbors(int[] n) {
			for(int i=0;i>n.length;i++)
				if(v.squaredDist(vertices[i])<=rsq)
					results.add(new PointWithSqDist(i,v.squaredDist(vertices[i])));
		}
		
		public int[] getNeighbors() {
			getNeighbors(0,mesh.getNumVertices(),mn,mx);
			int[] ret=new int[results.size()];
			for(int i=results.size()-1;i>=0;i--)
				ret[i]=((PointWithSqDist)(results.poll())).p;
			return ret;
		}

		private void getNeighbors(int l, int r,Vector3D mn,Vector3D mx) {
			if(r-l<=minIntervalLength){
				double dpq=rsq;
				if(results.size()>0)
					dpq=Math.min(dpq,((PointWithSqDist)(results.peek())).dist);
				for(int i=l;i<r;i++) {
					double di=v.squaredDist(vertices[i]);
					if(di<=dpq)
						results.add(new PointWithSqDist(i,di));
				}
				while(results.size()>k)
					results.poll();
				return;
			}
			int m=((r-l)>>1)+l;
			Vector3D vm=vertices[m];
			double dm=v.squaredDist(vm);
			double dpq=rsq;
			if(results.size()>0)
				dpq=Math.min(dpq,((PointWithSqDist)(results.peek())).dist);
			if(dm<dpq){
				results.add(new PointWithSqDist(m,dm));
				if(results.size()>k)
					results.poll();
			}
			int dim=0;
			double diff=mx.p[0]-mn.p[0];
			double diffy=mx.p[1]-mn.p[1];
			double diffz=mx.p[2]-mn.p[2];
			if(diffy>diff) {
				dim=1;
				diff=diffy;
			}
			if(diffz>diff) {
				dim=2;
				diff=diffz;
			}
			if(v.p[dim]<vm.p[dim]){
				Vector3D mx2=new Vector3D(mx);
				mx2.p[dim]=vm.p[dim];
				getNeighbors(l+1,m+1,mn,mx2);
				double d=rsq;
				if(results.size()>0)
					d=Math.min(d,((PointWithSqDist)(results.peek())).dist);
				dm=vm.p[dim]-v.p[dim];
				if(dm*dm<=d){
					Vector3D mn2=new Vector3D(mn);
					mn2.p[dim]=vm.p[dim];
					getNeighbors(m+1,r,mn2,mx);
				}
			}else{
				Vector3D mn2=new Vector3D(mn);
				mn2.p[dim]=vm.p[dim];
				getNeighbors(m+1,r,mn2,mx);
				double d=rsq;
				if(results.size()>0)
					d=Math.min(d,((PointWithSqDist)(results.peek())).dist);
				dm=vm.p[dim]-v.p[dim];
				if(dm*dm<=d){
					Vector3D mx2=new Vector3D(mx);
					mx2.p[dim]=vm.p[dim];
					getNeighbors(l+1,m+1,mn,mx2);
				}
			}
		}
	}
/*
	private class kNN{
		final Vector3D v;
		PriorityQueue<PointWithSqDist> results;
		PriorityQueue<Subbranch> visit;
		final int k;
		double rsq;
		final int minIntervalLength;
		final Vector3D mn;
		final Vector3D mx;
		final Mesh mesh;
		
		private class PointWithSqDist implements Comparable<PointWithSqDist> {
			public int p;
			public double dist;
			
			public PointWithSqDist(int p,double dist) {
				this.p=p;
				this.dist=dist;
			}
			public int compareTo( PointWithSqDist v ) {
				return Double.compare(v.dist,dist);
			}
		}

		private class Subbranch implements Comparable<Subbranch> {
			public int l;
			public int r;
			public double dist;
			public Vector3D dists;
			public Vector3D mn;
			public Vector3D mx;
			public Subbranch(int l,int r,double dist,Vector3D dists,Vector3D mn, Vector3D mx) {
				this.l=l;
				this.r=r;
				this.dist=dist;
				this.dists=dists;
				this.mn=mn;
				this.mx=mx;
			}
			public int compareTo( Subbranch v ) {
				return Double.compare(dist,v.dist);
			}
		}
		
		public kNN(Mesh mesh, Vector3D v0, int k,double rsq,int minIntervalLength) {
			this.mesh=mesh;
			if(k<1) k=1;
			if(rsq<0) rsq=0;
			if(minIntervalLength<2) minIntervalLength=2;
			this.v=v0;
			this.k=k;
			this.rsq=rsq*rsq;
			this.minIntervalLength=minIntervalLength;
			this.mn=mesh.boxMin;
			this.mx=mesh.boxMax;
			results=new PriorityQueue<PointWithSqDist>(k);
		}
		
		public kNN(Mesh m, Vector3D v,int k, double rsq) {
			this(m,v,k,rsq,64);
		}
		
		public kNN(Mesh m, Vector3D v,int k, int minIntervalLength) {
			this(m,v,k,Double.POSITIVE_INFINITY,minIntervalLength);
		}
		
		public kNN(Mesh m, Vector3D v,double rsq, int minIntervalLength) {
			this(m,v,Integer.MAX_VALUE,rsq,minIntervalLength);
		}
		
		public kNN(Mesh m, Vector3D v,int k) {
			this(m,v,k,Double.POSITIVE_INFINITY,64);
		}
		
		public kNN(Mesh m, Vector3D v,double rsq) {
			this(m,v,Integer.MAX_VALUE,rsq,64);
		}
		
		public int[] getNeighbors() {
			results.clear();
			getNeighbors(0,mesh.getNumVertices(),mn,mx);
			int[] ret=new int[results.size()];
			for(int i=results.size()-1;i>=0;i--)
				ret[i]=((PointWithSqDist)(results.poll())).p;
			return ret;
		}

		private void getNeighbors(int l, int r,Vector3D mn,Vector3D mx) {
			visit=new PriorityQueue<Subbranch>();
			Vector3D dists=new Vector3D();
			dists.setMax(v.sub(mx)).setMax(mn.sub(v));
			dists.square();
			double mindist=dists.getSum();
			// for(int i=1;i<3;i++) {
				// dists.p[i]=Math.max(0.0,Math.max(v.p[i]-mx.p[i],mn.p[i]-v.p[i]));
				// dists[i]*=dists[i];
				// mindist+=dists[i];
			// }
			visit.add(new Subbranch(l,r,mindist,dists,mn,mx));
			
			while(visit.size()>0) {
				Subbranch s=(Subbranch)(visit.poll());
				mindist=s.dist;
				dists=s.dists;
				l=s.l;
				r=s.r;
				mn=s.mn;
				mx=s.mx;
				if(r-l<=minIntervalLength){
					double dpq=rsq;
					if(results.size()>0)
						dpq=Math.min(dpq,((PointWithSqDist)(results.peek())).dist);
					for(int i=l;i<r;i++) {
						double di=v.squaredDist(vertices[i]);
						if(di<dpq)
							results.add(new PointWithSqDist(i,di));
					}
					while(results.size()>k)
						results.poll();
					if(results.size()==k)
						rsq=((PointWithSqDist)(results.peek())).dist;
				} else {
					if(mindist>rsq) {
						break;
					}
					int m=((r-l)>>1)+l;
					Vector3D vm=vertices[m];
					double dm=v.squaredDist(vm);
					int dim=0;
					double diff=mx.p[0]-mn.p[0];
					double diffy=mx.p[1]-mn.p[1];
					double diffz=mx.p[2]-mn.p[2];
					if(diffy>diff) {
						dim=1;
						diff=diffy;
					}
					if(diffz>diff) {
						dim=2;
						diff=diffz;
					}
					int ln,rn,lf,rf;
					Vector3D mnn,mxn,mnf,mxf;
					diff=vm.p[dim]-v.p[dim];
					int nl,nr,fl,fr;
					if(diff>0){
						nl=l+1;
						nr=m+1;
						fl=m+1;
						fr=r;
						mnn=mn;
						mxn=new Vector3D(mx);
						mxn.p[dim]=vm.p[dim];
						mnf=new Vector3D(mn);
						mnf.p[dim]=vm.p[dim];
						mxf=mx;
					}else{
						fl=l+1;
						fr=m+1;
						nl=m+1;
						nr=r;
						mnf=mn;
						mxf=new Vector3D(mx);
						mxf.p[dim]=vm.p[dim];
						mnn=new Vector3D(mn);
						mnn.p[dim]=vm.p[dim];
						mxn=mx;
					}
					visit.add(new Subbranch(nl,nr,mindist,dists,mnn,mxn));
					Vector3D sd=new Vector3D(dists);
					double dd=diff*diff;
					sd.p[dim]=dd;
					mindist+=dd-dists.p[dim];
					if(mindist<=rsq)
						visit.add(new Subbranch(fl,fr,mindist,sd,mnf,mxf));
				}
				
			}
		}
	}
*/	
		
	public synchronized void computeNormals(int k) {
        double timer=System.currentTimeMillis();
		System.out.println("Start kD-Tree-sorting...");
		kdtreesort(0,getNumVertices(),boxMin,boxMax);
		System.out.println("Finished kD-Tree-sorting in "+(System.currentTimeMillis()-timer)+"msec.");
		System.out.println("Computing Normals______________________");
		int[] neighb={0};
		for(int i=0;i<numVertices;i++) {
			kNN knn=new kNN(this,vertices[i],k,5);
			// knn.addInitialNeighbors(neighb);
			neighb=knn.getNeighbors();
			if((int)(i*40/numVertices)>(int)((i-1)*40/numVertices)) System.out.print(".");

			Vector3D M=new Vector3D();
			for (int j=0;j<neighb.length;j++)
				M.incBy(vertices[neighb[j]]);
			M.scalarDivideBy(neighb.length);
			double M002=0;double M020=0;double M200=0;
			double M011=0;double M101=0;double M110=0;
			for (int j=0;j<neighb.length;j++) {
				Vector3D N=vertices[neighb[j]].sub(M);
				M002+=N.p[2]*N.p[2];M020+=N.p[1]*N.p[1];M200+=N.p[0]*N.p[0];
				M011+=N.p[1]*N.p[2];M101+=N.p[0]*N.p[2];M110+=N.p[0]*N.p[1];
			}
			double[][] eig=Eigenvector.SolveEigenSystem(M200,M020,M002,M110,M101,M011);
			Vector3D norm=Eigenvector.eigVec3(eig);
			norm.normalize();
			if(norm.dot(zVec)<0)norm.scalarMultiplyBy(-1.0);
			if(norm.getLength()==1.0)
				vertices[i].setNormal(norm);
		}
	}

	public synchronized void guessNormals(int k) {
		System.out.println("Guessing Normals by Generating Randomized KD-Trees");
		int iterations=5;
		for(int i=0;i<iterations;i++) {
	        long timer=System.currentTimeMillis();
			rndtreenormals(0,getNumVertices(),k,boxMin,boxMax);
			System.out.println("Guessing step "+(i+1)+"/"+iterations+" took "+(System.currentTimeMillis()-timer)+"msec...");
		}
	}

	/* Initializes the kNN-guess by iterations, boxsize, neighbors */
	public synchronized void guessNeighbors(int iterations, int boxsize, int neighbors) {
		System.out.println("Start guessing " + neighbors + "-Nearest-Neighbors for each of " + totalNumVertices + " vertices...");
		System.out.println("...Guessing neighbors by generating RPS-KD-Trees with iterations="+iterations+" and boxsize="+boxsize+"...");
		
		long start=System.currentTimeMillis();
		long timer;
		
		/* Generates a MaxHeap for each vertex in the mesh, if not already existing */
		if(kNearestNeighbors==null) {
			System.out.print("...Generating MaxHeaps for all vertices...");
			timer=System.currentTimeMillis();
			kNearestNeighbors = new MaxHeap[totalNumVertices];
			for(int i=0; i<totalNumVertices; i++) {
				kNearestNeighbors[i] = new MaxHeap(neighbors);
			}
			System.out.print("done! (~"+(System.currentTimeMillis()-timer)+"msec)\n");
		}
		
		for(int i=1;i<=iterations;i++) {
		System.out.print("...Guessing iteration "+(i)+"/"+iterations+"...");
		timer=System.currentTimeMillis();
		RPS_KD_Tree(0,getNumVertices(),boxsize,boxMin,boxMax);
		System.out.print("done! (~"+(System.currentTimeMillis()-timer)+"msec)\n");
		}
		
		System.out.println("Finished guessing neighbors in ~"+(System.currentTimeMillis()-start)+"msec.");
	}
	
	/* Space partitioning by RPS-KD-Tree and filtering possible k-Nearest-Neighbors */
	public void RPS_KD_Tree(int start, int end, int boxsize, Vector3D mn, Vector3D mx) {
		if(end-start<=boxsize){
			for(int i=start; i<end; i++) {
				for(int j=start; j<end; j++) {
					kNearestNeighbors[i].add(vertices[i].squaredDist(vertices[j]), vertices[j]);
				}
			}
		}
		else {
			int dim=0;
			double diff=mx.p[0]-mn.p[0];
			double diffy=mx.p[1]-mn.p[1];
			double diffz=mx.p[2]-mn.p[2];
			if(diffy>diff) {
				dim=1;
				diff=diffy;
			}
			if(diffz>diff) {
				dim=2;
				diff=diffz;
			}
			
			int k=centerpercentile(start,end,dim);
			
			Vector3D mx2=new Vector3D(mx);
			mx2.p[dim]=vertices[k].p[dim];
			Vector3D mn2=new Vector3D(mn);
			mn2.p[dim]=vertices[k].p[dim];
			RPS_KD_Tree(start,k,boxsize,mn,mx2);
			RPS_KD_Tree(k,end,boxsize,mn2,mx);
		}
	}
	
	/* Initializes the kNN-guess by iterations, boxsize, neighbors regarding the added neighbors per round such as overall */
	public synchronized void guessNeighborsWithEvaluation(int iterations, int boxsize, int neighbors) {
		System.out.println("Start guessing " + neighbors + "-Nearest-Neighbors for each of " + totalNumVertices + " vertices...");
		System.out.println("...Guessing neighbors by generating RPS-KD-Trees with iterations="+iterations+" and boxsize="+boxsize+"...");
		
		addedNeighbors=notAddedNeighbors=0.0;
		
		if(kNearestNeighbors==null)
		{
			System.out.print("...Generating MaxHeaps for all vertices...");
			kNearestNeighbors = new MaxHeap[totalNumVertices];
			for(int i=0; i<totalNumVertices; i++)
			{
				kNearestNeighbors[i] = new MaxHeap(neighbors);
			}
			System.out.print("done!\n");
		}
		
		for(int i=1;i<=iterations;i++){
		System.out.print("...Guessing iteration "+(i)+"/"+iterations+"...");

		RPS_KD_TreeWithEvaluation(0, getNumVertices(), boxsize, boxMin, boxMax);
		System.out.print("done! (" + Math.round(addedNeighborsRound) + " neighbors added, " + Math.round(notAddedNeighborsRound) +" refused to add)\n");
		addedNeighbors = addedNeighbors + addedNeighborsRound;
		notAddedNeighbors = notAddedNeighbors + notAddedNeighborsRound;
		addedNeighborsRound=notAddedNeighborsRound=0.0;
		}
		
		System.out.println("Finished guessing neighbors.");
	}
	
	/* Space partitioning by RPS-KD-Tree, filtering possible k-Nearest-Neighbors and counting added/not added neighbors */
	public void RPS_KD_TreeWithEvaluation(int start, int end, int boxsize, Vector3D mn, Vector3D mx) {
		if(end-start<=boxsize){
			for(int i=start; i<end; i++) {
				for(int j=start; j<end; j++)
				{
					if(kNearestNeighbors[i].addWithEvaluation(vertices[i].squaredDist(vertices[j]), vertices[j]) == true)
					{
						addedNeighborsRound++;
					}
					else if(vertices[i] != vertices[j]) {
							notAddedNeighborsRound++;
					}
				}
			}
		}
		else {
			int dim=0;
			double diff=mx.p[0]-mn.p[0];
			double diffy=mx.p[1]-mn.p[1];
			double diffz=mx.p[2]-mn.p[2];
			if(diffy>diff) {
				dim=1;
				diff=diffy;
			}
			if(diffz>diff) {
				dim=2;
				diff=diffz;
			}
			// dim=(dim+generator.nextInt(2))%3;
			int k=centerpercentile(start,end,dim);
			Vector3D mx2=new Vector3D(mx);
			mx2.p[dim]=vertices[k].p[dim];
			Vector3D mn2=new Vector3D(mn);
			mn2.p[dim]=vertices[k].p[dim];
			RPS_KD_TreeWithEvaluation(start,k,boxsize,mn,mx2);
			RPS_KD_TreeWithEvaluation(k,end,boxsize,mn2,mx);
		}
	}
	
	/* Evaluates the a) mean data, b) internal operation data of MaxHeaps or a) and b) for a kNN-Guess */
	public void evaluateKNN(int start, int end, int neighbors, Integer iterations, int boxsize, boolean meanDataEvaluation, boolean heapOperationDataEvaluation)
	{
		if(meanDataEvaluation == true)
		{
		System.out.println("");
		System.out.println("Evaluating mean data...");
		Double[] meanData = kNNevaluationFunctions.getMeanData(start, end, neighbors, vertices, kNearestNeighbors);

		System.out.println("=============================================================");
		System.out.println("Evaluation of mean data for MaxHeap at " + start + " to MaxHeap at " + end + "");
		System.out.println("=============================================================");
		System.out.println("Incomplete MaxHeaps: " + Math.round(meanData[7]) + "/" + (end-start+1));
		System.out.println("Mean value of kNN: " + Math.round(10000*meanData[0]/meanData[1])/100.0);;
		System.out.println("Number of regarded neighbors: " + Math.round(meanData[1]));
		System.out.println("Number of estimated neighbors: " + Math.round(meanData[6]));
		System.out.println("Number of correctly estimated neighbors: " + Math.round(meanData[0]));
		System.out.println("Number of misestimated neighbors: " + Math.round(meanData[8]));
		System.out.println("Number of MaxHeaps containing misestimated neighbors: " + Math.round(meanData[4]) + "/" + (end-start+1));
		System.out.println("(1) Avg. dist. from q for exact "+neighbors+" nearest neighbors: " + Math.round(1000000*meanData[3])/1000000.0);
		System.out.println("(2) Avg. dist. from q for estimated " + Math.round(100*(meanData[6]/(end-start+1)))/100.0 + " nearest neighbors: " + Math.round(1000000*meanData[2])/1000000.0);
		System.out.println("Mean estimation error (1+e) -> (2) to (1): "+Math.round(10000*(meanData[2]/meanData[3]))/10000.0);
		System.out.println("-------------------------------------------------------------");
		System.out.println("Following data are only regarded for misestimated neighbors:");
		System.out.println("-------------------------------------------------------------");
		System.out.println("(3) Avg. dist. from q for exact neighbors that have not been hit: " + Math.round(1000000*meanData[10])/1000000.0);
		System.out.println("(4) Avg. dist. from q for worst exact k nearest neighbor: " + Math.round(1000000*meanData[12])/1000000.0);
		System.out.println("(5) Avg. dist. from q for misestimated neighbors: " + Math.round(1000000*meanData[11])/1000000.0);
		System.out.println("Mean estimation error (1+e) if misestimated -> (5) to (3): "+Math.round(10000*meanData[5])/10000.0);
		System.out.println("Mean estimation error (1+e) if misestimated -> (5) to (4): "+Math.round(10000*meanData[13])/10000.0);
		System.out.println("=============================================================");
		}
		
		if(heapOperationDataEvaluation == true)
		{
		Object[] heapOperationData = kNNevaluationFunctions.heapOperationData(totalNumVertices, kNearestNeighbors);
		
		double computationUnitsDD = Math.round(((Long)heapOperationData[7]).doubleValue()/1000000);
		double computationUnitsR = Math.round(((Long)heapOperationData[8]).doubleValue()/1000000);
		double computationUnitsA = Math.round(((Long)heapOperationData[9]).doubleValue()/1000000);
		double totalComputationUnits = computationUnitsDD + computationUnitsR + computationUnitsA;
		
		System.out.println("");
		System.out.println("=============================================================");
		System.out.println("Evaluation for internal operations of all MaxHeaps");
		System.out.println("=============================================================");
		System.out.println("Incomplete MaxHeaps: " + Math.round((Double)heapOperationData[0]) + "/" + (totalNumVertices));
		System.out.println("Added neighbors: " + Math.round(addedNeighbors));
		System.out.println("Neighbors refused to add: " + Math.round(notAddedNeighbors));
		System.out.println("Average added neighbors per iteration: " + Math.round((addedNeighbors).doubleValue()/iterations.doubleValue()));
		System.out.println("");
		System.out.println("Duplicate detection...");
		System.out.println("Initiations: " + Math.round((Double)heapOperationData[1]));
		System.out.println("Iterations: " + Math.round((Double)heapOperationData[2]));
		System.out.println("Average steps: " + Math.round(((Double)heapOperationData[2])/((Double)heapOperationData[1])*10)/10.0);
		System.out.println("Computation units: " + (int)computationUnitsDD + " ("+ Math.round((computationUnitsDD/totalComputationUnits)*100) +"%)");
		System.out.println("");
		System.out.println("Replacing worst neighbors...");
		System.out.println("Initations: " + Math.round((Double)heapOperationData[3]));
		System.out.println("Iterations: " + Math.round((Double)heapOperationData[4]));
		System.out.println("Average steps: " + Math.round(((Double)heapOperationData[4])/((Double)heapOperationData[3])*10)/10.0);
		System.out.println("Computation units: " + (int)computationUnitsR + " ("+ Math.round((computationUnitsR/totalComputationUnits)*100) +"%)");
		System.out.println("");
		System.out.println("Adding initial k-Nearest-Neighbors...");
		System.out.println("Initations: " + Math.round((Double)heapOperationData[5]));
		System.out.println("Iterations: " + Math.round((Double)heapOperationData[6]));
		System.out.println("Average steps: " + Math.round(((Double)heapOperationData[6])/((Double)heapOperationData[5])*10)/10.0);
		System.out.println("Computation units: " + (int)computationUnitsA + " ("+ Math.round((computationUnitsA/totalComputationUnits)*100) +"%)");
		System.out.println("=============================================================");
		}
	}
	
	/* Computes the average values for mean, standard deviation, error rate by performing multiple kNN-Guesses */
	public void computeMeanAndStandardDeviationAndErrorRate(int start, int end, int neighbors, int iterations, int boxsize, int repeatings)
	{
		System.out.println("Computing RPS-KD-Tree searches to calculate average mean, standard deviation and error rate for " + neighbors + "-Nearest-Neighbors with iterations=" +iterations+ " and boxsize=" +boxsize+ " at meshsize " + totalNumVertices +  " within " +  repeatings + " repeatings...");

		double meanSum = 0.0;
		double mean = 0.0;
		double variance = 0.0;
		double e = 0.0;
		double eMisestimatedNotHitExact = 0.0;
		double eMisestimatedWorst = 0.0;
		double valOfMisestimatedGuesses = 0.0;
		ArrayList<Double> listOfMeans = new ArrayList<Double>();
		
		for(int i=1; i<=repeatings; i++)
		{
			guessNeighbors(iterations, boxsize, neighbors);
			if(kNearestNeighbors != null)
			{
				kNNevaluation kNNevaluationFunctions = new kNNevaluation();
				Double[] meanData = kNNevaluationFunctions.getMeanData(start, end, neighbors, vertices, kNearestNeighbors);
				meanSum = meanSum + (100*(meanData[0]/meanData[1]));
				listOfMeans.add(100*(meanData[0]/meanData[1]));
				e = e + meanData[2]/meanData[3];
				if((meanData[5] != 1.0) || (meanData[13] != 1.0)){
					valOfMisestimatedGuesses++;
					eMisestimatedNotHitExact = eMisestimatedNotHitExact + meanData[5];
					eMisestimatedWorst = eMisestimatedWorst + meanData[13];
				}
			}
			kNearestNeighbors=null;
			System.out.print(".");
		}
		mean = meanSum/listOfMeans.size();
		for(int i=0; i<listOfMeans.size(); i++)
		{
			variance = variance+(Math.pow(listOfMeans.get(i)-mean, 2));
		}
		variance=variance/listOfMeans.size();
		e=e/listOfMeans.size();
		eMisestimatedNotHitExact = eMisestimatedNotHitExact/valOfMisestimatedGuesses;
		eMisestimatedWorst = eMisestimatedWorst/valOfMisestimatedGuesses;
		System.out.println("\n");
		System.out.println("Mean value of kNN: "+Math.round(10*mean)/10.0);
		System.out.println("Absolute standard deviation: "+Math.round(10*Math.sqrt(variance))/10.0);
		System.out.println("Relative standard deviation: "+Math.round((10*100*Math.sqrt(variance)/mean))/10.0);
		System.out.println("Mean estimation error (1+e) -> (2) to (1): " + Math.round(100*e)/100.0);
		System.out.println("Mean estimation error (1+e) if misestimated -> (5) to (3): " + Math.round(100*eMisestimatedNotHitExact)/100.0);
		System.out.println("Mean estimation error (1+e) if misestimated -> (5) to (4): " + Math.round(100*eMisestimatedWorst)/100.0);
	}
	
	/* Computes the average computation time by performing multiple kNN-Guesses */
	public void computeComputationTime(int neighbors, int iterations, int boxsize, int repeatings)
	{
		System.out.println("Computing RPS-KD-Tree searches to calculate average computation time for " + neighbors + " neighbors, " + iterations + " iterations, " + boxsize + " boxsize and meshsize " + totalNumVertices +  " in " +  repeatings + " repeatings...");
		Long meanComputationTime = System.currentTimeMillis();
		
		for(int i=0; i<repeatings; i++)
		{
			kNearestNeighbors=null;
			guessNeighbors(iterations, boxsize, neighbors);
			System.out.print(".");
		}
		meanComputationTime = System.currentTimeMillis() - meanComputationTime;
		System.out.println("\n" + "Computation time for meshsize " + totalNumVertices + ": " + Math.round(meanComputationTime.doubleValue()/repeatings) + "msec");
		
	}
	
	/* Computes the computation time by performing linear search for all points using MaxHeap to determine all exact kNN */
	public void kNN_LinearSearchViaMaxHeap(int neighbors)
	{
		kNearestNeighbors = null;
		
		System.out.println("Computing linear search for all points using MaxHeap to determine all exact " + neighbors + "-Nearest-Neighbors at meshsize " + totalNumVertices + "...");
		Long computationTime = System.currentTimeMillis();
		

			kNearestNeighbors = new MaxHeap[totalNumVertices];
			for(int i=0; i<totalNumVertices; i++) {
				kNearestNeighbors[i] = new MaxHeap(neighbors);
			}
			
			
			for(int i=0; i<totalNumVertices; i++) {
				for(int j=0; j<totalNumVertices; j++) {
					kNearestNeighbors[i].addLinearSearch(vertices[i].squaredDist(vertices[j]),vertices[j]);
				}
				if(((i+1) % (totalNumVertices/100)) == 0)
				{
					System.out.println((i+1)/(totalNumVertices/100)+" / 100 - " + (System.currentTimeMillis()-computationTime));
				}	
			}
		
		kNearestNeighbors = null;
		
		computationTime = System.currentTimeMillis() - computationTime;
		System.out.println("Computation time to compute " + neighbors + " nearest Neighbors for meshsize " + totalNumVertices + ": " + computationTime.doubleValue() + "msec");
	}
	
	/* Computes the computation time by performing linear search for all points using in-place-QuickSort to determine all exact kNN */
	public void kNN_LinearSearchViaQuickSort(int neighbors) {
		
		System.out.println("Computing linear search for all points using QuickSort to determine all exact " + neighbors + "-Nearest-Neighbors at meshsize " + totalNumVertices + "...");
		Long computationTime = System.currentTimeMillis();
		Vertex[][] kNearestNeighborsQS = new Vertex[totalNumVertices][neighbors];
		indexNeighbors_tmp = new int[totalNumVertices];
		sqDists_tmp = new double[totalNumVertices];
		
		for(int i=0; i<totalNumVertices; i++) {
			if(((i+1) % (totalNumVertices/100)) == 0)
			{
				System.out.println((i+1)/(totalNumVertices/100)+" / 100 - " + (System.currentTimeMillis()-computationTime) + "msec");
			}
			for(int j=0; j<totalNumVertices; j++) {
				sqDists_tmp[j] = vertices[i].squaredDist(vertices[j]);
				indexNeighbors_tmp[j] = j;
			}
			
			quickSortkNN(0, totalNumVertices-1);
			
			for(int j=0; j<neighbors; j++) {
				kNearestNeighborsQS[i][j] = vertices[indexNeighbors_tmp[j+1]];
			}
		}
		System.out.println("Computing kNN using QuickSort at meshsize "+totalNumVertices+" took " + (System.currentTimeMillis()-computationTime) + "msec.");
	}
	
	public void quickSortkNN(int left, int right) {
		if(left < right) {
			int pivotIndex = left;
			int pivotNewIndex = partitionQS(left, right, pivotIndex);
			quickSortkNN(left, pivotNewIndex-1);
			quickSortkNN(pivotNewIndex+1,right);
		}
	}
	
	public int partitionQS(int left, int right, int pivotIndex) {
		double pivotValue = sqDists_tmp[pivotIndex];
		
		sqDist_swap = sqDists_tmp[left];
		sqDists_tmp[left] = sqDists_tmp[right];
		sqDists_tmp[right] = sqDist_swap;
		indexNeighbor_swap = indexNeighbors_tmp[left];
		indexNeighbors_tmp[left] = indexNeighbors_tmp[right];
		indexNeighbors_tmp[right] = indexNeighbor_swap;
		
		int storeIndex = left;
		for(int i=left; i<right; i++) {
			if(sqDists_tmp[i] <= pivotValue) {
				sqDist_swap = sqDists_tmp[i];
				sqDists_tmp[i] = sqDists_tmp[storeIndex];
				sqDists_tmp[storeIndex] = sqDist_swap;
				indexNeighbor_swap = indexNeighbors_tmp[i];
				indexNeighbors_tmp[i] = indexNeighbors_tmp[storeIndex];
				indexNeighbors_tmp[storeIndex] = indexNeighbor_swap;
				storeIndex++;
			}
		}
		
		sqDist_swap = sqDists_tmp[storeIndex];
		sqDists_tmp[storeIndex] = sqDists_tmp[right];
		sqDists_tmp[right] = sqDist_swap;
		indexNeighbor_swap = indexNeighbors_tmp[storeIndex];
		indexNeighbors_tmp[storeIndex] = indexNeighbors_tmp[right];
		indexNeighbors_tmp[right] = indexNeighbor_swap;
		
		return storeIndex;
	}
	
	public void addNoiseToVertices(double noise) {
		System.out.println("Adding noise to vertices...");
		long timer = System.currentTimeMillis();
		for(int i=0; i<totalNumVertices; i++) {
			vertices[i].p[0] = vertices[i].p[0]+randomNoise(noise);
			vertices[i].p[1] = vertices[i].p[1]+randomNoise(noise);
			vertices[i].p[2] = vertices[i].p[2]+randomNoise(noise);
		}
		System.out.print("Finished adding noise to vertices in ~"+(System.currentTimeMillis()-timer)+"msec.\n");
	}
	
	public double randomNoise(double noise) {
		return generator.nextGaussian()*2*noise-noise;
	}

	@Override public String toString() {
		String ret="Number of Vertices: "+getNumVertices()+"\n";
		 for(int i=0;i<Math.min(60,getNumVertices());i++)
			ret+=getVertex(i)+"\n";
		return ret;
	}
	
}
