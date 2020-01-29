//
//  main.c
//  ChinesePreProcessing
//
//  Created by 翟文杰 on 2016/11/9.
//  Copyright © 2016年 翟文杰. All rights reserved.
//

#include <stdio.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>

//make sure your Operating System
#include <unistd.h>
//#include <windows.h>
//＝＝＝＝＝＝＝＝＝宏定义区＝＝＝＝＝＝＝＝＝＝＝
#define MAX_DIM 100
#define TRUE 1
#define FALSE 0
#define MAX_TXT_LENGTH 20000
#define MAX_WORD_NUM 2000
//＝＝＝＝＝＝＝＝＝宏定义区＝＝＝＝＝＝＝＝＝＝＝

//＝＝＝＝＝＝＝＝＝数据结构定义区＝＝＝＝＝＝＝＝＝＝＝
typedef struct{
    unsigned char FirstChar;
    
    unsigned char SecondChar;
    
    int Empty;
}HashCell;
struct{
    
    HashCell Cell;
    
}HASH_WORD[MAX_DIM][MAX_DIM];
//＝＝＝＝＝＝＝＝＝数据结构定义区＝＝＝＝＝＝＝＝＝＝＝


//＝＝＝＝＝＝＝＝＝全局变量定义区＝＝＝＝＝＝＝＝＝＝＝

char InPutCotainer[MAX_TXT_LENGTH];//原始文本容器

char Pre_Cotaniner[MAX_TXT_LENGTH];//预处理完成去掉符号以及其他的内容文本存入这个数组
long Pre_TXT_LEN = 0;//预处理完成后文本长度

char TXT_Word[MAX_WORD_NUM];//提取文本的单字存入这个数组
int WORD_NUM = 0;//单词个数

long Noisy_LEN = 0;

char One_Line_Cotaniner[MAX_TXT_LENGTH];
long One_Line_LEN = 0;


long Input_Txt_Length = 0;//载入文本字数目
//＝＝＝＝＝＝＝＝＝全局变量定义区＝＝＝＝＝＝＝＝＝＝＝
typedef  enum {OK,ERROR}Status;

//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
//     Function:Read the data from file
//
//     Meaning of parameters:
//     1.paraTXTPath ------ This function use it to locate the text you want to experiment with.
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
Status ReadDataFromFile(char *paraTXTPath){

    FILE *fp;
    
    fp = fopen(paraTXTPath, "r");
    if(!fp){
        printf("fail to load the Txt,please check the file path\n");
        exit(0);
    }
    
    fseek(fp, 0, SEEK_END);
    Input_Txt_Length = ftell(fp);
    printf("The Txt Len = %ld",Input_Txt_Length);
    rewind(fp);

    fread(InPutCotainer, Input_Txt_Length, 1, fp);
    fclose(fp);
    
    return OK;
}

//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
//     Function:Initialize the essential variable and data struct.
//
//     Meaning of parameters
//
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝

Status InitTheTool(){
    long i,j;
    for(i = 0 ; i < MAX_TXT_LENGTH; i ++) {
        InPutCotainer[i] = '\0';
        Pre_Cotaniner[i] = '\0';
        One_Line_Cotaniner[i] = '\0';
    }
    for(i = 0; i < MAX_WORD_NUM; i ++) TXT_Word[i] = '\0';
    
    for(i = 0; i < MAX_DIM; i ++){
        for(j = 0; j < MAX_DIM; j ++){
            HASH_WORD[i][j].Cell.Empty = 1;
        }
    }
    return OK;
}

//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
//     Function:To determine current two byte is Chinese characters or not.
//
//     Meaning of parameters
//     Hey guy it's easy to understand the meaning of parameters,right?
//     So there is no further explanation.
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
int IsThisCharacterValid(unsigned char FirstChar,unsigned char SecondChar){
    
    if((FirstChar >= 176 && SecondChar >= 161) && (FirstChar <= 247 && SecondChar <=254)){
        return 1;
    }
    return 0;
}
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
//     Function:Eliminate the the symbol in the text,only retain the Chinese characters
//
//     Meaning of parameters
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
Status TrimTheTxt(){
    long i;
    unsigned char FirstChar;
    unsigned char SecondChar;
    
    for(i = 0; i < Input_Txt_Length; i ++){
        //汉字范围
        //b0 a1 (176,161)
        //f7 fe (247,254)
        FirstChar = (unsigned char)InPutCotainer[i];
        if(FirstChar > 128){//只处理中文字符
            
            SecondChar = (unsigned char)InPutCotainer[i + 1];
            
            if(IsThisCharacterValid(FirstChar, SecondChar) == 1){
            
                Pre_Cotaniner[Pre_TXT_LEN ++] = FirstChar;
            
                Pre_Cotaniner[Pre_TXT_LEN ++] = SecondChar;
                i ++;
        }else{
                i ++;
        }
    }
}    return OK;
}

//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
//     Function:Extract the words from the text which has already been preprocessed
//
//     Meaning of parameters
//     1.Contaniner ------
//     2.ContanierLen ------ This parameter determine when dose the loop ends.
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
Status ExtractWordsFromContanier(char *Contaniner,long ContanierLen){
    long i;
    unsigned char FirstChar,SecondChar;
    for(i = 0; i < ContanierLen; i = i + 2){
        
        FirstChar = Contaniner[i];
        SecondChar = Contaniner[i + 1];
        FirstChar -= 161;
        SecondChar -= 161;
        
        if(HASH_WORD[FirstChar][SecondChar].Cell.Empty == 1){
            HASH_WORD[FirstChar][SecondChar].Cell.Empty = 0;
            HASH_WORD[FirstChar][SecondChar].Cell.FirstChar = FirstChar + 161;
            HASH_WORD[FirstChar][SecondChar].Cell.SecondChar = SecondChar + 161;
        }
    }
    return OK;
}
//Transit the word in hash table into array
Status TransitWordIntoArray(){
    int i,j;
    for(i = 0; i < MAX_DIM; i ++){
        for(j = 0; j < MAX_DIM; j ++){
            if(HASH_WORD[i][j].Cell.Empty == 0){
                
                TXT_Word[WORD_NUM] = HASH_WORD[i][j].Cell.FirstChar;WORD_NUM ++;

                TXT_Word[WORD_NUM] = HASH_WORD[i][j].Cell.SecondChar;WORD_NUM ++;
                
                TXT_Word[WORD_NUM] = ','; WORD_NUM ++;
            }
        }
    }
    
    TXT_Word[WORD_NUM] = '@';
    WORD_NUM ++;
    TXT_Word[WORD_NUM] = '\0';
    
    return OK;
}

char* ConvertTxtIntoOneLine(char *Container,long ContainerLen){
    
    char *One_Line = (char *) malloc(sizeof(char) * MAX_TXT_LENGTH * 2);
    
    long i;
    for(i = 0; i < ContainerLen ; i ++){
        if(Container[i] == '@'){
            One_Line[One_Line_LEN ++] = Container[i];
            One_Line[One_Line_LEN ++] = '\n';
        }else{
            One_Line[One_Line_LEN ++] = Container[i ++];
            One_Line[One_Line_LEN ++] = Container[i];
            One_Line[One_Line_LEN ++] = '\n';
        }
    }
    One_Line[One_Line_LEN] = '\0';

    return One_Line;
}

//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
//     Function:Extract the words from the text which has already been preprocessed
//
//     Meaning of parameters
//     1.Contaniner ------
//     2.ContanierLen ------ This parameter determine when dose the loop ends.
//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝

long * generateRandom(long TxtLen,double proportion){
    
    sleep(1);
    long InsertNum = (TxtLen/2) * proportion;
    long InsertPos = 0;
    long temp;
    int i,j;
    long *RandomPos = (long *) malloc(sizeof(long) * InsertNum);
    
    srand((unsigned)time(NULL));
    
    for(i = 0; i < InsertNum; i ++){
        InsertPos = rand()%Pre_TXT_LEN;
        RandomPos[i] = InsertPos;
    }

    //sort the random position sequence
    for(i = 0; i < InsertNum; i ++){
        for(j = i ; j < InsertNum; j ++){
            if(RandomPos[i] > RandomPos[j]){
                temp = RandomPos[i];
                RandomPos[i] = RandomPos[j];
                RandomPos[j] = temp;
            }
        }
    }
    
    return RandomPos;
}
int IsForwardTwoEmpty(int curIndex,long *insertPos,int CandidateInsertIndex){
    
    int i;
    for(i = curIndex; i - curIndex < 2; i ++ ){
        if(i == insertPos[CandidateInsertIndex]) return 0;
    }
    return 1;
}
char * InsertRandom(char *Container,long *insertPos,long TXT_LEN,long InserNum){
    //the length of new txt is the TXT_LEN + the number of noisy
    
    Noisy_LEN = TXT_LEN + InserNum;
    printf("insernum = %ld\n",InserNum);
    char *Result = (char *) malloc(sizeof(char) * (Noisy_LEN + 1));
    
    int i = 0,k = 0;

    int CandidateIndex = 0;
    while (Container[i] != '\0') {
        if(IsForwardTwoEmpty(i,insertPos,CandidateIndex)){
            Result[k] = Container[i];
            Result[ ++k] = Container[ ++i];
            k ++;i ++;
        }else{
            Result[k] = '@';
            k ++;
            CandidateIndex ++;
        }
    }
    
    Result[Noisy_LEN] = '\0';
    
    return Result;
}



char *Copy(char *paraContainer,long paraContainerLen){

    char *Container = (char *) malloc(sizeof(char) * paraContainerLen + 1);
    long i;
    
    for(i = 0; i < paraContainerLen; i ++){
        Container[i] = paraContainer[i];
    }
    
    Container[paraContainerLen] = '\0';
    
    return Container;
}

char *GenerateArrfWritePath(char *paraPath,double CurProportion,int Numi){
    char *Path;
    int i;
    int dec = 1,sign = 1;
    long paraPathLen = strlen(paraPath);
    
    char Suffix[18] = "Noisy";
    char *Txt = ".arff";
    char *proportion = ecvt(CurProportion, 2, &dec, &sign);
    char SingleNum[2];
    
    SingleNum[0] = Numi + 48;
    
    strcat(Suffix, proportion);
    strcat(Suffix, "-");
    strcat(Suffix, SingleNum);
    strcat(Suffix, Txt);
    
    Path = (char *) malloc(sizeof(char) * (paraPathLen + 9));
    
    for(i = 0; i < (paraPathLen + 9) ;i ++) Path[i] = '\0';
    
    for(i = 0; i < paraPathLen - 5;i ++) Path[i] = paraPath[i];
    
    strcat(Path, Suffix);
    return Path;

}

char *GenerateWritePath(char *paraPath,double CurProportion,int Numi){
    char *Path;
    int i;
    int dec = 1,sign = 1;
    long paraPathLen = strlen(paraPath);
    
    char Suffix[18] = "Noisy";
    char *Txt = ".txt";
    char *proportion = ecvt(CurProportion, 2, &dec, &sign);
    char SingleNum[2];
    
    SingleNum[0] = Numi + 48;
    
    strcat(Suffix, proportion);
    strcat(Suffix, "-");
    strcat(Suffix, SingleNum);
    strcat(Suffix, Txt);
   
    Path = (char *) malloc(sizeof(char) * (paraPathLen + 9));
    
    for(i = 0; i < (paraPathLen + 9) ;i ++) Path[i] = '\0';

    for(i = 0; i < paraPathLen - 5;i ++) Path[i] = paraPath[i];
    
    strcat(Path, Suffix);
    return Path;
}


Status WrieIntoArffFile(char *Atrributes,long AtrributeLen,char *Datas,long DataLen,char *ArrfPath){
    FILE *fp;
    char *Relation = "@RELATION Chineses\n";
    char *Attribute = "@ATTRIBUTE characters";
    char *Data = "@DATA\n";
    
    
    unsigned long len1 = strlen(Relation);
    unsigned long len2 = strlen(Attribute);
    unsigned long len3 = strlen(Data);
    
    fp = fopen(ArrfPath, "a+");
    if(!fp){
        printf("fail to open file,check the path\n");
        exit(0);
    }
    
    fwrite(Relation, len1, 1, fp);
    fwrite(Attribute, len2, 1, fp);
    fwrite("{", 1, 1, fp);
    
    fwrite(Atrributes, AtrributeLen, 1, fp);
    
    fwrite("}\n", 2, 1, fp);
    fwrite(Data, len3, 1, fp);
    fwrite(Datas, DataLen, 1, fp);
    fclose(fp);
    return OK;
}

Status WriteIntoFile(char *paraPath,char *Container,long Container_Len){

    FILE *fp;
    fp = fopen(paraPath, "w");

    if(!fp){
        printf("fail to open file,pleas check the path");
        exit(0);
    }
    fwrite(Container, Container_Len, 1, fp);
    fclose(fp);
    return OK;
}

//=====================================
//the values of this parameter ranges from 0 to 9.please make sure do not exceed the range.
//每次增加噪音的百分比
//文本最大噪音上限
//初始噪音比例
//This is the original Chinese text
//please make sure the two following parameter has the identical root path
//写入文本地址
//写入arff文件地址
//=======================================
void TransferAndInsertNoiseIntoTxt(int eachProportionNoisyTxtNum,
                    double NoisyPropotionIncrease,
                    double UpperBoundNoisyProportion,
                    double CurNoisyProportion,
                    char *readPath,
                    char *WritePath,
                    char *ArffPath ){
    
    if(eachProportionNoisyTxtNum > 9 || eachProportionNoisyTxtNum < 0) {
        printf("invlaid parameter,plz check the parameter\n");
        exit(1);
    }
    
    int i;
    char *CurNoisyTxt;
    long CurNoisyTxtLen;
    long *CurinserPos;
    char *CurtempContainer;
    char *CurPath;
    char *CurArrfPath;
    char *CurOneLine;
    
    InitTheTool();
    ReadDataFromFile(readPath);
    TrimTheTxt();
    ExtractWordsFromContanier(Pre_Cotaniner, Pre_TXT_LEN);
    TransitWordIntoArray();
    CurOneLine = ConvertTxtIntoOneLine(Pre_Cotaniner, Pre_TXT_LEN);
    WrieIntoArffFile(TXT_Word, WORD_NUM, CurOneLine, One_Line_LEN, ArffPath);
    One_Line_LEN = 0;
    
    while (CurNoisyProportion <= UpperBoundNoisyProportion) {
        
        for(i = 0; i < eachProportionNoisyTxtNum; i ++){
            
            CurtempContainer = Copy(Pre_Cotaniner, Pre_TXT_LEN);//Copy the pre_Container into the CurTemp
            
            CurinserPos = generateRandom(Pre_TXT_LEN, CurNoisyProportion);//生成随机噪音序列
            CurNoisyTxt = InsertRandom(CurtempContainer, CurinserPos, Pre_TXT_LEN, (Pre_TXT_LEN/2) * CurNoisyProportion);//依照随机噪音序列插入噪音符号
            CurNoisyTxtLen = Pre_TXT_LEN + ((Pre_TXT_LEN/2) * CurNoisyProportion);
            
            CurPath = GenerateWritePath(WritePath, CurNoisyProportion, i);//动态生成写入路径
            CurArrfPath = GenerateArrfWritePath(ArffPath, CurNoisyProportion, i);//动态生成arff写入路径
            
            CurOneLine = ConvertTxtIntoOneLine(CurNoisyTxt, CurNoisyTxtLen);
            
            WrieIntoArffFile(TXT_Word, WORD_NUM, CurOneLine,One_Line_LEN, CurArrfPath);
            WriteIntoFile(CurPath, CurNoisyTxt, CurNoisyTxtLen);
            
            
            free(CurtempContainer);
            free(CurOneLine);
            free(CurinserPos);
            free(CurNoisyTxt);
            free(CurPath);
            free(CurArrfPath);
            One_Line_LEN = 0;
        }
        
        CurNoisyProportion += NoisyPropotionIncrease;
    }
}

int main(){
    
    //使用说明：确保原始文本为GB2312编码，否则程序将不能正常运行
    //=====================================
    int eachProportionNoisyTxtNum = 9;//the values of this parameter ranges from 0 to 9.please make sure do not exceed the range.
    double NoisyPropotionIncrease = 0.1;//每次增加噪音的百分比
    double UpperBoundNoisyProportion = 1;//文本最大噪音上限
    double initialProportion = 1;//初始噪音比例
    char *readPath = "/Users/zhaiwenjie/Desktop/HistoryText/History2/History2.txt";//This is the original Chinese text
    //please make sure the two following parameter has the identical root path
    char *WritePath = "/Users/zhaiwenjie/Desktop/ChineseTest/PreChiniese.txt";//写入文本地址
    char *ArffPath = "/Users/zhaiwenjie/Desktop/ChineseTest/ChineseTxt.arff";//写入arff文件地址
    //=======================================
    
    TransferAndInsertNoiseIntoTxt(eachProportionNoisyTxtNum,NoisyPropotionIncrease,UpperBoundNoisyProportion,initialProportion,readPath,WritePath,ArffPath);
    
  
    return 0;
}