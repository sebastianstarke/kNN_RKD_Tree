package Points2Map;

import java.util.ArrayList;
import java.util.List;

public class MaxHeap{
	/* Data structures for the neighbors and their distances */
    private Vertex[] heap_neighbors;
    private double[] heap_squaredDists;
   
    /* Current and maximum value of nearest neighbors */
    public final int heap_max_neighbors;
    public int heap_current_neighbors;
    
    /* Variables used to swap 'neighbors' and 'squaredDists' in Arrays */
    Vertex v_tmp;
    double d_tmp;
    
    /* Information, whether the MaxHeap already contains a new probable nearest neighbor */
    private int contains_neighbor;
    
    /*Evaluation variables for internal operations of MaxHeap*/
    public long	timerDuplicate, timerReplace, timerAdd;
    public double loopIterationsDuplicate, loopInitiationsDuplicate,
    			  loopIterationsReplace, loopInitiationsReplace,
    			  loopIterationsAdd, loopInitiationsAdd;
    public boolean added;
    //
    
    /* Initializes the kNN data structures */
    public MaxHeap(int max_neighbors) {
    	heap_max_neighbors = max_neighbors;
    	heap_neighbors = new Vertex[max_neighbors];
    	heap_squaredDists = new double[max_neighbors];
    }
    
    /* Initializes the kNN data structures and continues with linear search */
    public MaxHeap(int max_neighbors, Vertex anchor, Vertex[] vertices) {
    	heap_max_neighbors = max_neighbors;
    	heap_neighbors = new Vertex[max_neighbors];
    	heap_squaredDists = new double[max_neighbors];
    	
    	for(int i=0; i<vertices.length; i++)
    	{
    		addLinearSearch(anchor.squaredDist(vertices[i]),vertices[i]);
    	}
    }
    
    /* Processes a new possible nearest neighbor */
    public void add(double squaredDist, Vertex neighbor){
    	/* Try to add new neighbor if not the neighbor itself */
	    if(squaredDist != 0) {
    		/* Initiate to check neighbors in MaxHeap */
	    	if(heap_current_neighbors != heap_max_neighbors || squaredDist < heap_squaredDists[0]) {
	    		/* Check whether MaxHeap already contains the new neighbor */
	    		contains_neighbor=0;
		    	for(int i=0; i<heap_current_neighbors; i++) {
		    		if(neighbor == heap_neighbors[i])
		    		{
		    			contains_neighbor = 1;
		    			break;
		    		}
		    	}
	
		    	/* Adding new neighbor if not already existing... */
		    	if(contains_neighbor == 0)
		    	{
		    		/* Replacing worst neighbor if MaxHeap is already filled with neighbors */
			    	if(heap_current_neighbors == heap_max_neighbors)
				    	{
			    			//Immediately Replaces the worst-Nearest-Neighbor
			    			heap_neighbors[0] = neighbor;
			    			heap_squaredDists[0] = squaredDist;
			    			//Performs Max-Heapify
			    			int leftChild, rightChild, pivot, pos=0;
			    			while(true)
			    			{
				    	    	leftChild = 2*pos+1;
				    	    	rightChild = 2*pos+2;
				    	    	pivot = pos;
				    	    	if(leftChild<heap_max_neighbors && heap_squaredDists[leftChild]>heap_squaredDists[pivot])
				    	    	{
				    	    		pivot = leftChild;
				    	    	}
				    	    	if(rightChild<heap_max_neighbors && heap_squaredDists[rightChild] > heap_squaredDists[pivot])
				    	    	{
				    	    		pivot = rightChild;
				    	    	}
				    	    	if(pivot != pos)
				    	    	{
				    	    		v_tmp = heap_neighbors[pos];
				    	    		heap_neighbors[pos]=heap_neighbors[pivot];
				    	    		heap_neighbors[pivot] = v_tmp;
				    	    		
				    	    		d_tmp = heap_squaredDists[pos];
				    	    		heap_squaredDists[pos]=heap_squaredDists[pivot];
				    	    		heap_squaredDists[pivot] = d_tmp;
				    	    		
				    	    		pos = pivot;
				    	    	}
				    	    	else{
				    	    		break;
				    	    	}
			    			}
				    	}
			    	else{
				   	/* Inserting new neighbor into MaxHeap */
			    	int parent, pos=heap_current_neighbors;
			    	while (pos>0){
			    		parent = (pos-1)/2;
				        if(squaredDist < heap_squaredDists[parent]){
				        	break;
				        }
				        heap_neighbors[pos] = heap_neighbors[parent];
				        heap_squaredDists[pos] = heap_squaredDists[parent];
				        pos=parent;
			        }
			        heap_neighbors[pos] = neighbor;
			        heap_squaredDists[pos] = squaredDist;
			        heap_current_neighbors++;
			    	}
		    	}
	    	}
	    }
    }
    
    /* Processes a new possible nearest neighbor regarding the evaluation */
    public boolean addWithEvaluation(double squaredDist, Vertex neighbor){
    long timer;
    added=false;
	    /* Try to add new neighbor if not the neighbor itself */
	    if(squaredDist != 0)
		    {
	    		/* Initiate to check neighbors in MaxHeap */
		    	if(heap_current_neighbors != heap_max_neighbors || squaredDist < heap_squaredDists[0])
		    	{
		    		/* Check whether MaxHeap already contains the new neighbor */
		    		loopInitiationsDuplicate++;
		    		timer = System.nanoTime();
		    		
		    		contains_neighbor=0;
			    	for(int i=0; i<heap_current_neighbors; i++) {
			    		loopIterationsDuplicate++;
			    		if(neighbor == heap_neighbors[i])
			    		{
			    			contains_neighbor = 1;
			    			break;
			    		}
			    	}
			    	timerDuplicate=timerDuplicate+(System.nanoTime()-timer);
		
			    	/* Adding new neighbor if not already existing... */
			    	if(contains_neighbor == 0)
			    	{
			    		added=true;
			    		/* Replacing worst neighbor if MaxHeap is already filled with neighbors */
				    	if(heap_current_neighbors == heap_max_neighbors)
					    	{
				    			loopInitiationsReplace++;
				    			timer = System.nanoTime();
				    			
				    			//Immediately Replaces the worst-Nearest-Neighbor
				    			heap_neighbors[0] = neighbor;
				    			heap_squaredDists[0] = squaredDist;
				    			//Performs Max-Heapify
				    			int leftChild, rightChild, pivot, pos=0;
				    			while(true) {
				    				loopIterationsReplace++;
					    	    	leftChild = 2*pos+1;
					    	    	rightChild = 2*pos+2;
					    	    	pivot = pos;
					    	    	if(leftChild<heap_max_neighbors && heap_squaredDists[leftChild]>heap_squaredDists[pivot])
					    	    	{
					    	    		pivot = leftChild;
					    	    	}
					    	    	if(rightChild<heap_max_neighbors && heap_squaredDists[rightChild] > heap_squaredDists[pivot])
					    	    	{
					    	    		pivot = rightChild;
					    	    	}
					    	    	if(pivot != pos)
					    	    	{
					    	    		v_tmp = heap_neighbors[pos];
					    	    		heap_neighbors[pos]=heap_neighbors[pivot];
					    	    		heap_neighbors[pivot] = v_tmp;
					    	    		
					    	    		d_tmp = heap_squaredDists[pos];
					    	    		heap_squaredDists[pos]=heap_squaredDists[pivot];
					    	    		heap_squaredDists[pivot] = d_tmp;
					    	    		
					    	    		pos = pivot;
					    	    	}
					    	    	else{
					    	    		break;
					    	    	}
				    			} 
						        timerReplace=timerReplace+(System.nanoTime()-timer);
					    	}
				    	else {
				    	/* Inserting new neighbor into MaxHeap */
				    	loopInitiationsAdd++;
				    	timer = System.nanoTime();
				    	
			    		int parent, pos=heap_current_neighbors;
			    		while (pos>0){
			    			loopIterationsAdd++;
			    			parent = (pos-1)/2;
			    			if(squaredDist < heap_squaredDists[parent]){
			    				break;
			    			}
			    			heap_neighbors[pos] = heap_neighbors[parent];
			    			heap_squaredDists[pos] = heap_squaredDists[parent];
			    			pos=parent;
			    		}
			    		heap_neighbors[pos] = neighbor;
			    		heap_squaredDists[pos] = squaredDist;
			    		heap_current_neighbors++;
				        
				        timerAdd=timerAdd+(System.nanoTime()-timer);
				    	}
			    	}
		    	}
		    }
	    return added;
    }
    
    /* Processes a new possible nearest neighbor regarding no duplicate detection */
    public void addLinearSearch(double squaredDist, Vertex neighbor){
	    /* Try to add new neighbor if not the neighbor itself */
	    if(squaredDist != 0)
		    {
	    		/* Initiate to check neighbors in MaxHeap */
	    		if(heap_current_neighbors != heap_max_neighbors || squaredDist < heap_squaredDists[0])
	    		{
		    		/* Replacing worst neighbor if MaxHeap is already filled with neighbors */
			    	if(heap_current_neighbors == heap_max_neighbors)
				    	{
			    			//Immediately Replaces the worst-Nearest-Neighbor
			    			heap_neighbors[0] = neighbor;
			    			heap_squaredDists[0] = squaredDist;
			    			//Performs Max-Heapify
			    			int leftChild, rightChild, pivot, pos=0;
			    			while(true)
			    			{
				    	    	leftChild = 2*pos+1;
				    	    	rightChild = 2*pos+2;
				    	    	pivot = pos;
				    	    	if(leftChild<heap_max_neighbors && heap_squaredDists[leftChild]>heap_squaredDists[pivot])
				    	    	{
				    	    		pivot = leftChild;
				    	    	}
				    	    	if(rightChild<heap_max_neighbors && heap_squaredDists[rightChild] > heap_squaredDists[pivot])
				    	    	{
				    	    		pivot = rightChild;
				    	    	}
				    	    	if(pivot != pos)
				    	    	{
				    	    		v_tmp = heap_neighbors[pos];
				    	    		heap_neighbors[pos]=heap_neighbors[pivot];
				    	    		heap_neighbors[pivot] = v_tmp;
				    	    		
				    	    		d_tmp = heap_squaredDists[pos];
				    	    		heap_squaredDists[pos]=heap_squaredDists[pivot];
				    	    		heap_squaredDists[pivot] = d_tmp;
				    	    		
				    	    		pos = pivot;
				    	    	}
				    	    	else{
				    	    		break;
				    	    	}
			    			}
				    	}
			    	else{
				   	/* Inserting new neighbor into MaxHeap */
			    	int parent, pos=heap_current_neighbors;
			    	while (pos>0){
			    		parent = (pos-1)/2;
				        if(squaredDist < heap_squaredDists[parent]){
				        	break;
				        }
				        heap_neighbors[pos] = heap_neighbors[parent];
				        heap_squaredDists[pos] = heap_squaredDists[parent];
				        pos=parent;
			        }
			        heap_neighbors[pos] = neighbor;
			        heap_squaredDists[pos] = squaredDist;
			        heap_current_neighbors++;
			    	}
		    	}
	    	
    	}
    }
    
    
    public void resetEvaluationData()
    {
    	loopIterationsDuplicate=loopInitiationsDuplicate=loopIterationsReplace=loopInitiationsReplace=loopInitiationsAdd=loopIterationsAdd=0.0;
    	timerDuplicate=timerReplace=timerAdd=0;
    }
    
    /* Returns an ArrayList containing the neighbors inside the MaxHeap */
    public List<Vertex> getListOfNearestNeighbors()
    {
    	List<Vertex> nearestNeighbors = new ArrayList<Vertex>();
    	for(int i=0; i<heap_current_neighbors; i++)
    	{
    		nearestNeighbors.add(i, heap_neighbors[i]);	
    	}
    	return nearestNeighbors;
    }

    public void outputConsole() {
    	for(int i=0; i<heap_current_neighbors; i++)
    	{
    		System.out.println("Neighbor: " + heap_neighbors[i] + " SquaredDistance: " + heap_squaredDists[i]); 
    	}
    }
}