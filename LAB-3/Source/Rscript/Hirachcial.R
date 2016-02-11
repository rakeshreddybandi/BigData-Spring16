distancevector<-dist(as.matrix(myData))
hirachialc<-hclust(distancevector)
plot(hirachialc)
