Data1=kcca(myData,k=3)
image(data1)
points(myData)
barplot(data1)

data2=kcca(myData,k=3,family=kccaFamily("kmedians"),control=list(initcent="kmeanspp"))
image(data2)
points(myData)
