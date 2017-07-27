package Points2Map;

/// This calculates the eigenvalues and eigenvectors of a symmetric square 
/// matrix. No complex numbers involved in this case.
/// The relation q^T a q = d will hold.
/// \returns true on success, false on failure.

class Eigenvector {

	public static final int sgn ( double diff ) {
		if ( diff > 0 )	return 1;
		if ( diff < 0 )	return -1;
		else return 0;
	}

	public static double[][] SolveEigenSystem(double xx,double yy,double zz,double xy,double xz,double yz) {
		double[][] q   = new double[3][3];
		double[]   d   = new double[3];
		double[][] a   = {{xx,xy,xz}, {xy,yy,yz}, {xz,yz,zz}};
		if(SolveEigenSystem(a,q,d)) {
			//In Ausgabeformat übertragen:
			double[][] eig={q[0],q[1],q[2],d};
			//Nach Größe der Eigenwerte sortieren:
			if(Math.abs(d[1])>Math.abs(d[0])) {
					double dh=d[0]; d[0]=d[1]; d[1]=dh;
					double[] eigh=eig[0]; eig[0]=eig[1]; eig[1]=eigh;
			}
			if(Math.abs(d[2])>Math.abs(d[0])) {
					double dh=d[0]; d[0]=d[2]; d[2]=dh;
					double[] eigh=eig[0]; eig[0]=eig[2]; eig[2]=eigh;
			}
			if(Math.abs(d[2])>Math.abs(d[1])) {
					double dh=d[1]; d[1]=d[2]; d[2]=dh;
					double[] eigh=eig[1]; eig[1]=eig[2]; eig[2]=eigh;
			}
			return eig;
		} else {
			return new double[4][4];
		}
	}

	public static double[] GetEigenValues(double xx,double yy,double zz,double xy,double xz,double yz) {
		double[] q  = new double[3];
		double xys=xy*xy;
		double xzs=xz*xz;
		double yzs=yz*yz;
		double b=xx+yy+zz;
		double c=xys+xzs+yzs-xx*yy-xx*zz-yy*zz;
		double d=xx*yy*zz-xx*yzs-yy*xzs-zz*xys+2*xy*xz*yz;
		double x=-c-b*b/3;
		double y=-2*b*b*b/27-b*c/3-d;
		double z=y*y/4+x*x*x/27;
		double i=Math.sqrt(y*y/4-z);
		double j=Math.pow(-i,1/3);
		double k=Math.acos(-y/2/i);
		double m=Math.cos(k/3);
		double n=Math.sqrt(3)*Math.sin(k/3);
		double p=-b/3;
		q[0]=2*j*m+p;
		q[1]=-j*(m+n)+p;
		q[2]=-j*(m-n)+p;
		return q;
	}
	
	public static double eigVal1(double[][] eig) {
		return eig[3][0];
	}

	public static double eigVal2(double[][] eig) {
		return eig[3][1];
	}

	public static double eigVal3(double[][] eig) {
		return eig[3][2];
	}

	public static Vector3D eigVec1(double[][] eig) {
		return new Vector3D(eig[0]);
	}

	public static Vector3D eigVec2(double[][] eig) {
		return new Vector3D(eig[1]);
	}

	public static Vector3D eigVec3(double[][] eig) {
		return new Vector3D(eig[2]);
	}


	public static boolean SolveEigenSystem(double[][] a,double[][] q,double[] d) {
		boolean ok=true;
		if(a.length!=q.length)ok=false;
		if(a.length!=d.length)ok=false;
		for(int i=0;i<a.length;i++)
			if((a[i].length!=a.length)||(q[i].length!=a.length)) ok=false;
		if(!ok) {
			System.out.println("Fehler! Matrizen haben nicht übereinstimmende Größen.");
			return false;
		}
			// First perform a householder tri-diagonalisation, makes the following QR // iterations better...  
		for (int k=0;k<a.length-2;k++)  {
			// Calculate householder vector, store it in the subdiagonal...   
			// (Replacing first value of v (Which is always 1) with beta.)   
			// (For off diagonal symmetric entries only fill in super-diagonal.)
			double sigma = 0;
			double x0 = a[k][k+1];
			for (int r=k+2;r<a.length;r++)
				sigma += a[k][r]*a[k][r];
			if (sigma==0.0) 
				a[k][k+1] = 0.0;
			else    {
				double mu = Math.sqrt(a[k][k+1]*a[k][k+1] + sigma);
				if (a[k][k+1]<=0) 
					a[k][k+1] = a[k][k+1] - mu;
				else 
					a[k][k+1] = -sigma/(a[k][k+1] + mu);
				for (int r=k+2;r<a.length;r++) 
					a[k][r] /= a[k][k+1];
				a[k][k+1] = 2.0 * a[k][k+1]*a[k][k+1]/(sigma + a[k][k+1]*a[k][k+1]);
			}
			// Set the symmetric entry, needs info from above...
			a[k+1][k] = Math.sqrt(sigma + x0*x0);
			// Update the matrix with the householder transform (Make use of symmetry)...    
			// Calculate p/beta, store in d...
			for (int c=k+1;c<a.length;c++)     {
				d[c] = a[k+1][c];
				// First entry of v is 1.
				for (int r=k+2;r<a.length;r++) 
					d[c] += a[k][r] * a[r][c];
			}
			// Calculate w, replace p with it in d...
			double mult = d[k+1];
			for (int r=k+2;r<a.length;r++)
				mult += a[k][r] * d[r];
			mult *= a[k][k+1]*a[k][k+1] / 2.0;
			d[k+1] = a[k][k+1] * d[k+1] - mult;
			for (int c=k+2;c<a.length;c++)
				d[c] = a[k][k+1] * d[c] - mult * a[k][c];
			// Apply the update - make use of symmetry by only calculating the lower     
			// triangular set...     
			// First column where first entry of v being 1 matters...
			a[k+1][k+1] -= 2.0 * d[k+1];
			for (int r=k+2;r<a.length;r++) 
				a[k+1][r] -= a[k][r] * d[k+1] + d[r];
			// Remaining columns...
			for (int c=k+2;c<a[0].length;c++)      
				for (int r=c;r<a.length;r++)       
					a[c][r] -= a[k][r] * d[c] + a[k][c] * d[r];
			// Do the mirroring...      
			for (int r=k+1;r<a.length;r++)      
				for (int c=r+1;c<a[0].length;c++) 
					a[c][r] = a[r][c];
		} 
		// Use the stored sub-diagonal house-holder vectors to initialise q...  
		for(int i=0;i<q.length;i++)
			for(int j=0;j<q[i].length;j++)
				q[i][j]=((i==j)?1:0);
		
		for (int k=a[0].length-3;k>=0;k--)  {
			// Arrange for v to start with 1 - avoids special cases...    
			double beta = a[k][k+1];
			a[k][k+1] = 1;
			// Update q, column by column...    
			for (int c=k+1;c<q.length;c++)    {
				// Copy column to temporary storage...      
				for (int r=k+1;r<q.length;r++) 
					d[r] = q[c][r];
				// Update each row in column...      
				for (int r=k+1;r<q.length;r++)      {
					double mult = beta * a[k][r];
					for (int i=k+1;i<q.length;i++) 
						q[c][r] -= mult * a[k][i] * d[i];
				}    
			}  
		} 
		// Now perform QR iterations till we have a diagonalised - at which point it  
		// will be the eigenvalues... (Update q as we go.)  
		// These parameters decide how many iterations are required...   
		double epsilon = 1e-6;
		int max_iters = 64;
		// Maximum iters per value pair.   
		int iters = 0;
		// Number of iterations done on current value pair.   
		boolean all_good = true;
		// Return value -set to false if iters ever reaches max_iters.   
		// Range of sub-matrix being processed - start is inclusive, end exclusive.   
		int start = a.length;
		// Set to force recalculate.   
		int end = a.length;
		// (Remember that below code ignores the sub-diagonal, as its a mirror of the super diagonal.)  
		while (true)  {   
			// Move end up as far as possible, finish if done...    
			int pend = end;
			while (true)    {     
				int em1 = end-1;
				int em2 = end-2;
				double tol = epsilon*(Math.abs(a[em2][em2]) + Math.abs(a[em1][em1]));
				if (Math.abs(a[em1][em2])<tol)     {      
					end -= 1;
					if (end<2) break;
				} else break;
			}
			if (pend==end)    {
				iters += 1;
				if (iters==max_iters)     {
					all_good = false;
					if (end==2) break;
					iters = 0;
					end -= 1;
					continue;
				}    
			}    else    {
				if (end<2) break;
				iters = 0;
			}   
			// If end has caught up with start recalculate it...    
			if ((start+2)>end)    {
				start = end-2;
				while (start>0)     {
					int sm1 = start-1;
					double tol = epsilon*(Math.abs(a[sm1][sm1]) + Math.abs(a[start][start]));
					if (Math.abs(a[start][sm1])>=tol) 
						start -= 1;
					else break;
				}   
			}   
			// Do the QR step, with lots of juicy optimisation...    
			// Calculate eigenvalue of trailing 2x2 matrix...     
			int em1 = end-1;
			int em2 = end-2;
			double temp = (a[em2][em2] - a[em1][em1]) / 2;
			double div = temp + sgn(temp) * Math.sqrt(temp*temp + a[em1][em2]*a[em1][em2]);
			double tev = a[em1][em1] - a[em1][em2]*a[em1][em2]/div;
			// Calculate and apply relevant sequence of givens transforms to    
			// flow the numbers down the super/sub-diagonals...     
			double x = a[start][start] - tev;
			double z = a[start+1][start];
			for (int k=start;;k++)     {
				// Calculate givens transform...       
				double gc = 1;
				double gs = 0;
				if (z!=0.0) {        
					if (Math.abs(z)>Math.abs(x))        {         
						double r = -x/z;
						gs = 1/Math.sqrt(1+r*r);
						gc = gs * r;
					} else {
						double r = -z/x;
						gc = 1/Math.sqrt(1+r*r);
						gs = gc * r;
					}
				}
				double gcc = gc*gc;
				double gss = gs*gs;
				// Update matrix q (Post multiply)...       
				for (int r=0;r<q.length;r++)       {
					double ck  = q[k][r];
					double ck1 = q[k+1][r];
					q[k][r]   = gc*ck - gs*ck1;
					q[k+1][r] = gs*ck + gc*ck1;
				}      
				// Update matrix a...       
				// Conditional on not being at start of range...        
				if (k!=start) 
					a[k][k-1] = gc*x - gs*z;
			   // Non-conditional...       
				double e = a[k][k];
				double f = a[k+1][k+1];
				double i = a[k+1][k];
				a[k][k] = gcc*e + gss*f - 2*gc*gs*i;
				a[k+1][k+1] = gss*e + gcc*f + 2*gc*gs*i;
				a[k+1][k] = gc*gs*(e-f) + (gcc - gss)*i;
				x = a[k+1][k];
				// Conditional on not being at end of range...        
				if (k!=end-2)        {         
					z = -gs*a[k+2][k+1];
					// a[k+2][k]
					a[k+2][k+1] *= gc;
				} else break;
			}  
		}
		// Fill in the diagonal...   
		for (int i=0;i<d.length;i++) 
			d[i] = a[i][i];
		return all_good;
	}
}