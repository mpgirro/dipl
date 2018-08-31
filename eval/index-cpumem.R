index_cpumem <- read.table("../src/benchmark/cpu-mem-index-i70-f100.csv", header=T, sep=";");

# CPU
par(mar=c(7,4,4,2))
barplot(index_cpumem$cpu, names.arg=index_cpumem$artefact, ylim = c(0, 1), las=2)


# MEM
par(mar=c(7,4,4,2))
barplot(index_cpumem$mem, names.arg=index_cpumem$artefact, ylim = c(0, 200), las=2)


# - - - - 
par(mar = c(7, 4, 2, 2) + 0.2) #add margin room for the rotated labels
barplot(index_cpumem$mem, names.arg=index_cpumem$artefact, ylim = c(0, 200),las=2)

end_point = 0.5 + nrow(index_cpumem) + nrow(index_cpumem)-1
text(seq(1.5,end_point,by=2), #par("usr")[3]-0.25, 
     srt = 60, adj= 1, xpd = TRUE,
     labels = index_cpumem$artefact, cex=1)




#use mtcars dataset to produce a barplot with qsec colum information
mtcars = mtcars[with(mtcars, order(-qsec)), ] #order mtcars data set by column "qsec" (source: http://stackoverflow.com/questions/1296646/how-to-sort-a-dataframe-by-columns-in-r)

end_point = 0.5 + nrow(mtcars) + nrow(mtcars)-1 #this is the line which does the trick (together with barplot "space = 1" parameter)

barplot(mtcars$qsec, col="grey50", 
        main="",
        ylab="mtcars - qsec", ylim=c(0,5+max(mtcars$qsec)),
        xlab = "",
        space=1)
#rotate 60 degrees, srt=60
text(seq(1.5,end_point,by=2), par("usr")[3]-0.25, 
     srt = 60, adj= 1, xpd = TRUE,
     labels = paste(rownames(mtcars)), cex=0.65)