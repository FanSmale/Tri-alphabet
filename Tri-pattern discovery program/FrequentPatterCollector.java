package tripattern;

import java.util.Arrays;

public class FrequentPatterCollector {
	
	private int [][] PatternContainer;
	private double []  FrequencyContainer;
	private int PatternCounter;
	
	public FrequentPatterCollector(){
		PatternContainer = new int [1000][];
		FrequencyContainer = new double [1000];
		PatternCounter = 0;
	}

	public void CollecteFrequencyAndPater(double Frequency,int [] Pattern){
		FrequencyContainer[PatternCounter] = Frequency;
		PatternContainer[PatternCounter] = Pattern;
		PatternCounter ++;
	}
/*
	public void SortThePatternByTheFrequency(){
		double TempFrequency;
		int [] TempPattern;
		for(int i = 0; i < PatternCounter; i ++){
			for(int j = i ; j < PatternCounter; j ++){
				if(FrequencyContainer[i] < FrequencyContainer[j]){
					//change the Frequency
					TempFrequency = FrequencyContainer[i];
					FrequencyContainer[i] = FrequencyContainer[j];
					FrequencyContainer[j] = TempFrequency;
					
					//change the pattern position
					TempPattern = PatternContainer[i];
					PatternContainer[i] = PatternContainer[j];
					PatternContainer[j] = TempPattern;
				}				
			}
		}
	}
	*/
	
	int partion(double array[],int low,int high){
	    
	    double pivotkey = array[low];
	    int pivotPattern[] = PatternContainer[low];
	    
	    while (low < high) {
	        
	        while (array[high] <= pivotkey && low < high) {
	            high --;
	        }
	        array[low] = array[high];
	        PatternContainer[low] = PatternContainer[high];
	        
	        while (array[low] >= pivotkey && low < high) {
	            low ++;
	        }
	        array[high] = array[low];
	        PatternContainer[high] = PatternContainer[low];
	    }
	    
	    array[low] = pivotkey;
	    PatternContainer[low] = pivotPattern;
	    
	    return low;
	}
	void QSort(double array[],int low,int high){
	    int pivotloc ;
	    
	    if(low < high){
	        pivotloc = partion(array, low, high);
	        QSort(array, low, pivotloc - 1);
	        QSort(array, pivotloc + 1, high);
	    }
	}
	public void SortThePatternByTheFrequency(){
		QSort(FrequencyContainer,0,PatternCounter - 1);
	}
	
	
	public void FreeTheMemory(){
		PatternCounter = 0;
	}
	
	public void PrintTheTopKPattern(int TopKnum){
		System.out.println("this is the Top " + TopKnum + " patterns");
		for(int i = 0; i < TopKnum; i ++) System.out.println( Arrays.toString(PatternContainer[i]));		
	}
	
	public int [][] GetTheTopKPattern(int TopKnum,int patternLen){
		int [][] TempTopKnumPattern = new int [TopKnum][];
		int [][] FinalTopKnumPattern;
		
		int k = 0;
		for(int i = 0; i < PatternCounter && k < TopKnum; i ++){			
			if(PatternContainer[i].length == patternLen){
				TempTopKnumPattern[k] = PatternContainer[i];
				k ++;
			}	
		}
	
		FinalTopKnumPattern = new int [k][];
		
		for(int i = 0 ; i < k ; i ++){
			FinalTopKnumPattern[i] = TempTopKnumPattern[i];
		}
		
		
		return FinalTopKnumPattern;
	}
}
