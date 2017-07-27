package Points2Map;

import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.Math;
import java.util.Random;
import java.util.Arrays;

public class ViewPanel extends JPanel implements MouseInputListener, KeyListener{

	private long countertimer;
	
	private long redrawcountertimer;
	
	private static int COLZ=0;
	private static int COLRGB=1;
	private static int COLSINE=2;
	private static int NRCOLMODES=3;
	private int COLMODE=0;

    private Random generator = new Random();
	private Mesh mesh;
    private Color bgColor;
	private int iterationCounter=1;

    private boolean lMouseDown;
    private boolean rMouseDown;
    private Point   lastClickPoint;

	private boolean SHIFTKEY=false;
	private boolean CRTLKEY=false;
	private boolean ALTKEY=false;

	public static boolean BACKLIGHT=false;
	public static boolean CONTOUR=true;
	public static boolean SPECULAR=true;
	public static boolean INVERT=false;
	public static boolean REFLECTIVE=false;
	public static boolean BRIGHTNESS=true;
	public static boolean AMBIENT=true;
	public static boolean NOISE=false;
	
	public int colRSelection=3;
	public int colGSelection=4;
	public int colBSelection=5;

    private Dimension currentSize;
	
	private long timer;

    private int numPoints;

    private int[][]		       zBuffer;
    private BufferedImage      dispBuffer;
    private int[][]		       indexBuffer;

    private Perspective 	    transformer;

	private int progress=0;
	private int redrawProgress=1;
	private int redrawStart=0;
	private boolean drawJustImg=false;

	private int[] colorVec=new int[4];
	private int[] colorVec2=new int[4];
	private int[] colorVecold=new int[4];
	private Vector3D min;
	private Vector3D max;
	private Vector3D mean;
	double meanColValue=0;
	int meanNr=0;
	
	public double brightness=1.0;
	public double ambient=1.0;
	public double noiselevel=0.01;
	public double specular=1.0;
	public double shadow=1.0;
	public double mirror=1.0;
	private Vector3D relLightDir=new Vector3D(1,0,0);
	private Vector3D lightDir=new Vector3D(0,0,0);
	private Vector3D specDir=new Vector3D(0,0,0);
	private Vector3D viewDir;
	private Vector3D rghtDir;
	private Vector3D upDir;
	private double hue=45;
	private double saturation=1;
	private Vector3D colVec=new Vector3D();
	private Vector3D colVecUser=new Vector3D();
	private boolean colNormalization=true;
	private double normFactor=1.0;
	
	private BufferedImage panoimg;
	private Raster pano_raster;
	private WritableRaster dispRaster;
	private int offsetx=0;
	private int offsety=0;
	private int fillSize=1;
	
    public ViewPanel() {

        transformer   = new Perspective();
        mesh           = null;
        bgColor       = Color.black;
        numPoints     = 0;

        lMouseDown = false;
        rMouseDown = false;

        currentSize = getSize();

		try{
			panoimg = ImageIO.read(new File("pano.bmp"));
			pano_raster=panoimg.getData();
		} catch (IOException e) {
            e.printStackTrace();
        }
        zBuffer = new int[250][250];
        dispBuffer = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        indexBuffer = new int[250][250];
		for(int[] subarray : indexBuffer) Arrays.fill(subarray, -1);
        this.setMinimumSize(new Dimension(250,250));
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.white);
        addMouseListener(this);
        addMouseMotionListener(this);
		addKeyListener(this);
    }

    public Perspective getPerspective() {
        return transformer;
    }

    public int getProgress() {
        return progress;
    }

    public Color getBackgroundColor() {
        return bgColor;
    }

    public void setBackgroundColor(Color newColor) {
        bgColor = newColor;
		fullPaint();
    }
	
	public void changeColMode() {
		COLMODE=(COLMODE+1)%NRCOLMODES;
		fullPaint();
	}

    public void setMesh(Mesh newMesh) {
        this.mesh = newMesh;

        numPoints = mesh.getNumVertices();
		progress=0;
		for(int[] subarray : indexBuffer) Arrays.fill(subarray, -1);

        setDefaultPerspective();
    }

    public void fullPaint() {
		drawJustImg=true;
		redrawStart=redrawProgress;
		redrawProgress++;
        repaint();
    }

	public void fullPaintWithNewIndexBuffer(){
		if(indexBuffer!=null)
			for(int[] subarray : indexBuffer) Arrays.fill(subarray, -1);
		drawJustImg=false;
		progress=0;
		repaint();
	}
	
    public void setDefaultPerspective() {

        Vector3D pos = new Vector3D(mesh.getMean());
		transformer.setAt(pos);
		pos.p[1] = pos.p[1] - (mesh.getMaxRadius() * 2);
		pos.p[2] = pos.p[2] + (mesh.getMaxRadius() * 0.75);
        transformer.setFrom(pos);
        transformer.setUp(new Vector3D(0, 0, 1.0));
        transformer.setAngle(45);
        fullPaint();
    }

    public boolean paintImage(Dimension testSize) {
		if (!currentSize.equals(testSize)) {
			currentSize = testSize;
			transformer.setScreenScale(currentSize.width, currentSize.height);
			zBuffer = new int[currentSize.width][currentSize.height];
			dispBuffer = new BufferedImage(currentSize.width, currentSize.height, BufferedImage.TYPE_INT_RGB);
			indexBuffer = new int[currentSize.width][currentSize.height];
			for(int[] subarray : indexBuffer) Arrays.fill(subarray, -1);
			progress=0;
		}
		return buildBuffer();
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		
		if (paintImage(getSize())) repaint();
		g.drawImage(dispBuffer, 0, 0, null);
    }

	public BufferedImage getImage() {
		return dispBuffer;
	}
	
	public BufferedImage getMap() {
		double nrPixelPerUnit=20;
		BufferedImage map;
		WritableRaster raster;
		Graphics2D graphics;
		int[] colV=new int[4];
		int[] colV2=new int[4];
		Vector3D min=mesh.getMin();
		Vector3D max=mesh.getMax();
		Vector3D mean=mesh.getMean();
		int sizex=(int)((max.p[0]-min.p[0])*nrPixelPerUnit+1);
		int sizey=(int)((max.p[1]-min.p[1])*nrPixelPerUnit+1);
		double sizez=max.p[2]-min.p[2];

		System.out.println("Generating Map Image___________________");
        map = new BufferedImage(sizex, sizey, BufferedImage.TYPE_INT_RGB);
		graphics = map.createGraphics();
		graphics.setPaint ( new Color ( 0, 0, 0 ) );
		graphics.fillRect ( 0, 0, map.getWidth(), map.getHeight() );
		raster = map.getRaster();

		for(int i=0;i<mesh.getNumVertices();i++) {
			Vertex v=mesh.getVertex(i);
			int x=(int)((v.p[0]-min.p[0])*nrPixelPerUnit);
			int y=(int)((v.p[1]-min.p[1])*nrPixelPerUnit);
			int z=(int)((v.p[2]-min.p[2])*(16777214)/sizez);
			
			raster.getPixel(x, y, colV2);
			
			colV[0]=z & 255;
			colV[1]=(z/256)&255;
			colV[2]=(z/65536)&255;
			int zold=colV2[0]+256*colV2[1]+65536*colV2[2];
			if((zold>z)||(zold==0))
				raster.setPixel(x,y, colV);
			if((int)(i*40/mesh.getNumVertices())>(int)((i-1)*40/mesh.getNumVertices())) System.out.print(".");
		}
		System.out.println();
		return map;
	}
	
	private void hs2rgb(double h, double s, Vector3D rgb) {
		h=h%360;
		if(h<0)h+=360;
		int i = (int) (h/60);
		double f = h - i;double p = 1 - s;double q = 1 - (s * f);double t = 1 - s * (1 - f);
		switch (i) {
			case 0:
				rgb.p[0]=1;rgb.p[1]=t;rgb.p[2]=p;break;
			case 1:
				rgb.p[0]=q;rgb.p[1]=1;rgb.p[2]=p;break;
			case 2:
				rgb.p[0]=p;rgb.p[1]=1;rgb.p[2]=t;break;
			case 3:
				rgb.p[0]=p;rgb.p[1]=q;rgb.p[2]=1;break;
			case 4:
				rgb.p[0]=t;rgb.p[1]=p;rgb.p[2]=1;break;
			case 5:
				rgb.p[0]=1;rgb.p[1]=p;rgb.p[2]=q;break;
			default:
				rgb.p[0]=1;rgb.p[1]=1;rgb.p[2]=1;break;
		}
	}
	
	private void updateLight() {
		viewDir=transformer.getAt().sub(transformer.getFrom());
		viewDir.normalize();
		upDir=transformer.getUp();
		upDir.normalize();
		rghtDir=viewDir.cross(upDir);
		lightDir.p[0]=viewDir.p[0]*relLightDir.p[2]+upDir.p[0]*relLightDir.p[1]+rghtDir.p[0]*relLightDir.p[0];
		lightDir.p[1]=viewDir.p[1]*relLightDir.p[2]+upDir.p[1]*relLightDir.p[1]+rghtDir.p[1]*relLightDir.p[0];
		lightDir.p[2]=viewDir.p[2]*relLightDir.p[2]+upDir.p[2]*relLightDir.p[1]+rghtDir.p[2]*relLightDir.p[0];
		lightDir.normalize();
		specDir.set(lightDir);
		specDir.incBy(viewDir);
		specDir.normalize();
	}
	
	public void fillHoles() {
	    int[][] zBuffer2=new int[indexBuffer.length][indexBuffer[0].length];
		int[][]	indexBuffer2=new int[indexBuffer.length][indexBuffer[0].length];
	    int[][] zBuffer3=new int[indexBuffer.length][indexBuffer[0].length];
		int[][]	indexBuffer3=new int[indexBuffer.length][indexBuffer[0].length];

		for(int y=0;y<indexBuffer[0].length;y++) {
			for(int i=0;i<fillSize;i++) {
				zBuffer2[i][y]=zBuffer[i][y];
				zBuffer2[indexBuffer.length-i-1][y]=zBuffer[indexBuffer.length-i-1][y];
			}
			for(int x=fillSize;x<indexBuffer.length-fillSize;x++) {
				int maxz=zBuffer[x][y];
				int maxi=indexBuffer[x][y];
				for(int i=1;i<=fillSize;i++) {
					if(zBuffer[x-i][y]>maxz){
						maxz=zBuffer[x-i][y];
						maxi=indexBuffer[x-i][y];
					}
					if(zBuffer[x+i][y]>maxz){
						maxz=zBuffer[x+i][y];
						maxi=indexBuffer[x+i][y];
					}
				}
				zBuffer2[x][y]=maxz;
				indexBuffer2[x][y]=maxi;
			}
		}
		for(int x=0;x<indexBuffer.length;x++)
			for(int y=fillSize;y<indexBuffer[0].length-fillSize;y++) {
				int maxz=zBuffer2[x][y];
				int maxi=indexBuffer2[x][y];
				for(int i=1;i<=fillSize;i++) {
					if(zBuffer2[x][y-i]>maxz){
						maxz=zBuffer2[x][y-i];
						maxi=indexBuffer2[x][y-i];
					}
					if(zBuffer2[x][y+i]>maxz){
						maxz=zBuffer2[x][y+i];
						maxi=indexBuffer2[x][y+i];
					}
				}
				zBuffer3[x][y]=maxz;
				indexBuffer3[x][y]=maxi;
			}
		for(int y=0;y<indexBuffer[0].length;y++) {
			for(int x=fillSize;x<indexBuffer.length-fillSize;x++) {
				int minz=zBuffer3[x][y];
				int mini=indexBuffer3[x][y];
				for(int i=1;i<=fillSize;i++) {
					if(zBuffer3[x-i][y]<minz){
						minz=zBuffer3[x-i][y];
						mini=indexBuffer3[x-i][y];
					}
					if(zBuffer3[x+i][y]<minz){
						minz=zBuffer3[x+i][y];
						mini=indexBuffer3[x+i][y];
					}
				}
				zBuffer2[x][y]=minz;
				indexBuffer2[x][y]=mini;
			}
		}
		for(int x=0;x<indexBuffer.length;x++)
			for(int y=fillSize;y<indexBuffer[0].length-fillSize;y++) {
				int minz=zBuffer2[x][y];
				int mini=indexBuffer2[x][y];
				for(int i=1;i<=fillSize;i++) {
					if(zBuffer2[x][y-i]<minz){
						minz=zBuffer2[x][y-i];
						mini=indexBuffer2[x][y-i];
					}
					if(zBuffer2[x][y+i]<minz){
						minz=zBuffer2[x][y+i];
						mini=indexBuffer2[x][y+i];
					}
				}
				zBuffer3[x][y]=minz;
				indexBuffer3[x][y]=mini;
			}
		for(int y=0;y<indexBuffer[0].length;y++) {
			for(int x=0;x<indexBuffer.length;x++) {
				if(zBuffer3[x][y]>zBuffer[x][y]+65000){
					zBuffer[x][y]=zBuffer3[x][y];
					indexBuffer[x][y]=indexBuffer3[x][y];
				}
			}
		}
		fillSize++;
	}

	public void growDots() {
	    int[][] zBuffer2=new int[indexBuffer.length][indexBuffer[0].length];
		int[][]	indexBuffer2=new int[indexBuffer.length][indexBuffer[0].length];
		for(int y=0;y<indexBuffer[0].length;y++) {
			zBuffer2[0][y]=zBuffer[0][y];
			zBuffer2[indexBuffer.length-1][y]=zBuffer[indexBuffer.length-1][y];
			for(int x=1;x<indexBuffer.length-1;x++) {
				int maxz=zBuffer[x][y];
				int maxi=indexBuffer[x][y];
				if(zBuffer[x-1][y]>maxz){
					maxz=zBuffer[x-1][y];
					maxi=indexBuffer[x-1][y];
				}
				if(zBuffer[x+1][y]>maxz){
					maxz=zBuffer[x+1][y];
					maxi=indexBuffer[x+1][y];
				}
				zBuffer2[x][y]=maxz;
				indexBuffer2[x][y]=maxi;
			}
		}
		for(int x=0;x<indexBuffer.length;x++)
			for(int y=1;y<indexBuffer[0].length-1;y++) {
				int maxz=zBuffer2[x][y];
				int maxi=indexBuffer2[x][y];
				if(zBuffer2[x][y-1]>maxz){
					maxz=zBuffer2[x][y-1];
					maxi=indexBuffer2[x][y-1];
				}
				if(zBuffer2[x][y+1]>maxz){
					maxz=zBuffer2[x][y+1];
					maxi=indexBuffer2[x][y+1];
				}
				zBuffer[x][y]=maxz;
				indexBuffer[x][y]=maxi;
			}
	}

    private boolean buildBuffer() {
		if(drawJustImg&&indexBuffer!=null){
			updateLight();
			redrawcountertimer=System.currentTimeMillis();

			int bits=((int)(Math.ceil(Math.log(indexBuffer.length*indexBuffer[0].length)/Math.log(2.0))));
			int binlog=1<<bits;
			for(;redrawProgress!=redrawStart;redrawProgress++){
				if(redrawProgress>=binlog)redrawProgress=0;
				int i=Integer.reverse(redrawProgress)>>>(32-bits);
				int x=i%indexBuffer.length;
				int y=i/indexBuffer.length;
				if(y<indexBuffer[0].length){
					int j=indexBuffer[x][y];
					if(j>=0){
						drawPoint(x,y,zBuffer[x][y],j);
					}
					if(System.currentTimeMillis()-redrawcountertimer>50){
						return true;
					}
				}
			}
			drawJustImg=false;
			return true;
		}
		if(progress>=mesh.getNumVertices()) return false;
		dispRaster = dispBuffer.getRaster();
		colorVec[3]=255;
		colorVec2[3]=255;

        Graphics2D g2 = dispBuffer.createGraphics();
		hs2rgb(hue,saturation,colVecUser);
		Vector3D colVec0=new Vector3D(colVecUser);
		Vector3D colVec1=new Vector3D(colVecUser);
		Vector3D colVec2=new Vector3D(colVecUser);

		fillSize=1;
		countertimer=System.currentTimeMillis();
		int oldprogress=progress;
		if(progress==0) {
			meanColValue=0.0;
			meanNr=0;
			// Fill the background with the background color
			for(int[] subarray : zBuffer) Arrays.fill(subarray, 0);
			for(int[] subarray : indexBuffer) Arrays.fill(subarray, -1);
			g2.setPaint(bgColor);
			g2.fill(new Rectangle(0, 0, currentSize.width, currentSize.height));

			if(mesh==null)return false;
			
			updateLight();
		}

		double length=0;
		double zz=mesh.getMaxRadius()+transformer.getPointToProjPlaneDist(mesh.getMean());
		Vector3D imgpos=new Vector3D();
		int bits=((int)(Math.ceil(Math.log(mesh.getNumVertices())/Math.log(2.0))));
		int binlog=1<<bits;
		for(;progress<binlog;progress++) {
			int j=Integer.reverse(progress)>>>(32-bits);
			
			if(j<mesh.getNumVertices()) {
				Vertex v1=mesh.getVertex(j);
			
				if(transformer.clipToPoint(v1,imgpos))	{
					drawPoint((int)imgpos.p[0],(int)imgpos.p[1],(int)((zz-imgpos.p[2])*(16777215)/(2*mesh.getMaxRadius())),j);
				}
			}
			if(System.currentTimeMillis()-countertimer>50)
				break;
		}
		if(progress>=binlog) {
			progress=0;
			return false;
		}
		return true;
	}
	
	private void setCol(Vertex v,int x,int y) {
		min=mesh.getMin();
		max=mesh.getMax();
		double dist=max.dist(min);
		double r=1;
		double g=1;
		double b=1;
		if(COLMODE==COLZ) {
			double c=(v.p[2]-min.p[2])/(max.p[2]-min.p[2])*6.9999999;
			int cseg=(int)Math.floor(c);
			double c0=c-cseg;
			switch(cseg) {
				case 0:
				  r=0;
				  g=0;
				  b=c0;
				  break;
				case 1:
				  r=c0;
				  g=0;
				  b=1-c0;
				  break;
				case 2:
				  r=1-c0;
				  g=c0;
				  b=0;
				  break;
				case 3:
				  r=0;
				  g=1;
				  b=c0;
				  break;
				case 4:
				  r=c0;
				  g=1-c0;
				  b=1;
				  break;
				case 5:
				  r=1;
				  g=c0;
				  b=1-c0;
				  break;
				case 6:
				  r=1;
				  g=1;
				  b=c0;
			}
		}else if(COLMODE==COLRGB) {
			double cx=(v.p[0]-min.p[0]+dist/1000000)/(max.p[0]-min.p[0]+dist/1000000);
			double cy=(v.p[1]-min.p[1]+dist/1000000)/(max.p[1]-min.p[1]+dist/1000000);
			double cz=(v.p[2]-min.p[2]+dist/1000000)/(max.p[2]-min.p[2]+dist/1000000);
			double nx=cx;
			double ny=cy;
			double nz=cz;
			Vector3D v2=new Vector3D(v.getNormal());
			if(v2!=null) {
				v2.normalize();
				nx=Math.abs(v2.p[0]);
				ny=Math.abs(v2.p[1]);
				nz=Math.abs(v2.p[2]);
			}
			switch(colRSelection){
				case 0: r=cx;break;
				case 1: r=cy;break;
				case 2: r=cz;break;
				case 3: r=nx;break;
				case 4: r=ny;break;
				case 5: r=nz;break;
				default: r=(colRSelection-6)/10.0;
			}
			switch(colGSelection){
				case 0: g=cx;break;
				case 1: g=cy;break;
				case 2: g=cz;break;
				case 3: g=nx;break;
				case 4: g=ny;break;
				case 5: g=nz;break;
				default: g=(colGSelection-6)/10.0;
			}
			switch(colBSelection){
				case 0: b=cx;break;
				case 1: b=cy;break;
				case 2: b=cz;break;
				case 3: b=nx;break;
				case 4: b=ny;break;
				case 5: b=nz;break;
				default: b=(colBSelection-6)/10.0;
			}
			if(r>1)r=1;
			if(r<0)r=0;
			if(g>1)g=1;
			if(g<0)g=0;
			if(b>1)b=1;
			if(b<0)b=0;
		}else if(COLMODE==COLSINE) {
			r=(v.p[0]-min.p[0]+dist/1000000)/(max.p[0]-min.p[0]+dist/1000000);
			g=(v.p[1]-min.p[1]+dist/1000000)/(max.p[1]-min.p[1]+dist/1000000);
			b=(v.p[2]-min.p[2]+dist/1000000)/(max.p[2]-min.p[2]+dist/1000000);
			r=Math.sin(r*2*Math.PI*10)/2.0+0.5;
			g=Math.sin(g*2*Math.PI*10)/2.0+0.5;
			b=Math.sin(b*2*Math.PI*10)/2.0+0.5;
		}
		if(mesh.colors!=null){
			byte[] col=mesh.colors[v.getPos()];
			if(col!=null){
				r=(col[0]&0xFF)/255.0;
				g=(col[1]&0xFF)/255.0;
				b=(col[2]&0xFF)/255.0;
			}
		}
		colVec.p[0]=r;
		colVec.p[1]=g;
		colVec.p[2]=b;

		Vector3D v2=v.getNormal();
		double n1=v2.p[0];
		double n2=v2.p[1];
		double n3=v2.p[2];
		double nn=Math.sqrt(n1*n1+n2*n2+n3*n3);
		n1/=nn;n2/=nn;n3/=nn;
		double sign=(viewDir.p[0]*n1+viewDir.p[1]*n2+viewDir.p[2]*n3);
		if(sign<0) {n1*=-1;n2*=-1;n3*=-1;}
		if(INVERT) {n1*=-1;n2*=-1;n3*=-1;}

		generator.setSeed((long)(n1*5633567+n2*245253447+n3*23523));
		if(NOISE){
			n1+=(generator.nextDouble()-0.5)*noiselevel;
			n2+=(generator.nextDouble()-0.5)*noiselevel;
			n3+=(generator.nextDouble()-0.5)*noiselevel;
		}
		double factor=(lightDir.p[0]*n1+lightDir.p[1]*n2+lightDir.p[2]*n3);
		if((!BACKLIGHT)&&(factor<0))factor=0;
		else factor*=factor;
		double factor2=1;
		if(CONTOUR) {
			factor2=(viewDir.p[0]*n1+viewDir.p[1]*n2+viewDir.p[2]*n3);
			if((!BACKLIGHT)&&(factor2<0))factor2=0;
			factor2*=factor2;
			factor2=1-Math.pow(1-factor2,3)*shadow;
		}
		double factor3=0;
		if(SPECULAR) {
			factor3=(specDir.p[0]*n1+specDir.p[1]*n2+specDir.p[2]*n3);
			if((!BACKLIGHT)&&(factor3<0))factor3=0;
			factor3=Math.pow(factor3,180)*400*specular;
		}
	
		double mirr=0;
		if(REFLECTIVE) {
			double c1=(n2 * upDir.p[2]) - (n3 * upDir.p[1]);
			double c2=(n3 * upDir.p[0]) - (n1 * upDir.p[2]);
			double c3=(n1 * upDir.p[1]) - (n2 * upDir.p[0]);
			int pano_x=(int)((panoimg.getWidth()-1)*(viewDir.p[0]*c1+viewDir.p[1]*c2+viewDir.p[2]*c3+1)/2)+offsetx+x/3;
			while(pano_x>=panoimg.getWidth())pano_x-=panoimg.getWidth();
			while(pano_x<0)pano_x+=panoimg.getWidth();
			int pano_y=(int)((panoimg.getHeight()-1)*(n1*upDir.p[0]+n2*upDir.p[1]+n3*upDir.p[2]+1)/2)+offsety+y/3;
			while(pano_y>=panoimg.getHeight())pano_y-=panoimg.getHeight();
			while(pano_y<0)pano_y+=panoimg.getHeight();
			pano_raster.getPixel(pano_x, pano_y, colorVec2);
			mirr=mirror;
		}
		double amb=ambient;
		if(!AMBIENT) amb=0;
		double bri=brightness;
		if(!BRIGHTNESS) bri=0;
		double a=factor*250*bri+50*amb*factor2;
		colorVec2[0]=(int)(a*(colVec.p[0])*(256+(colorVec2[0]-256)*mirr)/256+factor3);
		colorVec2[1]=(int)(a*(colVec.p[1])*(256+(colorVec2[1]-256)*mirr)/256+factor3);
		colorVec2[2]=(int)(a*(colVec.p[2])*(256+(colorVec2[2]-256)*mirr)/256+factor3);
		if(colorVec2[0]>255)colorVec2[0]=255;
		if(colorVec2[1]>255)colorVec2[1]=255;
		if(colorVec2[2]>255)colorVec2[2]=255;
		if(colorVec2[0]<0)colorVec2[0]=0;
		if(colorVec2[1]<0)colorVec2[1]=0;
		if(colorVec2[2]<0)colorVec2[2]=0;
	}

	public void drawPoint(int x,int y, int z, int index) {
		setCol(mesh.getVertex(index),x,y);
		int z2=zBuffer[x][y];
		if(z2<=z) {
			zBuffer[x][y]=z;
			dispRaster.setPixel(x,y, colorVec2);
			indexBuffer[x][y]=index;
		}
	}

	 /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
		int modifiersEx = e.getModifiersEx();
		SHIFTKEY=((modifiersEx&64)>0);
		CRTLKEY=((modifiersEx&128)>0);
		ALTKEY=((modifiersEx&512)>0);
        // displayInfo(e, "KEY TYPED: ");
    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
		int modifiersEx = e.getModifiersEx();
		SHIFTKEY=((modifiersEx&64)>0);
		CRTLKEY=((modifiersEx&128)>0);
		ALTKEY=((modifiersEx&512)>0);
       // displayInfo(e, "KEY PRESSED: ");
    }

    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
		int modifiersEx = e.getModifiersEx();
		SHIFTKEY=((modifiersEx&64)>0);
		CRTLKEY=((modifiersEx&128)>0);
		ALTKEY=((modifiersEx&512)>0);
        // displayInfo(e, "KEY RELEASED: ");
    }

    private void displayInfo(KeyEvent e, String keyStatus){
        
        int id = e.getID();
        String keyString;
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
        } else {
            int keyCode = e.getKeyCode();
            keyString = "key code = " + keyCode
                    + " ("
                    + KeyEvent.getKeyText(keyCode)
                    + ")";
        }
        
        int modifiersEx = e.getModifiersEx();
        String modString = "extended modifiers = " + modifiersEx;
        String tmpString = KeyEvent.getModifiersExText(modifiersEx);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no extended modifiers)";
        }
        
        String actionString = "action key? ";
        if (e.isActionKey()) {
            actionString += "YES";
        } else {
            actionString += "NO";
        }
        
        String locationString = "key location: ";
        int location = e.getKeyLocation();
        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
            locationString += "standard";
        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
            locationString += "left";
        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
            locationString += "right";
        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
            locationString += "numpad";
        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
            locationString += "unknown";
        }
		
		System.out.println(keyString);
		System.out.println(modString);
		System.out.println(actionString);
		System.out.println(locationString);
        
    }
	
	public void mouseDragged(MouseEvent e) {

        requestFocusInWindow();
		Point point = e.getPoint();
		
        //timer=System.currentTimeMillis();
		boolean redraw = false;
		boolean imgRedraw=false;

        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);

		if(!SHIFTKEY) {
			if (rMouseDown) {
				if(lMouseDown){
					double rightDegrees = 0;
					double upDegrees = 0;
			
					if (point.x < lastClickPoint.x - 1) {
						rightDegrees = (double)(point.x - lastClickPoint.x);
					} else if (point.x > lastClickPoint.x + 1) {
						rightDegrees = (double)(point.x - lastClickPoint.x);
					}
					if (point.y < lastClickPoint.y - 1) {
						upDegrees = (double) (lastClickPoint.y - point.y);
					} else if (point.y > lastClickPoint.y + 1) {
						upDegrees = (double) (lastClickPoint.y - point.y);
					}
					relLightDir.rotateX(upDegrees);
					relLightDir.rotateY(-rightDegrees);
					imgRedraw=true;
				} else {
					if (point.y < lastClickPoint.y) {
						transformer.zoom(95.0);
						redraw = true;
					} else if (point.y > lastClickPoint.y) {
						transformer.zoom(105.0);
						redraw = true;
					}
				}
			// If only the left button, rotate around At point.
			} else if (lMouseDown) {
				double rightDegrees = 0;
				double upDegrees = 0;
		
				if (point.x < lastClickPoint.x - 1) {
					rightDegrees = (double)(point.x - lastClickPoint.x);
				} else if (point.x > lastClickPoint.x + 1) {
					rightDegrees = (double)(point.x - lastClickPoint.x);
				}
				if (point.y < lastClickPoint.y - 1) {
					upDegrees = (double) (lastClickPoint.y - point.y);
				} else if (point.y > lastClickPoint.y + 1) {
					upDegrees = (double) (lastClickPoint.y - point.y);
				}
				transformer.rotate(upDegrees, rightDegrees);
				redraw = true;
			}
		} else {
			if (rMouseDown) {
				if(lMouseDown) {
					offsetx-=point.x-lastClickPoint.x;
					offsety-=point.y-lastClickPoint.y;
					imgRedraw=true;
				} else {
					if (point.y < lastClickPoint.y) {
						transformer.vertigo(95.0);
						redraw = true;
					} else if (point.y > lastClickPoint.y) {
						transformer.vertigo(105.0);
						redraw = true;
					}
				}
			} else if (lMouseDown) {
				double rightDegrees = 0;
				double upDegrees = 0;
				Vector3D p1=transformer.imgToWorldCenteredInEye(lastClickPoint.x,lastClickPoint.y,1);
				Vector3D p2=transformer.imgToWorldCenteredInEye(point.x,point.y,1).sub(p1);
				Vector3D v=transformer.getAt().sub(transformer.getFrom());
				// Vector3D v2=v.scalarDivide(v.getlength());
				double factor=v.getLength()/transformer.EyeToWorld(new Vector3D(0,0,1)).getLength();
				// double factor=v2.dot(p1);
				p2.scalarMultiplyBy(factor);
				transformer.setFrom(transformer.getFrom().sub(p2));
				transformer.setAt(transformer.getAt().sub(p2));
				redraw = true;
			}
		}

        if (redraw) {
			progress=0;
            repaint();
		}
		if(imgRedraw) {
			// progress=0;
            // repaint();
			fullPaint();
		}

        // Save the current point for the next call.
        lastClickPoint = point;
    }

    public void mouseMoved(MouseEvent e) {
		requestFocusInWindow();	
        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
    }

    public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
		if(e.getClickCount()==2){
			if((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				transformer.setAt(mesh.getMean());
				progress=0;
				repaint();
			}else{
				Point p = e.getPoint();
				int newIndex=indexBuffer[p.x][p.y];
				if((newIndex==-1)&& (p.x>0))
					newIndex=indexBuffer[p.x-1][p.y];
				if((newIndex==-1)&& (p.x<indexBuffer.length-1))
					newIndex=indexBuffer[p.x+1][p.y];
				if((newIndex==-1)&& (p.y>0))
					newIndex=indexBuffer[p.x][p.y-1];
				if((newIndex==-1)&& (p.y<indexBuffer[0].length-1))
					newIndex=indexBuffer[p.x][p.y+1];
				if(newIndex>=0){
					transformer.setAt(mesh.getVertex(newIndex));
					progress=0;
					repaint();
				}
			}
		}
    }

    public void mouseEntered(MouseEvent e) {
		requestFocusInWindow();
        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
	}

    public void mouseExited(MouseEvent e) {
        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
    }

    public void mousePressed(MouseEvent e) {
		requestFocusInWindow();
        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
        timer=System.currentTimeMillis();
        lastClickPoint = e.getPoint();
    }

    public void mouseReleased(MouseEvent e) {
        lMouseDown = ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK);
        rMouseDown = ((e.getModifiers() & InputEvent.BUTTON3_MASK) != InputEvent.BUTTON3_MASK);
		iterationCounter=1;
    }

}