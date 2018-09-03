
quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()
#par(family = "CM Sans")

index_cpumem <- read.table("data/index-cpumem.csv", header=T, sep=";");

# this function plots the graphic of the data
g <- function() {
  par(mar=c(8,4,4,2))
  barplot(index_cpumem$mem, 
          names.arg=index_cpumem$artefact, 
          space=c(1, 2),
          col=c("tomato1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1","dodgerblue1"),
          ylim = c(0, 200), ylab = "Memory Usage [Megabytes]", las=2)
}

# execute this to show in Rstudio
par(family = "cmu_sans")
g()


dest <- "out/eval-index-mem"

# output PNG
png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
par(family = "cmu_sans")
g()
dev.off()

# output PDF
pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
par(family = "CM Sans")
g()
dev.off()
# embed the CM Sans font into the PDF, or printing might become a problem
embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))


#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#


# CPU
#par(mar=c(8,4,4,2))
barplot(index_cpumem$cpu, names.arg=index_cpumem$artefact, ylim = c(0, 1), las=2)




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