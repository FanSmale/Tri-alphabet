package tripattern;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import weka.core.Attribute;
import weka.core.Instances;

public class SemiWildCard extends Instances {
	
	public static int MAX_LENGTH = 10;
	static int TopK;
	int [][][] frequentSubsequences;
	
	CodeTable Table;
	FrequentPatterCollector Collecter;
	
	public double frequencyThreshold;

	public int wildcardGapLowerBound;
	public int wildcardGapUpperBound;
	
	public SemiWildCard(Instances paraInstances) {
		super(paraInstances);
	}

	public void SetGap(int parawildcardGapLowerBound,int parawildcardGapUpperBound){
		if(parawildcardGapLowerBound > parawildcardGapUpperBound 
				|| parawildcardGapLowerBound < 0
				|| parawildcardGapUpperBound < 0){
			
			System.out.println("error set the gaps");
			System.exit(1);
		}
		wildcardGapLowerBound = parawildcardGapLowerBound;
		wildcardGapUpperBound = parawildcardGapUpperBound;
	}
	
	public void SetfrequencyThreshold(double paraThreshold){
		if(paraThreshold < 0){
			System.out.print("error set the Threshold");
			System.exit(1);
		}
		
		frequencyThreshold = paraThreshold;
	}
	
	public void SetTopK(int paraTopK){
		if(TopK < 0){
			System.out.print("error set the TopK");
			System.exit(1);
		}
		
		TopK = paraTopK;
	}
	
	public SemiWildCard(Reader paraReader,int LowerBound,int UpperBound,double Threshold,int TopK,CodeTable paraTable) throws IOException {
		super(paraReader);
		frequentSubsequences = new int [MAX_LENGTH][][];
		Table = paraTable;
		Collecter = new FrequentPatterCollector();
	
		SetGap(LowerBound,UpperBound);
		SetfrequencyThreshold(Threshold);
		SetTopK(TopK);
	}
	
	//******************************
	//	function:As literal meaning,generate longer Subsequences by attaching single 1 length frequent pattern.  
	//	  	
	//******************************
	
	void generateLongerSubsequences(){
		int tempRound = 1;
		int[][] tempPatterns;
		int[] tempSubsequence;
		int[] tempSubsequnce2;
		int tempLength = 0;
		boolean flag = false;
		
		while(frequentSubsequences[tempRound - 1].length > 0){
			
			tempLength = 0;
			tempPatterns = new int[frequentSubsequences[tempRound - 1].length * 100][];
			
			for(int i = 0 ; i < frequentSubsequences[tempRound - 1].length ; i ++){
				
				for(int j = 0 ; j < frequentSubsequences[0].length ; j ++ ){
					flag = false;
					tempSubsequence = new int[tempRound + 1];
					tempSubsequnce2 = new int[tempRound];
					
					for(int k = 0 ; k <  tempRound;k ++){						
						tempSubsequence[k] = frequentSubsequences[tempRound - 1][i][k];
					}
					tempSubsequence[tempRound] = frequentSubsequences[0][j][0];
					
					//============add this proc will faster a lot==================
					for(int k = 1; k < tempSubsequence.length ; k ++){
						tempSubsequnce2[k - 1] = tempSubsequence[k];
					}
					for(int q = 0; q  < frequentSubsequences[tempRound - 1].length;q ++){
						if(IsTwoPatternEqual(frequentSubsequences[tempRound - 1][q],tempSubsequnce2)) {
							flag = true;
							break;
						}
					}
					//====================================================
					double tempFrequency = 0.0;
					if(flag)tempFrequency = computeWildcardPatternFrequency(tempSubsequence);
					
					if(tempFrequency >= frequencyThreshold){
						tempPatterns[tempLength] = tempSubsequence;
						tempLength ++;
						Collecter.CollecteFrequencyAndPater(tempFrequency, tempSubsequence);
					}	
				}
				
				frequentSubsequences[tempRound] = new int[tempLength][];
				for(int k = 0 ; k < tempLength ; k ++){
					frequentSubsequences[tempRound][k] = tempPatterns[k];
				}
			}
			// running on the Chinese text
			//for(int i = 0; i < frequentSubsequences[tempRound].length; i ++) PrintFrequentPatter(frequentSubsequences[tempRound][i]);
			tempRound ++;
		}		
	}
	
	//******************************
	//	function:Calculate the accuracy of current noisy text
	//	parameter:  
	//		NosiyTopKPattern - this parameter represent the top k frequent patterns excavated on the text with noisy 
	//		NoNosiyTopKPattern - this parameter represent the top k frequent patterns excavated on the text without noisy
	//	
	//	The accuracy are defined as follows
	// 		
	//******************************
	
	//use this function will faster a little bit 
	boolean IsTwoPatternEqual(int[] FirstPattern,int [] SencondPattern){
	
		int flag = 0;
		for(int i = 0 ; i < FirstPattern.length; i ++){
			flag += SencondPattern[i] - FirstPattern[i];		
		}

		return flag == 0 ? true : false;
	}
	double CalculateTheAcc(int [][]NosiyTopKPattern,int [][] PureTopKPattern){
		
		int Counter = 0;	
		boolean flag = false;
		for(int i = 0; i < NosiyTopKPattern.length;i ++){
			for(int j = 0; j < PureTopKPattern.length; j ++){
	
				//use this will faster a little bit
				flag = IsTwoPatternEqual(NosiyTopKPattern[i],PureTopKPattern[j]);
				Counter = flag == true ? Counter + 1 : Counter;
			}
		}

		return (Counter * 1.0) / PureTopKPattern.length;
	}
	//******************************
	//	function:generate the 1 length frequent pattern
	//	
	//******************************
	void generateLengh1Subsequences(){	

		int [] tempCounter = new int[attribute(0).numValues()];
		int [] tempArray = new int[attribute(0).numValues()];
		int InstancesLen = numInstances();
		int Temp;
		String TempChar;
		
		int tempLength = 0;
			
		for(int i = 0; i < InstancesLen; i ++){
			// running on the alphabet is divided into three parts
			Temp = (int) instance(i).value(0);
			TempChar = attribute(0).value(Temp);
			
			 if(Table.isCharSemi(TempChar) || Table.isCharStrong(TempChar)){		
				tempCounter[Temp] ++;	
			}
		}
		
		for(int i = 0; i < tempCounter.length; i ++){
			if((tempCounter[i] + 0.0) / InstancesLen>= frequencyThreshold){		
				tempArray[tempLength] = i;
				tempLength++;
			}
		}
		frequentSubsequences[0] = new int[tempLength][1];
		
		for(int i = 0; i < tempLength; i ++) frequentSubsequences[0][i][0] = tempArray[i];
	}	
	
	//******************************
	//	function:As literal meaning,it's easy to understand right?
	//	So there is no further explanation
	//******************************
	void PrintFrequentPatter(int [] frequentSubsequences){
		System.out.print('[');
		for(int m :frequentSubsequences ){
			
			System.out.print(attribute(0).value(m) + ',');
		}
		System.out.print(']');
		System.out.print(',');
	}
	
	//******************************
	//	function:As literal meaning,it's easy to understand right?
	//	So there is no further explanation
	//******************************
	int computeWildcardPatternOccurrences(int [] paraPattern,int paraStart,int paraOffset){
		if(paraOffset == paraPattern.length) return 1;
		
		int tempCounter = 0;
		int Temp;
		int InstancesLen = numInstances();
		
		for(int i = wildcardGapLowerBound + 1 ; i <= wildcardGapUpperBound + 1; i ++){ 
			
			if(paraStart + i >= InstancesLen) break;
					
			if(paraPattern[paraOffset] == instance(paraStart + i).value(0)){
				tempCounter += computeWildcardPatternOccurrences(paraPattern,paraStart + i ,paraOffset + 1);
			}

			Temp = (int)instance(paraStart + i).value(0);
			if(Table.isCharStrong(attribute(0).value(Temp)) == true) break; 		
		}
		
		return tempCounter;
	}
	
	//******************************
	//	function:As literal meaning,it's easy to understand right?
	//	So there is no further explanation
	//******************************
	double computeWildcardPatternFrequency(int [] paraPattern){
		int tempCounter = 0;
		int Inslen = numInstances();
		int limit = Inslen - paraPattern.length ; //use this will fater a little
	
		for(int i = 0 ; i < limit  ; i ++){
			
			if(paraPattern[0] != instance(i).value(0)) continue;
			
			int[] tempPattern = new int[paraPattern.length - 1];
			
			for(int j = 0; j < tempPattern.length ; j ++ ){
				tempPattern[j] = paraPattern[j + 1];
			}
			
			tempCounter += computeWildcardPatternOccurrences(tempPattern,i,0);
		}
		//very imprtant !!!! this will influce the proc a lot , original version !
		//double tempFrequency = (tempCounter + 0.0) / (numInstances() * Math.pow(wildcardGapUpperBound - wildcardGapLowerBound + 1,paraPattern.length - 1));				
		double tempFrequency = (tempCounter + 0.0) / (Inslen * Math.pow(wildcardGapUpperBound - wildcardGapLowerBound + 1,paraPattern.length - 1));
		
		return tempFrequency;
	}
	

	
	
	// ===================originla function ================
	double CaculateTheVariance(double [] paraDatas,double Average){
		
		double Result = 0;

		for(int i = 0; i < paraDatas.length;i ++){
			Result += Math.pow((paraDatas[i] - Average), 2);
		}
		
		Result = Result/(paraDatas.length - 1);
		Result = Math.sqrt(Result);
		
		return Result;
	}
	

	
	public static String GenerateTheDataPath(String CurrentPath,double CurNoisy,int CurId){
		CurNoisy *= 100;
		String ReplaceString = "ChineseTxtNoisy";
		String StrCurNoisy = String.valueOf((int)CurNoisy);
		String StrCurId = String.valueOf(CurId);
		String NewPath;
		ReplaceString = ReplaceString + StrCurNoisy + "-" + StrCurId + ".arff";
			
		NewPath = CurrentPath.replace("ChineseTxt.arff", ReplaceString);
		return NewPath;
	}
	
	public static int [][] GeneratePurePattern(String arffFilePath,CodeTable table,int lowerBound,int upperBound,double threhold,int patternLen,int Topk){
		int PureTopKpattern[][] = null;
		
		try{
			FileReader fileReader = new FileReader(arffFilePath);
			SemiWildCard pureData = new SemiWildCard(fileReader,lowerBound,upperBound,threhold,Topk,table);
			
			fileReader.close();
			System.out.println("This is the data:\n" + pureData);
			pureData.generateLengh1Subsequences();
			pureData.generateLongerSubsequences();	
			pureData.Collecter.SortThePatternByTheFrequency();
			PureTopKpattern = pureData.Collecter.GetTheTopKPattern(TopK,patternLen);
			System.out.println("\n This is the Top " + TopK + " number " + patternLen + " length prure text frequent pattern" + "This is the pattern len "  + PureTopKpattern.length);
			for(int i = 0; i < PureTopKpattern.length; i ++) pureData.PrintFrequentPatter(PureTopKpattern[i]);
			System.out.println(Arrays.deepToString(PureTopKpattern));
			System.out.print('\n');
			pureData.Collecter.FreeTheMemory();
		}catch(Exception ee){
			System.out.println("error");
		}
		
		return PureTopKpattern;
	}
	
	public static void OutputTheAcc(String arffFilePath,int [][]PureStandardTopKpattern,CodeTable table,int lowerBound,int upperBound,double threhold,int patternLen,int Topk){
		
		//=================plz make sure the following parameters are the same as C source file=====================
		int eachProportionNoisyTxtNum = 9;//the values of this parameter ranges from 0 to 9.please make sure do not exceed the range.
		double NoisyPropotionIncrease = 0.1f; 
		double UpperBoundNoisyProportion = 1.0;
	    double CurNoisyProportion = 0.1;
		//==================================================================================================
	
	    ArrayList<SemiWildCard> objList = new ArrayList<SemiWildCard>();
		int index;
		int offset;
	    int ArrayCount = 0 ;
	    int ResultArrayCount = 0;
	    int CurNoisyTopKpattern[][];
	    
		double ToltalAcc = 0;
	    double Average = 0;
	    double Variance = 0;
	    double[] CurAccArray = new double[9];
		double[] AverageArray = new double [10];
	   	double[] VarianceArray = new double [10];
	    double CurAcc = 0 ;
	    
	 	FileReader CurFileReadr = null;
	    SemiWildCard CurNoisyData = null;
	    String CurdataPath;
	    
	    
	    try{
			//==============load file first will faster a lot============
			while(CurNoisyProportion < UpperBoundNoisyProportion + 0.1 ){
				for(int i = 0; i < eachProportionNoisyTxtNum; i ++){
					CurdataPath = GenerateTheDataPath(arffFilePath,CurNoisyProportion,i);	
					CurFileReadr  = new FileReader(CurdataPath);
					CurNoisyData = new SemiWildCard(CurFileReadr,lowerBound,upperBound,threhold,Topk,table);
					
					objList.add(CurNoisyData);
				}
				CurNoisyProportion += NoisyPropotionIncrease;
			}
			
			CurNoisyProportion = 0.1;
		
			while(CurNoisyProportion < UpperBoundNoisyProportion + 0.1 ){
				
				offset = (int) (((CurNoisyProportion * 10) - 1) * eachProportionNoisyTxtNum);//move calculation  before iteration will faster a little bit
				for(int i = 0; i < eachProportionNoisyTxtNum; i ++){
				
					index = offset  + i ;			
					CurNoisyData = objList.get(index);
					CurNoisyData.generateLengh1Subsequences();
					CurNoisyData.generateLongerSubsequences();
					CurNoisyData.Collecter.SortThePatternByTheFrequency();
					CurNoisyTopKpattern = CurNoisyData.Collecter.GetTheTopKPattern(TopK, patternLen);
								
					CurAcc = CurNoisyData.CalculateTheAcc(CurNoisyTopKpattern, PureStandardTopKpattern);
					CurAccArray[ArrayCount ++] = CurAcc;
					
					ToltalAcc += CurAcc;
					
					System.out.println(CurAcc);
					
					CurNoisyData.Collecter.FreeTheMemory();
				}
				
				Average = ToltalAcc/eachProportionNoisyTxtNum;
				Variance = CurNoisyData.CaculateTheVariance(CurAccArray,Average);
				
				System.out.println("The average Acc of " + CurNoisyProportion + " Noisy text is" + Average);
				System.out.println("The average Variance of " + CurNoisyProportion + " Noisy text is" + Variance);
				ToltalAcc = 0;
				ArrayCount = 0;
				
				CurNoisyProportion += NoisyPropotionIncrease;
				
				AverageArray[ResultArrayCount] = Average;
				VarianceArray[ResultArrayCount] = Variance;
				ResultArrayCount ++;
			}
			
			for(double i : AverageArray){
				System.out.print(i + ",");
			}
			System.out.print("\n");
			
			for(double i : VarianceArray){
				System.out.print(i + ",");
			}
			            
		}catch(Exception ee){
			ee.printStackTrace();
			System.out.println("error");
		}
	}	
	
	
	public static void main(String[] args){
		int PureStandardTopKpattern [][];
		int patternLen = 2;
		int Topk = 10;
		CodeTable TypeIITable = new CodeTable();
		CodeTable TypeITable = new CodeTable();
		
		String TypeIWeak[] = {"@","��","Ҳ","��","��","��","��","֮","��","��","��","��","��","��","��","��",};
		String TypeISemiStrong[] = {"��","��","��","��","��","��","��","��","��","��","��","ȴ","��","��","ʹ","��","��","��","��","��","��","��","Ϊ","��","��","��"};
		
		String TypeIIWeak[] = {};
		String TypeIISemiStrong[] = {};
		
		TypeIITable.setTheSemiStrongCha(TypeIIWeak);
		TypeIITable.setTheWeakCha(TypeIISemiStrong);
		
		TypeITable.setTheSemiStrongCha(TypeISemiStrong);
		TypeITable.setTheWeakCha(TypeIWeak);
		
		String arffFilePath = "C:\\Users\\Lab\\Desktop\\NovelTextFigure\\ChineseTxt.arff";//pure data path
		

		PureStandardTopKpattern = GeneratePurePattern(arffFilePath,TypeIITable,0,0,0.0038,patternLen,Topk);
		
		OutputTheAcc(arffFilePath,PureStandardTopKpattern,TypeITable,0,1,0.0019,patternLen,Topk);
	
	}
}
