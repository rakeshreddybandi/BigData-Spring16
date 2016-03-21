Datain <- read.csv(file.choose(),sep = ",",header=FALSE)
YY <- Datain[,dim(Datain)[2]]
XX <- Datain[,1:(dim(Datain)[2]-1)]
pdata <- princomp(X, cor=T)
pc.data <- pdata$scores
pc.data1 <- -1*pc.data[,1]
pc.data2 <- -1*pc.data[,2] 
X <- cbind(pc.data1, pc.data2)
cl <- kmeans(X,10)
cl$cluster
plot(pc.data1, pc.data2,col=cl$cluster)
points(cl$centers, pch=16)
