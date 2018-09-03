
quartzFonts(cmu_sans = c("CMU Sans Serif", "CMU Sans Serif Bold", "CMU Sans Serif Oblique", "CMU Sans Serif BoldOblique"))
#quartzFonts(cmu_serif = c("CMU Serif Roman", "CMU Serif Bold", "CMU Serif Italic", "CMU Serif BoldItalic"))

library(extrafont)
font_import(pattern = "CM")
loadfonts()
#par(family = "CM Sans")


search_akka_delegation_data <- read.table("data/search-akka-delegation-rtt-overall.csv", header=T, sep=";")
search_akka_future_data <- read.table("data/search-akka-future-rtt-overall.csv", header=T, sep=";")
search_msa_data <- read.table("data/search-msa-rtt-overall.csv", header=T, sep=";")

# milliseconds -> seconds
search_akka_delegation_data$overallRT = search_akka_delegation_data$overallRT / 1000
search_akka_future_data$overallRT = search_akka_future_data$overallRT / 1000
search_msa_data$overallRT = search_msa_data$overallRT / 1000

# aggregate the values for every X, so we'll just show the means of them
a <- aggregate(search_akka_delegation_data$overallRT, list(input_size=search_akka_delegation_data$input_size), mean)
b <- aggregate(search_akka_future_data$overallRT, list(input_size=search_akka_future_data$input_size), mean)
c <- aggregate(search_msa_data$overallRT, list(input_size=search_msa_data$input_size), mean)

# this functions plots the graphic of the data
g1 <- function() {
  par(mar=c(4,4,4,2))
  plot(c$input_size, c$x, 
       type="l", pch=18, col="forestgreen", lty=2, lwd=2,
       cex.axis = 1.1, cex.lab = 1.1,
       xlab = "Search Requests", ylab = "Overall Runtime [Seconds]",
       #xlab="", ylab="",
       xlim=c(0,3000), ylim=c(0, 150), las=1)
  #xaxt="n", yaxt="n")
  lines(a$input_size, a$x, pch=19, col="tomato1", type="l", lty=1, lwd=2)	
  #lines(b$input_size, b$x, pch=19, col="tomato1", type="l", lty=4, lwd=2)	
  legend("topleft", legend=c("Akka", "MSA"),
         col=c("tomato1", "dodgerblue1"), lty=1:2, cex=1.1, lwd=2)
}

g2 <- function() {
  par(mar=c(4,4,4,2))
  plot(b$input_size, b$x, 
       type="l", pch=18, col="forestgreen", lty=4, lwd=2,
       cex.axis = 1.1, cex.lab = 1.1,
       xlab = "Search Requests", ylab = "Overall Runtime [Seconds]",
       #xlab="", ylab="",
       xlim=c(0,3000), ylim=c(0, 20), las=1)
  #xaxt="n", yaxt="n")
  lines(a$input_size, a$x, pch=19, col="tomato1", type="l", lwd=2)	
  legend("topleft", legend=c("Akka (Delegation)", "Akka (Future)"),
         col=c("tomato1", "forestgreen"), lty=1:4, cex=1.1, lwd=2)
}

save_akka_msa_images <- function() {
  dest <- "out/eval-search-comparison-akka-delegation-future"
  
  # output PNG
  png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
  par(family = "cmu_sans")
  g2()
  dev.off()
  
  # output PDF
  pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
  par(family = "CM Sans")
  g2()
  dev.off()
  # embed the CM Sans font into the PDF, or printing might become a problem
  embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))
}

save_akka_delegation_future_images <- function() {
  dest <- "out/eval-search-akka-msa"
  
  # output PNG
  png(paste(dest, ".png", sep=""), width = 5, height = 5, units = 'in', res = 300)
  par(family = "cmu_sans")
  g1()
  dev.off()
  
  # output PDF
  pdf(paste(dest, ".pdf", sep=""), family="CM Sans", width = 5, height = 5)
  par(family = "CM Sans")
  g1()
  dev.off()
  # embed the CM Sans font into the PDF, or printing might become a problem
  embed_fonts(paste(dest, ".pdf", sep=""), outfile=paste(dest, "_embed.pdf", sep=""))
}


# execute this to show in Rstudio
par(family = "cmu_sans")
g1()


par(family = "cmu_sans")
g2()


par(family = "cmu_sans")
save_akka_msa_images()


par(family = "cmu_sans")
save_akka_delegation_future_images()



#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#


plot(search_akka_delegation_data$input_size, search_akka_delegation_data$overallRT, type="l", pch=18, col="blue", lty=2, cex=1.2, 
     xlab = "Search Requests", ylab = "Elapsed Time [Seconds]",
     xlim=c(0,5000), ylim=c(0, 250))
lines(search_akka_future_data$input_size, search_akka_future_data$overallRT, type="l", pch=19, col="red")			   
legend("topleft", legend=c("Akka (Delegation)", "Akka (Future)"),
       col=c("red", "blue"), lty=1:2, cex=1)





akka_search_overall_data <- read.table("../src/benchmark/akka-search-rtt-overall.csv", header=T, sep=";")
msa_search_overall_data <- read.table("../src/benchmark/msa-search-rtt-overall.csv", header=T, sep=";")

# reduce overallRT from milliseconds to seconds 
akka_search_overall_data$overallRT = akka_search_overall_data$overallRT / 1000
msa_search_overall_data$overallRT = msa_search_overall_data$overallRT / 1000

# add colors
akka_search_overall_data$color <- rep("red",nrow(akka_search_overall_data))
msa_search_overall_data$color <- rep("blue",nrow(msa_search_overall_data))


x1 <- akka_search_overall_data$input_size;
y1 <- akka_search_overall_data$overallRT;
x1.lm <- lm(y1~x1);

x2 <- msa_search_overall_data$input_size;
y2 <- msa_search_overall_data$overallRT;
x2.lm <- lm(y2~x2);


# comparison with approximation lines
search_overall_data <- rbind( akka_search_overall_data, msa_search_overall_data);
plot(search_overall_data$input_size,search_overall_data$overallRT, 
     col=search_overall_data$color, pch = 4,
     xlim=c(0,5000), ylim=c(0, 250))
abline(x1.lm, col="red");
abline(x2.lm, col="blue");


# only Akka
plot(x1, y1, 
     type="b", pch=18, col="red", lty=2, cex=1.2, 
     xlab = "Search requests", ylab = "Response Time [ms]",
     xlim=c(0,5000), ylim=c(0, 250))
abline(x1.lm, col="red");

# only MSA
plot(x2, y2, 
     type="b", pch=19, col="blue", lty=2, cex=1.2, 
     xlab = "Search requests", ylab = "Response Time [ms]",
     xlim=c(0,5000), ylim=c(0, 250))
abline(x2.lm, col="blue");





pdf("eval-search-overall.pdf");

plot(x2, y2, type="l", pch=18, col="blue", lty=2, cex=1.2, 
     xlab = "Search Requests", ylab = "Elapsed Time [Seconds]",
     xlim=c(0,5000), ylim=c(0, 250))
lines(x1, y1, type="l", pch=19, col="red")			   
legend("topleft", legend=c("Akka", "MSA"),
       col=c("red", "blue"), lty=1:2, cex=1)

dev.off()