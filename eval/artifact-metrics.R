

quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()


artifact_metrics <- read.table("data/artifact-metrics.csv", header=T, sep=";");

artifact_metrics$fjar <- artifact_metrics$fjar / 1000

artifact_metrics

#attach(artifact_metrics)
#options(scipen=100)
par(mar=c(8,4,4,4))
plot(x = artifact_metrics$artifact, 
     y = artifact_metrics$fjar, 
     col = "blue", type = "h", las=2,
     names.arg=index_cpumem$artefact, 
     xlab = "x", ylab = "y1", main = "")
par(new = T)
plot(x = artifact_metrics$artifact, 
     y = artifact_metrics$startup, 
     col = "green", type = "h", las=2,
     xaxt = "n", yaxt = "n", 
     xlab = "", ylab = "")
axis(4)
mtext("y2", side = 4, line = 3)



par(mar=c(8,4,4,2))
barplot(index_cpumem$mem, 
        names.arg=index_cpumem$artefact, 
        space=c(1, 2),
        col=c("tomato1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1"),
        ylim = c(0, 200), ylab = "Memory Usage [Megabytes]", las=2)







x <- 1:5
y1 <- rnorm(5)
y2 <- rnorm(5,20)
par(mar=c(5,4,4,5)+.1)
plot(x,y1,type="l",col="red")
par(new=TRUE)
plot(x, y2,,type="l",col="blue",xaxt="n",yaxt="n",xlab="",ylab="")
axis(4)
mtext("y2",side=4,line=3)
legend("topleft",col=c("red","blue"),lty=1,legend=c("y1","y2"))




set.seed(2015-04-13)

d = data.frame(x =seq(1,10),
               n = c(0,0,1,2,3,4,4,5,6,6),
               logp = signif(-log10(runif(10)), 2))

par(new = T)
with(d, plot(x, n, pch=16, axes=F, xlab=NA, ylab=NA, cex=1.2))
axis(side = 4)
mtext(side = 4, line = 3, 'Number genes selected')
legend("topleft",
       legend=c(expression(-log[10](italic(p))), "N genes"),
       lty=c(1,0), pch=c(NA, 16), col=c("red3", "black"))
