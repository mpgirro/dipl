
quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()


index_akka_data <- read.table("data/index-akka-rtt-overall.csv", header=T, sep=";");
index_msa_data <- read.table("data/index-msa-rtt-overall.csv", header=T, sep=";");

# milliseconds -> seconds
index_akka_data$overallRT <- index_akka_data$overallRT / 1000
index_msa_data$overallRT <- index_msa_data$overallRT / 1000

# add color rows
index_akka_data$color <- rep("red",nrow(index_akka_data));
index_msa_data$color <- rep("blue",nrow(index_msa_data));

# aggregate the values for every X, so we'll just show the means of them
a <- aggregate(index_akka_data$overallRT, list(input_size=index_akka_data$input_size), mean)
b <- aggregate(index_msa_data$overallRT, list(input_size=index_msa_data$input_size), mean)

# this function plots the graphic of the data
g <- function() {
  par(mar=c(4,4,1,1))
  plot(b$input_size, b$x, 
       type="l", pch=18, col="dodgerblue1", lty=2, lwd=3,
       cex.axis = 1.3, cex.lab = 1.3,
       xlab = "Number of Feeds", ylab = "Overall Runtime [Seconds]",
       #xlab="", ylab="",
       xlim=c(0,500), ylim=c(0, 400), las=1)
       #xaxt="n", yaxt="n")
  lines(a$input_size, a$x, pch=19, col="tomato1", type="l", lwd=3)	
  legend("topleft", legend=c("Akka", "MSA"),
         col=c("tomato1", "dodgerblue1"), lty=1:2, cex=1.3, lwd=3)
}

# execute this to show in Rstudio
par(family = "cmu_sans")
g()


dest <- "out/eval-index-overall"

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
