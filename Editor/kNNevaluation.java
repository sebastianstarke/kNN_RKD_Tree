package Points2Map;

import java.util.*;

public class kNNevaluation {
	
	public kNNevaluation()
	{
	}

	public static void main(String[] args)
		{   
		
		}
	
	/* Computes the approximate mean data for a kNN-Guess */
	public Double[] getMeanData(int start, int end, int neighbors, Vertex[] vertices, MaxHeap[] kNearestNeighbors)
	{
		/* Initializes all required variables */
		Double[] meanData = new Double[14];
		
		Double incompleteHeaps = 0.0;
		Double valOfMisestimatedHeaps = 0.0;
		Double valOfEstimatedHeaps = 0.0;

		Double valOfRegardedNeighbors = 0.0 + (end-start+1)*neighbors;
		Double valOfEstimatedNeighbors = 0.0;
		Double valOfCorrectlyEstimatedNeighbors = 0.0;
		Double valOfMisestimatedNeighbors = 0.0;
		
		Double meanEstimationError = 0.0;
		Double meanEstimationErrorIfMisestimatedNotHit = 0.0;
		Double meanEstimationErrorIfMisestimatedWorst = 0.0;
		Double overallDistExactNeighbors = 0.0;
		Double overallDistNotHitExactNeighbors = 0.0;
		Double overallDistEstimatedNeighbors = 0.0;
		Double overallDistMisestimatedNeighbors = 0.0;
		Double overallDistWorstExactNeighbor = 0.0;
		
		Double averageDistExactNeighbors = 0.0;
		Double averageDistNotHitExactNeighbors = 0.0;
		Double averageDistEstimatedNeighbors = 0.0;
		Double averageDistMisestimatedNeighbors = 0.0;
		Double averageDistWorstExactNeighbor = 0.0;
		
		/* Sets the interval for the evaluation of mean data */
		for(int i=start; i<=end; i++)
		{
			if(kNearestNeighbors[i].heap_current_neighbors != kNearestNeighbors[i].heap_max_neighbors)
			{
					incompleteHeaps++;
			}
			
			List<Vertex> exactNeighbors = new MaxHeap(neighbors, vertices[i], vertices).getListOfNearestNeighbors();
			List<Vertex> estimatedNeighbors = ((MaxHeap)kNearestNeighbors[i]).getListOfNearestNeighbors();
			
			//Average of all distances regarding the MaxHeaps exact k-Nearest-Neighbors
			double distExactNeighbors = 0.0;
			for(int j=0; j<exactNeighbors.size(); j++)
			{
				distExactNeighbors = distExactNeighbors+exactNeighbors.get(j).dist(vertices[i]);
			}
			
			//Average of all distances regarding the MaxHeaps approximated k-Nearest-Neighbors
			double distEstimatedNeighbors = 0.0;
			for(int j=0; j<estimatedNeighbors.size(); j++)
			{
				valOfEstimatedNeighbors++;
				distEstimatedNeighbors = distEstimatedNeighbors+estimatedNeighbors.get(j).dist(vertices[i]);
			}
			
			//Add distances if the MaxHeap is defined by at least one neighbor
			if(estimatedNeighbors.size() != 0)
			{
				valOfEstimatedHeaps++;
				
				distExactNeighbors = distExactNeighbors/exactNeighbors.size();
				overallDistExactNeighbors = overallDistExactNeighbors+distExactNeighbors;
				
				distEstimatedNeighbors = distEstimatedNeighbors/estimatedNeighbors.size();
				overallDistEstimatedNeighbors = overallDistEstimatedNeighbors+distEstimatedNeighbors;
			}
			
			//Counts correct estimated neighbors, drops out all hit neighbors and transfers misestimated ones into another array
			List<Vertex> misestimatedNeighbors = new ArrayList<Vertex>();
			for(int j=0; j<estimatedNeighbors.size(); j++) {
				if(exactNeighbors.contains(estimatedNeighbors.get(j))) {
					valOfCorrectlyEstimatedNeighbors++;
					exactNeighbors.remove(estimatedNeighbors.get(j));
				}
				else
				{
					misestimatedNeighbors.add(estimatedNeighbors.get(j));
					valOfMisestimatedNeighbors++;
				}
			}
			
			//exactNeighbors was previously modified such that it only remains neighbors that have not been hit
			List<Vertex> notHitExactNeighbors = exactNeighbors;
			if(misestimatedNeighbors.size() != 0)
			{
				valOfMisestimatedHeaps++;
			
				//Average of all distances regarding the MaxHeaps misestimated k-Nearest-Neighbors
				double distMisestimatedNeighbors = 0.0;
				for(int j=0; j<misestimatedNeighbors.size(); j++)
				{
					distMisestimatedNeighbors = distMisestimatedNeighbors + misestimatedNeighbors.get(j).dist(vertices[i]);
				}
				distMisestimatedNeighbors = distMisestimatedNeighbors/misestimatedNeighbors.size();
				overallDistMisestimatedNeighbors = overallDistMisestimatedNeighbors + distMisestimatedNeighbors;
				
				//Average distance for exact neighbors that have not been estimated
				double distNotHitExactNeighbors = 0.0;
				for(int j=0; j<notHitExactNeighbors.size(); j++)
				{
					distNotHitExactNeighbors = distNotHitExactNeighbors + notHitExactNeighbors.get(j).dist(vertices[i]);
				}
				distNotHitExactNeighbors = distNotHitExactNeighbors/notHitExactNeighbors.size();
				overallDistNotHitExactNeighbors = overallDistNotHitExactNeighbors + distNotHitExactNeighbors;
				
				//Distance regarding the MaxHeaps worst exact k-Nearest-Neighbor
				overallDistWorstExactNeighbor = overallDistWorstExactNeighbor + exactNeighbors.get(0).dist(vertices[i]);
			}
		}
		
		//Average distance regarding all MaxHeaps worst neighbors
		averageDistWorstExactNeighbor = overallDistWorstExactNeighbor/(valOfMisestimatedHeaps);
		
		//Computes the mean estimation error
		if(valOfEstimatedHeaps != 0.0)
		{
		averageDistExactNeighbors = overallDistExactNeighbors/valOfEstimatedHeaps;
		averageDistEstimatedNeighbors = overallDistEstimatedNeighbors/valOfEstimatedHeaps;
		meanEstimationError = averageDistEstimatedNeighbors/averageDistExactNeighbors;
		}
		else
		{
			meanEstimationError = 1.0;
		}
		
		//Computes the mean estimation error regarding only misestimated neighbors 
		if(valOfMisestimatedHeaps != 0.0)
		{
			averageDistNotHitExactNeighbors = overallDistNotHitExactNeighbors/valOfMisestimatedHeaps;
			averageDistMisestimatedNeighbors = overallDistMisestimatedNeighbors/valOfMisestimatedHeaps;
			meanEstimationErrorIfMisestimatedNotHit = averageDistMisestimatedNeighbors/averageDistNotHitExactNeighbors;
			meanEstimationErrorIfMisestimatedWorst = averageDistMisestimatedNeighbors/averageDistWorstExactNeighbor;
		}
		else
		{
			meanEstimationErrorIfMisestimatedNotHit = 1.0;
			meanEstimationErrorIfMisestimatedWorst = 1.0;
		}

		//Fills the to be returned array with the mean data
		meanData[0] = valOfCorrectlyEstimatedNeighbors;
		meanData[1] = valOfRegardedNeighbors;
		meanData[2] = averageDistEstimatedNeighbors;
		meanData[3] = averageDistExactNeighbors;
		meanData[4] = valOfMisestimatedHeaps;
		meanData[5] = meanEstimationErrorIfMisestimatedNotHit;
		meanData[6] = valOfEstimatedNeighbors;
		meanData[7] = incompleteHeaps;
		meanData[8] = valOfMisestimatedNeighbors;
		meanData[9] = meanEstimationError;
		meanData[10] = averageDistNotHitExactNeighbors;
		meanData[11] = averageDistMisestimatedNeighbors;
		meanData[12] = averageDistWorstExactNeighbor;
		meanData[13] = meanEstimationErrorIfMisestimatedWorst;
		
		return meanData;
	}
	
	/* Computes the internal operation data of MaxHeaps for a kNN-Guess */
	public Object[] heapOperationData(int totalnumVertices, MaxHeap[] kNearestNeighbors)
	{
		/* Initializes all required variables */
		Object[] heapOperationData = new Object[10];
		Double incompleteHeaps = 0.0;
		Double loopInitiationsDuplicate = 0.0;
		Double loopIterationsDuplicate = 0.0;
		Double loopInitiationsRemove = 0.0;
		Double loopIterationsRemove = 0.0;
		Double loopInitiationsAdd = 0.0;
		Double loopIterationsAdd = 0.0;
		long timerDuplicate=0, timerRemove=0, timerAdd=0;
		
		//Collects the counting variables inside all MaxHeaps
		for(int i=0; i<totalnumVertices; i++)
		{
			if(kNearestNeighbors[i].heap_current_neighbors != kNearestNeighbors[i].heap_max_neighbors)
			{
				incompleteHeaps++;
			}
			loopIterationsDuplicate = loopIterationsDuplicate+kNearestNeighbors[i].loopIterationsDuplicate;
			loopInitiationsDuplicate = loopInitiationsDuplicate+kNearestNeighbors[i].loopInitiationsDuplicate;
			loopIterationsRemove = loopIterationsRemove+kNearestNeighbors[i].loopIterationsReplace;
			loopInitiationsRemove = loopInitiationsRemove+kNearestNeighbors[i].loopInitiationsReplace;
			loopIterationsAdd = loopIterationsAdd+kNearestNeighbors[i].loopIterationsAdd;
			loopInitiationsAdd = loopInitiationsAdd+kNearestNeighbors[i].loopInitiationsAdd;
			timerDuplicate = timerDuplicate+kNearestNeighbors[i].timerDuplicate;
			timerRemove = timerRemove+kNearestNeighbors[i].timerReplace;
			timerAdd = timerAdd+kNearestNeighbors[i].timerAdd;
			
			kNearestNeighbors[i].resetEvaluationData();
		}

		//Fills the to be returned array with the operation data of MaxHeaps
		heapOperationData[0] = incompleteHeaps;
		heapOperationData[1] = loopInitiationsDuplicate;
		heapOperationData[2] = loopIterationsDuplicate;
		heapOperationData[3] = loopInitiationsRemove;
		heapOperationData[4] = loopIterationsRemove;
		heapOperationData[5] = loopInitiationsAdd;
		heapOperationData[6] = loopIterationsAdd;
		heapOperationData[7] = timerDuplicate;
		heapOperationData[8] = timerRemove;
		heapOperationData[9] = timerAdd;

		return heapOperationData;
	}
	
	public double kNNmeanValueOfOnePoint(int neighbors, MaxHeap kNN, Vertex anchor, Vertex[] vertices) {
		double valOfCorrectlyEstimatedNeighbors = 0.0;
		double valOfNeighbors = neighbors;
		
		MaxHeap exactkNN = new MaxHeap(neighbors);
		for(int i=0; i<vertices.length; i++) {
			exactkNN.addLinearSearch(anchor.squaredDist(vertices[i]), vertices[i]);
		}
		
		List<Vertex> exactNeighbors = exactkNN.getListOfNearestNeighbors();
		List<Vertex> estimatedNeighbors = kNN.getListOfNearestNeighbors();
		
		for(int i=0; i<estimatedNeighbors.size(); i++) {
			if(exactNeighbors.contains(estimatedNeighbors.get(i))) {
				valOfCorrectlyEstimatedNeighbors++;
			}
		}
		
		return valOfCorrectlyEstimatedNeighbors/valOfNeighbors;
	}
}