setwd("~/Portugal/Error_Space_GP/ESGSGP/GSGP/results/")
outputs <- read.csv("outputs.txt", sep=",")
##below is only for removing last empty column of outputs - clean this up in java instead
outputs<-outputs[,-ncol(outputs)]
population<-read.csv("population.txt",sep=",")
# trainingData<-read.table("../ppb_training.txt", quote="\"", comment.char="")
# unseenData<-read.table("../ppb_unseen.txt",quote="\"", comment.char="")
# trainingTarget=t(trainingData[,ncol(trainingData)])

trainingData<-read.table("../Dataset_bioav/test1_.txt", quote="\"", comment.char="")
unseenData<-read.table("../Dataset_bioav/train1_.txt",quote="\"", comment.char="")
trainingTarget=t(trainingData[,ncol(trainingData)])

trainingLength = 252
unseenLength = 107

trainingErrorEnd =trainingLength+3
unseenErrorEnd = unseenLength+3

trainingErrors<-outputs[,1:trainingErrorEnd]
trainingOutputs<-outputs[,-4:-trainingErrorEnd]


subsetOutput <- subset(trainingOutputs, ID==1710)
subsetError <- subset(trainingErrors, ID==1710)


theta <- acos( sum(subsetError[,4:trainingErrorEnd][1,]*subsetError[,4:trainingErrorEnd][2,]) / ( sqrt(sum(subsetError[,4:trainingErrorEnd][1,] * subsetError[,4:trainingErrorEnd][1,])) * sqrt(sum(subsetError[,4:trainingErrorEnd][2,] * subsetError[,4:trainingErrorEnd][2,])) ) )

ratios=subsetError[,4:trainingErrorEnd][1,]/subsetError[,4:trainingErrorEnd][2,]
ratios=sort(ratios)

k=0.958

reconstructed =1/(1-k)*subsetOutput[1,4:trainingErrorEnd]-k/(1-k)*subsetOutput[2,4:trainingErrorEnd]
rmseTraining=sqrt(mean((reconstructed-trainingTarget)^2))

##below was for checking error values 
# targetMatrix <-matrix(trainingTarget,nrow=nrow(trainingOutputs),ncol=length(trainingTarget),byrow=TRUE)
# calcError=trainingOutputs[,4:ncol(trainingOutputs)]-targetMatrix
# errorerror=calcError-trainingErrors[,4:ncol(trainingErrors)]

##Calc of k here doesn't take into account removal of infinite ratios
# l=length(ratios)/2
# l2=l-1
# k=as.numeric((ratios[l]+ratios[l2])/2)
# k=3.32
##k is calculated differently depending if ratios lenght is odd or even
#k=as.numeric((ratios[l]))
